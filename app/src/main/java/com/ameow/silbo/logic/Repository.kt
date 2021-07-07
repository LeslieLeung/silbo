package com.ameow.silbo.logic

import androidx.lifecycle.liveData
import com.ameow.silbo.logic.model.Request
import com.ameow.silbo.logic.network.SilboNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

object Repository {

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}