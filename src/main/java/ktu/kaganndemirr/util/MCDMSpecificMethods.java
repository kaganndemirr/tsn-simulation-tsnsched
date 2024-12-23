package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCL;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.GraphPath;

import java.util.*;

public class MCDMSpecificMethods {
    public static Map<GCLEdge, Double> getEdgeDurationMap(List<Unicast> ttUnicasts) {
        Map<GCLEdge, Double> edgeDurationMap = new HashMap<>();
        for (Unicast uc : ttUnicasts) {
            for (GCLEdge edge : uc.getRoute().getEdgeList()) {
                for (GCL gcl : edge.getGCLs()) {
                    if (!edgeDurationMap.containsKey(edge)) {
                        edgeDurationMap.put(edge, (gcl.getDuration() / (uc.getApplication().getCMI() / gcl.getFrequency())));
                    } else {
                        edgeDurationMap.put(edge, edgeDurationMap.get(edge) + (gcl.getDuration() / (uc.getApplication().getCMI() / gcl.getFrequency())));
                    }
                }

            }
        }
        return edgeDurationMap;
    }

    public static int countSameElements(List<GCLEdge> l1, List<GCLEdge> l2) {
        int i = 0;
        for (GCLEdge edge : l1) {
            if (l2.contains(edge)) {
                i++;
            }
        }
        return i;
    }

    public static ArrayList<GCLEdge> getSameElements(List<GCLEdge> l1, List<GCLEdge> l2) {
        ArrayList<GCLEdge> sameEdges = new ArrayList<>();
        for (GCLEdge edge : l1) {
            if (l2.contains(edge)) {
                sameEdges.add(edge);
            }
        }
        return sameEdges;
    }

    public static Map<GCLEdge, Double> findCurrentUtilMap(List<Unicast> partialSolution, int rate) {
        Map<GCLEdge, Double> edgeUtilMap = new HashMap<>();
        for(Unicast u: partialSolution){
            for(GCLEdge edge: u.getRoute().getEdgeList()){
                if (!edgeUtilMap.containsKey(edge)) {
                    edgeUtilMap.put(edge, u.getApplication().getMessageSizeMbps() / rate);
                } else {
                    edgeUtilMap.put(edge, edgeUtilMap.get(edge) + u.getApplication().getMessageSizeMbps() / rate);
                }
            }
        }

        return edgeUtilMap;
    }

    public static double findMaxUtilEdge(Map<GCLEdge, Double> edgeUtilMap) {
        double maxUtil = 0;
        for(Map.Entry<GCLEdge, Double> entry: edgeUtilMap.entrySet()){
            if (entry.getValue() > maxUtil){
                maxUtil = entry.getValue();
            }
        }

        double finalMaxUtil = maxUtil;
        for(Map.Entry<GCLEdge, Double> entry: edgeUtilMap.entrySet()){
            if (entry.getValue() == maxUtil){
                finalMaxUtil += entry.getValue();
            }
        }

        return finalMaxUtil;
    }

