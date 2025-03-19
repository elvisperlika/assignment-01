package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    private boolean running = true;

    public Worker(String name, List<Boid> boidList, BoidsModel model, CyclicBarrier barrier, CyclicBarrier positionBarrier) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.velocityBarrier = barrier;
        this.positionBarrier = positionBarrier;
    }

    public void run() {
        while (true) {
            if (running) {
                log(getName() + " - running ");
                try {
                    boidList.forEach(boid -> boid.updateVelocity(model));
                    boidList.forEach(boid -> boid.normalizeVelocity(model));
                    velocityBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                if (velocityBarrier.isBroken()) {
                    velocityBarrier.reset();
                }

                try {
                    boidList.forEach(boid -> boid.updatePos(model));
                    positionBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                if (positionBarrier.isBroken()) {
                    positionBarrier.reset();
                }
            } else {
                log(getName() + " - stopped ");
            }
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + msg);
        }
    }

    public void play() {
        running = true;
    }

    public void pause() {
        running = false;
    }

}
