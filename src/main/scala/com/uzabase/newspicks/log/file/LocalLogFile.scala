package com.uzabase.newspicks.log.file

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.file.Path
import java.util.zip.GZIPInputStream

import scala.io.Source

class LocalLogFile(path: Path) extends NewsPicksLogFile {

  def getLines(): List[ElasticSearchRequest] = {
    Source.fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))).getLines.map(l => addTimestamp(l)).toList
  }

  def getPath = path.toString()

}
