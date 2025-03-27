package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.LockSupport;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier updateVelocityBarrier;
    private final ManagerMonitor managerMonitor;
    private volatile boolean simulationRunning = false;

    public Worker(String name,
                  List<Boid> boidList,
                  BoidsModel model,
                  CyclicBarrier updateVelocityBarrier,
                  ManagerMonitor managerMonitor) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.managerMonitor = managerMonitor;
    }

    public void run() {
        while (true) {
            if (!simulationRunning) {
                LockSupport.park();
            }
            updateVelocityWithBarrier();
            updatePosition();
            workIsComplete();
        }
    }

    private void workIsComplete() {
        managerMonitor.incWorksCompleted();
        LockSupport.park();
    }

    private void updatePosition() {
        boidList.forEach(boid -> boid.updatePos(model));
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
        simulationRunning = true;
        LockSupport.unpark(this);
    }

    public void pause() {
        simulationRunning = false;
    }
}
