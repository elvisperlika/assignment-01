package pcd.ass01;

import java.util.List;

public class Worker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Monitor monitor;
    private final Barrier updVelBarrier;
    private final Barrier updPosBarrier;
    private final Barrier calVelBarrier;

    public Worker(String name,
                  List<Boid> boidsPartition,
                  BoidsModel model,
                  Monitor monitor,
                  Barrier calVelBarrier,
                  Barrier updVelBarrier,
                  Barrier updPosBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.monitor = monitor;
        this.calVelBarrier = calVelBarrier;
        this.updVelBarrier = updVelBarrier;
        this.updPosBarrier = updPosBarrier;
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
        calVelBarrier.await();
    }

    private void updateVelocityAndWaitCycleBarrier() {
        boidsPartition.forEach(boid -> boid.updateVelocity(model));
        updVelBarrier.await();
    }

    private void updatePositionAndWaitBarrier() {
        boidsPartition.forEach(boid -> boid.updatePosition(model));
        updPosBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }

}
