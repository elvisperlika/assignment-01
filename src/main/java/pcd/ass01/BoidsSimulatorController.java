package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulatorController {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private long t0;
    private boolean isTime0Updated;
    private volatile boolean loop = true ;
    private Monitor managerMonitor;
    private CycleBarrier calculateVelocityCycleBarrier;
    private CycleBarrier updateVelocityCycleBarrier;
    private CycleBarrier updatePositionCycleBarrier;
    private List<Thread> virtualThreads;

    public BoidsSimulatorController(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initVirtualThreads();
    }

    private void initVirtualThreads() {
        virtualThreads = new ArrayList<>();
        isTime0Updated = false;
        var boids = model.getBoids();
        var boidsNumber = boids.size();
        managerMonitor = new Monitor();
        calculateVelocityCycleBarrier = new CycleBarrierImpl(boidsNumber);
        updateVelocityCycleBarrier = new CycleBarrierImpl(boidsNumber);
        updatePositionCycleBarrier = new CycleBarrierImpl(boidsNumber + 1); // + 1 is the Main Thread

        boids.forEach(boid -> {
            Thread t = Thread.ofVirtual().unstarted(() -> {
                while (loop) {
                    try {
                        managerMonitor.waitUntilWorkStart();
                        System.out.println("CAL");
                        boid.calculateVelocity(model);
                        calculateVelocityCycleBarrier.await();
                        System.out.println("VEL-2");
                        boid.updateVelocity(model);
                        updateVelocityCycleBarrier.await();
                        System.out.println("POS");
                        boid.updatePosition(model);
                        updatePositionCycleBarrier.await();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            virtualThreads.add(t);
        });
        System.out.println("CREATI: " +  virtualThreads.size());
        virtualThreads.forEach(Thread::start);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (loop) {
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    managerMonitor.startWork();
                    updateTime0();
                    if (updatePositionCycleBarrier.isBrokening()) {
                        view.get().update(framerate);
                        updateFrameRate(t0);
                        updatePositionCycleBarrier.await();
                    }
                } else {
                    managerMonitor.stopWork();
                }
                if (view.get().isResetButtonPressed()) {
                    managerMonitor.stopWork();
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initVirtualThreads();
                    view.get().setResetButtonUnpressed();
                }
            }
        }
    }

    private void updateTime0() {
        if (!isTime0Updated) {
            t0 = System.currentTimeMillis();
            isTime0Updated = true;
        }
    }

    private void updateFrameRate(long t0) {
        isTime0Updated = false;
        var t1 = System.currentTimeMillis();
        var dtElapsed = t1 - t0;
        var frameratePeriod = 1000 / FRAMERATE;
        if (dtElapsed < frameratePeriod) {
            try {
                Thread.sleep(frameratePeriod - dtElapsed);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            framerate = FRAMERATE;
        } else {
            framerate = (int) (1000 / dtElapsed);
        }
    }
}
