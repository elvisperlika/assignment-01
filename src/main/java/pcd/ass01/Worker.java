package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier calculateVelocityBarrier;
    private final CyclicBarrier updateVelocityBarrier;
    private final CyclicBarrier positionBarrier;
    private final Semaphore pauseSemaphore;
    private volatile boolean running = true;

    public Worker(String name, List<Boid> boidList, BoidsModel model,
                  CyclicBarrier calculateVelocityBarrier,
                  CyclicBarrier updateVelocityBarrier,
                  CyclicBarrier positionBarrier) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.calculateVelocityBarrier = calculateVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.positionBarrier = positionBarrier;
        this.pauseSemaphore = new Semaphore(0);
    }

    public void run() {
        while (true) {
            log(getName() + " is run ");
            if (isSimulationPaused()) {
                rest();
            }
            calculateVelocityWithBarrier();
            updateVelocityWithBarrier();
            updatePositionWithBarrier();
        }
    }

    private void rest() {
        log(getName() + " rest PRE acquire " + pauseSemaphore.getQueueLength());
        try {
            pauseSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log(getName() + " rest POST acquire " + pauseSemaphore.getQueueLength());
    }

    private boolean isSimulationPaused() {
        return !running;
    }

    private void updatePositionWithBarrier() {
        try {
            boidList.forEach(boid -> boid.updatePos(model));
            positionBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateVelocityWithBarrier() {
        try {
            boidList.forEach(boid -> boid.updateAndNormalizeVelocity(model));
            updateVelocityBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        if (updateVelocityBarrier.isBroken()) {
            updateVelocityBarrier.reset();
        }
    }

    private void calculateVelocityWithBarrier() {
        try {
            boidList.forEach(boid -> boid.calculateVelocity(model));
            calculateVelocityBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        if (calculateVelocityBarrier.isBroken()) {
            calculateVelocityBarrier.reset();
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

    public void releaseWork() {
        if (positionBarrier.isBroken()) {
            positionBarrier.reset();
        }
    }
}
