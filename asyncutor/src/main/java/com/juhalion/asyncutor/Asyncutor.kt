package com.juhalion.asyncutor

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

abstract class Asyncutor<Params, Progress, Result> {
    private val handler = Handler(Looper.getMainLooper())
    private val cancelled = AtomicBoolean(false)
    private var result: Result? = null
    private var resultFuture: Future<Result>? = null
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()

    @get:AnyThread
    var status = Status.PENDING
        private set

    protected abstract fun doInBackground(params: Params?): Result
    protected fun onPreExecuted() {}
    protected open fun onPostExecuted(result: Result?) {}
    protected fun onProgressUpdate(progress: Progress) {}
    @WorkerThread
    protected fun onPublishProgress(progress: Progress) {
        if (!isCancelled()) {
            handler.post { onProgressUpdate(progress) }
        }
    }

    private fun onCancelled() {}
    protected fun onCancelled(result: Result) {
        onCancelled()
    }

    @AnyThread
    fun isCancelled(): Boolean {
        return cancelled.get()
    }

    @MainThread
    fun cancel(mayInterruptIfRunning: Boolean) {
        cancelled.set(true)
        if (resultFuture != null) {
            resultFuture!!.cancel(mayInterruptIfRunning)
        }
    }

    protected val isShutdown: Boolean = executorService.isShutdown

    @MainThread
    fun execute(params: Params?): Future<Result>? {
        status = Status.RUNNING
        onPreExecuted()
        try {
            val backgroundCallback = Callable { doInBackground(params) }
            resultFuture = executorService.submit(backgroundCallback)
            executorService.submit(getResult())
            return resultFuture
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            executorService.shutdown()
        }
        return null
    }

    private fun getResult(): Runnable? {
        val runnable = Runnable {
            try {
                if (!isCancelled()) {
                    result = resultFuture!!.get()
                    handler.post { onPostExecuted(result) }
                } else {
                    handler.post { onCancelled() }
                }
                status = Status.FINISHED
            } catch (e: InterruptedException) {
                Log.e("TAG", "Exception while trying to get result" + e.message)
            } catch (e: ExecutionException) {
                Log.e("TAG", "Exception while trying to get result" + e.message)
            }
        }
        return runnable
    }

    enum class Status {
        FINISHED, PENDING, RUNNING
    }
}