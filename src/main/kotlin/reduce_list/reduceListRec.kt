package reduce_list

import kotlinx.coroutines.*

fun <T> reduceList(list: List<T>, operation: (acc: T, value: T) -> T): T = runBlocking(Dispatchers.Default)  {
    fun reduceListRec(leftIndex: Int, rightIndex: Int): Deferred<T> = async{
        if (rightIndex - leftIndex <= 10) {
            var acc = list[rightIndex]
            for (i in leftIndex + 1..<rightIndex) {
                acc = operation(acc, list[i])
            }
            return@async acc
        }
        val newRightForLeftTask = (leftIndex + rightIndex) / 2
        val newLeftForRightTask = newRightForLeftTask + 1

        val leftTask = reduceListRec(leftIndex, newRightForLeftTask)
        val rightTask = reduceListRec(newLeftForRightTask, rightIndex)

        val leftAcc = leftTask.await()
        val rightAcc = rightTask.await()
        return@async operation(leftAcc, rightAcc)
    }

    return@runBlocking reduceListRec(0, list.lastIndex).await()
}

