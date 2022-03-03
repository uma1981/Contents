/*Parameterize arrays with types*/
val bigval = 123
var big = new Array[String](3)


/*Had you been in a more explicit mood, you could have specified the type
of greetStrings explicitly like this*/
val greetStrings:Array[String] = new Array[String](3)

//Or simply you can define like this
//val greetStrings = new Array[String](3)
greetStrings(0) = "Hello"
greetStrings(1) = ", "
greetStrings(2) = "world!\n"
for (i <- 0 to 2)
  print(greetStrings(i))

val greetStrings1:Array[String] = new Array[String](3)


//My finding 1 - Reassignment to val is not possible, will throw error.
//greetStrings = greetStrings1

greetStrings.update(0,"Hello")
greetStrings.update(1,"Hi")
greetStrings.update(2,"World")

val numArrays = Array("One","Two","Three")

val oneTwoThree = List(1, 2, 3)

/*Immutable example*/
val oneTwo = List(1, 2)
val threeFour = List(3, 4)
val oneTwoThreeFour = oneTwo ::: threeFour
println(oneTwo +" and "+ threeFour +" were not mutated.")
println("Thus, "+ oneTwoThreeFour +" is a new list.")

var twoThree = List(2, 3)

var oneTwoThree1 = 1 :: twoThree



println(oneTwoThree1)

/*
The reason you need Nil at the end is that :: is defined on class List.
If you try to just  say 1 :: 2 :: 3, it won’t compile because 3 is an Int,
which doesn’t have a :: method.*/

val oneTwoThree2 = 1 :: 2 :: 3 :: Nil
println(oneTwoThree2)

val thrill = "Will" :: "fill" ::  "until" :: Nil

thrill.count(s => s.length == 4)

thrill.filter(s => s.length == 4)

/*Tuples.
* Can hold multiple data types
* */
val pair = (99, "Luftballons")
println(pair._1)
println(pair._2)

/*Use sets and maps - Example 14
* Below is immutable, run the example and see the result
* */
var jetset = Set("B","A")
jetset += "L"
println(jetset.mkString(","))
println(jetset.contains("C"))

/*Use sets and maps - Example 15
* Below program gives the error while add an element, because with out importing
* import scala.collection.mutable.Set, it will not work using val.
* but it will work using var.
* */
/*
val movieSet = Set("Hitch", "Poltergeist")
movieSet += "Shrek"
println(movieSet)
*/

import scala.collection.mutable.Set
val movieSet = Set("Hitch", "Poltergeist")
movieSet += "Shrek"
println(movieSet)

/*Map -  Example 16 */
import scala.collection.mutable.Map
val treasureMap = Map[Int,String]()
treasureMap += (1 -> "Go to Island")
treasureMap += (2 -> "Find big x on ground")
treasureMap += (3 -> "Dig")
println(treasureMap(2))

/* Example 17
Consider the following while loop example, adapted from
Chapter 2,which uses a var and is therefore in the imperative style:like(C#)
This example is changed to functional style in Example-18, by removing
var.
* */

def printArgs(args: Array[String]): Unit = {
  var i = 0
  while (i < args.length) {
    println(args(i))
    i += 1
  }
}

/* Example 18
You can transform this bit of code into a more functional style
by getting rid of the var, for example, like this:
You can modify this in Example-19
*/
def printArgs1(args: Array[String]): Unit = {
  for (arg <- args)
    println(arg)
}

/* Example 19 - or this:*/
def printArgs2(args: Array[String]): Unit = {
  args.foreach(println)
}

import scala.io.Source
var args : Array[String] = new Array[String](1)
args.update(0,"E:\\Muthu\\Hadoop\\Have to know.txt")
def widthOfLength(s: String) = s.length.toString.length
if (args.length > 0) {
  val lines = Source.fromFile(args(0)).getLines().toList
  val longestLine = lines.reduceLeft(
    (a, b) => if (a.length > b.length) a else b
  )
  val maxWidth = widthOfLength(longestLine)
  for (line <- lines)
  {
    val numSpaces = maxWidth - widthOfLength(line)
    val padding = " " * numSpaces
    println(padding + line.length +" | "+ line)
  }
}
else
  Console.err.println("Please enter filename")