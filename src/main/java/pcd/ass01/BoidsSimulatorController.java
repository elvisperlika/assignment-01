package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulatorController {

    private final BoidsModel model;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES - 1; // remove the Master
    private ForkJoinPool forkJoinPool;
    private List<Callable<Void>> calculateVelocityTaskList;
    private List<Callable<Void>> updateVelocityTaskList;
    private List<Callable<Void>> updatePositionTaskList;
    private Monitor managerMonitor;
    private int i = 0;
    private int N_LOOP = 1_500;

    public BoidsSimulatorController(BoidsModel model) {
        this.model = model;
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

        forkJoinPool = new ForkJoinPool(N_WORKERS);
        managerMonitor = new Monitor();
        MasterWorker master = new MasterWorker("Master",
                managerMonitor,
                calculateVelocityTaskList,
                updateVelocityTaskList,
                updatePositionTaskList,
                forkJoinPool);
        master.start();
    }

    public void runSimulation() {
        while (i < N_LOOP) {
            managerMonitor.startWork();
            if (managerMonitor.isWorkComplete()) {
                i++;
            }
        }
    }
}
