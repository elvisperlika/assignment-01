package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor {
    private boolean working = false;
    private final ReentrantLock mutex = new ReentrantLock();
    private final Condition cond = mutex.newCondition();

    public void waitUntilWorkStart() {
        if (!working) {
            try {
                mutex.lock();
                cond.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                mutex.unlock();
            }
        }
    }

    public void startWork() {
        if (!working) {
            working = true;
            mutex.lock();
            try {
                cond.signalAll();
            } finally {
                mutex.unlock();
            }
        }
    }

    public void stopWork() {
        if (working) {
            mutex.lock();
            working = false;
            mutex.unlock();
        }
    }

}
