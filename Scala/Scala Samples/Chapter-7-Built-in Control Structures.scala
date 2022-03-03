/*This code declares a variable, filename, and initializes it to a default value.
It then uses an if expression to check whether any arguments were supplied
to the program. If so, it changes the variable to hold the value specified in
the argument list. If no arguments were supplied, it leaves the variable set to
the default value.*/
var filename = "default.txt"
var args: Array[String] = new Array[String](3)
if (!args.isEmpty)
  filename = args(0)

/*This code can be written more nicely*/
val filename1 =
  if (!args.isEmpty) args(0)
  else "default.txt"

/*While loop*/
def gcdLoop(x: Long, y: Long): Long = {
  var a = x
  var b = y
  while (a != 0) {
    val temp = a
    a = b % a
    b = temp
  }
  b
}

/*Scala also has a do-while loop. This works like the while loop except
that it tests the condition after the loop body instead of before.*/
var line = ""
do {
  line = scala.io.readLine()
  println("Read: "+ line)
} while (line != "")


/*Example -4, variable scope*/
def printMultiTable() {
  var i = 1
  // only i in scope here
  while (i <= 10) {
    var j = 1
    // both i and j in scope here
    while (j <= 10) {
      val prod = (i * j).toString
      // i, j, and prod in scope here
      var k = prod.length
      // i, j, prod, and k in scope here
      while (k < 4) {
        print(" ")
        k += 1
      }
      print(prod)
      j += 1
    }
    // i and j still in scope; prod and k out of scope
    println()
    i += 1
  }
  // i still in scope; j, prod, and k out of scope
}