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
                  CyclicBarrier positionBarrier,
                  Semaphore pauseSemaphore) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.calculateVelocityBarrier = calculateVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.positionBarrier = positionBarrier;
        this.pauseSemaphore = pauseSemaphore;
    }

    public void run() {
        while (true) {
            checkIfSimulationIsPause();
            calculateVelocityWithBarrier();
            updateVelocityWithBarrier();
            updatePositionWithBarrier();
        }
    }

    private void updatePositionWithBarrier() {
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

    private void checkIfSimulationIsPause() {
        if (!running) {
            try {
                pauseSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
