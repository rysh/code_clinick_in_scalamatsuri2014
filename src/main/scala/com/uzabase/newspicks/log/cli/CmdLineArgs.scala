package com.uzabase.newspicks.log.cli

import java.nio.file.Path
import java.nio.file.Paths

trait CmdLineArgs {

  val parser = new scopt.OptionParser[Config]("scopt") {
    head("Newspicks-Log-Loader", "0.1")
    opt[String]('h', "host") required () valueName ("") action { (x, c) => c.copy(host = x) }                  text ("""elasticsearch's "hostname" or IP address (required)""")
    opt[String]('t', "target")           valueName ("") action { (x, c) => c.copy(target = Some(x)) }          text (""""target" is the type of executing function""")
    opt[String]('k', "key")              valueName ("") action { (x, c) => c.copy(key = x) }                   text ("""an AWS access "key"""")
    opt[String]('s', "secret")           valueName ("") action { (x, c) => c.copy(secret = x) }                text ("""an AWS access "secret"""")
    opt[String]('p', "period")           valueName ("") action { (x, c) => c.copy(period = ArgPeriod(x)) }     text ("""you can specify 2014050100-2014050523 to "period"  (also 2014060100, 20140601, 0601, 060121, 20140601-20140602 and 0601-0602) """)
    opt[String]('f', "file")             valueName ("") action { (x, c) => c.copy(file = Some(Paths.get(x))) } text ("""if you want to parse a local file, specify the path to "file"""")
    note("""
        ===================
        Example
        
        Indexing log from s3 
        > run -h hostname -k s3key -h s3secret -p 0610
        
        Indexing log from local file 
        > run -h hostname -p 0610 -f path
        
        Indexing dummy users 
        > run -h hostname -t user
        
        """)
  }
}

case class Config(host: String = "", key: String = "", secret: String = "", target: Option[String] = None, period: ArgPeriod = new ArgPeriod("", ""), file: Option[Path] = None) {
  def isLog: Boolean = {
    target match {
      case Some(t) => t.equals("main")
      case None => true
    }
  }
  def isUser: Boolean = {
    target match {
      case Some(t) => t.equals("user")
      case None => false
    }
  }
  def isAws: Boolean = file.isEmpty
}