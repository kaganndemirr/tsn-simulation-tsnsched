package ktu.kaganndemirr.architecture;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GCLEdge extends DefaultWeightedEdge {

    private static final Logger logger = LoggerFactory.getLogger(GCLEdge.class.getSimpleName());

    //region <Private Variables>
    private final double idleSlope;
    private final List<GCL> GCLs;
    private final int rateMbps;
    //endregion

    /**
     * Constructor
     *
     * @param rateMbps the transmission speed of the link
     * @param latency  mac delay of corresponding switch (us)
     */
    public GCLEdge(int rateMbps, double idleSlope) {
        this.rateMbps = rateMbps;
        GCLs = new ArrayList<>();
        this.idleSlope = idleSlope;
    }

    public GCLEdge(GCLEdge edge) {
        this.rateMbps = edge.rateMbps;
        this.GCLs = edge.GCLs;
        this.idleSlope = edge.idleSlope;

    }

    public void addGCL(GCL gcl) {
        GCLs.add(gcl);
    }

    public List<GCL> getGCLs() {
        return GCLs;
    }

    public int getRateMbps() {
        return rateMbps;
    }

    public double getIdleSlope() {
        return idleSlope;
    }

    //region <equals and hashCode>
    @Override
    public Node getSource() {
        return (Node) super.getSource();
    }

    @Override
    public Node getTarget() {
        return (Node) super.getTarget();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        GCLEdge edge = (GCLEdge) obj;
        return Objects.equals(getSource().name, edge.getSource().name) && Objects.equals(getTarget().name, edge.getTarget().name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(getSource().name) + Objects.hash(getTarget().name);
    }
    //endregion

    public double calculateWorstCaseInterference(double duration) {
        double interference = duration;
        if (!GCLs.isEmpty()) {
            List<GCE> gces = convertGCLToGCEs(GCLs);
            double iMax = getIMax(duration, gces, GCLs.getFirst().getHyperPeriod());
            interference = duration + iMax;
        }
        return interference;
    }

    private double getIMax(double duration, List<GCE> gces, double hyperPeriod) {
        double iMax = 0;
        for (int i = 0; i < gces.size(); i++) {
            double i_curr = 0, rem = duration;
            int index = i;
            while (rem > 0) {
                i_curr += gces.get(index).getDuration();
                rem -= getSlack(gces.get((index + 1) % gces.size()), gces.get(index), hyperPeriod);
                index = (index + 1) % gces.size();
            }
            if (i_curr > iMax) {
                iMax = i_curr;
            }
        }
        return iMax;
    }

    private double getSlack(GCE next, GCE curr, double hyperPeriod) {
        if (next.aStart < curr.aEnd) {
            return (next.aStart + hyperPeriod - curr.aEnd);
        } else {
            return next.aStart - curr.aEnd;
        }
    }

    private List<GCE> convertGCLToGCEs(List<GCL> gcls) {
        List<GCE> gces = new ArrayList<>();
        for (GCL gcl : gcls) {
            for (int i = 0; i < gcl.getFrequency(); i++) {
                gces.add(new GCE(gcl.getOffset(), gcl.getOffset() + gcl.getDuration()));
            }
        }
        return gces;
    }

    private static class GCE {
        private final double aStart;
        private final double aEnd;

        GCE(double start, double end) {
            aStart = start;
            aEnd = end;
        }

        private double getDuration() {
            return aEnd - aStart;
        }

        @Override
        public String toString() {
            return "[" + aStart + "-" + aEnd + "]";
        }
    }
}
