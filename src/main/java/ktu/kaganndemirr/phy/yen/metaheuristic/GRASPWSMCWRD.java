package ktu.kaganndemirr.phy.yen.metaheuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.phy.yen.YenGraphPaths;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.util.MCDMSpecificMethods;
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


public class GRASPWSMCWRD extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(GRASPWSMCWRD.class.getSimpleName());

    private final Object costLock;

    private Cost globalBestCost;
    private List<Unicast> bestSolution;

    private Evaluator evaluator;

    private final int k;

    private static final int PROGRESS_PERIOD = 10000;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    public GRASPWSMCWRD(int k) {
        this.k = k;
        costLock = new Object();
    }

    private double graphPathsTotalTime;

    private Map<Double, Double> durationMap;

    @Override
    public Solution solveMWSMCWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String wsmNormalization, final String mcdmObjective, final String randomizationCWR, final int rate, final String mhType, final int maxIter) {
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();

        this.evaluator = evaluator;

        Instant graphPathsStartTime = Instant.now();
        YenGraphPaths gp = new YenGraphPaths(physicalTopology, applications, k);
        Instant graphPathsEndTime = Instant.now();
        long graphPathsDuration = Duration.between(graphPathsStartTime, graphPathsEndTime).toMillis();
        graphPathsTotalTime = graphPathsDuration / 1e3;

        avbUnicastCandidates = gp.getAVBRoutingCandidates();
        ttUnicasts = gp.getTTRoutes();

        durationMap = new HashMap<>();

        try (ExecutorService exec = Executors.newFixedThreadPool(threadNumber)) {
            logger.info("Solving problem using {} threads", threadNumber);

            //For monitoring time and reporting of progress (as this may take some time)
            Timer timer = MetaheuristicMethods.getTimer(PROGRESS_PERIOD, dur, globalBestCost);

            for (int i = 0; i < threadNumber; i++) {
                exec.execute(new GRASPWSMCWRDRunnable(mcdmObjective, wsmNormalization, randomizationCWR, rate));
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

    private class GRASPWSMCWRDRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        String mcdmObjective;
        String wsmNormalization;
        String randomizationCWR;
        int rate;

        public GRASPWSMCWRDRunnable(String mcdmObjective, String wsmNormalization, String randomizationCWR, int rate) {
            this.mcdmObjective = mcdmObjective;
            this.wsmNormalization = wsmNormalization;
            this.randomizationCWR = randomizationCWR;
            this.rate = rate;
        }

        @Override
        public void run() {
            //Just run until interrupted.
            while (!Thread.currentThread().isInterrupted()) {
                i++;
                //STEP 1 : Construct Greedy Randomized Solution
                List<Unicast> solution = null;
                if (Objects.equals(mcdmObjective, Constants.AVBTT)){
                    //TODO: Implement this
                } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTH)) {
                    if(Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTRANDOM)){
                        solution = MCDMSpecificMethods.WSMCWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wsmNormalization);
                    } else if (Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTSECURERANDOM)) {
                        solution = MCDMSpecificMethods.WSMCWRDAVBTTLengthSecureRandom(avbUnicastCandidates, ttUnicasts, wsmNormalization);
                    }

                } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTHUTIL)) {
                    if(Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTRANDOM)){
                        solution = MCDMSpecificMethods.WSMCWRDAVBTTLengthUtil(avbUnicastCandidates, ttUnicasts, wsmNormalization, rate);
                    } else if (Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTSECURERANDOM)) {
                        solution = MCDMSpecificMethods.WSMCWRDAVBTTLengthUtilSecureRandom(avbUnicastCandidates, ttUnicasts, wsmNormalization, rate);
                    }
                }

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
                            assert solution != null;
                            bestSolution.addAll(solution);
                        }
                    }
                }
            }
            logger.info(" {} finished in {} iterations", Thread.currentThread().getName(), i);
        }
    }
}
