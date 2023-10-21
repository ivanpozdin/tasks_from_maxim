import reduce_list.reduceList
import kotlin.system.measureTimeMillis

val add = { a: Int, b: Int -> a + b }
fun main(args: Array<String>) {
    val size = 1000_0024
    val myList = List(size) { index -> index }

    val stdRes: Int
    val stdReduceTime = measureTimeMillis {
        stdRes = myList.reduce(add)
    }
    println("Std reduce fun time: $stdReduceTime, res=$stdRes")

    val myRes: Int
    val myReduceTime = measureTimeMillis {
        myRes = reduceList(myList, add)
    }
    println("My reduce fun time: $myReduceTime, res=$myRes")


}