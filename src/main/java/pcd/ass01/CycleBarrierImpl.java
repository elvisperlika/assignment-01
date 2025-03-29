package pcd.ass01;

public class CycleBarrierImpl extends BarrierImpl {
    private int generation;

    public CycleBarrierImpl(int parties) {
        super(parties);
        this.generation = 0;
        this.count = parties;
    }

    @Override
    public synchronized void await() {
        var gen = generation;
        count--;
        if (count != 0) {
            while (gen == generation) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            count = parties;
            generation++;
            notifyAll();
        }
    }
}
