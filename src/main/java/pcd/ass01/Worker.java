package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier updateVelocityBarrier;
    private final CyclicBarrier positionBarrier;
    private volatile boolean running = false;
    private boolean workComplete = false;

    public Worker(String name, List<Boid> boidList, BoidsModel model,
                  CyclicBarrier updateVelocityBarrier,
                  CyclicBarrier positionBarrier) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.positionBarrier = positionBarrier;
    }

    public void run() {
        while (true) {
            if (isSimulationPaused()) {
                rest();
            }
            // log(" is working ");
            updateVelocityWithBarrier();
            updatePositionWithBarrier();
        }
    }

    private void rest() {
        // log("REST");
        LockSupport.park();
    }

    private boolean isSimulationPaused() {
        return !running;
    }

    private void updatePositionWithBarrier() {
        boidList.forEach(boid -> boid.updatePos(model));
        setWorkComplete();
        rest();
    }

    private void setWorkComplete() {
        workComplete = true;
    }

    private void updateVelocityWithBarrier() {
        try {
            boidList.forEach(boid -> boid.updateVelocity(model));
            updateVelocityBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }

    public void play() {
        running = true;
        LockSupport.unpark(this);
    }

    public void pause() {
        running = false;
    }

    public void resumeWork() {
        LockSupport.unpark(this);
    }

    public boolean isWorkComplete() {
        return workComplete;
    }
}
