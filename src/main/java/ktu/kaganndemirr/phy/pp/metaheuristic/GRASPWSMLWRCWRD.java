package ktu.kaganndemirr.phy.pp.metaheuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.phy.pp.PPRandomizedGraphPaths;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.solver.Solution;
import ktu.kaganndemirr.solver.Solver;
import ktu.kaganndemirr.util.MCDMSpecificMethods;
import ktu.kaganndemirr.util.Constants;
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

public class GRASPWSMLWRCWRD extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(GRASPWSMLWRCWRD.class.getSimpleName());

    private final Object costLock;

    private Cost globalBestCost;
    private List<Unicast> bestSolution;

    private Evaluator evaluator;

    private final int k;

    private static final int PROGRESS_PERIOD = 10000;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    public GRASPWSMLWRCWRD(int k) {
        this.k = k;
        costLock = new Object();
    }

    private Map<Double, Double> durationMap;

    private Graph<Node, GCLEdge> physicalTopology;

    private List<Application> applications;

    @Override
    public Solution solveMWSMLWRCWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String wsmNormalization, final String mcdmObjective, final String randomizationLWR, final int rate, final String mhType, final int maxIter, final String randomizationCWR) {
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();

        this.evaluator = evaluator;

        this.physicalTopology = physicalTopology;
        this.applications = applications;

        durationMap = new HashMap<>();

        PPRandomizedGraphPaths ppRandomizedGraphPaths = new PPRandomizedGraphPaths(physicalTopology, applications);
        ttUnicasts = ppRandomizedGraphPaths.getTTUnicasts();

        if (Objects.equals(mhType, Constants.DURATION)){
            try (ExecutorService exec = Executors.newFixedThreadPool(threadNumber)) {
                logger.info("Solving problem using {} threads", threadNumber);

                Timer timer = MetaheuristicMethods.getTimer(PROGRESS_PERIOD, dur, globalBestCost);
                for (int i = 0; i < threadNumber; i++) {
                    exec.execute(new GRASPWSMLWRCWRDRunnable(mcdmObjective, wsmNormalization, rate, randomizationLWR, randomizationCWR));
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
        } else if (Objects.equals(mhType, Constants.ITERATIVE)) {
            MetaheuristicMethods.iterativeWSMLWRCWRGRASP(physicalTopology, applications, k, ttUnicasts, mcdmObjective, wsmNormalization, rate, randomizationLWR, randomizationCWR, maxIter, evaluator, globalBestCost, durationMap, bestSolution);
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

    private class GRASPWSMLWRCWRDRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        String mcdmObjective;
        String wsmNormalization;
        String randomizationLWR;
        String randomizationCWR;
        int rate;

        public GRASPWSMLWRCWRDRunnable(String mcdmObjective, String wsmNormalization, int rate, String randomizationLWR, String randomizationCWR) {
            this.mcdmObjective = mcdmObjective;
            this.wsmNormalization = wsmNormalization;
            this.randomizationLWR = randomizationLWR;
            this.randomizationCWR = randomizationCWR;
            this.rate = rate;
        }

        @Override
        public void run() {
            //Just run until interrupted.
            while (!Thread.currentThread().isInterrupted()) {
                i++;
                PPRandomizedGraphPaths ppRandomizedGraphPaths = new PPRandomizedGraphPaths(physicalTopology, applications, k, randomizationLWR);

                avbUnicastCandidates = ppRandomizedGraphPaths.getAVBUnicastCandidates();
                //STEP 1 : Construct Greedy Randomized Solution
                List<Unicast> solution = null;
                if (Objects.equals(mcdmObjective, Constants.AVBTT)) {
                    //TODO: Implement this
                } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTH)) {
                    if (Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTRANDOM)) {
                        solution = MCDMSpecificMethods.WSMCWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wsmNormalization);
                    } else if (Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTSECURERANDOM)) {
                        solution = MCDMSpecificMethods.WSMCWRDAVBTTLengthSecureRandom(avbUnicastCandidates, ttUnicasts, wsmNormalization);
                    }

                } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTHUTIL)) {
                    if (Objects.equals(randomizationCWR, Constants.RANDOMIZEWEIGHTRANDOM)) {
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
                            durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3));
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
