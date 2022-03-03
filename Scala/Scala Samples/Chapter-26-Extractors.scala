object EMail {
  // The injection method (optional)
  def apply(user: String, domain: String) = user +"@"+ domain
  // The extraction method (mandatory)
  def unapply(str: String): Option[(String, String)] = {
    val parts = str split "@"
    if (parts.length == 2) Some(parts(0), parts(1)) else None
  }


}

val Decimal(sign, integerpart, decimalpart) = "1.23"



