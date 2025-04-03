package pcd.ass01;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

public class MasterWorker extends Thread {

    private final Monitor managerMonitor;
    private final List<Callable<Void>> calculateVelocityTaskList;
    private final List<Callable<Void>> updateVelocityTaskList;
    private final List<Callable<Void>> updatePositionTaskList;
    private final ForkJoinPool forkJoinPool;

    public MasterWorker(String name,
                        Monitor managerMonitor,
                        List<Callable<Void>> calculateVelocityTaskList,
                        List<Callable<Void>> updateVelocityTaskList,
                        List<Callable<Void>> updatePositionTaskList,
                        ForkJoinPool forkJoinPool) {
        super(name);
        this.managerMonitor = managerMonitor;
        this.calculateVelocityTaskList = calculateVelocityTaskList;
        this.updateVelocityTaskList = updateVelocityTaskList;
        this.updatePositionTaskList = updatePositionTaskList;
        this.forkJoinPool = forkJoinPool;
    }

    public void run() {
        while (true) {
            managerMonitor.waitUntilWorkStart();
            try {
                forkJoinPool.invokeAll(calculateVelocityTaskList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                forkJoinPool.invokeAll(updateVelocityTaskList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                forkJoinPool.invokeAll(updatePositionTaskList);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                managerMonitor.setWorkCompleteAndRest();
            }
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}
