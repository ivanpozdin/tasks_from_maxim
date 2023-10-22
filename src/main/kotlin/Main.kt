import reduce_list.reduceAssociatively
import kotlin.system.measureTimeMillis

val add = { a: Int, b: Int -> a + b }
fun main() {
    val myList = List(123_456_789) { i -> i / 10 }

    val stdRes: Int
    val stdReduceTime = measureTimeMillis {
        stdRes = myList.reduce(add)
    }
    println("Std reduce fun time: $stdReduceTime, res=$stdRes")

    val myRes: Int
    val myReduceTime = measureTimeMillis {
        myRes = myList.reduceAssociatively(add)
    }
    println("My reduce fun time:  $myReduceTime, res=$myRes")
}