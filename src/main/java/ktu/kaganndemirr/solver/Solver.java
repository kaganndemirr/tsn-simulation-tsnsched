package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.Graph;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class Solver {

    // Shortestpath
    public Solution solveSP(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator) {
        return null;
    }

    // Heuristic, WSM
    public Solution solveHWSM(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final String wsmNormalization, final double wAVB, final double wTT, final double wLength, final double wUtil, final int rate, final String mcdmObjective) {
        return null;
    }

    // Heuristic, WPM
    public Solution solveHWPM(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final double wAVB, final double wTT, final double wLength, final double wUtil, final int rate, final String mcdmObjective, final String wpmVersion, final String wpmValueType) {
        return null;
    }

    // Metaheuristic
    public Solution solveM(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String mhType, final int maxIter) {
        return null;
    }

    // Metaheuristic, Random
    public Solution solveMLWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String randomizationLWR, final String mhType, final int maxIter) {
        return null;
    }

    // Metaheuristic, WSM, LWR,
    public Solution solveMWSMLWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, Evaluator evaluator, Duration dur, int threadNumber, String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate, String mcdmObjective, String randomizationMethod, String mhType, int maxIter) {
        return null;
    }

    // Metaheuristic, WPM, LWR,
    public Solution solveMWPMLWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, Evaluator evaluator, Duration dur, int threadNumber, double wAVB, double wTT, double wLength, double wUtil, int rate, String mcdmObjective, String randomizationLWR, String mhType, int maxIter, final String wpmVersion, final String wpmValueType) {
        return null;
    }

    // Metaheuristic, WSM, CWR
    public Solution solveMWSMCWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String wsmNormalization, final String mcdmObjective, final String randomizationCWR, final int rate, final String mhType, final int maxIter) {
        return null;
    }

    // Metaheuristic, WPM, CWR
    public Solution solveMWPMCWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String mcdmObjective, final String randomizationCWR, final int rate, final String mhType, final int maxIter, final String wpmVersion, final String wpmValueType) {
        return null;
    }

    // Metaheuristic, WSM, LWR, CWR
    public Solution solveMWSMLWRCWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String wsmNormalization, final String mcdmObjective, final String randomizationLWR, final int rate, final String mhType, final int maxIter, final String randomizationCWR) {
        return null;
    }

    // Metaheuristic, MTR
    public Solution solveMM(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, Evaluator evaluator, Duration dur, int threadNumber, String differentDursComputationMethod, String baStandard, boolean overload) {
        return null;
    }

    // Heuristic, U
    public Solution solveHU(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final int rate) {
        return null;
    }

    // Heuristic
    public Solution solveH(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator) {
        return null;
    }

    public List<Unicast> getSolution() {
        return null;
    }

    public List<UnicastCandidates> getAVBUnicastCandidates() {
        return null;
    }

    public Map<Double, Double> getDurationMap() {
        return null;
    }

    public List<Unicast> getTTUnicasts() {
        return null;
    }
}
