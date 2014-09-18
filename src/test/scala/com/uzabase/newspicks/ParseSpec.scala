package com.uzabase.newspicks

import java.nio.file.Paths
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import com.uzabase.newspicks.log.LogFileRepository
import com.uzabase.newspicks.log.cli.ArgPeriod
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.log.file.LocalLogFile
import com.uzabase.newspicks.log.file.NewsPicksLogFiles
import com.uzabase.newspicks.log.file.NewsPicksLogFile
import com.uzabase.newspicks.log.file.LocalLogFile
import java.nio.file.Files
import com.uzabase.newspicks.date.DateUtil

class ParseSpec extends Specification with JsonMatchers {

  val log = """2014-06-01T00:26:34Z	nginx.access	{"host":"119.72.195.198","user":"-","req":"GET /users/100718/cover HTTP/1.1","method":"GET","uri":"/users/100718/cover","protocol":"HTTP/1.1","status":"200","size":"3086","reqsize":"603","referer":"http://newspicks.com/w/news/459215","ua":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.76.4 (KHTML, like Gecko) Version/7.0.4 Safari/537.76.4","vhost":"contents.newspicks.com","reqtime":"0.001","cache":"-","runtime":"-","apptime":"0.001","uid":"-"}"""
  val json = """{"host":"119.72.195.198","user":"-","req":"GET /users/100718/cover HTTP/1.1","method":"GET","uri":"/users/100718/cover","protocol":"HTTP/1.1","status":"200","size":"3086","reqsize":"603","referer":"http://newspicks.com/w/news/459215","ua":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.76.4 (KHTML, like Gecko) Version/7.0.4 Safari/537.76.4","vhost":"contents.newspicks.com","reqtime":"0.001","cache":"-","runtime":"-","apptime":"0.001","uid":"-","timestamp":"2014-06-01T00:26:34Z"}"""
  val config = Config("10.102.107.7", "", "", None, new ArgPeriod("", ""), Some(Paths.get("src/main/resources/test/logfile")))
  
  
  "ログの取得" should {
    "ファイルを検索できる" in {
      LogFileRepository.load(config) must haveClass[NewsPicksLogFiles]
    }
    "TimeSampをUTCからJSTに変換できる" in {
      DateUtil.toJst("2014-06-01T00:26:34Z") === "2014-06-01T09:26:34"
    }
    "JSONにTimeSampを追加できる" in {
      new LocalLogFile(null).addTimestamp(log).json must /("timestamp" -> "2014-06-01T09:26:34")
    }
    "gzipを解凍して読み込める" in {
      new LocalLogFile(Paths.get("src/test/resources/logfile/ip-10-131-11-11_0.gz")).getLines.size === 5808
    }
  }
}