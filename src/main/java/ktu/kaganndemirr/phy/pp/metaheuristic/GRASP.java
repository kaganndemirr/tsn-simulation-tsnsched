package ktu.kaganndemirr.phy.pp.metaheuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.phy.pp.PPGraphPaths;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.LaursenInitialSolutionMethods;
import ktu.kaganndemirr.util.MetaheuristicMethods;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GRASP extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(GRASP.class.getSimpleName());

    private final Object costLock;

    private Cost globalBestCost;
    private List<Unicast> bestSolution;

    private Evaluator evaluator;

    private final int k;

    private static final int PROGRESS_PERIOD = 10000;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    public GRASP(int k) {
        this.k = k;
        costLock = new Object();
    }

    private double graphPathsTotalTime;
    private Map<Double, Double> durationMap;

    @Override
    public Solution solveM(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String mhType, final int maxIter) {
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();

        this.evaluator = evaluator;

        Instant graphPathsStartTime = Instant.now();
        PPGraphPaths gp = new PPGraphPaths(physicalTopology, applications, k);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();
        graphPathsTotalTime = graphPathsDuration / 1e3;

        avbUnicastCandidates = gp.getAVBRoutingCandidates();
        ttUnicasts = gp.getTTRoutes();

        durationMap = new HashMap<>();

        try (ExecutorService exec = Executors.newFixedThreadPool(threadNumber)) {
            logger.info("Solving problem using {} threads", threadNumber);

            Timer timer = MetaheuristicMethods.getTimer(PROGRESS_PERIOD, dur, globalBestCost);

            for (int i = 0; i < threadNumber; i++) {
                exec.execute(new GRASPRunnable());
            }
            exec.awaitTermination(dur.toSeconds(), TimeUnit.SECONDS);
            exec.shutdown();
            if (!exec.isTerminated()) {
                exec.shutdownNow();
            }
            timer.cancel();
        } catch (InterruptedException e) {
            logger.info("Executor interrupted");
        }
        return new Solution(globalBestCost, Multicast.generateMulticasts(bestSolution));
    }

    @Override
    public List<Unicast> getSolution() {
        return bestSolution;
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

    private class GRASPRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        @Override
        public void run() {
            //Just run until interrupted.
            while (!Thread.currentThread().isInterrupted()) {
                i++;
                //STEP 1 : Construct Greedy Randomized Solution
                List<Unicast> solution = LaursenInitialSolutionMethods.constructInitialSolution(avbUnicastCandidates, ttUnicasts, k, evaluator);
                //STEP 2 : Perform a local search, to improve the result.
                MetaheuristicMethods.GRASP(solution, evaluator, avbUnicastCandidates, globalBestCost);

                //Evaluate and see if better than anything we have seen before
                Cost cost = evaluator.evaluate(solution);
                //pre-check before entering critical-section
                if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                    synchronized (costLock) {
                        if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                            globalBestCost = cost;
                            Instant solutionEndTime = Instant.now();
                            durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3) + graphPathsTotalTime);
                            bestSolution.clear();
                            bestSolution.addAll(solution);
                        }
                    }
                }
            }
            logger.info(" {} finished in {} iterations", Thread.currentThread().getName(), i);
        }
    }
}
