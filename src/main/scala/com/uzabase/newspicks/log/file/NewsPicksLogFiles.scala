package com.uzabase.newspicks.log.file

import com.typesafe.scalalogging.slf4j.Logging
import com.uzabase.newspicks.log.LogFileRepository
import com.uzabase.newspicks.log.cli.Config

class NewsPicksLogFiles(list: List[NewsPicksLogFile]) extends Logging {

  def size = list.size

  def createIndex(implicit config: Config) = {
    //Actor使いたい
    logger.info(s"size : ${list.size}")
    list.foreach(f => logger.info(f.getPath))
    list.par.foreach(o => LogFileRepository.store(o))
  }

}