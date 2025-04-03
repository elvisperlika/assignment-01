package pcd.ass01;

public class Monitor {
    private boolean working = false;
    private boolean isWorkComplete = false;

    public synchronized void waitUntilWorkStart() {
        while (!working) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void startWork() {
        if (!working) {
            working = true;
            notifyAll();
        }
    }

    public synchronized void stopWork() {
        if (working) {
            working = false;
        }
    }

    public synchronized void setWorkCompleteAndRest() {
        isWorkComplete = true;
        stopWork();
    }

    public synchronized boolean isWorkComplete() {
        return isWorkComplete;
    }
}
