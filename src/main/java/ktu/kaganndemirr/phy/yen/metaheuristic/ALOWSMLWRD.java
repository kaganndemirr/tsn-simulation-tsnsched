package ktu.kaganndemirr.phy.yen.metaheuristic;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.phy.yen.YenRandomizedGraphPaths;
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


public class ALOWSMLWRD extends Solver {
    private static final Logger logger = LoggerFactory.getLogger(ALOWSMLWRD.class.getSimpleName());

    private final Object costLock;

    private Cost globalBestCost;
    private List<Unicast> bestSolution;

    private Evaluator evaluator;

    private final int k;

    private static final int PROGRESS_PERIOD = 10000;

    private List<UnicastCandidates> avbUnicastCandidates;
    private List<Unicast> ttUnicasts;

    public ALOWSMLWRD(int k) {
        this.k = k;
        costLock = new Object();
    }

    private Map<Double, Double> durationMap;

    private Graph<Node, GCLEdge> physicalTopology;

    private List<Application> applications;

    @Override
    public Solution solveMWSMLWR(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final Evaluator evaluator, final Duration dur, final int threadNumber, final String wsmNormalization, final double wAVB, final double wTT, final double wLength, final double wUtil, final int rate, final String mcdmObjective, final String randomizationLWR, final String mhType, final int maxIter) {
        globalBestCost = new AVBLatencyMathCost();
        bestSolution = new ArrayList<>();

        this.evaluator = evaluator;

        this.physicalTopology = physicalTopology;
        this.applications = applications;

        YenRandomizedGraphPaths yenRandomizedGraphPaths = new YenRandomizedGraphPaths(physicalTopology, applications);

        ttUnicasts = yenRandomizedGraphPaths.getTTUnicasts();

        durationMap = new HashMap<>();

        try (ExecutorService exec = Executors.newFixedThreadPool(threadNumber)) {
            logger.info("Solving problem using {} threads", threadNumber);

            Timer timer = MetaheuristicMethods.getTimer(PROGRESS_PERIOD, dur, globalBestCost);

            for (int i = 0; i < threadNumber; i++) {
                exec.execute(new ALOWSMLWRDRunnable(mcdmObjective, wsmNormalization, wAVB, wTT, wLength, wUtil, rate, randomizationLWR));
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

    private class ALOWSMLWRDRunnable implements Runnable {
        private int i = 0;
        Instant solutionStartTime = Instant.now();

        String mcdmObjective;
        String wsmNormalization;
        String randomizationLWR;
        double wAVB;
        double wTT;
        double wLength;
        double wUtil;
        int rate;

        public ALOWSMLWRDRunnable(String mcdmObjective, String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate, String randomizationLWR) {
            this.mcdmObjective = mcdmObjective;
            this.wsmNormalization = wsmNormalization;
            this.randomizationLWR = randomizationLWR;
            this.wAVB = wAVB;
            this.wTT = wTT;
            this.wLength = wLength;
            this.wUtil = wUtil;
            this.rate = rate;
        }

        @Override
        public void run() {
            //Just run until interrupted.
            while (!Thread.currentThread().isInterrupted()) {
                i++;
                YenRandomizedGraphPaths yenRandomizedGraphPaths = new YenRandomizedGraphPaths(physicalTopology, applications, k, randomizationLWR);

                avbUnicastCandidates = yenRandomizedGraphPaths.getAVBUnicastCandidates();
                //STEP 1 : Construct Greedy Randomized Solution
                List<Unicast> antSolution = null;
                List<Unicast> antLionSolution = null;
                if (Objects.equals(mcdmObjective, Constants.AVBTT)){
                    antSolution = MCDMSpecificMethods.WSMLWRDAVBTT(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT);
                    antLionSolution = MCDMSpecificMethods.WSMLWRDAVBTT(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT);
                } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTH)) {
                    antSolution = MCDMSpecificMethods.WSMLWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength);
                    antLionSolution = MCDMSpecificMethods.WSMLWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength);
                } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTHUTIL)) {
                    antSolution = MCDMSpecificMethods.WSMLWRDAVBTTLengthUtil(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength, wUtil, rate);
                    antLionSolution = MCDMSpecificMethods.WSMLWRDAVBTTLengthUtil(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength, wUtil, rate);
                }

                //STEP 2 : Perform a local search, to improve the result.
                List<Unicast> solution = MetaheuristicMethods.ALO(antSolution, antLionSolution, avbUnicastCandidates, k, evaluator);

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
                            bestSolution.addAll(solution);
                        }
                    }
                }
            }

            logger.info(" {} finished in {} iterations", Thread.currentThread().getName(), i);
        }
    }
}