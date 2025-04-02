package pcd.ass01;

import java.util.List;

public class Worker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Monitor monitor;
    private final CycleBarrier updVelCycleBarrier;
    private final CycleBarrier updPosCycleBarrier;
    private final CycleBarrier calVelCycleBarrier;

    public Worker(String name,
                  List<Boid> boidsPartition,
                  BoidsModel model,
                  Monitor monitor,
                  CycleBarrier calVelCycleBarrier,
                  CycleBarrier updVelCycleBarrier,
                  CycleBarrier updPosCycleBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.monitor = monitor;
        this.calVelCycleBarrier = calVelCycleBarrier;
        this.updVelCycleBarrier = updVelCycleBarrier;
        this.updPosCycleBarrier = updPosCycleBarrier;
    }

    public void run() {
        while (true) {
            monitor.waitUntilWorkStart();
            calculateVelocityAndWaitCycleBarrier();
            updateVelocityAndWaitCycleBarrier();
            updatePositionAndWaitBarrier();
        }
    }

    private void calculateVelocityAndWaitCycleBarrier() {
        boidsPartition.forEach(boid -> boid.calculateVelocity(model));
        calVelCycleBarrier.await();
    }

    private void updateVelocityAndWaitCycleBarrier() {
        boidsPartition.forEach(boid -> boid.updateVelocity(model));
        updVelCycleBarrier.await();
    }

    private void updatePositionAndWaitBarrier() {
        boidsPartition.forEach(boid -> boid.updatePosition(model));
        updPosCycleBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }

}
