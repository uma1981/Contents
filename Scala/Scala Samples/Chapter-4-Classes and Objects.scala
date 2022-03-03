class ChecksumAccumulator {
  var sum = 0
}
val acc = new ChecksumAccumulator
val csa = new ChecksumAccumulator

/*Accessing the variables*/
acc.sum=3

/*Reassign to val is not permitted, throwing compile time error*/
/*acc = new ChecksumAccumulator*/

/*Creating private variables inside the class and it can not
* accessed outside*/
class ChecksumAccumulator1 {
  private var sum = 0
}

/*var obj = new ChecksumAccumulator1
obj.sum = 9 - Won’t compile, because sum is private*/

/**/
class ChecksumAccumulator2 {
  private var sum = 0
  def add(b: Byte): Unit = {
    //b = 1 This won’t compile, because b is a val by default
    sum += b
  }
  def checksum(): Int = {
    return ~(sum & 0xFF) + 1
  }
}

/*Another shorthand for methods is that you can leave off the
curly braces if a method computes only a single result expression.*/

class ChecksumAccumulator3 {
  private var sum = 0
  def add(b: Byte): Unit = sum += b
  def checksum(): Int = ~(sum & 0xFF) + 1
}

/*Another way to express such methods is to leave off the result type
and the equals sign for Unit return type, and enclose the body of the method in curly braces.*/
class ChecksumAccumulator4 {
  private var sum = 0
  def add(b: Byte) { sum += b }
  def checksum(): Int = ~(sum & 0xFF) + 1
}

def f(): Unit = "this String gets lost"
f()

def h() = { "this String gets returned!" }
h: ()