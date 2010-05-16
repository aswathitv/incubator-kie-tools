/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.async.scheduling;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.async.TimedTask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;

public class PooledExecutorService implements TaskProvider {
    private final BlockingQueue<TimedTask> queue;

    /**
     * this *must* be an ordered Set. The narrower type is to accomodate wrapping with
     * Collections.synchronizedSet();
     */
    private final BlockingQueue<TimedTask> scheduledTasks;
    private final ThreadWorkerPool pool;

    private boolean stopped = false;

    private final SchedulerThread schedulerThread;

    private final ReentrantLock mutex = new ReentrantLock(true);
    private final int maxQueueSize;

    /**
     * Constructs a new PooledExecutorService with the specified queue size.
     *
     * @param queueSize The size of the underlying worker queue.
     */
    public PooledExecutorService(int queueSize) {
        maxQueueSize = queueSize;
        queue = new ArrayBlockingQueue<TimedTask>(queueSize);
        pool = new ThreadWorkerPool(this);

        scheduledTasks = new PriorityBlockingQueue<TimedTask>();
        schedulerThread = new SchedulerThread();
    }

    /**
     * Schedule a task for immediate execution.
     *
     * @param runnable Runnable task
     * @throws InterruptedException thrown if the thread waiting for an empty spot on the execution queue is
     *                              interrupted.
     */
    public void execute(final Runnable runnable) throws InterruptedException {
        queue.add(new SingleFireTask(runnable));
    }

    public AsyncTask schedule(final Runnable runnable, TimeUnit unit, long interval) {
        mutex.lock();
        try {
            TimedTask task;
            scheduledTasks.offer(task = new DelayedTask(runnable, unit.toMillis(interval)));

            return task;
        }
        finally {
            mutex.unlock();
        }
    }

    public AsyncTask scheduleRepeating(final Runnable runnable, final TimeUnit unit, final long initial, final long interval) {
        mutex.lock();
        try {
            TimedTask task;
            scheduledTasks.offer(task = new RepeatingTimedTask(runnable, unit.toMillis(initial), unit.toMillis(interval)));

            return task;
        }
        finally {
            mutex.unlock();
        }
    }

    public void start() {
        mutex.lock();
        try {
            if (stopped) {
                throw new IllegalStateException("work queue cannot be started after it's been stopped");
            }

            schedulerThread.start();

            pool.startPool();
        }
        finally {
            mutex.unlock();
        }
    }

    public void shutdown() {
        mutex.lock();
        try {
            schedulerThread.requestStop();
            queue.clear();
            stopped = true;
        }
        finally {
            mutex.unlock();
        }
    }

    private long runAllDue() throws InterruptedException {
        long nextRunTime = 0;
        TimedTask task;

        while ((task = scheduledTasks.poll(60, java.util.concurrent.TimeUnit.SECONDS)) != null) {
            if (!task.isDue(currentTimeMillis())) {
                long wait = task.nextRuntime() - currentTimeMillis();
                if (wait > 0) {
                    try {
                        Thread.sleep(wait);
                    }
                    catch (InterruptedException e) {
                        if (stopped) return 0;
                    }
                }
            }

            /**
             * Sechedule the task for execution.
             */
            if (!queue.offer(task, 5, java.util.concurrent.TimeUnit.SECONDS)) {
                throw new RuntimeException("could not schedule task: queue is saturated");
            }

            if (task.calculateNextRuntime()) {
                scheduledTasks.offer(task);
            }
        }

        return nextRunTime;

    }

    private volatile int idleCount = 0;
  //  private final Object loadLock = new Object();

    private void checkLoad() {
  //      synchronized (loadLock) {
            int queueSize = queue.size();

            if (queueSize == 0) return;
            else if (queueSize > (0.80d * maxQueueSize)) {
                pool.addWorker();
            } else if (queueSize == 0) {
                idleCount++;
            }

            if (idleCount > 100) {
                idleCount = 0;
                pool.removeWorker();
            }
    //    }
    }

    /**
     * Returns the next Runnable task that is currently due to run.  This method will block until a task is available.
     *
     * @return Runnable task.
     * @throws InterruptedException thrown if the thread waiting on a ready task is interrupted.
     */
    public TimedTask getNextTask() throws InterruptedException {
        checkLoad();
        return queue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private static class SingleFireTask extends TimedTask {
        private final Runnable runnable;
        boolean fired = false;

        private SingleFireTask(Runnable runnable) {
            this.runnable = runnable;
            period = -1;
            nextRuntime = -1;
        }

        @Override
        public boolean isDue(long time) {
            synchronized (this) {
                return !fired;
            }
        }

        public void run() {
            synchronized (this) {
                fired = true;
                runnable.run();
            }
        }
    }

    private static class DelayedTask extends TimedTask {
        private final Runnable runnable;
        private boolean fired = false;

        private DelayedTask(Runnable runnable, long delayMillis) {
            this.runnable = runnable;
            this.period = -1;
            this.nextRuntime = System.currentTimeMillis() + delayMillis;
        }

        @Override
        public boolean isDue(long time) {
            synchronized (this) {
                return !fired && time >= nextRuntime;
            }
        }

        public void run() {
            synchronized (this) {
                fired = true;
                nextRuntime = -1;
                runnable.run();
            }
        }
    }

    private static class RepeatingTimedTask extends TimedTask {
        private final Runnable runnable;

        private RepeatingTimedTask(Runnable runnable, long initialMillis, long intervalMillis) {
            this.runnable = runnable;
            nextRuntime = System.currentTimeMillis() + initialMillis;
            period = intervalMillis;
        }

        public void run() {
            runnable.run();
        }
    }

    private class SchedulerThread extends Thread {
        //private volatile long sleepInterval;
        private volatile boolean running = false;


        private SchedulerThread() {
        }

        @Override
        public void run() {


            while (running) {
                try {
                    while (running) {
                        runAllDue();
                    }
                }
                catch (InterruptedException e) {
                    // just fall through.
                }
            }
        }

        public void start() {
            running = true;
            super.start();
        }

        public void requestStop() {
            running = false;
            interrupt();
        }
    }

}
