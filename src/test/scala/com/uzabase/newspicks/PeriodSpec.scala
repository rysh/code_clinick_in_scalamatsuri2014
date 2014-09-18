package com.uzabase.newspicks

import org.scalamock.specs2.MockFactory
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import com.uzabase.newspicks.log.cli.ArgPeriod
import com.uzabase.newspicks.log.cli.S3LogFilePath

class PeriodSpec extends Specification with JsonMatchers with MockFactory {

  "引数で指定できる日付け/期間は下記に対応できる" should {
    "2014060100" in {
      val period = new ArgPeriod("2014060100", "2014")
      period.start.toString === "2014060100"
      period.end.toString === "2014060100"
    }

    "20140601" in {
      val period = new ArgPeriod("20140601", "2014")
      period.start.toString === "2014060100"
      period.end.toString === "2014060123"
    }
    "0601" in {
      val period = new ArgPeriod("0601", "2014")
      period.start.toString === "2014060100"
      period.end.toString === "2014060123"
    }
    "060121" in {
      val period = new ArgPeriod("060121", "2014")
      period.start.toString === "2014060121"
      period.end.toString === "2014060121"
    }
    "20140601-20140602" in {
      val period = new ArgPeriod("20140601-20140602", "2014")
      period.start.toString === "2014060100"
      period.end.toString === "2014060223"
    }
    "0601-0602" in {
      val period = new ArgPeriod("0601-0602", "2014")
      period.start.toString === "2014060100"
      period.end.toString === "2014060223"
    }

    "2014060100-2014060223" in {
      val period = new ArgPeriod("2014060100-2014060223", "2014")
      period.start.toString === "2014060100"
      period.end.toString === "2014060223"
    }
  }

  "値を変換できる" should {

    "2014060100 to 2014 , 06 , 01 and 00 " in {
      val period = new ArgPeriod("2014060100", "2014")
      period.start.year === "2014"
      period.start.month === "06"
      period.start.date === "01"
      period.start.hour === "00"
    }

    "S3key to yyyymmddhh" in {
      new S3LogFilePath("2014053900").keyToYmdh("access/y=2014/m=05/d=30/h=00/ip-10-131-11-18_2.gz") == 2014053000
    }
  }

  "S3keyの日付けと引数の日付を比較できる" should {
    "key is included period" should {
      "単体指定" in {
        new ArgPeriod("2014052923", "2014").include("access/y=2014/m=05/d=30/h=00/ip-10-131-11-18_2.gz") === false //境界値
        new ArgPeriod("2014053000", "2014").include("access/y=2014/m=05/d=30/h=00/ip-10-131-11-18_2.gz") === true //一致
        new ArgPeriod("2014053001", "2014").include("access/y=2014/m=05/d=30/h=00/ip-10-131-11-18_2.gz") === false //境界値
      }
      "範囲指定" in {
        val period = new ArgPeriod("2014053003-2014070222", "2014")
        period.include("access/y=2014/m=05/d=30/h=02/ip-10-131-11-18_2.gz") === false //境界値
        period.include("access/y=2014/m=05/d=30/h=03/ip-10-131-11-18_2.gz") === true //境界値
        period.include("access/y=2014/m=06/d=30/h=00/ip-10-131-11-18_2.gz") === true //中央値
        period.include("access/y=2014/m=07/d=02/h=22/ip-10-131-11-18_2.gz") === true //境界値
        period.include("access/y=2014/m=07/d=02/h=23/ip-10-131-11-18_2.gz") === false //境界値
      }
    }
  }
}