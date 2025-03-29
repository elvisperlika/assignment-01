package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulatorController {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final List<Worker> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    private long t0;
    private final Monitor playMonitor = new Monitor();

    public BoidsSimulatorController(BoidsModel model) {
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
            workers.add(new Worker("W" + i,
                    part,
                    model,
                    playMonitor
            ));
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
            synchronized (System.out) {
            }
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    playMonitor.startWork();
                    view.get().update(framerate);
                } else {
                    playMonitor.stopWork();
                }
                if (view.get().isResetButtonPressed()) {
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initWorkers();
                    view.get().setResetButtonUnpressed();
                }
            }
        }
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
