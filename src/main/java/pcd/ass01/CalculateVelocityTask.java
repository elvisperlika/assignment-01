package pcd.ass01;

import java.util.concurrent.Callable;

public class CalculateVelocityTask extends Task {

    public CalculateVelocityTask(Boid boid, BoidsModel model) {
        super(boid, model);
    }

    @Override
    public Boolean call() throws Exception {
        boid.calculateVelocity(model);
        return true;
    }
}
