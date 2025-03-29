package pcd.ass01;

public class BarrierImpl implements Barrier {
    protected final int parties;
    protected int count;
    private boolean cond;

    public BarrierImpl(int parties) {
        this.parties = parties;
        this.count = 0;
        this.cond = false;
    }

    @Override
    public synchronized void await() {
        cond = false;
        count++;
        while (count <= parties && !cond) {
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
        cond = true;
        notifyAll();
    }

}
