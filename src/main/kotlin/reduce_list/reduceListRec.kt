package reduce_list

import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.min

fun <T> reduceList(list: List<T>, operation: (acc: T, value: T) -> T): T {
    if (list.isEmpty()) throw IllegalArgumentException("List must contain at least 1 element.")
    if (list.size == 1) {
        return list[0]
    }
    val elementsNumberForOneCoroutine = 10_000
    var coroutinesNumber = ceil(list.size.toDouble() / elementsNumberForOneCoroutine.toDouble()).toInt()
    val accumulators = MutableList<T>(coroutinesNumber) { list[0] }

    runBlocking(Dispatchers.Default) {
        val coroutines = List(coroutinesNumber) { index ->
            async{
                val leftBound = index * elementsNumberForOneCoroutine
                val rightBound = min(list.lastIndex, (index + 1) * elementsNumberForOneCoroutine - 1)
                var acc = list[rightBound]
                for (i in leftBound..<rightBound) {
                    acc = operation(acc, list[i])
                }
                accumulators[index] = acc
            }
        }
        coroutines.forEach{it.await()}
       }
    val previousAccumulators = accumulators.toMutableList()

    while (coroutinesNumber > 1) {
        val newCoroutinesNumber = ceil(coroutinesNumber.toDouble() / elementsNumberForOneCoroutine.toDouble()).toInt()
        runBlocking(Dispatchers.Default) {
            val coroutines = List(newCoroutinesNumber) { index ->
                async{
                    val leftBound = index * elementsNumberForOneCoroutine
                    val rightBound = min(coroutinesNumber - 1, (index + 1) * elementsNumberForOneCoroutine - 1)
                    var acc = previousAccumulators[rightBound]
                    for (i in leftBound..<rightBound) {
                        acc = operation(acc, previousAccumulators[i])
                    }
                    accumulators[index] = acc
                }
            }
            coroutines.forEach{it.await()}
            coroutinesNumber = newCoroutinesNumber
            for (i in 0..<coroutinesNumber) {
                previousAccumulators[i] = accumulators[i]
            }
        }
    }
    return accumulators[0]
}