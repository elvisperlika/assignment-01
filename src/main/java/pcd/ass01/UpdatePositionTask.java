package pcd.ass01;

public class UpdatePositionTask extends Task {

    public UpdatePositionTask(Boid boid, BoidsModel model) {
        super(boid, model);
    }

    @Override
    public Boolean call() throws Exception {
        boid.updatePos(model);
        return true;
    }
}
