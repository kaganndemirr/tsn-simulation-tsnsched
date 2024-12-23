package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.phy.pp.PPRandomizedGraphPaths;
import ktu.kaganndemirr.route.Route;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MetaheuristicMethods {
    private static final Logger logger = LoggerFactory.getLogger(MetaheuristicMethods.class.getSimpleName());

    public static Timer getTimer(int PROGRESS_PERIOD, Duration dur, Cost globalBestCost) {
        Timer timer = new Timer();
        TimerTask progressUpdater = new TimerTask() {
            private int i = 0;
            private final DecimalFormat numberFormat = new DecimalFormat(".00");

            @Override
            public void run() {
                //Report progress every 10sec

                float searchProgress = (++i * (float) PROGRESS_PERIOD) / dur.toMillis();
                logger.info("Searching {}% : CurrentBest {}", numberFormat.format(searchProgress * 100), globalBestCost);
            }
        };
        timer.schedule(progressUpdater, PROGRESS_PERIOD, PROGRESS_PERIOD);
        return timer;
    }

    public static void GRASP(List<Unicast> solution, Evaluator evaluator, List<UnicastCandidates> avbUnicastCandidates, Cost globalBestCost) {
        Cost cost = evaluator.evaluate(solution);
        Cost bestCost = cost;

        Map<Route, Route> mapping = new HashMap<>(solution.size());
        for (UnicastCandidates uc : avbUnicastCandidates) {
            mapping.put(uc, uc);
        }
        for (int sample = 0; sample < solution.size() / 2; sample++) {
            int index = ThreadLocalRandom.current().nextInt(solution.size());

            Unicast old = solution.get(index);
            Route uc = mapping.get(old);
            if (uc instanceof UnicastCandidates candidate) {
                for (int j = 0; j < candidate.getCandidates().size(); j++) {
                    Unicast temp = new Unicast(old.getApplication(), old.getDestNode(), candidate.getCandidates().get(j));
                    solution.set(index, temp);
                    cost = evaluator.evaluate(solution);
                    if (cost.getTotalCost() < bestCost.getTotalCost()) {
                        bestCost = cost;
                        sample--;
                        if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                            sample -= solution.size() / 2;
                        }
                        break;
                    } else {
                        //Go back again
                        solution.set(index, old);
                    }
                }

            }
        }
    }

    public static List<Unicast> ALO(List<Unicast> antSolution, List<Unicast> antLionSolution, List<UnicastCandidates> avbUnicastCandidates, int k, Evaluator evaluator) {
        List<Unicast> eliteSolution = new ArrayList<>(antLionSolution);
        Cost evaledElite = evaluator.evaluate(antLionSolution);

        for (UnicastCandidates uc : avbUnicastCandidates) {
            int randomAntLionIndex = ThreadLocalRandom.current().nextInt(uc.getCandidates().size());
            int antIndex = createRandomIndexWithExcludedNumber(k, randomAntLionIndex);
            GraphPath<Node, GCLEdge> antPath = uc.getCandidates().get(antIndex);

            for (Unicast ant: antSolution){
                if(Objects.equals(uc.getApplication().getName(), ant.getApplication().getName())){
                    ant.setRoute(antPath);
                }
            }
        }

        if (evaluator.evaluate(antSolution).getTotalCost() < evaluator.evaluate(antLionSolution).getTotalCost()) {
            antLionSolution = antSolution;
        }

        if (evaluator.evaluate(antLionSolution).getTotalCost() < evaledElite.getTotalCost()) {
            eliteSolution = antLionSolution;
        }

        return eliteSolution;
    }

    private static int createRandomIndexWithExcludedNumber(int k, int excludedNumber) {
        int index;
        do {
            index = ThreadLocalRandom.current().nextInt(k);
        } while (index == excludedNumber);

        return index;
    }

    public static void iterWSMLWRGRASP(Graph<Node, GCLEdge> physicalTopology,
                                         List<Application> applications,
                                         int k,
                                         List<Unicast> ttUnicasts,
                                         String mcdmObjective,
                                         String wsmNormalization,
                                         double wAVB,
                                         double wTT,
                                         double wLength,
                                         double wUtil,
                                         int rate,
                                         String randomizationLWR,
                                         int maxIter,
                                         Evaluator evaluator,
                                         Cost globalBestCost,
                                         Map<Double, Double> durationMap,
                                         List<Unicast> bestSolution){

        Instant solutionStartTime = Instant.now();
        for(int i = 0; i < maxIter; i++){
            PPRandomizedGraphPaths ppRandomizedGraphPaths = new PPRandomizedGraphPaths(physicalTopology, applications, k, randomizationLWR);

            List<UnicastCandidates> avbUnicastCandidates = ppRandomizedGraphPaths.getAVBUnicastCandidates();
            List<Unicast> solution = null;
            if (Objects.equals(mcdmObjective, Constants.AVBTT)){
                solution = MCDMSpecificMethods.WSMLWRDAVBTT(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT);
            } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTH)) {
                solution = MCDMSpecificMethods.WSMLWRDAVBTTLength(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength);
            } else if (Objects.equals(mcdmObjective, Constants.AVBTTLENGTHUTIL)) {
                solution = MCDMSpecificMethods.WSMLWRDAVBTTLengthUtil(avbUnicastCandidates, ttUnicasts, wsmNormalization, wAVB, wTT, wLength, wUtil, rate);
            }

            MetaheuristicMethods.GRASP(solution, evaluator, avbUnicastCandidates, globalBestCost);

            Cost cost = evaluator.evaluate(solution);
            if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                globalBestCost = cost;
                Instant solutionEndTime = Instant.now();
                durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3));
                bestSolution.clear();
                assert solution != null;
                bestSolution.addAll(solution);
            }

            if(i % 100 == 0){
                logger.info("Iteration {}, CurrentBest {}", i, globalBestCost);
            }
        }
    }

    public static void iterativeWSMLWRCWRGRASP(Graph<Node, GCLEdge> physicalTopology,
                                         List<Application> applications,
                                         int k,
                                         List<Unicast> ttUnicasts,
                                         String mcdmObjective,
                                         String wsmNormalization,
                                         int rate,
                                         String randomizationLWR,
                                         String randomizationCWR,
                                         int maxIter,
                                         Evaluator evaluator,
                                         Cost globalBestCost,
                                         Map<Double, Double> durationMap,
                                         List<Unicast> bestSolution){

        Instant solutionStartTime = Instant.now();
        for(int i = 0; i < maxIter; i++){
            PPRandomizedGraphPaths ppRandomizedGraphPaths = new PPRandomizedGraphPaths(physicalTopology, applications, k, randomizationLWR);

            List<UnicastCandidates> avbUnicastCandidates = ppRandomizedGraphPaths.getAVBUnicastCandidates();
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

            MetaheuristicMethods.GRASP(solution, evaluator, avbUnicastCandidates, globalBestCost);

            Cost cost = evaluator.evaluate(solution);
            if (cost.getTotalCost() < globalBestCost.getTotalCost()) {
                globalBestCost = cost;
                Instant solutionEndTime = Instant.now();
                durationMap.put(Double.parseDouble(globalBestCost.toString().split("\\s")[0]), (Duration.between(solutionStartTime, solutionEndTime).toMillis() / 1e3));
                bestSolution.clear();
                assert solution != null;
                bestSolution.addAll(solution);
            }

            if(i % 100 == 0){
                logger.info("Iteration {}, CurrentBest {}", i, globalBestCost);
            }
        }
    }
}
