package pcd.ass01;

public interface Barrier {

    void await();

    boolean isBroken();

    void reset();
}
