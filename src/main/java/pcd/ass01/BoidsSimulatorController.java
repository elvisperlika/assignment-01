package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulatorController {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    // private static final int FRAMERATE = 50;
    // private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    // private long t0;
    // private boolean isTime0Updated = false;
    private ForkJoinPool forkJoinPool;
    private List<Callable<Void>> calculateVelocityTaskList;
    private List<Callable<Void>> updateVelocityTaskList;
    private List<Callable<Void>> updatePositionTaskList;
    // private volatile boolean loop = true ;
    private Monitor managerMonitor;
    private int i = 0;
    private int N_LOOP = 1_500;

    public BoidsSimulatorController(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initTasksAndMaster();
    }

    private void initTasksAndMaster() {
        calculateVelocityTaskList = new ArrayList<>();
        updateVelocityTaskList = new ArrayList<>();
        updatePositionTaskList = new ArrayList<>();
        var boids = model.getBoids();
        boids.forEach(boid -> {
            calculateVelocityTaskList.add(new Task(boid, model, Boid::calculateVelocity));
            updateVelocityTaskList.add(new Task(boid, model, Boid::updateVelocity));
            updatePositionTaskList.add(new Task(boid, model, Boid::updatePosition));
        });

        forkJoinPool = new ForkJoinPool();
        managerMonitor = new Monitor();
        MasterWorker master = new MasterWorker("Master",
                managerMonitor,
                calculateVelocityTaskList,
                updateVelocityTaskList,
                updatePositionTaskList,
                forkJoinPool);
        master.start();
    }

    public void attachView(BoidsView view) {
        // this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (i < N_LOOP) {
            managerMonitor.startWork();
            if (managerMonitor.isWorkComplete()) {
            }
            /* if (view.isPresent()) {
                if (view.get().isRunning()) {
                    updateTime0();
                }
                if (view.get().isResetButtonPressed()) {
                    forkJoinPool.shutdownNow();
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initTasksAndMaster();
                    view.get().setResetButtonUnpressed();
                } */
            }
        }
    }

    /* private void updateTime0() {
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
    } */
}
