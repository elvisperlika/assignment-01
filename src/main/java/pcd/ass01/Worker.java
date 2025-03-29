package pcd.ass01;

import java.util.List;

public class Worker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Monitor monitor;
    private Barrier calVelBarrier;
    private final Barrier updVelBarrier;
    private final Barrier updPosBarrier;

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
            calculateVelocityAndWaitBarrier();
            updateVelocityAndWaitBarrier();
            updatePositionAndWaitBarrier();
        }
    }

    private void calculateVelocityAndWaitBarrier() {
        boidsPartition.forEach(boid -> boid.calculateVelocity(model));
        calVelBarrier.await();
    }

    private void updateVelocityAndWaitBarrier() {
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
