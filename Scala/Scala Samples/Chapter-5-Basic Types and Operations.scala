/*Integer Literals - If the number begins with a 0x or 0X,
it is hexadecimal*/

val hex = 0x5

val hex2 = 0x00FF

val magic = 0xcafebabe

/*If the number begins with a zero, it
is octal (base 8), and may, therefore, only contain digits 0 through 7*/
/*val oct = 035 // (35 octal is 29 decimal)
val nov = 0777
val dec = 0321*/

/*If the number begins with a non-zero digit, and is otherwise undecorated,
it is decimal (base 10). For example:*/

val dec1 = 31
val dec2 = 255
val dec3 = 20

/*If an integer literal ends in an L or l, it is a Long,
otherwise it is an Int. Some examples of Long integer literals are:*/

val prog = 0XCAFEBABEL
val tower = 35L
val of = 31l

/*If an Int literal is assigned to a variable of type Short or Byte, the
literal is treated as if it were a Short or Byte type */

val little: Short = 367
val littler: Byte = 38

/*Floating point literals -optionally containing a decimal point,
and optionally followed by an E or e and an exponent*/

val big = 1.2345
val bigger = 1.2345e1
val biggerStill = 123E45

/*If a floating-point literal ends in an F or f, it is a Float,
otherwise it is a Double. Optionally, a Double floating-point literal can
end in D or d. Some examples of Float literals are:*/

val littlee = 1.2345F
val littleBigger = 3e5f

/*That last value expressed as a Double could take these (and other) forms:*/
val anotherDouble = 3e5
val yetAnother = 3e5D

/*Character literals*/
/*Character literals are composed of any Unicode character between
single quotes, such as:
 */
val a = 'A'

/*you can provide an octal or hex number for the character code point
preceded by a backslash. The octal number must be between '\0' and '\377'.
 For example, the Unicode character code point for the letter A is 101 octal.
  Thus:
 */

val c = '\101'

/*A character literal can also be given as a general Unicode character
consisting of four hex digits and preceded by a \u, as in:
 */

val d = '\u0041'
val f = '\u0044'

/*Operators are methods*/

val sum = 1 + 2

val sumMore = (1).+(2)

val longSum = 1 + 2L //+ overload, will return long

/*operator notation.
*When you write “s.indexOf('o')”, indexOf is not an operator. But
when you write “s indexOf 'o'”, indexOf is an operator, because
you’re using it in operator notation.
*
* */
val s = "Hello, world!"
s indexOf('o') // Scala invokes s.indexOf(’o’)
s indexOf ('o', 5) // Scala invokes s.indexOf(’o’, 5)

/*Postfix operators
* Postfix operators are methods that take no arguments, when they are invoked
without a dot or parentheses. In Scala, you can leave off empty parentheses
on method calls. The*/
s.toLowerCase
s toLowerCase


/* 5.5 Relational and logical operations
* In the first expression, pepper and salt are invoked, but in the second,
* only salt is invoked. Given salt returns false,
* there’s no need to call pepper.*/
def salt() = { println("salt"); false }
def pepper() = { println("pepper"); true }
pepper() && salt()
salt() && pepper()

var i = 2 + 2 << 2
println(i)

/*Object equality
* If you want to compare two objects for equality, you can use either ==, or its
inverse !=.
*
* */

1 == 2 //false

1 != 2 //true

2 == 2 //true

List(1, 2, 3) == List(1, 2, 3) //true

List(1, 2, 3) == List(4, 5, 6) //false

1 == 1.0 //true

List(1, 2, 3) == "hello" //false

List(1, 2, 3) == null //false

null == List(1, 2, 3) //false

("he"+"llo") == "hello" //true

val a12 = 1;
{
  val a12 = 2 // Compiles just fine
  println("h")
  println(a12)
}
println("h1")
println(a12)