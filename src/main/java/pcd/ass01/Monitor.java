package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor {
    private boolean working = false;
    private final ReentrantLock mutex = new ReentrantLock();
    private final Condition cond = mutex.newCondition();

    public void waitUntilWorkStart() {
        mutex.lock();
        if (!working) {
            try {
                cond.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
            }
        }
        mutex.unlock();
    }

    public void startWork() {
        mutex.lock();
        if (!working) {
            working = true;
            try {
                cond.signalAll();
            } finally {
            }
        }
        mutex.unlock();
    }

    public void stopWork() {
        mutex.lock();
        if (working) {
            working = false;
        }
        mutex.unlock();
    }

}
