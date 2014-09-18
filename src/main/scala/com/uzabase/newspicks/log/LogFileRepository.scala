package com.uzabase.newspicks.log

import java.io.File
import java.nio.file.Path
import scala.Array.canBuildFrom
import com.typesafe.scalalogging.slf4j.Logging
import com.uzabase.newspicks.api.ElasticsearchApi
import com.uzabase.newspicks.log.file.LocalLogFile
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.log.file.NewsPicksLogFile
import com.uzabase.newspicks.log.file.NewsPicksLogFiles
import com.uzabase.newspicks.api.AwsS3Api
import com.uzabase.newspicks.log.file.S3LogFile

object LogFileRepository extends Logging {

  def load(implicit config: Config): NewsPicksLogFiles = {

    def search(file: File): List[Path] = 
      if (file.isDirectory()) 
       for { 
          f <- file.listFiles.toList
          path <- search(f)
        } yield path                      
      else if (file.getName().startsWith("ip-")) 
        List(file.toPath())
      else Nil

    new NewsPicksLogFiles(search(config.file.get.toFile).map(p => new LocalLogFile(p)))
  }

  def loadFromS3(implicit config: Config) = new NewsPicksLogFiles(AwsS3Api.search.map(key => new S3LogFile(AwsS3Api.downloadFile(key))))
  

  def store(log: NewsPicksLogFile)(implicit config: Config) = {

    try {
      val elasticsearch = new ElasticsearchApi
      elasticsearch.insertBulk("newspicks", "log", log.getLines)
      elasticsearch.close
      log.print("Done")
    } catch {
      case e: Exception => {
        logger.info("hello")
        e.printStackTrace()
        log.print("Error")
        logger.info(e.getMessage())
        logger.info("hello")
      }

    }
  }
}