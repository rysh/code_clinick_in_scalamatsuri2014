package com.uzabase.newspicks.log.file

import java.util.LinkedHashMap
import scala.collection.JavaConversions.mapAsScalaMap
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.LocalDateTime
import org.joda.time.DateTime
import com.uzabase.newspicks.date.DateUtil

trait NewsPicksLogFile extends Logging {

  def addTimestamp(line: String): ElasticSearchRequest = {
    val pieces = line.split("\t")
    val map: LinkedHashMap[String, String] = new ObjectMapper().readValue(pieces(2), new TypeReference[LinkedHashMap[String, String]]() {});
    var map2: LinkedHashMap[String, String] = new LinkedHashMap[String, String]()
    var uid = "0"
    map.foreach {
      case (key, value) =>
        if (!"-".equals(value)) {
          map2.put(key, value)
          if ("uid".equals(key)) {
            uid = value
          }
        }
    }
    map2.put("timestamp", DateUtil.toJst(pieces(0)))
    ElasticSearchRequest(uid, new ObjectMapper().writeValueAsString(map2))
  }
  def getLines(): List[ElasticSearchRequest]

  def getPath: String

  def print(str: String) = logger.info(s"$str : $getPath")
}
case class ElasticSearchRequest(routing: String, json: String)