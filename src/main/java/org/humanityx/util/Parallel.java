package org.humanityx.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Parallel processing with a given number of threads.
 * Created by arvid on 23-6-15.
 */
public class Parallel {

    public static <T> T run(Callable<T> parallelTask){
        return run(parallelTask, Runtime.getRuntime().availableProcessors(), null);
    }


    public static <T> T run(Callable<T> parallelTask, int threadCount){
        return run(parallelTask, threadCount, null);
    }

    public static <T> T run(Callable<T> parallelTask, int threadCount, T defaultResult){
        // The problem is that all parallel streams use common fork-join thread pool and if
        // you submit a long-running task, you effectively block all threads in the pool.
        // Consequently you block all other tasks that are using parallel streams.
        // Imagine a servlet environment, when one request calls getStockInfo() and another one countPrimes().
        // One will block the other one even though each of them requires different resources.
        // What's worse, you can not specify thread pool for parallel streams, the whole class
        // loader has to use the same one.
        // See: http://java.dzone.com/articles/think-twice-using-java-8

        // So, no...
        // urls.stream().parallel().forEach(consumer);

        // But there is a trick that will allow you to execute parallel operation in a specific thread-pool:
        // See: http://blog.krecan.net/2014/03/18/how-to-specify-thread-pool-for-java-8-parallel-streams/

        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        try {
            // parallel task here
            return forkJoinPool.submit(parallelTask).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return defaultResult;

        // So we are able to use parallel streams and choose the thread-pool at the same time.
        // But that's not all. The trick solves other two issues you might not be aware of.

        // The first one is that the submitting thread is used as a worker. In other words,
        // if you execute calculation on a parallel stream, the thread that submitted the calculation
        // is used for processing. So you are basically mixing threads from a pool with a thread that
        // has completely different life-cycle. I can imagine several scenarios where it can cause problems.
        // Some of them are described here. By explicitly using fork join pool, we make sure that the
        // parallel stream is processed only by threads from the thread pool.

        // The other problem is that the parallel processing might take a long time and I would like to
        // limit the time spent on the task. By explicitly using submit, I can specify a timeout in the
        // get method. It comes handy in real-world systems where we just do not want to hope that everything
        // will go according to plan.
    }

    public static <T> void run(final Runnable parallelTask, final int threadCount){
        if(parallelTask == null)
            return;

        final ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        try {
            // parallel task here
            forkJoinPool.submit(parallelTask).join();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
