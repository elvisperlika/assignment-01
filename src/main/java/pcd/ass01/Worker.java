package pcd.ass01;

import java.util.List;

public class Worker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Monitor monitor;

    public Worker(String name,
                  List<Boid> boidsPartition,
                  BoidsModel model,
                  Monitor monitor) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.monitor = monitor;
    }

    public void run() {
        while (true) {
            monitor.waitUntilWorkStart();
            log("work");
            boidsPartition.forEach(boid -> boid.calculateVelocity(model));
            boidsPartition.forEach(boid -> boid.calculateVelocity(model));
            boidsPartition.forEach(boid -> boid.updatePosition(model));
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }

}
