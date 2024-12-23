package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.GraphPath;

import java.util.*;
import java.util.stream.Collectors;

import static ktu.kaganndemirr.util.UnicastCandidatesSortingMethods.assignDSTToUC;

public class USpecificMethods {
    @SafeVarargs
    private static Set<GCLEdge> findDuplicateKeys(Map<GCLEdge, ?>... maps) {
        Set<GCLEdge> keys = new HashSet<>();
        return Arrays.stream(maps)
                .flatMap(map -> map.keySet().stream())
                .filter(key -> !keys.add(key))
                .collect(Collectors.toSet());
    }

    public static List<Unicast> constructSolution(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int rate) {
        //Add all TT-Routes
        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);


        Map<GCLEdge, Double> utilizationMap = new HashMap<>();
        Map<GCLEdge, Double> utilizationMapCopy;
        Map<GCLEdge, Double> candidateUtilizationMap;

        Map<GCLEdge, Double> bestUtilizationMap = new HashMap<>();

        if (!ttUnicasts.isEmpty()) {

            for (Unicast u : ttUnicasts) {
                for (GCLEdge edge : u.getRoute().getEdgeList()) {
                    if (!utilizationMap.containsKey(edge)) {
                        utilizationMap.put(edge, u.getApplication().getMessageSizeMbps() / rate);
                    } else {
                        utilizationMap.put(edge, utilizationMap.get(edge) + u.getApplication().getMessageSizeMbps() / rate);
                    }
                }
            }
        }

