package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AVBLatencyMath extends Evaluator {
    private static final Logger logger = LoggerFactory.getLogger(AVBLatencyMath.class.getSimpleName());

    @Override
    public Cost evaluate(List<Unicast> route) {
        Map<GCLEdge, List<Allocation>> ttAllocMap = new HashMap<>();
        List<Multicast> multicasts = Multicast.generateMulticasts(route);
        AVBLatencyMathCost cost = new AVBLatencyMathCost();

        //region <O3>
        HashMap<Application, HashSet<GCLEdge>> edgeMap = new HashMap<>();
        for (Unicast r : route) {
            if (!edgeMap.containsKey(r.getApplication())) {
                edgeMap.put(r.getApplication(), new HashSet<>());
            }
            edgeMap.get(r.getApplication()).addAll(r.getRoute().getEdgeList());
        }

        HashSet<GCLEdge> disjointEdges = new HashSet<>();
        for (Map.Entry<Application, HashSet<GCLEdge>> entry : edgeMap.entrySet()) {
            if (entry.getKey() instanceof SRTApplication) {
                disjointEdges.addAll(entry.getValue());
            }
        }

        cost.add(AVBLatencyMathCost.Objective.three, disjointEdges.size());
        //endregion

        //region <O1 and O2>
        for (Multicast m : multicasts) {
            if (m.getApplication() instanceof TTApplication) {
                for (Unicast u : m.getUnicasts()) {
                    TTApplication uTT = ((TTApplication) u.getApplication());
                    for (GCLEdge edge : u.getRoute().getEdgeList()) {
                        if(!ttAllocMap.containsKey(edge)){
                            ttAllocMap.put(edge, new ArrayList<>());
                            ttAllocMap.get(edge).add(new Allocation(true, uTT.getCMI(), uTT.getMessageSizeMbps()));
                        }
                        else {
                            for(Allocation allocation: ttAllocMap.get(edge)) {
                                allocation.setAllocMbps(allocation.getAllocMbps() + uTT.getMessageSizeMbps());
                            }

                        }
                    }
                }
            }
        }

        Map<GCLEdge,List<Allocation>> allocMap = new HashMap<>(ttAllocMap);

        for (Multicast m : multicasts) {
            if (m.getApplication() instanceof SRTApplication app) {
                //Run over all the unique edges in that application
                for (GCLEdge edge : edgeMap.get(app)) {
                    //If not already there, put it there
                    if (!allocMap.containsKey(edge)) {
                        allocMap.put(edge, new ArrayList<>());
                        allocMap.get(edge).add(new Allocation(false, app.getCMI(), 0));
                    }

                    double allocMbps = app.getMessageSizeMbps();
                    boolean isFound = false;
                    for(Allocation allocation: allocMap.get(edge)){
                        if(!allocation.getIsTT() && allocation.getPeriodCMI() == app.getCMI()){
                            allocation.setAllocMbps(allocation.getAllocMbps() + allocMbps);
                            isFound = true;
                            break;
                        }
                    }

                    if(!isFound){
                        allocMap.get(edge).add(new Allocation(false, app.getCMI(), allocMbps));
                    }
                }
            }
        }

        for (Multicast m : multicasts) {
            if (m.getApplication() instanceof SRTApplication app) {
                double maxLatency = 0;
                for (Unicast u : m.getUnicasts()) {
                    double latency = 0;
                    for (GCLEdge edge : u.getRoute().getEdgeList()) {

                        double capacity = edge.getIdleSlope();
                        for(Allocation allocation: allocMap.get(edge)){
                            if(allocation.getIsTT()){
                                capacity -= allocation.getAllocMbps() / edge.getRateMbps();
                            }
                        }

                        if (capacity < 0) {
                            capacity = 0.01;
                        }

                        latency += calculateMaxLatency(edge, allocMap.get(edge), app, capacity);
                    }
                    //For multicast routing, were only interested in the worst route
                    if (maxLatency < latency) {
                        maxLatency = latency;
                    }
                }
                cost.setWCD(m, maxLatency);
                if (maxLatency > app.getDeadline()) {
                    cost.add(AVBLatencyMathCost.Objective.one, 1);
                }
                cost.add(AVBLatencyMathCost.Objective.two, maxLatency / app.getDeadline());
            }
        }
        //endregion

        return cost;
    }

    private double calculateMaxLatency(GCLEdge edge, List<Allocation> totalAllocmbps, SRTApplication app, double capacity) {
        double tDevice = Constants.DEVICE_DELAY / edge.getRateMbps();

        double tMaxPacketSFDIPG = (double) ((Constants.MAX_BE_FRAME_BYTES + Constants.SFD + Constants.IPG) * 8) / edge.getRateMbps();

        double tAllStreams = 0;
        for(Allocation allocation: totalAllocmbps){
            if(!allocation.getIsTT()){
                tAllStreams += (allocation.getAllocMbps() * app.getCMI()) / edge.getRateMbps();
            }
        }

        double tStreamPacketSFDIPG = (double) ((app.getFrameSizeByte() + Constants.SFD + Constants.IPG) * 8) / edge.getRateMbps();

        double tStreamPacket = (double) (app.getFrameSizeByte() * 8) / edge.getRateMbps();

        double evaluator = (tDevice + tMaxPacketSFDIPG + tAllStreams - tStreamPacketSFDIPG) * (edge.getRateMbps() / capacity) / 100 + tStreamPacket;

        return edge.calculateWorstCaseInterference(evaluator);
    }
}
