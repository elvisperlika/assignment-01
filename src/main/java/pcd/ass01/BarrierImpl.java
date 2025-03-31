package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierImpl implements Barrier {
    protected final int parties;
    protected int count;
    protected ReentrantLock mutex = new ReentrantLock();
    protected Condition cond = mutex.newCondition();
    private boolean broken = false;
    private int exitCount;

    public BarrierImpl(int parties) {
        this.parties = parties;
        this.count = 0;
        this.exitCount = parties;
    }

    @Override
    public void await() {
        mutex.lock();
        count++;
        while (!broken) {
            try {
                cond.await(); // Release the lock and wait
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                exitCount--;
                mutex.unlock();
            }
        }
    }

    public boolean isBrokening() {
        if (count == parties) {
            broken = true;
        }
        return broken;
    }

    public void reset() {
        mutex.lock();
        if (exitCount == 0) {
            count = 0;
            broken = false;
        }
        try {
            cond.signalAll();
        } finally {
            mutex.unlock();
        }
    }

}
