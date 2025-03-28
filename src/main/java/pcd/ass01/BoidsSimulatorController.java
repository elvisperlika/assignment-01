package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;

public class BoidsSimulatorController {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private final List<Worker> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    private final CyclicBarrier calculateVelocityBarrier = new CyclicBarrier(N_WORKERS);
    private final CyclicBarrier updateVelocityBarrier = new CyclicBarrier(N_WORKERS);
    private long t0;
    private final ManagerMonitor managerMonitor = new ManagerMonitor(N_WORKERS);
    private boolean workersWorking;

    public BoidsSimulatorController(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initWorkers();
        view.ifPresent(boidsView -> workersWorking = boidsView.isRunning());
    }

    private void initWorkers() {
        workers.clear();

        List<List<Boid>> partitions = new ArrayList<>();
        for (int i = 0; i < N_WORKERS; i++) {
            partitions.add(new ArrayList<>());
        }

        int i = 0;
        for (Boid boid : model.getBoids()) {
            if (i == partitions.size()) {
                i = 0;
            }
            partitions.get(i).add(boid);
            i++;
        }

        i = 0;
        for (List<Boid> part : partitions) {
            workers.add(new Worker("W" + i,
                    part,
                    model,
                    calculateVelocityBarrier,
                    updateVelocityBarrier,
                    managerMonitor));
            i++;
        }
        startWorkers();
    }

    private void startWorkers() {
        workers.forEach(Worker::start);
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    resumeWork();
                    if (managerMonitor.isAllWorkComplete()) {
                        view.get().update(framerate);
                        managerMonitor.resetWorksCounter();
                        updateFrameRate(t0);
                        workersWorking = false;
                    }
                } else {
                    pauseWork();
                }

                if (isSetNewNumberOfBoids()) {
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initWorkers();
                    workersWorking = false;
                }
            }
        }
    }

    private void pauseWork() {
        if (workersWorking) {
            workers.forEach(Worker::pauseWorker);
            workersWorking = false;
        }
    }

    private void resumeWork() {
        if (!workersWorking) {
            t0 = System.currentTimeMillis();
            workers.forEach(Worker::resumeWorker);
            workersWorking = true;
        }
    }

    private boolean isSetNewNumberOfBoids() {
        return view.get().getNumberOfBoids() != model.getBoids().size();
    }

    private void updateFrameRate(long t0) {
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
