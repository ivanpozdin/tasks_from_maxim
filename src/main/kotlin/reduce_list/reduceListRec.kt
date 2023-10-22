package reduce_list

import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.min

private const val ELEMENTS_NUMBER_FOR_ONE_COROUTINE = 10_000

private fun <T> prepareCurrentAccumulators(
    coroutinesNumber: Int, list: List<T>, operation: (acc: T, value: T) -> T
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

private data class Accumulators<T>(val current: MutableList<T>, val previous: MutableList<T>, var coroutinesNumber: Int)

private fun <T> reducePart(
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

private fun <T> copyToPrevious(accumulators: Accumulators<T>) {
    for (i in 0..<accumulators.coroutinesNumber) {
        accumulators.previous[i] = accumulators.current[i]
    }
}

private fun getCoroutinesNumber(elementsNumber: Int): Int {
    return ceil(elementsNumber.toDouble() / ELEMENTS_NUMBER_FOR_ONE_COROUTINE.toDouble()).toInt()
}

fun <T> List<T>.reduceAssociatively(operation: (acc: T, value: T) -> T): T {
    if (this.isEmpty()) throw IllegalArgumentException("List must contain at least 1 element.")
    if (this.size == 1) return this[0]

    val coroutinesNumber = getCoroutinesNumber(this.size)
    val current = prepareCurrentAccumulators(coroutinesNumber, this, operation)
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
    return accumulators.current.first()
}