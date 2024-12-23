package ktu.kaganndemirr.output.shapers.phy;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.output.holders.phy.PHYWPMCWRv1Holder;
import ktu.kaganndemirr.route.Multicast;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class PHYWPMCWRv1OutputShaper {
    private static final Logger logger = LoggerFactory.getLogger(PHYWPMCWRv1OutputShaper.class.getSimpleName());
    private final String topologyOutputLocation;
    private final String mainOutputLocation;
    private final Map<GCLEdge, Double> utilizationMap;

    public PHYWPMCWRv1OutputShaper(PHYWPMCWRv1Holder phyWPMCWRv1Holder) {
        topologyOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getSolver(), phyWPMCWRv1Holder.getMethod(), phyWPMCWRv1Holder.getAlgorithm(), phyWPMCWRv1Holder.getRandomizationCWR(), phyWPMCWRv1Holder.getMCDMObjective(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getWPMVersion(), phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName()).toString();

        new File(topologyOutputLocation).mkdirs();

        mainOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getSolver(), phyWPMCWRv1Holder.getMethod(), phyWPMCWRv1Holder.getAlgorithm(), phyWPMCWRv1Holder.getRandomizationCWR(), phyWPMCWRv1Holder.getMCDMObjective(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getWPMVersion()).toString();

        utilizationMap = new HashMap<>();
    }


    public void writeSolutionToFile(List<Unicast> solution) {
        try {
            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "Routes.txt").toString()));

            for (Unicast u : solution) {
                if (u.getApplication() instanceof SRTApplication) {
                    writer.write(u.getApplication().getName() + ": ");
                    writer.write(u.getRoute().getEdgeList() + ", ");
                    writer.write("Length(weight non-aware): " + u.getRoute().getEdgeList().size());
                    writer.newLine();
                }
            }

            writer.write("Average Length (ESs included): " + findAverageWithES(solution) + ", ");
            writer.write("Average Length (between switches): " + findAverageWithSW(solution));

            writer.write("\n");
            writer.write("\n");

            for (Unicast u : solution) {
                if (u.getApplication() instanceof TTApplication) {
                    writer.write(u.getApplication().getName() + ": ");
                    writer.write(String.valueOf(u.getRoute().getEdgeList()));
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Routes written to {}", Paths.get(topologyOutputLocation, "Routes.txt file."));
    }

    private double findAverageWithES(List<Unicast> solution) {
        double total = 0;
        int size = 0;
        for (Unicast u : solution) {
            if (u.getApplication() instanceof SRTApplication) {
                total += u.getRoute().getEdgeList().size();
                size++;
            }
        }

        return total / size;
    }

    private double findAverageWithSW(List<Unicast> solution) {
        List<List<GCLEdge>> onlySwitchLinks = solution.stream()
                .map(item -> item.getRoute().getEdgeList().subList(1, item.getRoute().getEdgeList().size() - 1)).toList();

        Map<Integer, Integer> lengthMap = new HashMap<>();

        for (int i = 0; i < onlySwitchLinks.size(); i++) {
            List<GCLEdge> subList = onlySwitchLinks.get(i);
            lengthMap.put(i, subList.size());
        }

        double total = 0;
        for (int i = 0; i < lengthMap.size(); i++) {
            total += lengthMap.get(i);
        }

        return total / lengthMap.size();
    }

    public void writeWCDsToFile(Map<Multicast, Double> aWCDMap) {
        try {
            double total = 0;
            BufferedWriter wcdWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "WCDs.txt").toString()));
            BufferedWriter mainResultWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(mainOutputLocation, "Results.txt").toString(), true));

            for (Map.Entry<Multicast, Double> entry : aWCDMap.entrySet()) {
                total += entry.getValue();
                wcdWriter.write(entry.getKey().getApplication().getName() + "\t" + entry.getKey().getApplication().getDeadline() + "\t" + entry.getValue().toString());
                wcdWriter.newLine();
            }
            double mean = total / aWCDMap.size();
            wcdWriter.write("Average WCD: " + mean + "\t");

            // The variance
            double variance = 0;
            for (Map.Entry<Multicast, Double> entry : aWCDMap.entrySet()) {
                variance += Math.pow(entry.getValue() - mean, 2);
            }
            variance /= aWCDMap.size();
            wcdWriter.write("Variance: " + variance + "\t");

            // Standard Deviation
            double std = Math.sqrt(variance);
            wcdWriter.write("Std: " + std);

            mainResultWriter.write("Average WCD: " + mean + "\t" + "Variance: " + variance + "\t" + "Std: " + std + "\n");

            wcdWriter.close();
            mainResultWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("WCDs, average WCD, variance and std written to {}", Paths.get(topologyOutputLocation, "WCDs.txt file."));
        logger.info("Also average WCD, variance and std written to {}", Paths.get(mainOutputLocation, "Results.txt file."));
    }

    public void writeLinkUtilizationsToFile(List<Unicast> solution, Graph<Node, GCLEdge> graph, int rate) {
        try {
            for (Unicast uc : solution) {
                for (GCLEdge edge : uc.getRoute().getEdgeList()) {
                    if (!utilizationMap.containsKey(edge)) {
                        utilizationMap.put(edge, uc.getApplication().getMessageSizeMbps() / rate);
                    } else {
                        utilizationMap.put(edge, utilizationMap.get(edge) + uc.getApplication().getMessageSizeMbps() / rate);
                    }
                }
            }

            double total = 0;
            for (Map.Entry<GCLEdge, Double> entry : utilizationMap.entrySet()) {
                total += entry.getValue();
            }

            int unusedLinks = graph.edgeSet().size() - utilizationMap.size();

            Map<String, Double> utilizationMapString = new HashMap<>();

            for (GCLEdge edge : graph.edgeSet()) {
                utilizationMapString.put(edge.toString(), utilizationMap.getOrDefault(edge, (double) 0));
            }

            BufferedWriter sortedByNamesWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "LinkUtilsSortedByNames.txt").toString()));
            BufferedWriter sortedByUtilsWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "LinkUtilsSortedByUtils.txt").toString()));

            Map<String, Double> treeMap = new TreeMap<>(utilizationMapString);
            for (Map.Entry<String, Double> entry : treeMap.entrySet()) {
                sortedByNamesWriter.write(entry.getKey() + "\t" + entry.getValue().toString());
                sortedByNamesWriter.newLine();
            }

            for (GCLEdge edge : graph.edgeSet()) {
                if (!utilizationMap.containsKey(edge)) {
                    utilizationMap.put(edge, 0.0);
                }
            }

            LinkedHashMap<GCLEdge, Double> sortedMapbyUtilization = new LinkedHashMap<>();
            utilizationMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMapbyUtilization.put(x.getKey(), x.getValue()));

            for (Map.Entry<GCLEdge, Double> entry : sortedMapbyUtilization.entrySet()) {
                sortedByUtilsWriter.write(entry.getKey().toString() + "\t" + entry.getValue().toString());
                sortedByUtilsWriter.newLine();
            }

            double maxUtilization = 0;
            for (Map.Entry<GCLEdge, Double> entry : sortedMapbyUtilization.entrySet()) {
                maxUtilization = entry.getValue();
                break;
            }

            int maxLoadedLinkCounter = 0;
            for (Map.Entry<GCLEdge, Double> entry : utilizationMap.entrySet()) {
                if (entry.getValue().equals(maxUtilization)) {
                    maxLoadedLinkCounter++;
                }
            }

            double mean = total / graph.edgeSet().size();

            double variance = 0;
            for (Map.Entry<GCLEdge, Double> entry : utilizationMap.entrySet()) {
                variance += Math.pow(entry.getValue() - mean, 2);
            }

            for (int i = 0; i < unusedLinks; i++) {
                variance += Math.pow(0 - mean, 2);
            }

            variance /= graph.edgeSet().size();

            double std = Math.sqrt(variance);

            BufferedWriter mainResultWriter = new BufferedWriter(new java.io.FileWriter(Paths.get(mainOutputLocation, "Results.txt").toString(), true));

            mainResultWriter.write("Unused Links: " + unusedLinks + "/" + graph.edgeSet().size() + ", ");
            mainResultWriter.write("Max Loaded Link Number: " + maxLoadedLinkCounter + ", ");
            mainResultWriter.write("Max Loaded Link Utilization: " + maxUtilization + ", ");
            mainResultWriter.write("Average Link Utilization: " + mean + ", ");
            mainResultWriter.write("Variance: " + variance + ", ");
            mainResultWriter.write("Std: " + std + "\n");

            sortedByNamesWriter.write("Unused Links: " + unusedLinks + "/" + graph.edgeSet().size() + ", ");
            sortedByNamesWriter.write("Max Loaded Link Number: " + maxLoadedLinkCounter + ", ");
            sortedByNamesWriter.write("Max Loaded Link Utilization: " + maxUtilization + ", ");
            sortedByNamesWriter.write("Average Link Utilization: " + mean + ", ");
            sortedByNamesWriter.write("Variance: " + variance + ", ");
            sortedByNamesWriter.write("Std: " + std + "\n");

            sortedByUtilsWriter.write("Unused Links: " + unusedLinks + "/" + graph.edgeSet().size() + ", ");
            sortedByUtilsWriter.write("Max Loaded Link Number: " + maxLoadedLinkCounter + ", ");
            sortedByUtilsWriter.write("Max Loaded Link Utilization: " + maxUtilization + ", ");
            sortedByUtilsWriter.write("Average Link Utilization: " + mean + ", ");
            sortedByUtilsWriter.write("Variance: " + variance + ", ");
            sortedByUtilsWriter.write("Std: " + std + "\n");

            sortedByNamesWriter.close();
            sortedByUtilsWriter.close();
            mainResultWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Link utilization's sorted by link names " + Paths.get(topologyOutputLocation, "LinkUtilsSortedByNames.txt file."));
        logger.info("Link utilization's sorted by link utilization's " + Paths.get(topologyOutputLocation, "LinkUtilsSortedByUtils.txt file."));
        logger.info("Unused Links, Max Loaded Link Number, Max Loaded Link Utilization, Average Link Utilization, Variance and Std written to " + Paths.get(mainOutputLocation, "Results.txt"));
    }

    public void writeDurationMap(Map<Double, Double> durationMap) {
        try {
            LinkedHashMap<Double, Double> sortedDurationMap = new LinkedHashMap<>();
            durationMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x -> sortedDurationMap.put(x.getKey(), x.getValue()));

            BufferedWriter writer2 = new BufferedWriter(new java.io.FileWriter(Paths.get(mainOutputLocation, "Results.txt").toString(), true));
            writer2.write("Costs and computation times(sec): " + sortedDurationMap + "\n");
            writer2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Costs and computation times written to {}", Paths.get(mainOutputLocation, "Results.txt file."));
    }

    public void writeAVBCandidatesTTIntersectionToFile(List<UnicastCandidates> avbRoutes, List<Unicast> ttRoutes) {
        try {
            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "AVBCandidateRoutesTTIntersection.txt").toString()));
            for (UnicastCandidates uc : avbRoutes) {
                writer.write(uc.getApplication().getName() + "\t" + "How many links intersect with TT streams" + "\t" + "How many different TT streams" + "\t\t" + "These TT Streams and Links\n");
                int index = 1;
                for (GraphPath<Node, GCLEdge> graphPath : uc.getCandidates()) {
                    int counter = 0;
                    Map<String, Integer> unicastMap = new HashMap<>();
                    for (GCLEdge avbEdge : graphPath.getEdgeList()) {
                        for (Unicast u : ttRoutes) {
                            if (u.getRoute().getEdgeList().contains(avbEdge)) {
                                counter++;
                                if (!unicastMap.containsKey(u.getApplication().getName())) {
                                    unicastMap.put(u.getApplication().getName(), 1);
                                } else {
                                    unicastMap.put(u.getApplication().getName(), unicastMap.get(u.getApplication().getName()) + 1);
                                }
                            }
                        }
                    }
                    writer.write(uc.getApplication().getName() + "_" + index + "\t\t\t\t\t\t" + counter + "\t\t\t\t\t\t\t\t" + unicastMap.keySet().size() + "\t\t\t\t" + unicastMap + "\n");
                    index++;
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("AVB Candidate Routes TT intersection written to {}", Paths.get(topologyOutputLocation, "AVBCandidateRoutesTTIntersection.txt file."));
    }

    public void writeAVBTTIntersectionToFile(List<Unicast> solution) {
        // Export to excel
        try {
            ArrayList<Unicast> AVBUnicastList = new ArrayList<>();
            ArrayList<Unicast> TTUnicastList = new ArrayList<>();
            for (Unicast u : solution) {
                if (u.getApplication() instanceof SRTApplication) {
                    AVBUnicastList.add(u);
                } else {
                    TTUnicastList.add(u);
                }
            }

            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "AVBTTIntersection.txt").toString()));
            writer.write("AVB Names" + "\t" + "How many links intersect with other AVB streams" + "\t\t" + "How many AVB streams" + "\t" + "These AVB Streams" + "\t\t\t\t" + "How many links intersect with TT streams" + "\t" + "How many TT streams" + "\t\t" + "These TT Streams and Links" + "\n");
            for (Unicast u : AVBUnicastList) {
                int AVBCounter = 0;
                Map<String, Integer> AVBMap = new HashMap<>();
                for (GCLEdge edge : u.getRoute().getEdgeList()) {
                    for (Unicast uInner : AVBUnicastList) {
                        if (u != uInner) {
                            if (uInner.getRoute().getEdgeList().contains(edge)) {
                                AVBCounter++;
                                if (!AVBMap.containsKey(uInner.getApplication().getName())) {
                                    AVBMap.put(uInner.getApplication().getName(), 1);
                                } else {
                                    AVBMap.put(uInner.getApplication().getName(), AVBMap.get(uInner.getApplication().getName()) + 1);
                                }
                            }
                        }
                    }
                }
                int TTCounter = 0;
                Map<String, Integer> TTMap = new HashMap<>();
                for (GCLEdge edge : u.getRoute().getEdgeList()) {
                    for (Unicast uTT : TTUnicastList) {
                        if (uTT.getRoute().getEdgeList().contains(edge)) {
                            TTCounter++;
                            if (!TTMap.containsKey(uTT.getApplication().getName())) {
                                TTMap.put(uTT.getApplication().getName(), 1);
                            } else {
                                TTMap.put(uTT.getApplication().getName(), TTMap.get(uTT.getApplication().getName()) + 1);
                            }
                        }
                    }
                }
                writer.write(u.getApplication().getName() + "\t\t\t\t\t\t" + AVBCounter + "\t\t\t\t\t\t\t\t\t\t" + AVBMap.keySet().size() + "\t\t\t\t" + AVBMap + "\t\t\t\t\t\t\t" + TTCounter + "\t\t\t\t\t\t\t\t" + TTMap.keySet().size() + "\t\t\t\t\t\t\t\t" + TTMap + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("AVB Routes TT intersection written to {}", Paths.get(topologyOutputLocation, "AVBTTIntersection.txt file"));
    }

    public void writeLinkAVBTTIntersectionToFile(Graph<Node, GCLEdge> graph, List<Unicast> solution) {
        try {
            ArrayList<String> edgeList = new ArrayList<>();
            for (GCLEdge edge : graph.edgeSet()) {
                edgeList.add(edge.toString());
            }

            Collections.sort(edgeList);

            ArrayList<GCLEdge> sortedGCLEdge = new ArrayList<>();
            for (String edge : edgeList) {
                for (GCLEdge edgeInner : graph.edgeSet()) {
                    if (Objects.equals(edgeInner.toString(), edge)) {
                        sortedGCLEdge.add(edgeInner);
                    }
                }
            }

            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "LinkAVBTTIntersection.txt").toString()));
            writer.write("Link Names " + "\t" + "How Many AVB Streams Are Using" + "\t" + "These AVB Streams" + "\t" + "How Many TT Streams Are Using" + "\t" + "These TT Streams\n");
            for (GCLEdge edge : sortedGCLEdge) {
                int AVBCounter = 0;
                int TTCounter = 0;
                Map<String, Integer> AVBMap = new HashMap<>();
                Map<String, Integer> TTMap = new HashMap<>();
                for (Unicast u : solution) {
                    if (u.getRoute().getEdgeList().contains(edge)) {
                        if (u.getApplication() instanceof TTApplication) {
                            TTCounter++;
                            if (!TTMap.containsKey(u.getApplication().getName())) {
                                TTMap.put(u.getApplication().getName(), 1);
                            } else {
                                TTMap.put(u.getApplication().getName(), TTMap.get(u.getApplication().getName()) + 1);
                            }
                        } else {
                            AVBCounter++;
                            if (!AVBMap.containsKey(u.getApplication().getName())) {
                                AVBMap.put(u.getApplication().getName(), 1);
                            } else {
                                AVBMap.put(u.getApplication().getName(), AVBMap.get(u.getApplication().getName()) + 1);
                            }
                        }
                    }
                }
                writer.write(edge + "\t\t\t" + AVBCounter + "\t\t\t\t\t\t\t" + AVBMap + "\t\t\t\t\t\t" + TTCounter + "\t\t\t\t\t\t" + TTMap + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("Link's AVB and TT intersection written to {}", Paths.get(topologyOutputLocation, "LinkAVBTTIntersection.txt file."));
    }

    public void writeAVBCandidateRoutesToFile(List<UnicastCandidates> avbRoutes) {
        try {
            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(Paths.get(topologyOutputLocation, "AVBCandidateRoutes.txt").toString()));
            for (UnicastCandidates uc : avbRoutes) {
                for (GraphPath<Node, GCLEdge> gp : uc.getCandidates()) {
                    writer.write(uc.getApplication().getName() + "\t" + gp.getEdgeList() + "\tLength(weight aware): " + gp.getWeight() + "\tLength(weight non-aware): " + gp.getEdgeList().size());
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("AVB Candidate Routes written to {}", Paths.get(topologyOutputLocation, "AVBCandidateRoutes.txt file."));
    }
}



