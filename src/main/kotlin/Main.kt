import reduce_list.reduceList
import kotlin.system.measureTimeMillis

val add = { a: Int, b: Int -> a + b }
fun main() {
    val size = 178_345_123
    val myList = List(size){i -> i / 10}

    val stdRes: Int
    val stdReduceTime = measureTimeMillis {
        stdRes = myList.reduce(add)
    }
    println("Std reduce fun time: $stdReduceTime, res=$stdRes")

    val myRes: Int
    val myReduceTime = measureTimeMillis {
        myRes = reduceList(myList, add)
    }
    println("My reduce fun time:  $myReduceTime, res=$myRes")
}