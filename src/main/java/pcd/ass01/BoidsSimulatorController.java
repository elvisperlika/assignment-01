package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

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
    private Barrier calVelCycleBarrier;
    private Barrier updVelCycleBarrier;
    private Barrier updPosBarrier;
    private volatile int i = 0;
    private int N_LOOP = 100;
    private final List<Long> deltaTimes = new ArrayList<>();

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
        updPosBarrier = new CycleBarrierImpl(boidsSize + 1); // + 1 is the Main Thread

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
                        updPosBarrier.await();
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
        while (i < N_LOOP) {
            updateTime0();
            managerMonitor.startWork();
            if (updPosBarrier.isBrokening()) {
                // view.get().update(framerate);
                updateFrameRate(t0);
                i++;
                updPosBarrier.await();
            }
//            if (view.isPresent()) {
//                if (view.get().isRunning()) {
//                } else {
//                    managerMonitor.stopWork();
//                }
//                if (view.get().isResetButtonPressed()) {
//                    managerMonitor.stopWork();
//                    model.resetBoids(view.get().getNumberOfBoids());
//                    initTasksAndVirtualThreads();
//                    view.get().setResetButtonUnpressed();
//                }
//            }
        }
        System.out.println("Mean Delta Time in ms: " + deltaTimes.stream().mapToLong(a -> a).average().orElse(0.0));
        System.exit(-1);
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
        deltaTimes.add(dtElapsed);
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
