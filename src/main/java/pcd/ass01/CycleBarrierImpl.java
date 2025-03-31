package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CycleBarrierImpl implements Barrier {
    private final int parties;
    private int count;
    private int generation;
    private ReentrantLock mutex = new ReentrantLock();
    private Condition cond = mutex.newCondition();

    public CycleBarrierImpl(int parties) {
        this.parties = parties;
        this.generation = 0;
        this.count = 0;
    }

    @Override
    public void await() {
        mutex.lock();
        var gen = generation;
        count++;
        if (count == parties) {
            generation++;
            count = 0;
            cond.signalAll();
            mutex.unlock();
        } else {
            while (gen == generation) {
                try {
                    cond.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    mutex.unlock();
                }
            }
        }
    }

    @Override
    public boolean isBrokening() {
        return count == (parties - 1);
    }

}
