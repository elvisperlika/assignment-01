package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private List<Worker> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = 50;
    private CyclicBarrier velocityBarrier = new CyclicBarrier(N_WORKERS);
    private CyclicBarrier positionBarrier = new CyclicBarrier(N_WORKERS);

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initWorkers();
    }

    private void startWorkers() {
        for(Worker worker : workers) {
            worker.start();
        }
    }

    private void initWorkers() {
        int boidsForWorker = model.getBoids().size() / N_WORKERS;
        System.out.println("bois " + model.getBoids().size());
        System.out.println("workers " + N_WORKERS);
        System.out.println("bFR " + boidsForWorker);

        List<List<Boid>> partitions = new ArrayList<>();
        for (int i=0; i< model.getBoids().size(); i += boidsForWorker) {
            partitions.add(model.getBoids().subList(i, Math.min(i + boidsForWorker, model.getBoids().size())));
        }

        int i = 0;
        for (List<Boid> partition : partitions) {
            i++;
            workers.add(new Worker("W" + i, partition, model, velocityBarrier, positionBarrier));
        }
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() {
        startWorkers();
    	while (true) {
            var t0 = System.currentTimeMillis();

    		if (view.isPresent()) {
            	view.get().update(framerate);
            	var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var framratePeriod = 1000/FRAMERATE;
                
                if (dtElapsed < framratePeriod) {		
                	try {
                		Thread.sleep(framratePeriod - dtElapsed);
                	} catch (Exception ex) {}
                	framerate = FRAMERATE;
                } else {
                	framerate = (int) (1000/dtElapsed);
                }
    		}
            
    	}
    }
}
