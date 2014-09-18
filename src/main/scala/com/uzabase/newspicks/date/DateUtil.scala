package com.uzabase.newspicks.date

import java.text.SimpleDateFormat

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

object DateUtil{
  def toJst(time: String): String = format(DateTime.parse(time).toDateTime(DateTimeZone.forOffsetHours(9)))
  def now():LocalDateTime = {LocalDateTime.now(DateTimeZone.forOffsetHours(9))}
  def format(date:DateTime):String = date.toString("yyyy-MM-dd\'T\'HH:mm:ss")
  def format(date:LocalDateTime):String = date.toString("yyyy-MM-dd\'T\'HH:mm:ss")

}
