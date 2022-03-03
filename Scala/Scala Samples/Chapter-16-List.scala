/* Ex
val fruit: List[String] = List("apples", "oranges", "pears")
val nums: List[Int] = List(1, 2, 3, 4)
val diag3: List[List[Int]] =
  List(
    List(1, 0, 0),
    List(0, 1, 0),
    List(0, 0, 1)
  )
val empty: List[Nothing] = List()*/

/* Ex
val fruit = "apples" :: ("oranges" :: ("pears" :: Nil))
val nums = 1 :: (2 :: (4 :: (3 :: Nil)))
val diag3 = (1 :: (0 :: (0 :: Nil))) ::
  (0 :: (1 :: (0 :: Nil))) ::
  (0 :: (0 :: (1 :: Nil))) :: Nil
val empty = Nil
empty.isEmpty
fruit.isEmpty
fruit.head
fruit.tail
fruit.tail.head
def isort(xs: List[Int]): List[Int] =
  if (xs.isEmpty) Nil
  else insert(xs.head, isort(xs.tail))

def insert(x: Int, xs: List[Int]): List[Int] =
  if (xs.isEmpty || x <= xs.head) x :: xs
  else xs.head :: insert(x, xs.tail)

isort(nums)

*/

/* Ex
val fruit = "apples" :: ("oranges" :: ("pears" :: Nil))
val List(a, b, c) = fruit

fruit.reverse

fruit take 4

fruit.splitAt(2)

fruit(2)
*/

/* Ex
//List(1, 2, 3, 4)
List(1, 1, 2, 1, 2, 3)

/*List.range(1, 1)
List.range(1, 2)
List.range(1, 3)
List.range(1, 4)
List.range(1, 5)
res11: List[Int] = List()
res12: List[Int] = List(1)
res13: List[Int] = List(1, 2)
res14: List[Int] = List(1, 2, 3)
res15: List[Int] = List(1, 2, 3, 4)*/

List.range(1, 5) flatMap (
  i => List.range(1, i)
)

for (i <- List.range(1, 5);j <- List.range(1, i)) {

  println("i:"+i.toString() + "-j:" + j)
  /* println(j)

   print(i.toString()+"-"+List.range(1, i))*/
}

var sum=0
List(1, 2, 3, 4, 5) foreach (sum += _)
sum
*/

/* Ex
var words = List("the", "quick", "brown", "fox")
words.head
words.tail
(words.head /: words.tail) (_ +" "+ _)
*/

var s:String = "hi"
