package pcd.ass01;

public class CycleBarrierImpl implements CycleBarrier {
    public static final int GENERATION_LIMIT = 3;
    private final int parties;
    private int count;
    private int generation;

    public CycleBarrierImpl(int parties) {
        this.parties = parties;
        this.generation = 0;
        this.count = 0;
    }

    @Override
    public synchronized void await() {
        int currentGeneration = generation;
        count++;
        if (count == parties) {
            generation++;
            count = 0;
            notifyAll();
        } else {
            while (currentGeneration == generation) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (generation == GENERATION_LIMIT) {
                        generation = 0;
                    }
                }
            }
        }
    }

    @Override
    public synchronized boolean isBrokening() {
        return count == (parties - 1);
    }

}
