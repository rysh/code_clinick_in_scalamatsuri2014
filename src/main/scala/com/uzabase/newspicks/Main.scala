package com.uzabase.newspicks

import com.typesafe.scalalogging.slf4j.Logging
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.log.cli.CmdLineArgs
import com.uzabase.newspicks.log.LogFileRepository
import com.uzabase.newspicks.user.DummyUser

object Main extends App with CmdLineArgs with Logging {
  parser.parse(args, Config()) match {
    case Some(config)  => 
      if (config.isUser) 
        // Regidter dummy user
        DummyUser.createDummyUser(config)
       else if (config.isLog && config.isAws) 
        // Load from AWS
        LogFileRepository
          .loadFromS3(config)
          .createIndex(config)
       else 
        // If nothing is specified, then read files in DIR
        LogFileRepository
          .load(config)
          .createIndex(config)
      logger.info("Complete Successfully !!!")
    case None => // nop
  }
}

