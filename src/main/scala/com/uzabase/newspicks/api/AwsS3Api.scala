package com.uzabase.newspicks.api

import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.transfer.TransferManager
import com.typesafe.scalalogging.slf4j.Logging
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.log.cli.ArgPeriod
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import scala.collection.mutable.MutableList

object AwsS3Api extends Logging with App{

  lazy val s3BucketName = "newspicks-log"


  def search(implicit config: Config): List[String] = {
    val s3 = new AmazonS3Client(new BasicAWSCredentials(config.key, config.secret))
    val s3FilePath = s"access/y=${config.period.start.year}/m=${config.period.start.month}/"
    
    logger.info(s"searching s3 key : $s3BucketName/$s3FilePath")
    
    /*
     * 月跨ぎの指定には対応していない(あまり時間をかけたくなかったので)
     * 1月分取得してから指定範囲に含まれるものに絞り込む
     */
    val keyList = new ArrayList[String]
    try {

      var objectListing = s3.listObjects(s3BucketName, s3FilePath)

      do {
        keyList.addAll(objectListing.getObjectSummaries.map(o => o.getKey()).filter(f => config.period.include(f)))

        objectListing = s3.listNextBatchOfObjects(objectListing);
      } while (objectListing.getNextMarker != null)
    } finally {
      s3.shutdown();
    }
    keyList.toList
  }

  def downloadFile(s3Key: String)(implicit config: Config) = {
    val s3 = new AmazonS3Client(new BasicAWSCredentials(config.key, config.secret))
    val tm = new TransferManager(s3);
    val req = new GetObjectRequest(s3BucketName, s3Key);

    val s3Object = s3.getObject(req);
    val pathWithTime = Paths.get(s3Key.replaceAll("/", "").replaceAll(".=", ""))
    val file = Files.createFile(pathWithTime).toFile()
    try {
      tm.download(req, file).waitForCompletion();
    } catch {
      case e: Exception =>
        logger.info("ERROR : AwsS3Api.downloadFile")
        e.printStackTrace()
    } finally {
      try {
        tm.shutdownNow()
        s3Object.close()
      }
    }
    file
  }
  def periodToKeys(period:ArgPeriod):List[String] = {
    val f = DateTimeFormat.forPattern("yyyyMMddHH")
    val f2 = DateTimeFormat.forPattern("'access/y='yyyy'/m='MM'/d='dd'/h='HH'/")
    var start = DateTime.parse(period.start.toString, f)
    val end = DateTime.parse(period.end.toString, f)
    val keys = MutableList[String]()
    while (!start.isAfter(end)) {
      keys += start.toString(f2)
      start = start.plusHours(1)
    }
    keys.toList
  }
}