package pcd.ass01;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyExecutorService {

    private ExecutorService executorService;
    private final List<Boid> boids;

    public MyExecutorService(int nWorkers, List<Boid> boids) {
        executorService = Executors.newFixedThreadPool(nWorkers);
        this.boids = boids;
    }

    public void compute() {

    }
}
