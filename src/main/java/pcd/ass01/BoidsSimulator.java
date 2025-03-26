package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private final List<Worker> workers = new ArrayList<>();

    private static final int FRAMERATE = 25;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private final CyclicBarrier updateVelocityBarrier = new CyclicBarrier(N_WORKERS);
    private final CyclicBarrier updatePositionBarrier = new CyclicBarrier(N_WORKERS);
    private boolean workersArePlaying = false;
    private long t0;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initWorkers();
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
            workers.add(new Worker("W" + i, part, model,
                    updateVelocityBarrier,
                    updatePositionBarrier));
            i++;
        }

        startWorkers();
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    if (!workersArePlaying) {
                        playWorkers();
                        t0 = System.currentTimeMillis();
                    }
                    if (isAllWorkComplete()) {
                        view.get().update(framerate);
                        updateFrameRate(t0);
                        t0 = System.currentTimeMillis();
                        resumeWorkers();
                    }
                } else {
                    if (workersArePlaying) {
                        pauseWorkers();
                    }
                }

                if (setNewBoidsSize()) {
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initWorkers();
                    playWorkers();
                }
            }
        }
    }

    private void updateFrameRate(long t0) {
        var t1 = System.currentTimeMillis();
        var dtElapsed = t1 - t0;

        synchronized (System.out) {
            System.out.println("DTE: " + dtElapsed / 1000);
        }

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
            System.out.println("FR: " + framerate);
        }
    }

    private boolean isAllWorkComplete() {
        return workers.stream().allMatch(Worker::isWorkComplete);
    }

    private boolean setNewBoidsSize() {
        return view.get().getNumberOfBoids() != model.getBoids().size();
    }

    private void resumeWorkers() {
        workers.forEach(Worker::resumeWork);
    }

    private void pauseWorkers() {
        workers.forEach(Worker::pause);
    }

    private void playWorkers() {
        workersArePlaying = true;
        workers.forEach(Worker::play);
    }

    private void startWorkers() {
        workersArePlaying = false;
        workers.forEach(Worker::start);
    }

}
