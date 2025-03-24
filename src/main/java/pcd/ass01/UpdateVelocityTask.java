package pcd.ass01;

public class UpdateVelocityTask extends Task {
    public UpdateVelocityTask(Boid boid, BoidsModel model) {
        super(boid, model);
    }

    @Override
    public Boolean call() throws Exception {
        boid.updateAndNormalizeVelocity(model);
        return true;
    }
}
