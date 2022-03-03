/*trait Abstract {
  type T
  def transform(x: T): T
  val initial: T
  var current: T
}
class Concrete extends Abstract {
  type T = String
  def transform(x: String) = x + x
  val initial = "hi"
  var current = initial
}

val ty = new Concrete()
ty.initial
*/

//
/*class Food
abstract class Animal {
  def eat(food: Food)
}

class Grass extends Food
class Cow extends Animal {
  override def eat(food: Grass): Unit = {
    var x=2
  } // This won’t compile
}
*/

/*
class Food
abstract class Animal {
  def eat(food: Food)
}
class Grass extends Food
class Cow extends Animal {
  override def eat(food: Grass) {} // This won’t compile,
} // but if it did,...
class Fish extends Food
val bessy: Animal = new Cow
bessy eat (new Fish) // ...you could feed fish to cows.
*/