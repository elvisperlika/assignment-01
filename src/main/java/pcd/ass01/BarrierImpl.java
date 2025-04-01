package pcd.ass01;

public class BarrierImpl implements Barrier {
    protected final int parties;
    protected int count;
    private boolean broken;

    public BarrierImpl(int parties) {
        this.parties = parties;
        this.count = 0;
        this.broken = false;
    }

    @Override
    public synchronized void await() {
        broken = false;
        count++;
        while (count <= parties && !broken) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized boolean isBroken() {
        return count == parties;
    }

    @Override
    public synchronized void reset() {
        count = 0;
        broken = true;
        notifyAll();
    }

}
