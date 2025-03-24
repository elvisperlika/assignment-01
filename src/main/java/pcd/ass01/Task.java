package pcd.ass01;

import java.util.concurrent.Callable;

public class Task implements Callable<Boolean> {
    protected final Boid boid;
    protected final BoidsModel model;

    public Task(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    @Override
    public Boolean call() throws Exception {
        boid.calculateVelocity(model);
        return true;
    }
}