    public static Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP(List<Unicast> partialSolution, UnicastCandidates uc, List<GraphPath<Node, GCLEdge>> gpList, int rate){
        Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> gpUtilMap = new HashMap<>();

        if (partialSolution.isEmpty()){
            for(GraphPath<Node, GCLEdge> gp: gpList){
                Map<GCLEdge, Double> edgeUtilMap = new HashMap<>();
                for(GCLEdge edge: gp.getEdgeList()){
                    edgeUtilMap.put(edge, (((SRTApplication) uc.getApplication()).getFrameSizeByte() * 8.0) * ((SRTApplication) uc.getApplication()).getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                }
                gpUtilMap.put(gp, edgeUtilMap);
            }

            return gpUtilMap;
        }

        Map<GCLEdge, Double> edgeUtilMap = MCDMSpecificMethods.findCurrentUtilMap(partialSolution, rate);

        for(GraphPath<Node, GCLEdge> gp: gpList){
            for(GCLEdge edge: gp.getEdgeList()){
                if(!edgeUtilMap.containsKey(edge)){
                    edgeUtilMap.put(edge, (uc.getApplication().getFrameSizeByte() * 8.0) * uc.getApplication().getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                }
                else {
                    edgeUtilMap.put(edge, edgeUtilMap.get(edge) + (uc.getApplication().getFrameSizeByte() * 8.0) * uc.getApplication().getNumberOfFrames() / uc.getApplication().getCMI() / rate);
                }
            }

            gpUtilMap.put(gp, edgeUtilMap);
        }

        return gpUtilMap;
    }

    private static Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil(Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP){
        Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil = new HashMap<>();
        for(Map.Entry<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> entry: edgeUtilMapWithGP.entrySet()){
            double max = findMaxUtilEdge(entry.getValue());
            gpMaxUtil.put(entry.getKey(), max);
        }

        return gpMaxUtil;
    }

    private static double getGPUtil(List<Map<GraphPath<Node, GCLEdge>, Double>> normalizedCostUtilList, GraphPath<Node, GCLEdge> gp){
        double util = 0;
        for(Map<GraphPath<Node, GCLEdge>, Double> gpUtilMap: normalizedCostUtilList){
            for(Map.Entry<GraphPath<Node, GCLEdge>, Double> gpUtil: gpUtilMap.entrySet()){
                if(gp == gpUtil.getKey()){
                    util = gpUtil.getValue();
                }
            }
        }
        return util;
    }

    //region <Sorted By Deadline>
    public static List<Unicast> WSMLWRDAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WPMLWRDAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, double wAVB, double wTT, String wpmVersion, String wpmValueType) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(costAVBList.get(i), wAVB) * Math.pow(costTTList.get(i), wTT);
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = MCDMSpecificMethods.getWPMv2AVBTTGraphPath(wAVB, wTT, costAVBList, costTTList, gpList, wpmValueType);
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMLWRDAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i) + wLength * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WPMLWRDAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, double wAVB, double wTT, double wLength, String wpmVersion, String wpmValueType) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(costAVBList.get(i), wAVB) * Math.pow(costTTList.get(i), wTT) * Math.pow(gpList.get(i).getEdgeList().size(), wLength);
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = MCDMSpecificMethods.getWPMv2AVBTTLengthGraphPath(wAVB, wTT, wLength, costAVBList, costTTList, gpList, wpmValueType);
            }


            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMLWRDAVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP = edgeUtilMapWithGP(partialSolution, uc, gpList, rate);
            Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil = gpMaxUtil(edgeUtilMapWithGP);

            ArrayList<Double> normalizedCostGPList;
            ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedCostUtilList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMax(gpMaxUtil);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostVector(gpMaxUtil);
                }
                case null, default -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMinMax(gpMaxUtil);
                }
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i) + wLength * normalizedCostGPList.get(i) + wUtil * getGPUtil(normalizedCostUtilList, gpList.get(i));
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMLWRDAVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, double wAVB, double wTT, double wLength, double wUtil, int rate, String wpmVersion, String wpmValueType) {
        return null;
    }
    //TODO
    public static List<Unicast> WSMCWRDAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        return null;
    }
    //TODO
    public static List<Unicast> WPMCWRDAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        return null;
    }

    public static List<Unicast> WSMCWRDAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.getLast() * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WPMCWRDAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wpmVersion, String wpmValueType) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLength();

            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(costAVBList.get(i), weightList.getFirst()) * Math.pow(costTTList.get(i), weightList.get(1)) * Math.pow(gpList.get(i).getEdgeList().size(), weightList.getLast());
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = MCDMSpecificMethods.getWPMv2AVBTTLengthGraphPath(weightList.getFirst(), weightList.get(1), weightList.getLast(), costAVBList, costTTList, gpList, wpmValueType);
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMCWRDAVBTTLengthSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsSecureRandomAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.getLast() * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WPMCWRDAVBTTLengthSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wpmVersion, String wpmValueType) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsSecureRandomAVBTTLength();

            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(costAVBList.get(i), weightList.getFirst()) * Math.pow(costTTList.get(i), weightList.get(1)) * Math.pow(gpList.get(i).getEdgeList().size(), weightList.getLast());
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = MCDMSpecificMethods.getWPMv2AVBTTLengthGraphPath(weightList.getFirst(), weightList.get(1), weightList.getLast(), costAVBList, costTTList, gpList, wpmValueType);
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMCWRDAVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, int rate) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP = edgeUtilMapWithGP(partialSolution, uc, gpList, rate);
            Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil = gpMaxUtil(edgeUtilMapWithGP);

            ArrayList<Double> normalizedCostGPList;
            ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedCostUtilList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMax(gpMaxUtil);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostVector(gpMaxUtil);
                }
                case null, default -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMinMax(gpMaxUtil);
                }
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLengthUtil();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.get(2) * normalizedCostGPList.get(i) + weightList.getLast() * getGPUtil(normalizedCostUtilList, gpList.get(i));
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMCWRDAVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int rate) {
        return null;
    }

    public static List<Unicast> WSMCWRDAVBTTLengthUtilSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, int rate) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDeadline(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP = edgeUtilMapWithGP(partialSolution, uc, gpList, rate);
            Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil = gpMaxUtil(edgeUtilMapWithGP);

            ArrayList<Double> normalizedCostGPList;
            ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedCostUtilList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMax(gpMaxUtil);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostVector(gpMaxUtil);
                }
                case null, default -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMinMax(gpMaxUtil);
                }
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsSecureRandomAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.get(2) * normalizedCostGPList.get(i) + weightList.getLast() * getGPUtil(normalizedCostUtilList, gpList.get(i));
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMCWRDAVBTTLengthUtilSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, int rate) {
        return null;
    }
    //endregion

    //region <Sorted By DST>
    public static List<Unicast> WSMLWRDSTAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDST(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMLWRDSTAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, double wAVB, double wTT) {
        return null;
    }

    public static List<Unicast> WSMLWRDSTAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDST(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i) + wLength * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WPMLWRDSTVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, double wAVB, double wTT, double wLength, String wpmVersion, String wpmValueType) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDST(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                for (int i = 0; i < gpList.size(); i++) {
                    double cost = Math.pow(costAVBList.get(i), wAVB) * Math.pow(costTTList.get(i), wTT) * Math.pow(gpList.get(i).getEdgeList().size(), wLength);
                    if (cost < maxCost) {
                        maxCost = cost;
                        selectedGP = gpList.get(i);
                        if (maxCost == 0) {
                            break;
                        }
                    }
                }
            }
            else {
                selectedGP = MCDMSpecificMethods.getWPMv2AVBTTLengthGraphPath(wAVB, wTT, wLength, costAVBList, costTTList, gpList, wpmValueType);
            }


            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMLWRDSTAVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDST(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP = edgeUtilMapWithGP(partialSolution, uc, gpList, rate);
            Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil = gpMaxUtil(edgeUtilMapWithGP);

            ArrayList<Double> normalizedCostGPList;
            ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedCostUtilList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMax(gpMaxUtil);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostVector(gpMaxUtil);
                }
                case null, default -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMinMax(gpMaxUtil);
                }
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i) + wLength * normalizedCostGPList.get(i) + wUtil * getGPUtil(normalizedCostUtilList, gpList.get(i));
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMLWRDSTAVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate) {
        return null;
    }
    //TODO
    public static List<Unicast> WSMCWRDSTAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        return null;
    }
    //TODO
    public static List<Unicast> WPMCWRDSTAVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        return null;
    }

    public static List<Unicast> WSMCWRDSTAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDST(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.getLast() * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMCWRDSTAVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        return null;
    }

    public static List<Unicast> WSMCWRDSTAVBTTLengthSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        Map<UnicastCandidates, Double> ucDeadlineMap = UnicastCandidatesSortingMethods.assignDSTToUC(avbUnicastCandidates);

        LinkedHashMap<UnicastCandidates, Double> sortedUCByDeadline = UnicastCandidatesSortingMethods.sortUCByDST(ucDeadlineMap);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDeadline.keySet()) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsSecureRandomAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.getLast() * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WPMCWRDSTAVBTTLengthSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        return null;
    }
    //endregion

    //region <Sorted By DSTv2>
    public static List<Unicast> WSMLWRDSTv2AVBTT(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT) {
        List<UnicastCandidatesSortingMethods.DSTv2Holder> dsTv2HolderList = UnicastCandidatesSortingMethods.assignDSTv2ToUC(avbUnicastCandidates);

        List<UnicastCandidates> sortedUCByDSTv2 = UnicastCandidatesSortingMethods.sortUCByDSTv2(dsTv2HolderList);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDSTv2) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();


            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMLWRDSTv2AVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength) {
        List<UnicastCandidatesSortingMethods.DSTv2Holder> dsTv2HolderList = UnicastCandidatesSortingMethods.assignDSTv2ToUC(avbUnicastCandidates);

        List<UnicastCandidates> sortedUCByDSTv2 = UnicastCandidatesSortingMethods.sortUCByDSTv2(dsTv2HolderList);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDSTv2) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i) + wLength * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMLWRDSTv2AVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, double wAVB, double wTT, double wLength, double wUtil, int rate) {
        List<UnicastCandidatesSortingMethods.DSTv2Holder> dsTv2HolderList = UnicastCandidatesSortingMethods.assignDSTv2ToUC(avbUnicastCandidates);

        List<UnicastCandidates> sortedUCByDSTv2 = UnicastCandidatesSortingMethods.sortUCByDSTv2(dsTv2HolderList);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDSTv2) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            Map<GraphPath<Node, GCLEdge>, Map<GCLEdge, Double>> edgeUtilMapWithGP = edgeUtilMapWithGP(partialSolution, uc, gpList, rate);
            Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil = gpMaxUtil(edgeUtilMapWithGP);

            ArrayList<Double> normalizedCostGPList;
            ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedCostUtilList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMax(gpMaxUtil);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostVector(gpMaxUtil);
                }
                case null, default -> {
                    normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
                    normalizedCostUtilList = NormalizationMethods.normalizeUtilCostMinMax(gpMaxUtil);
                }
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            for (int i = 0; i < gpList.size(); i++) {
                double cost = wAVB * normalizedCostAVBList.get(i) + wTT * normalizedCostTTList.get(i) + wLength * normalizedCostGPList.get(i) + wUtil * getGPUtil(normalizedCostUtilList, gpList.get(i));
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMCWRDSTv2AVBTTLength(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        List<UnicastCandidatesSortingMethods.DSTv2Holder> dsTv2HolderList = UnicastCandidatesSortingMethods.assignDSTv2ToUC(avbUnicastCandidates);

        List<UnicastCandidates> sortedUCByDSTv2 = UnicastCandidatesSortingMethods.sortUCByDSTv2(dsTv2HolderList);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDSTv2) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.getLast() * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }

    public static List<Unicast> WSMCWRDSTv2AVBTTLengthSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization) {
        List<UnicastCandidatesSortingMethods.DSTv2Holder> dsTv2HolderList = UnicastCandidatesSortingMethods.assignDSTv2ToUC(avbUnicastCandidates);

        List<UnicastCandidates> sortedUCByDSTv2 = UnicastCandidatesSortingMethods.sortUCByDSTv2(dsTv2HolderList);

        List<Unicast> partialSolution = new ArrayList<>(ttUnicasts);

        Map<GCLEdge, Double> edgeDurationMap = MCDMSpecificMethods.getEdgeDurationMap(ttUnicasts);

        for (UnicastCandidates uc : sortedUCByDSTv2) {
            ArrayList<Double> costAVBList = new ArrayList<>();
            ArrayList<Double> costTTList = new ArrayList<>();
            List<GraphPath<Node, GCLEdge>> gpList = uc.getCandidates();

            ArrayList<Double> normalizedCostGPList;
            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMax(gpList);
                case Constants.NORMALIZATIONVECTOR -> normalizedCostGPList = NormalizationMethods.normalizeGPCostVector(gpList);
                case null, default -> normalizedCostGPList = NormalizationMethods.normalizeGPCostMinMax(gpList);
            }

            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double costAVB = 0;
                double costTT = 0;
                for (Unicast u : partialSolution) {
                    if (u.getApplication() instanceof TTApplication) {
                        ArrayList<GCLEdge> sameElements = MCDMSpecificMethods.getSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        for (GCLEdge edge : sameElements) {
                            costTT += edgeDurationMap.get(edge);
                        }
                    } else if (u.getApplication() instanceof SRTApplication avbApp) {
                        int sameElementsCounter = MCDMSpecificMethods.countSameElements(gp.getEdgeList(), u.getRoute().getEdgeList());
                        double sTraffic = (((SRTApplication) uc.getApplication()).getFrameSizeByte() * ((SRTApplication) uc.getApplication()).getNumberOfFrames()) / (double) (uc.getApplication().getCMI());
                        double fTraffic = (avbApp.getFrameSizeByte() * avbApp.getNumberOfFrames()) / (double) (u.getApplication().getCMI());
                        costAVB += sameElementsCounter * (fTraffic * sTraffic);
                    }
                }
                costTTList.add(costTT);
                costAVBList.add(costAVB);
            }

            ArrayList<Double> normalizedCostAVBList;
            ArrayList<Double> normalizedCostTTList;

            switch (wsmNormalization) {
                case Constants.NORMALIZATIONMAX -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMax(costTTList);
                }
                case Constants.NORMALIZATIONVECTOR -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostVector(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostVector(costTTList);
                }
                case null, default -> {
                    normalizedCostAVBList = NormalizationMethods.normalizeAVBTTCostMinMax(costAVBList);
                    normalizedCostTTList = NormalizationMethods.normalizeAVBTTCostMinMax(costTTList);
                }
            }

            double maxCost = Double.MAX_VALUE;
            GraphPath<Node, GCLEdge> selectedGP = null;

            List<Double> weightList = RandomNumberGenerator.generateRandomWeightsSecureRandomAVBTTLength();

            for (int i = 0; i < gpList.size(); i++) {
                double cost = weightList.getFirst() * normalizedCostAVBList.get(i) + weightList.get(1) * normalizedCostTTList.get(i) + weightList.getLast() * normalizedCostGPList.get(i);
                if (cost < maxCost) {
                    maxCost = cost;
                    selectedGP = gpList.get(i);
                    if (maxCost == 0) {
                        break;
                    }
                }
            }

            Unicast selectedU = new Unicast(uc.getApplication(), uc.getDestNode(), selectedGP);
            partialSolution.add(selectedU);

        }
        return partialSolution;
    }
    //TODO
    public static List<Unicast> WSMCWRDSTv2AVBTTLengthUtil(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, int rate) {
        return null;
    }
    //TODO
    public static List<Unicast> WSMCWRDSTv2AVBTTLengthUtilSecureRandom(List<UnicastCandidates> avbUnicastCandidates, List<Unicast> ttUnicasts, String wsmNormalization, int rate) {
        return null;
    }
    //endregion

    private static GraphPath<Node, GCLEdge> getWPMv2AVBTTGraphPath(double wAVB, double wTT, List<Double> costAVBList, List<Double> costTTList, List<GraphPath<Node, GCLEdge>> gpList, String wpmValueType) {
        return null;
    }

    private static GraphPath<Node, GCLEdge> getWPMv2AVBTTLengthGraphPath(double wAVB, double wTT, double wLength, List<Double> costAVBList, List<Double> costTTList, List<GraphPath<Node, GCLEdge>> gpList, String wpmValueType) {
        Map<GraphPath<Node, GCLEdge>, Integer> gpPathScore = new HashMap<>();
        GraphPath<Node, GCLEdge> selectedGP;

        if(Objects.equals(wpmValueType, Constants.ACTUAL)){
            for(int i = 0; i < gpList.size(); i++){
                gpPathScore.put(gpList.get(i), 0);
                for(int j = i + 1; j < gpList.size(); j++){
                    if(!isSameGp(gpList.get(i), gpList.get(j))) {
                        gpPathScore.put(gpList.get(j), 0);
                        double cost;
                        if(costAVBList.get(j) == 0 || costTTList.get(j) == 0){
                            cost = Constants.NEWCOST;
                        }
                        else {
                            cost = Math.pow((costAVBList.get(i) / costAVBList.get(j)), wAVB) * Math.pow((costTTList.get(i) / costTTList.get(j)), wTT) * Math.pow(((double) gpList.get(i).getLength() / gpList.get(j).getLength()), wLength);
                        }

                        if (cost < Constants.WPMTHRESHOLD) {
                            gpPathScore.put(gpList.get(i), gpPathScore.get(gpList.get(i)) + 1);

                        } else {
                            gpPathScore.put(gpList.get(i), gpPathScore.get(gpList.get(j)) + 1);
                        }
                    }
                }
            }
            LinkedHashMap<GraphPath<Node, GCLEdge>, Integer> sortedMapbyScore = new LinkedHashMap<>();
            gpPathScore.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMapbyScore.put(x.getKey(), x.getValue()));

            selectedGP = sortedMapbyScore.entrySet().stream().findFirst().get().getKey();
        }
        else{
            for(int i = 0; i < gpList.size(); i++){
                gpPathScore.put(gpList.get(i), 0);
                double relativeAVBCostFirst = costAVBList.get(i) / (costAVBList.get(i) + costTTList.get(i) + gpList.get(i).getLength());
                double relativeTTCostFirst = costTTList.get(i) / (costAVBList.get(i) + costTTList.get(i) + gpList.get(i).getLength());
                double relativeLengthCostFirst = gpList.get(i).getLength() / (costAVBList.get(i) + costTTList.get(i) + gpList.get(i).getLength());
                for(int j = i + 1; j < gpList.size(); j++){
                    if(!isSameGp(gpList.get(i), gpList.get(j))){
                        gpPathScore.put(gpList.get(j), 0);
                        double cost;
                        if(costAVBList.get(j) == 0 || costTTList.get(j) == 0){
                            cost = Constants.NEWCOST;
                        }
                        else {
                            double adjustedRelativeAVBCost = costAVBList.get(j) / (costAVBList.get(j) + costTTList.get(j) + gpList.get(j).getLength());
                            double adjustedRelativeTTCost = costTTList.get(j) / (costAVBList.get(j) + costTTList.get(j) + gpList.get(j).getLength());
                            double relativeLengthCostSecond = gpList.get(j).getLength() / (costAVBList.get(j) + costTTList.get(j) + gpList.get(j).getLength());

                            cost = Math.pow((relativeAVBCostFirst / adjustedRelativeAVBCost), wAVB) * Math.pow((relativeTTCostFirst / adjustedRelativeTTCost), wTT) * Math.pow((relativeLengthCostFirst / relativeLengthCostSecond), wLength);
                        }

                        if(cost < Constants.WPMTHRESHOLD){
                            gpPathScore.put(gpList.get(i), gpPathScore.get(gpList.get(i)) + 1);
                        }
                        else{
                            gpPathScore.put(gpList.get(j), gpPathScore.get(gpList.get(j)) + 1);
                        }
                    }
                }
            }
            LinkedHashMap<GraphPath<Node, GCLEdge>, Integer> sortedMapbyScore = new LinkedHashMap<>();
            gpPathScore.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMapbyScore.put(x.getKey(), x.getValue()));

            selectedGP = sortedMapbyScore.entrySet().stream().findFirst().get().getKey();
        }
        return selectedGP;
    }

    private static GraphPath<Node, GCLEdge> getWPMv2AVBTTLengthUtilGraphPath(double wAVB, double wTT, double wLength, ArrayList<Double> costAVBList, ArrayList<Double> costTTList, String wpmValueType) {
        return null;
    }

    private static boolean isSameGp(GraphPath<Node, GCLEdge> gp1, GraphPath<Node, GCLEdge> gp2){
        boolean isSame = false;
        for(GCLEdge edge: gp1.getEdgeList()){
            if(gp2.getEdgeList().contains(edge)){
                isSame = true;
                break;
            }
        }
        return isSame;
    }

    //endregion
}
