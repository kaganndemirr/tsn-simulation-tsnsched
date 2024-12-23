package ktu.kaganndemirr.phy.pp.heuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.phy.pp.PPGraphPaths;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.LaursenInitialSolutionMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class LaursenInitialSolution extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(LaursenInitialSolution.class.getSimpleName());

    private final int k;

    public LaursenInitialSolution(int k) {
        this.k = k;
    }

    Cost cost;
    List<Unicast> solution;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    private Map<Double, Double> durationMap;


    @Override
    public Solution solveH(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator) {

        durationMap = new HashMap<>();

        Instant graphPathsStartTime = Instant.now();
        PPGraphPaths gp = new PPGraphPaths(physicalTopology, applications, k);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();

        avbUnicastCandidates = gp.getAVBRoutingCandidates();
        ttUnicasts = gp.getTTRoutes();

        Instant solutionStartTime = Instant.now();
        solution = LaursenInitialSolutionMethods.constructInitialSolution(avbUnicastCandidates, ttUnicasts, k, evaluator);
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
