package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier calculateVelocityBarrier;
    private final CyclicBarrier updateVelocityBarrier;
    private final CyclicBarrier positionBarrier;
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
    }

    public void run() {
        while (true) {
            if (isSimulationPaused()) {
                rest();
            }
            calculateVelocityWithBarrier();
            updateVelocityWithBarrier();
            updatePositionWithBarrier();
            LockSupport.park();
        }
    }

    private void rest() {
        LockSupport.park();
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
    }

    private void calculateVelocityWithBarrier() {
        try {
            boidList.forEach(boid -> boid.calculateVelocity(model));
            calculateVelocityBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + msg);
        }
    }

    public void play() {
        running = true;
        LockSupport.unpark(this);
    }

    public void pause() {
        running = false;
    }

    public void releaseWork() {
        LockSupport.unpark(this);
    }
}
