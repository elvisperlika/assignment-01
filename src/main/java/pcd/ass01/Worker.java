package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    private final Semaphore pauseSemaphore;
    private boolean running = true;

    public Worker(String name, List<Boid> boidList, BoidsModel model, CyclicBarrier barrier, CyclicBarrier positionBarrier, Semaphore pauseSemaphore) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.velocityBarrier = barrier;
        this.positionBarrier = positionBarrier;
        this.pauseSemaphore = pauseSemaphore;
    }

    public void run() {
        while (true) {
            if (running) {
                log(getName() + " - running ");
            } else {
                try {
                    pauseSemaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
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
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + msg);
        }
    }

    public void play() {
        running = true;
        pauseSemaphore.release();
    }

    public void pause() {
        running = false;
    }

}
