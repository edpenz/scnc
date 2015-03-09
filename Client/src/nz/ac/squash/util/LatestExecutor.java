package nz.ac.squash.util;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class LatestExecutor extends AbstractExecutorService implements Runnable {
    Runnable mRunning = null;
    Runnable mPending = null;

    boolean mIsShutdown = false;
    Thread mExecutor;

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public synchronized boolean isShutdown() {
        return mIsShutdown;
    }

    @Override
    public boolean isTerminated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public synchronized void shutdown() {
        mIsShutdown = true;
        notify();
    }

    @Override
    public List<Runnable> shutdownNow() {
        // TODO
        shutdown();
        return null;
    }

    @Override
    public synchronized void execute(Runnable command) {
        if (mExecutor == null) {
            mExecutor = new Thread(this);
            mExecutor.setName(LatestExecutor.class.getSimpleName() +
                              " work thread");
            mExecutor.start();
        }

        mPending = command;
        notify();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (mIsShutdown) return;

                if (mPending != null) {
                    mRunning = mPending;
                    mPending = null;
                } else {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
            }

            mRunning.run();
            mRunning = null;
        }
    }

}
