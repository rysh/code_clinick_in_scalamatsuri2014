package com.uzabase.newspicks.user

import java.text.SimpleDateFormat
import java.util.Calendar
import spray.json.DefaultJsonProtocol
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.pimpString
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.api.ElasticsearchApi
import com.uzabase.newspicks.log.file.ElasticSearchRequest
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.LocalDateTime
import com.uzabase.newspicks.date.DateUtil

object DummyUser extends Logging{

  val max = MaxUser
  def createDummyUser(implicit config: Config) = {
    DummyUser.indexingUsers(DummyUser.createVirtualUserJsons(0, 199999).flatten)
  }

  def createNewUser(map: scala.collection.mutable.Map[String, scala.collection.mutable.Set[ElasticSearchRequest]])(implicit config: Config) = {
    map.foreach {
      case (routing, jsonSet) => {
        val uid = routing.toInt
        if (max < uid) {
          val newIdStart = max.get + 1
          logger.info(s"created New Users : $newIdStart to ${uid}")
          indexingUsers(createNewUserJson(newIdStart, uid))
          max.set(uid)
        }
      }
    }
  }
  
  private def indexingUsers(json: List[String])(implicit config: Config) = {

    val elasticsearch = new ElasticsearchApi
    json.zipWithIndex.par.foreach { case (json, i) => elasticsearch.insert("newspicks", "user", String.valueOf(i), json) }
    elasticsearch.close
  }
  private def indexingUsers(json: Map[Int,String])(implicit config: Config) = {

    val elasticsearch = new ElasticsearchApi
    json.par.foreach { case (uid, json) => elasticsearch.insert("newspicks", "user", String.valueOf(uid), json) }
    elasticsearch.close
  }

 private def createVirtualUserJsons(start: Int, end: Int): List[List[String]] = {
    val maxUser = end + 1
    val dailyEntryUserPerDay = 50

    var n = maxUser
    var cal = DateUtil.now
    val f = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")

    //ダミーユーザを作成する期間をと開始日を決める
    var dateCountOfPeriod = start
    do {
      dateCountOfPeriod = dateCountOfPeriod + 1
      n = n - dailyEntryUserPerDay
      cal.minusDays(1)
    } while (n > 0)

    val numberOfDayList = (0 to dateCountOfPeriod - 1).toList

    //一日毎に登録されるユーザのリスト
    numberOfDayList.map(count => {
      cal.plusDays(1)
      val dailyEntryUids = (count * dailyEntryUserPerDay to (count + 1) * dailyEntryUserPerDay - 1).toList

      //1日に登録されるユーザ
      dailyEntryUids.map(uid => makeUserJson(cal, uid))
    })
  }
  def createNewUserJson(start: Int, end: Int): Map[Int,String] = (start to end).map(uid => (uid, makeUserJson(DateUtil.now, uid))).toMap


  private case class User(uid: Int, start_date: String)
  private object UserJsonProtocol extends DefaultJsonProtocol {
    implicit val colorFormat = jsonFormat2(User)
  }

  def getMap(max: Int)(implicit config: Config): Map[Int, String] = {
    import UserJsonProtocol._
    createVirtualUserJsons(0, max).flatMap(x => x.map(e => {
      val js: User = e.parseJson.convertTo[User]
      (js.uid, js.start_date)
    })).toMap
  }

  def getMap(implicit config: Config): Map[Int, String] = {
    getMap(max.get)
  }
  
  private def makeUserJson(date: LocalDateTime, uid: Int): String = {
    val f = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
    JsObject("uid" -> JsNumber(uid), "start_date" -> JsString(DateUtil.format(date))).compactPrint
  }

  object MaxUser {
    var _max = 0
    def get(implicit config: Config): Int = {
      _max match {
        case 0 => (new ElasticsearchApi).searchLastUser
        case _ => _max
      }
    }
    def <(routing: Int): Boolean = routing > _max
    def set(newMax: Int) = { _max = newMax }
  }

}