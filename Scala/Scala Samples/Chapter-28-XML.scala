def proc(node: scala.xml.Node): String =
  node match {
    case <a>{contents}</a> => "It's an a: "+ contents
    case <b>{contents}</b> => "It's a b: "+ contents
    case _ => "It's something else."
  }
