package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulatorController {

    private static final int N_LOOP = 1_500;
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
    private int i = 0;

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
                        boid.calculateVelocity(model);
                        calculateVelocityCycleBarrier.await();
                        boid.updateVelocity(model);
                        updateVelocityCycleBarrier.await();
                        boid.updatePosition(model);
                        updatePositionCycleBarrier.await();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            virtualThreads.add(t);
        });
        virtualThreads.forEach(Thread::start);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (i < N_LOOP) {
            managerMonitor.startWork();
            if (updatePositionCycleBarrier.isBrokening()) {
                updatePositionCycleBarrier.await();
                i++;
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
