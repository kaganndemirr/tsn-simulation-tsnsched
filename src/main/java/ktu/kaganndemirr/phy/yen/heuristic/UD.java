package ktu.kaganndemirr.phy.yen.heuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.phy.yen.YenGraphPaths;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.USpecificMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UD extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(UD.class.getSimpleName());

    private final int k;

    public UD(int k) {
        this.k = k;
    }

    Cost cost;
    List<Unicast> solution;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    private Map<Double, Double> durationMap;

    @Override
    public Solution solveHU(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, int rate) {

        durationMap = new HashMap<>();

        Instant graphPathsStartTime = Instant.now();
        YenGraphPaths gp = new YenGraphPaths(physicalTopology, applications, k);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();

        avbUnicastCandidates = gp.getAVBRoutingCandidates();
        ttUnicasts = gp.getTTRoutes();

        Instant solutionStartTime = Instant.now();
        solution = USpecificMethods.constructSolutionD(avbUnicastCandidates, ttUnicasts, rate);
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
