package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CycleBarrierImpl implements CycleBarrier {
    private final int parties;
    private int count;
    private final ReentrantLock mutex = new ReentrantLock();
    private final Condition cond = mutex.newCondition();
    private boolean broken = false;

    public CycleBarrierImpl(int parties) {
        this.parties = parties;
        this.count = 0;
    }

    @Override
    public void await() {
        mutex.lock();
        try {
            broken = false;
            count++;
            if (count == parties) {
                broken = true;
                count = 0;
                cond.signalAll();
            } else {
                while (count < parties && !broken) {
                    try {
                        cond.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
    }

    public boolean isBrokening() {
        var isBrokening = false;
        mutex.lock();
        if (count == (parties - 1)) {
            isBrokening = true;
        }
        mutex.unlock();
        return isBrokening;
    }

}
