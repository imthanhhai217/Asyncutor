package com.jaroidx.asyncutor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Asyncutor<Params, Progress, Result> {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Result result;
    private Future<Result> resultFuture;
    private ExecutorService executorService;
    private Status status = Status.PENDING;

    protected abstract Result doInBackground(Params params);

    protected void onPreExecuted() {
    }

    protected void onPostExecuted(Result result) {
    }

    protected void onProgressUpdate(Progress progress) {

    }

    @WorkerThread
    protected void onPublishProgress(Progress progress) {
        if (!isCancelled()) {
            handler.post(() -> onProgressUpdate(progress));
        }
    }

    protected void onCancelled() {

    }

    protected void onCancelled(Result result) {
        onCancelled();
    }

    @AnyThread
    public final boolean isCancelled() {
        return cancelled.get();
    }

    @AnyThread
    public final Status getStatus() {
        return status;
    }

    @MainThread
    public final void cancel(boolean mayInterruptIfRunning) {
        cancelled.set(true);
        if (resultFuture != null) {
            resultFuture.cancel(mayInterruptIfRunning);
        }
    }

    protected boolean isShutdown() {
        return executorService.isShutdown();
    }

    @MainThread
    public final Future<Result> execute(@Nullable Params params) {
        status = Status.RUNNING;
        onPreExecuted();
        try {
            executorService = Executors.newSingleThreadExecutor();
            Callable<Result> backgroundCallback = () -> doInBackground(params);
            resultFuture = executorService.submit(backgroundCallback);
            executorService.submit(this::getResult);
            return resultFuture;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
        return null;
    }

    private Runnable getResult() {
        return () -> {
            try {
                if (!isCancelled()) {
                    result = resultFuture.get();
                    handler.post(() -> onPostExecuted(result));
                } else {
                    handler.post(this::onCancelled);
                }
                status = Status.FINISHED;
            } catch (InterruptedException | ExecutionException e) {
                Log.e("TAG", "Exception while trying to get result" + e.getMessage());
            }
        };
    }

    public enum Status {
        FINISHED, PENDING, RUNNING
    }
}
