package pcd.ass01;

public class ManagerMonitor {

    private final int nWorkers;
    private int numberWorkedCompleted = 0;

    public ManagerMonitor(int nWorkers) {
        this.nWorkers = nWorkers;
    }

    public synchronized void incWorksCompleted() {
        numberWorkedCompleted = numberWorkedCompleted + 1;
    }

    public synchronized boolean isAllWorkComplete() {
        return numberWorkedCompleted == nWorkers;
    }

    public synchronized void resetWorksCounter() {
        numberWorkedCompleted = 0;
    }
}
