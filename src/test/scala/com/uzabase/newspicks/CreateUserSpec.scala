package com.uzabase.newspicks

import java.nio.file.Paths
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.log.LogFileRepository
import com.uzabase.newspicks.log.file.LocalLogFile
import com.uzabase.newspicks.log.file.NewsPicksLogFiles
import com.uzabase.newspicks.user.DummyUser
import com.uzabase.newspicks.log.cli.ArgPeriod

class CreateUserSpec extends Specification with JsonMatchers {

  val log = """2014-06-01T00:26:34Z	nginx.access	{"host":"119.72.195.198","user":"-","req":"GET /users/100718/cover HTTP/1.1","method":"GET","uri":"/users/100718/cover","protocol":"HTTP/1.1","status":"200","size":"3086","reqsize":"603","referer":"http://newspicks.com/w/news/459215","ua":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.76.4 (KHTML, like Gecko) Version/7.0.4 Safari/537.76.4","vhost":"contents.newspicks.com","reqtime":"0.001","cache":"-","runtime":"-","apptime":"0.001","uid":"-"}"""
  val json = """{"host":"119.72.195.198","user":"-","req":"GET /users/100718/cover HTTP/1.1","method":"GET","uri":"/users/100718/cover","protocol":"HTTP/1.1","status":"200","size":"3086","reqsize":"603","referer":"http://newspicks.com/w/news/459215","ua":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.76.4 (KHTML, like Gecko) Version/7.0.4 Safari/537.76.4","vhost":"contents.newspicks.com","reqtime":"0.001","cache":"-","runtime":"-","apptime":"0.001","uid":"-","timestamp":"2014-06-01T00:26:34Z"}"""
  val config = Config("10.102.107.7", "", "", None, new ArgPeriod("", ""), Some(Paths.get("src/main/resources/test/logfile")))
  
  "ユーザ関連" should {

    "ダミーユーザのMapを用意できる" in {
      DummyUser.getMap(99)(config).size === 100
    }
    "新規ユーザのjsonを用意できる" in {
      //TODO 日付けをmockしてassersionをJSONにする
      DummyUser.createNewUserJson(1,2) must size(2)
    }
  }
}