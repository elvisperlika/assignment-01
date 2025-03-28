package pcd.ass01;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Worker extends Thread {

    private final List<Boid> boidList;
    private final BoidsModel model;
    private final CyclicBarrier updateVelocityBarrier;
    private final CyclicBarrier calculateVelocityBarrier;
    private final ManagerMonitor managerMonitor;
    private volatile boolean running;

    public Worker(String name,
                  List<Boid> boidList,
                  BoidsModel model,
                  CyclicBarrier calculateVelocityBarrier,
                  CyclicBarrier updateVelocityBarrier,
                  ManagerMonitor managerMonitor, boolean running) {
        super(name);
        this.boidList = boidList;
        this.model = model;
        this.calculateVelocityBarrier = calculateVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.managerMonitor = managerMonitor;
        this.running = running;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (this) {
                while (!running) {
                    try {
                        wait(); // Wait until notified
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            // Execute thread logic here
            calculateVelocityWithBarrier();
            updateVelocityWithBarrier();
            updatePositionAndRest();
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
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

    private void updateVelocityWithBarrier() {
        try {
            boidList.forEach(boid -> boid.updateVelocity(model));
            updateVelocityBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePositionAndRest() {
        boidList.forEach(boid -> boid.updatePos(model));
        managerMonitor.incWorksCompleted();
        running = false;
    }

    public synchronized void pauseWorker() {
        running = false;
    }

    public synchronized void resumeWorker() {
        running = true;
        notify(); // Notify the thread to continue
    }
}
