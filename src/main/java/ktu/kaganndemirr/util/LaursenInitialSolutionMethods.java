package ktu.kaganndemirr.util;

import ktu.kaganndemirr.evaluator.Evaluator;
import ktu.kaganndemirr.evaluator.AVBLatencyMathCost;
import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;

import java.util.*;

import static ktu.kaganndemirr.util.UnicastCandidatesSortingMethods.assignDSTToUC;

public class LaursenInitialSolutionMethods {
    public static List<Unicast> constructInitialSolution(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int k, Evaluator evaluator) {

        List<Unicast> initialSolution = new ArrayList<>(ttUnicasts);

        List<UnicastCandidates> shuffledAvbList = new ArrayList<>(avbUnicastCandidates);
        Collections.shuffle(shuffledAvbList);

        for (UnicastCandidates uc : shuffledAvbList) {
            Cost currBestCost = new AVBLatencyMathCost();
            Unicast currUnicast;
            Unicast currBestUnicast = null;
            for (int u = 0; u < k; u++) {
                currUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), uc.getCandidates().get(u));
                initialSolution.add(currUnicast);
                Cost cost = evaluator.evaluate(initialSolution);
                if (cost.getTotalCost() < currBestCost.getTotalCost()) {
                    currBestCost = cost;
                    currBestUnicast = currUnicast;
                }
                //Remove it again
                initialSolution.remove(currUnicast);

            }
            initialSolution.add(currBestUnicast);
        }
        return initialSolution;
    }

    public static List<Unicast> constructInitialSolutionD(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int k, Evaluator evaluator) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = new LinkedHashMap<>();
        ucDeadlineMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedUCByDeadline.put(x.getKey(), x.getValue()));

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            Cost currBestCost = new AVBLatencyMathCost();
            Unicast currUnicast;
            Unicast currBestUnicast = null;
            for (int u = 0; u < k; u++) {
                currUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), uc.getCandidates().get(u));
                partialSolution.add(currUnicast);
                Cost cost = evaluator.evaluate(partialSolution);
                if (cost.getTotalCost() < currBestCost.getTotalCost()) {
                    currBestCost = cost;
                    currBestUnicast = currUnicast;
                }
                //Remove it again
                partialSolution.remove(currUnicast);

            }
            partialSolution.add(currBestUnicast);
        }
        return partialSolution;
    }

    public static List<Unicast> constructInitialSolutionDST(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int k, Evaluator evaluator) {
        Map<UnicastCandidates, Double> ucDSTMap = assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = new LinkedHashMap<>();
        ucDSTMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedUCByDeadline.put(x.getKey(), x.getValue()));

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            Cost currBestCost = new AVBLatencyMathCost();
            Unicast currUnicast;
            Unicast currBestUnicast = null;
            for (int u = 0; u < k; u++) {
                currUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), uc.getCandidates().get(u));
                partialSolution.add(currUnicast);
                Cost cost = evaluator.evaluate(partialSolution);
                if (cost.getTotalCost() < currBestCost.getTotalCost()) {
                    currBestCost = cost;
                    currBestUnicast = currUnicast;
                }
                //Remove it again
                partialSolution.remove(currUnicast);

            }
            partialSolution.add(currBestUnicast);
        }
        return partialSolution;
    }
}
