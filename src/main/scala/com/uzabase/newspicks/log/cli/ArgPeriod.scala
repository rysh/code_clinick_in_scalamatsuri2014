package com.uzabase.newspicks.log.cli

import scala.Array.canBuildFrom

import org.joda.time.DateTime

class ArgPeriod(arg: String, currentYear: String) {
  val parms = arg.split("-")
  if (parms.size > 2) {
    throw new IllegalArgumentException()
  }
  def startOf(date: String): String = {
    date.length match {
      case 4 => s"${currentYear}${date}00"
      case 6 => s"${currentYear}${date}"
      case 8 => s"${date}00"
      case _ => date
    }
  }
  def endOf(date: String): String = {
    date.length match {
      case 4 => s"${currentYear}${date}23"
      case 6 => s"${currentYear}${date}"
      case 8 => s"${date}23"
      case _ => date
    }
  }

  val start: S3LogFilePath = new S3LogFilePath(startOf(parms(0)))

  val end: S3LogFilePath = {
    if (parms.length == 2) {
      new S3LogFilePath(endOf(parms(1)))
    } else if (Set(4, 8).contains(parms(0).length())) {
      new S3LogFilePath(endOf(parms(0)))
    } else {
      start
    }
  }

  def include(key: String): Boolean = start <= key && end >= key
}
object ArgPeriod {
  def apply(arg: String) = {
    new ArgPeriod(arg, new DateTime().getYear().toString)
  }
}
class S3LogFilePath(arg: String) {
  def year = arg.substring(0, 4)
  def month = arg.substring(4, 6)
  def date = arg.substring(6, 8)
  def hour = arg.substring(8, 10)
  override def toString = s"$year$month$date$hour"
  def <=(key: String): Boolean = toString.toInt <= keyToYmdh(key)
  def >=(key: String): Boolean = toString.toInt >= keyToYmdh(key)
  /* key = "access/y=2014/m=05/d=30/h=00/ip-xxx.gz" */
  def keyToYmdh(key: String): Int = key.split("/").zipWithIndex.filter { case (e, i) => List(1, 2, 3, 4).contains(i) }.map { case (e, i) => e.substring(2) }.mkString.toInt;
}