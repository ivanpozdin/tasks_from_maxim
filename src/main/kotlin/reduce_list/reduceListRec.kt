package reduce_list

import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.min

const val ELEMENTS_NUMBER_FOR_ONE_COROUTINE = 10_000

fun <T> prepareCurrentAccumulators(
    coroutinesNumber: Int,
    list: List<T>,
    operation: (acc: T, value: T) -> T
): MutableList<T> {
    val accumulators = MutableList<T>(coroutinesNumber) { list[0] }

    runBlocking(Dispatchers.Default) {
        val coroutines = List(coroutinesNumber) { index ->
            async {
                val leftBound = index * ELEMENTS_NUMBER_FOR_ONE_COROUTINE
                val rightBound = min(list.lastIndex, (index + 1) * ELEMENTS_NUMBER_FOR_ONE_COROUTINE - 1)
                var acc = list[rightBound]
                for (i in leftBound..<rightBound) {
                    acc = operation(acc, list[i])
                }
                accumulators[index] = acc
            }
        }
        coroutines.forEach { it.await() }
    }
    return accumulators
}

data class Accumulators<T>(val current: MutableList<T>, val previous: MutableList<T>, var coroutinesNumber: Int)

fun <T> reducePart(
    index: Int, operation: (acc: T, value: T) -> T, accumulators: Accumulators<T>
) {
    val firstIndex = index * ELEMENTS_NUMBER_FOR_ONE_COROUTINE
    val lastIndex = min(accumulators.coroutinesNumber - 1, (index + 1) * ELEMENTS_NUMBER_FOR_ONE_COROUTINE - 1)
    var acc = accumulators.previous[lastIndex]
    for (i in firstIndex..<lastIndex) {
        acc = operation(acc, accumulators.previous[i])
    }
    accumulators.current[index] = acc
}

fun <T> copyToPrevious(accumulators: Accumulators<T>) {
    for (i in 0..<accumulators.coroutinesNumber) {
        accumulators.previous[i] = accumulators.current[i]
    }
}

fun getCoroutinesNumber(elementsNumber: Int): Int {
    return ceil(elementsNumber.toDouble() / ELEMENTS_NUMBER_FOR_ONE_COROUTINE.toDouble()).toInt()
}

fun <T> reduceList(list: List<T>, operation: (acc: T, value: T) -> T): T {
    if (list.isEmpty()) throw IllegalArgumentException("List must contain at least 1 element.")
    if (list.size == 1) {
        return list[0]
    }
    val coroutinesNumber = getCoroutinesNumber(list.size)
    val current = prepareCurrentAccumulators(coroutinesNumber, list, operation)
    val accumulators = Accumulators(current, current.toMutableList(), coroutinesNumber)

    while (accumulators.coroutinesNumber > 1) {
        val newCoroutinesNumber = getCoroutinesNumber(accumulators.coroutinesNumber)
        runBlocking(Dispatchers.Default) {
            List(newCoroutinesNumber) { index ->
                async { reducePart(index, operation, accumulators) }
            }.forEach { it.await() }
            accumulators.coroutinesNumber = newCoroutinesNumber
            copyToPrevious(accumulators)
        }
    }
    return accumulators.current[0]
}