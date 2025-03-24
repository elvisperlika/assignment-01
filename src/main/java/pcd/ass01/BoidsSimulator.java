package pcd.ass01;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private final List<Worker> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private final CyclicBarrier calculateVelocityBarrier = new CyclicBarrier(N_WORKERS);
    private final CyclicBarrier updateVelocityBarrier = new CyclicBarrier(N_WORKERS);
    private final CyclicBarrier positionBarrier = new CyclicBarrier(N_WORKERS);
    private boolean workersAreAlive = false;
    private boolean workersStarted = false;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initWorkers();
    }

    private void initWorkers() {
        workers.clear();

        int boidsForWorker = model.getBoids().size() / N_WORKERS;
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
            workers.add(new Worker("W" + i, part, model,
                    calculateVelocityBarrier,
                    updateVelocityBarrier,
                    positionBarrier));
        }
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {
            var t0 = System.currentTimeMillis();
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    if (setNewBoidsSize()) {
                        pauseWorkers();
                        model.resetBoids(view.get().getSizeBoids());
                        initWorkers();
                        workersStarted = false;
                    }
                    activeWorkers();
                    view.get().update(framerate);
                    releaseWorkers();
                } else {
                    pauseWorkers();
                }

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
    }

    private boolean setNewBoidsSize() {
        return view.get().getSizeBoids() != model.getBoids().size();
    }

    private void releaseWorkers() {
        workers.forEach(Worker::releaseWork);
    }

    private void pauseWorkers() {
        if (workersAreAlive) {
            workers.forEach(Worker::pause);
        }
        workersAreAlive = false;
    }

    private void activeWorkers() {
        if (!workersStarted) {
            workers.forEach(Worker::start);
            workersStarted = true;
        }
        if (!workersAreAlive) {
            workers.forEach(Worker::play);
            workersAreAlive = true;
        }
    }

}
