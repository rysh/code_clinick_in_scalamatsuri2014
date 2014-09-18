package com.uzabase.newspicks.log.file

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import scala.io.Source

class S3LogFile(file: File) extends NewsPicksLogFile {

  def getLines(): List[ElasticSearchRequest] = {
    val list = readFile.getLines.map(l => addTimestamp(l)).toList
    file.delete()
    list
  }
  private def readFile = Source.fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))

  def getPath = file.getAbsolutePath()
}
