package ktu.kaganndemirr.phy.shortestpath.dijkstra;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.GraphSpecificMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dijkstra extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(Dijkstra.class.getSimpleName());

    Cost cost;
    List<Unicast> solution;

    private List<Unicast> ttUnicasts;
    private List<Unicast> avbUnicasts;

    private Map<Double, Double> durationMap;

    @Override
    public Solution solveSP(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator) {
        solution = new ArrayList<>();
        durationMap = new HashMap<>();

        List<Application> applicationsHaveNotExplicitPath = GraphSpecificMethods.findApplicationsHaveNotExplicitPath(applications);
        List<Application> applicationsHaveExplicitPath = GraphSpecificMethods.findApplicationsHaveExplicitPath(applications);
        List<Unicast> applicationsHaveExplicitPathUnicasts = GraphSpecificMethods.createUnicastForApplicationsHaveExplicitPath(applicationsHaveExplicitPath, physicalTopology);
        solution.addAll(applicationsHaveExplicitPathUnicasts);

        Instant graphPathStartTime = Instant.now();
        DijkstraGraphPath gp = new DijkstraGraphPath(physicalTopology, applicationsHaveNotExplicitPath);
        Instant graphPathEndTime = Instant.now();

        long graphPathDuration = Duration.between(graphPathStartTime, graphPathEndTime).toMillis();

        avbUnicasts = gp.getAVBUnicasts();
        ttUnicasts = gp.getTTUnicasts();

        solution.addAll(avbUnicasts);
        solution.addAll(ttUnicasts);

        Instant solutionStartTime = Instant.now();
        cost = evaluator.evaluate(solution);
        Instant solutionEndTime = Instant.now();


        long solutionDuration = Duration.between(solutionStartTime, solutionEndTime).toMillis();

        durationMap.put(Double.parseDouble(cost.toString().split("\\s")[0]), (graphPathDuration / 1e3) + (solutionDuration / 1e3));

        return new Solution(cost, Multicast.generateMulticasts(solution));
    }

    @Override
    public List<Unicast> getSolution() {
        return solution;
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
