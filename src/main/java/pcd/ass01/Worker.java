package pcd.ass01;

import java.util.List;
import java.util.concurrent.locks.Lock;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final Barrier barrier;

    public Worker(String name, List<Boid> boidList, BoidsModel model, Barrier barrier) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.barrier = barrier;
    }

    public void run() {
        while (true) {
            boidList.forEach(boid -> boid.updateVelocity(model));
            boidList.forEach(boid -> boid.normalizeVelocity(model));
            boidList.forEach(boid -> boid.updatePos(model));
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + msg);
        }
    }
}
