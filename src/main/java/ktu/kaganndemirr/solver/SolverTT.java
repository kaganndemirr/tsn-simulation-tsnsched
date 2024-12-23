package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.Graph;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class SolverTT {

    // Shortestpath
    public Solution solveSP(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator) {
        return null;
    }

    public List<Unicast> getSolution() {
        return null;
    }

    public Map<Double, Double> getDurationMap() {
        return null;
    }

    public List<Unicast> getTTUnicasts() {
        return null;
    }
}
