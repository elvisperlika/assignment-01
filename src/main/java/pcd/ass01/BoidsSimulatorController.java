package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulatorController {

    private static final int FRAMERATE = 50;
    private final BoidsModel model;
    private final List<Worker> workers = new ArrayList<>();
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    private Optional<BoidsView> view;
    private int framerate;
    private long t0;
    private Monitor managerMonitor = new Monitor();
    private Barrier calVelCycleBarrier;
    private Barrier updVelCycleBarrier;
    private Barrier updPosBarrier;
    private boolean isTime0Updated = false;
    private int i = 0;
    private int N_LOOP = 100;
    private final List<Long> deltaTimes = new ArrayList<>();

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

        managerMonitor = new Monitor();
        calVelCycleBarrier = new CycleBarrierImpl(N_WORKERS);
        updVelCycleBarrier = new CycleBarrierImpl(N_WORKERS);
        updPosBarrier = new BarrierImpl(N_WORKERS);

        i = 0;
        for (List<Boid> part : partitions) {
            workers.add(new Worker("W" + i,
                    part,
                    model,
                    managerMonitor,
                    calVelCycleBarrier,
                    updVelCycleBarrier,
                    updPosBarrier
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
        while (i < N_LOOP) {
            managerMonitor.startWork();
            updateTime0();
            if (updPosBarrier.isBroken()) {
                // view.get().update(framerate);
                updateFrameRate(t0);
                i++;
                updPosBarrier.reset();
            }


//            if (view.isPresent()) {
//                if (view.get().isRunning()) {
//                    updateTime0();
//                } else {
//                    managerMonitor.stopWork();
//                }
//                if (view.get().isResetButtonPressed()) {
//                    managerMonitor.stopWork();
//                    model.resetBoids(view.get().getNumberOfBoids());
//                    view.get().update(framerate);
//                    initWorkers();
//                    view.get().setResetButtonUnpressed();
//                }
//            }
        }
        System.out.println("Mean Delta Time in ms: " + deltaTimes.stream().mapToLong(a -> a).average().orElse(0.0));
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
