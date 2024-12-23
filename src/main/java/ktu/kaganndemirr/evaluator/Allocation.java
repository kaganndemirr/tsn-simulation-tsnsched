package ktu.kaganndemirr.evaluator;

public class Allocation {
    private final boolean isTT;
    private final double periodCMI;
    private double allocMbps;

    public Allocation(boolean isTT, double periodCMI, double allocMbps){
        this.isTT = isTT;
        this.periodCMI = periodCMI;
        this.allocMbps = allocMbps;
    }

    public boolean getIsTT() {
        return isTT;
    }

    public double getAllocMbps() {
        return allocMbps;
    }

    public double getPeriodCMI() {
        return periodCMI;
    }

    public void setAllocMbps(double allocMbps) {
        this.allocMbps = allocMbps;
    }
}