        Unicast bestUnicast = null;
        for (UnicastCandidates uc : avbUnicastCandidates) {
            if (partialSolution.isEmpty()) {
                partialSolution.add(new Unicast(uc.getApplication(), uc.getDestNode(), uc.getCandidates().getFirst()));
                for (GCLEdge edge : uc.getCandidates().getFirst().getEdgeList()) {
                    if(!utilizationMap.containsKey(edge)){
                        utilizationMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }
                    else{
                        utilizationMap.put(edge, utilizationMap.get(edge) + (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }
                }
            } else {
                double minUtilization = Double.MAX_VALUE;
                for (GraphPath<Node, GCLEdge> gp : uc.getCandidates()) {
                    candidateUtilizationMap = new HashMap<>();
                    for (GCLEdge edge : gp.getEdgeList()) {
                        candidateUtilizationMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }

                    double maxUmax = 0;
                    Set<GCLEdge> sameEdgeSet = findDuplicateKeys(utilizationMap, candidateUtilizationMap);
                    if (!sameEdgeSet.isEmpty()) {
                        ArrayList<Double> sameEdgeUtil = new ArrayList<>();
                        for (GCLEdge edge : sameEdgeSet) {
                            sameEdgeUtil.add(candidateUtilizationMap.get(edge) + utilizationMap.get(edge));
                        }
                        maxUmax = Collections.max(sameEdgeUtil);
                    }
                    if (maxUmax == 0) {
                        utilizationMapCopy = new HashMap<>(utilizationMap);
                        utilizationMapCopy.putAll(candidateUtilizationMap);
                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), gp);
                        break;
                    }

                    if (maxUmax < minUtilization) {
                        utilizationMapCopy = new HashMap<>(utilizationMap);
                        for (Map.Entry<GCLEdge, Double> entry : candidateUtilizationMap.entrySet()) {
                            if (utilizationMapCopy.containsKey(entry.getKey())) {
                                utilizationMapCopy.put(entry.getKey(), utilizationMapCopy.get(entry.getKey()) + candidateUtilizationMap.get(entry.getKey()));
                            } else {
                                utilizationMapCopy.put(entry.getKey(), candidateUtilizationMap.get(entry.getKey()));
                            }
                        }
                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), gp);
                        minUtilization = maxUmax;
                    }
                }
                partialSolution.add(bestUnicast);
                utilizationMap = bestUtilizationMap;
            }
        }
        return partialSolution;
    }

    public static List<Unicast> constructSolutionD(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int rate) {
        //Add all TT-Routes
        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);


        Map<GCLEdge, Double> utilizationMap = new HashMap<>();
        Map<GCLEdge, Double> utilizationMapCopy;
        Map<GCLEdge, Double> candidateUtilizationMap;

        Map<GCLEdge, Double> bestUtilizationMap = new HashMap<>();

        if (!ttUnicasts.isEmpty()) {

            for (Unicast u : ttUnicasts) {
                for (GCLEdge edge : u.getRoute().getEdgeList()) {
                    if (!utilizationMap.containsKey(edge)) {
                        utilizationMap.put(edge, u.getApplication().getMessageSizeMbps() / rate);
                    } else {
                        utilizationMap.put(edge, utilizationMap.get(edge) + u.getApplication().getMessageSizeMbps() / rate);
                    }
                }
            }
        }

        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = new LinkedHashMap<>();
        ucDeadlineMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedUCByDeadline.put(x.getKey(), x.getValue()));

        Unicast bestUnicast = null;
        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            if (partialSolution.isEmpty()) {
                partialSolution.add(new Unicast(uc.getApplication(), uc.getDestNode(), uc.getCandidates().getFirst()));
                for (GCLEdge edge : uc.getCandidates().getFirst().getEdgeList()) {
                    if(!utilizationMap.containsKey(edge)){
                        utilizationMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }
                    else{
                        utilizationMap.put(edge, utilizationMap.get(edge) + (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }
                }
            } else {
                double minUtilization = Double.MAX_VALUE;
                for (GraphPath<Node, GCLEdge> gp : uc.getCandidates()) {
                    candidateUtilizationMap = new HashMap<>();
                    for (GCLEdge edge : gp.getEdgeList()) {
                        candidateUtilizationMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }

                    double maxUmax = 0;
                    Set<GCLEdge> sameEdgeSet = findDuplicateKeys(utilizationMap, candidateUtilizationMap);
                    if (!sameEdgeSet.isEmpty()) {
                        ArrayList<Double> sameEdgeUtil = new ArrayList<>();
                        for (GCLEdge edge : sameEdgeSet) {
                            sameEdgeUtil.add(candidateUtilizationMap.get(edge) + utilizationMap.get(edge));
                        }
                        maxUmax = Collections.max(sameEdgeUtil);
                    }
                    if (maxUmax == 0) {
                        utilizationMapCopy = new HashMap<>(utilizationMap);
                        utilizationMapCopy.putAll(candidateUtilizationMap);
                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), gp);
                        break;
                    }

                    if (maxUmax < minUtilization) {
                        utilizationMapCopy = new HashMap<>(utilizationMap);
                        for (Map.Entry<GCLEdge, Double> entry : candidateUtilizationMap.entrySet()) {
                            if (utilizationMapCopy.containsKey(entry.getKey())) {
                                utilizationMapCopy.put(entry.getKey(), utilizationMapCopy.get(entry.getKey()) + candidateUtilizationMap.get(entry.getKey()));
                            } else {
                                utilizationMapCopy.put(entry.getKey(), candidateUtilizationMap.get(entry.getKey()));
                            }
                        }
                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), gp);
                        minUtilization = maxUmax;
                    }
                }
                partialSolution.add(bestUnicast);
                utilizationMap = bestUtilizationMap;
            }
        }
        return partialSolution;
    }

    public static List<Unicast> constructSolutionDST(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int rate) {
        //Add all TT-Routes
        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);


        Map<GCLEdge, Double> utilizationMap = new HashMap<>();
        Map<GCLEdge, Double> utilizationMapCopy;
        Map<GCLEdge, Double> candidateUtilizationMap;

        Map<GCLEdge, Double> bestUtilizationMap = new HashMap<>();

        if (!ttUnicasts.isEmpty()) {

            for (Unicast u : ttUnicasts) {
                for (GCLEdge edge : u.getRoute().getEdgeList()) {
                    if (!utilizationMap.containsKey(edge)) {
                        utilizationMap.put(edge, u.getApplication().getMessageSizeMbps() / rate);
                    } else {
                        utilizationMap.put(edge, utilizationMap.get(edge) + u.getApplication().getMessageSizeMbps() / rate);
                    }
                }
            }
        }

        Map<UnicastCandidates, Double> ucDSTMap = assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = new LinkedHashMap<>();
        ucDSTMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedUCByDeadline.put(x.getKey(), x.getValue()));

        Unicast bestUnicast = null;
        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            if (partialSolution.isEmpty()) {
                partialSolution.add(new Unicast(uc.getApplication(), uc.getDestNode(), uc.getCandidates().getFirst()));
                for (GCLEdge edge : uc.getCandidates().getFirst().getEdgeList()) {
                    if(!utilizationMap.containsKey(edge)){
                        utilizationMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }
                    else{
                        utilizationMap.put(edge, utilizationMap.get(edge) + (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }
                }
            } else {
                double minUtilization = Double.MAX_VALUE;
                for (GraphPath<Node, GCLEdge> gp : uc.getCandidates()) {
                    candidateUtilizationMap = new HashMap<>();
                    for (GCLEdge edge : gp.getEdgeList()) {
                        candidateUtilizationMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                    }

                    double maxUmax = 0;
                    Set<GCLEdge> sameEdgeSet = findDuplicateKeys(utilizationMap, candidateUtilizationMap);
                    if (!sameEdgeSet.isEmpty()) {
                        ArrayList<Double> sameEdgeUtil = new ArrayList<>();
                        for (GCLEdge edge : sameEdgeSet) {
                            sameEdgeUtil.add(candidateUtilizationMap.get(edge) + utilizationMap.get(edge));
                        }
                        maxUmax = Collections.max(sameEdgeUtil);
                    }
                    if (maxUmax == 0) {
                        utilizationMapCopy = new HashMap<>(utilizationMap);
                        utilizationMapCopy.putAll(candidateUtilizationMap);
                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), gp);
                        break;
                    }

                    if (maxUmax < minUtilization) {
                        utilizationMapCopy = new HashMap<>(utilizationMap);
                        for (Map.Entry<GCLEdge, Double> entry : candidateUtilizationMap.entrySet()) {
                            if (utilizationMapCopy.containsKey(entry.getKey())) {
                                utilizationMapCopy.put(entry.getKey(), utilizationMapCopy.get(entry.getKey()) + candidateUtilizationMap.get(entry.getKey()));
                            } else {
                                utilizationMapCopy.put(entry.getKey(), candidateUtilizationMap.get(entry.getKey()));
                            }
                        }
                        bestUtilizationMap = utilizationMapCopy;
                        bestUnicast = new Unicast(uc.getApplication(), uc.getDestNode(), gp);
                        minUtilization = maxUmax;
                    }
                }
                partialSolution.add(bestUnicast);
                utilizationMap = bestUtilizationMap;
            }
        }
        return partialSolution;
    }
}
