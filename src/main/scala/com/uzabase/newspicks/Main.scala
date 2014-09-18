package com.uzabase.newspicks

import com.typesafe.scalalogging.slf4j.Logging
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.log.cli.CmdLineArgs
import com.uzabase.newspicks.log.LogFileRepository
import com.uzabase.newspicks.user.DummyUser

object Main extends App with CmdLineArgs with Logging {

  // parser.parse returns Option[C]
  parser.parse(args, Config()) map { config =>
    if (config.isUser) {
      //Dummyのユーザを登録する
      DummyUser.createDummyUser(config)
    } else if (config.isLog && config.isAws) {
      //AWSから取り込む
      LogFileRepository
        .loadFromS3(config)
        .createIndex(config)
    } else {
      //何も指定しなければDIR以下のファイルを読み込む
      LogFileRepository
        .load(config)
        .createIndex(config)
    }
    logger.info("Complete Successfully !!!")

  } getOrElse {

  }

}

