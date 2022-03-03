package org.inceptez.hack
import scala.util.matching.Regex

class allmethods extends Serializable {
  def remspecialchar(a:String):String={
         val re = new Regex("[;\\/:*?\"<>|&'0-9]")
         val outputString = re.replaceAllIn(a, " ")
         return outputString

  }
}
