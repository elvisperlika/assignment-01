package pcd.ass01;

import java.util.Optional;

public class BoidsSimulatorController {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private long t0;
    private boolean isTime0Updated = false;
    private volatile boolean loop = true ;
    private Monitor managerMonitor;
    private CycleBarrier calVelCycleBarrier;
    private CycleBarrier updVelCycleBarrier;
    private CycleBarrier updPosCycleBarrier;

    public BoidsSimulatorController(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initTasksAndVirtualThreads();
    }

    private void initTasksAndVirtualThreads() {
        var boids = model.getBoids();
        var boidsSize = boids.size();
        managerMonitor = new Monitor();
        calVelCycleBarrier = new CycleBarrierImpl(boidsSize);
        updVelCycleBarrier = new CycleBarrierImpl(boidsSize);
        updPosCycleBarrier = new CycleBarrierImpl(boidsSize + 1); // + 1 is the Main Thread

        boids.forEach(boid -> {
            Thread t = Thread.ofVirtual().unstarted(() -> {
                while (loop) {
                    try {
                        managerMonitor.waitUntilWorkStart();
                        boid.calculateVelocity(model);
                        calVelCycleBarrier.await();
                        boid.updateVelocity(model);
                        updVelCycleBarrier.await();
                        boid.updatePosition(model);
                        updPosCycleBarrier.await();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            t.start();
        });
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    managerMonitor.startWork();
                    updateTime0();
                    if (updPosCycleBarrier.isBrokening()) {
                        managerMonitor.stopWork();
                        view.get().update(framerate);
                        updateFrameRate(t0);
                        updPosCycleBarrier.await();
                    }
                } else {
                    managerMonitor.stopWork();
                }
                if (view.get().isResetButtonPressed()) {
                    managerMonitor.stopWork();
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initTasksAndVirtualThreads();
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
