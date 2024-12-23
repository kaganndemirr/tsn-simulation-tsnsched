package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.GraphPath;

import java.util.*;

public class NormalizationMethods {
    private static double getMaxUtil(Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil){
        double max = 0;
        for(Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()){
            if (entry.getValue() > max){
                max = entry.getValue();
            }
        }

        return max;
    }

    private static double getMinUtil(Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil){
        double min = Double.MAX_VALUE;
        for(Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()){
            if (entry.getValue() < min){
                min = entry.getValue();
            }
        }

        return min;
    }

    public static int findMaxCandidateLength(List<GraphPath<Node, GCLEdge>> gpList) {
        int maxLength = 0;
        for(GraphPath<Node, GCLEdge> gp: gpList){
            if(gp.getEdgeList().size() > maxLength){
                maxLength = gp.getEdgeList().size();
            }
        }

        return maxLength;
    }

    public static int findMinCandidateLength(List<GraphPath<Node, GCLEdge>> gpList) {
        int minLength = Integer.MAX_VALUE;
        for(GraphPath<Node, GCLEdge> gp: gpList){
            if(gp.getEdgeList().size() < minLength){
                minLength = gp.getEdgeList().size();
            }
        }

        return minLength;
    }

    public static ArrayList<Double> normalizeAVBTTCostMinMax(ArrayList<Double> l1) {
        double min = Collections.min(l1);
        double max = Collections.max(l1);

        ArrayList<Double> normalizedList = new ArrayList<>();

        if (min == max && min == 0) {
            for (int i = 0; i < l1.size(); i++) {
                normalizedList.add(0.0);
            }
        } else if (min == max) {
            for (int i = 0; i < l1.size(); i++) {
                normalizedList.add(1.0);
            }
        } else {
            for (Double d : l1) {
                double normalizedValue = (d - min) / (max - min);
                normalizedList.add(normalizedValue);
            }
        }

        return normalizedList;
    }

    public static ArrayList<Double> normalizeGPCostMinMax(List<GraphPath<Node, GCLEdge>> gpList) {
        double min = findMinCandidateLength(gpList);
        double max = findMaxCandidateLength(gpList);

        ArrayList<Double> normalizedList = new ArrayList<>();

        if (min == max) {
            for (int i = 0; i < gpList.size(); i++) {
                normalizedList.add(1.0);
            }
        } else {
            for (GraphPath<Node, GCLEdge> gp : gpList) {
                double normalizedValue = (gp.getEdgeList().size() - min) / (max - min);
                normalizedList.add(normalizedValue);
            }
        }

        return normalizedList;
    }

    public static ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizeUtilCostMinMax(Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil) {
        double max = getMaxUtil(gpMaxUtil);
        double min = getMinUtil(gpMaxUtil);

        ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedList = new ArrayList<>();

        if (min == max) {
            for(Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()){
                Map<GraphPath<Node, GCLEdge>, Double> gpNormalized = new HashMap<>();
                gpNormalized.put(entry.getKey(), 1.0);
                normalizedList.add(gpNormalized);
            }
        } else {
            for(Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()){
                double normalizedValue = (entry.getValue() - min) / (max - min);
                Map<GraphPath<Node, GCLEdge>, Double> gpNormalized = new HashMap<>();
                gpNormalized.put(entry.getKey(), normalizedValue);
                normalizedList.add(gpNormalized);
            }
        }

        return normalizedList;
    }

    public static ArrayList<Double> normalizeAVBTTCostMax(ArrayList<Double> l1) {
        double max = Collections.max(l1);

        ArrayList<Double> normalizedList = new ArrayList<>();

        if (max == 0) {
            for (int i = 0; i < l1.size(); i++) {
                normalizedList.add(0.0);
            }
        } else {
            for (Double d : l1) {
                normalizedList.add(d / max);
            }
        }

        return normalizedList;
    }

    public static ArrayList<Double> normalizeGPCostMax(List<GraphPath<Node, GCLEdge>> gpList) {
        double max = findMaxCandidateLength(gpList);

        ArrayList<Double> normalizedList = new ArrayList<>();

        for (GraphPath<Node, GCLEdge> gp : gpList) {
            normalizedList.add(gp.getEdgeList().size() / max);
        }

        return normalizedList;
    }

    public static ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizeUtilCostMax(Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil) {
        double max = getMaxUtil(gpMaxUtil);

        ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedList = new ArrayList<>();

        for(Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()){
            double normalizedValue = entry.getValue() / max;
            Map<GraphPath<Node, GCLEdge>, Double> gpNormalized = new HashMap<>();
            gpNormalized.put(entry.getKey(), normalizedValue);
            normalizedList.add(gpNormalized);
        }

        return normalizedList;
    }

    public static ArrayList<Double> normalizeAVBTTCostVector(ArrayList<Double> l1) {
        ArrayList<Double> normalizedList = new ArrayList<>();
        double total = 0.0;
        for (Double d : l1) {
            total += d * d;
        }
        double totalSqrt = Math.sqrt(total);

        if (totalSqrt == 0) {
            for (Double ignored : l1) {
                normalizedList.add(0.0);
            }
        } else {
            for (Double d : l1) {
                normalizedList.add(d / totalSqrt);
            }
        }

        return normalizedList;
    }

    public static ArrayList<Double> normalizeGPCostVector(List<GraphPath<Node, GCLEdge>> gpList) {
        ArrayList<Double> normalizedList = new ArrayList<>();
        double total = 0.0;
        for (GraphPath<Node, GCLEdge> gp : gpList) {
            total += gp.getEdgeList().size() * gp.getEdgeList().size();
        }
        double totalSqrt = Math.sqrt(total);

        for (GraphPath<Node, GCLEdge> gp : gpList) {
            normalizedList.add(gp.getEdgeList().size() / totalSqrt);
        }

        return normalizedList;
    }

    public static ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizeUtilCostVector(Map<GraphPath<Node, GCLEdge>, Double> gpMaxUtil) {
        ArrayList<Map<GraphPath<Node, GCLEdge>, Double>> normalizedList = new ArrayList<>();

        double total = 0.0;
        for (Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()) {
            total += entry.getValue() * entry.getValue();
        }

        double totalSqrt = Math.sqrt(total);

        for(Map.Entry<GraphPath<Node, GCLEdge>, Double> entry: gpMaxUtil.entrySet()){
            double normalizedValue = entry.getValue() / totalSqrt;
            Map<GraphPath<Node, GCLEdge>, Double> gpNormalized = new HashMap<>();
            gpNormalized.put(entry.getKey(), normalizedValue);
            normalizedList.add(gpNormalized);
        }

        return normalizedList;
    }
}
