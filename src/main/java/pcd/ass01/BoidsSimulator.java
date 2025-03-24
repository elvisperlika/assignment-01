package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private ForkJoinPool forkJoinPool;

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private List<CalculateVelocityTask> calcVelocityTasks;
    private List<UpdateVelocityTask> updateVelocityTasks;
    private List<UpdatePositionTask> updatePositionTasks;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initWorkers();
    }

    private void initWorkers() {
        forkJoinPool = new ForkJoinPool(N_WORKERS);
        calcVelocityTasks = new ArrayList<>();
        updateVelocityTasks = new ArrayList<>();
        updatePositionTasks = new ArrayList<>();
        model.getBoids().forEach(boid -> {
                    calcVelocityTasks.add(new CalculateVelocityTask(boid, model));
                    updateVelocityTasks.add(new UpdateVelocityTask(boid, model));
                    updatePositionTasks.add(new UpdatePositionTask(boid, model));
                });

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
                        forkJoinPool.shutdownNow();
                        model.resetBoids(view.get().getSizeBoids());
                        initWorkers();
                    }

                    try {
                        forkJoinPool.invokeAll(calcVelocityTasks);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    try {
                        forkJoinPool.invokeAll(updateVelocityTasks);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    try {
                        forkJoinPool.invokeAll(updatePositionTasks);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    view.get().update(framerate);
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

}
