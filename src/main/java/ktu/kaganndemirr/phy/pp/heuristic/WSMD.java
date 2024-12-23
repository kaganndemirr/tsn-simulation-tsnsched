package ktu.kaganndemirr.phy.pp.heuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.phy.pp.PPGraphPaths;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.MCDMSpecificMethods;
import ktu.kaganndemirr.util.Constants;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class WSMD extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(WSMD.class.getSimpleName());

    private final int k;

    public WSMD(int k) {
        this.k = k;
    }

    Cost cost;
    List<Unicast> solution;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    private Map<Double, Double> durationMap;

    String wsmNormalization;
    String mcdmObjective;

    @Override
    public Solution solveHWSM(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate, final String mcdmObjective) {
        durationMap = new HashMap<>();

        this.wsmNormalization = wsmNormalization;
        this.mcdmObjective = mcdmObjective;

        Instant graphPathsStartTime = Instant.now();
        PPGraphPaths gp = new PPGraphPaths(physicalTopology, applications, k);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();

        avbUnicastCandidates = gp.getAVBRoutingCandidates();
        ttUnicasts = gp.getTTRoutes();

        Instant solutionStartTime = Instant.now();
        if (Objects.equals(mcdmObjective, Constants.AVBTT)){
            solution = MCDMSpecificMethods.WSMLWRDAVBTT(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT);
        } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTH)) {
            solution = MCDMSpecificMethods.WSMLWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength);
        } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTHUTIL)) {
            solution = MCDMSpecificMethods.WSMLWRDAVBTTLengthUtil(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength, wUtil, rate);
        }

        Instant solutionEndTime = Instant.now();
        long solutionDuration = Duration.between(solutionStartTime, solutionEndTime).toMillis();
        cost = evaluator.evaluate(solution);

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (solutionDuration / 1e3) + (graphPathsDuration) / 1e3);

        return new Solution(cost, Multicast.generateMulticasts(solution));
    }

    @Override
    public List<Unicast> getSolution() {
        return solution;
    }

    @Override
    public List<UnicastCandidates> getAVBUnicastCandidates() {
        return avbUnicastCandidates;
    }

    @Override
    public Map<Double, Double> getDurationMap() {
        return durationMap;
    }

    @Override
    public List<Unicast> getTTUnicasts() {
        return ttUnicasts;
    }

}
