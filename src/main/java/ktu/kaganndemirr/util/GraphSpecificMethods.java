package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.*;
import ktu.kaganndemirr.route.Unicast;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public class GraphSpecificMethods {
    public static Graph<Node, GCLEdge> copyGraph(Graph<Node, GCLEdge> physicalTopology) {
        Graph<Node, GCLEdge> copiedGraph = new SimpleDirectedWeightedGraph<>(GCLEdge.class);

        for (Node n : physicalTopology.vertexSet()) {
            if (n instanceof EndSystem) {
                copiedGraph.addVertex(new EndSystem(n));
            } else {
                copiedGraph.addVertex(new Switch(n));
            }
        }

        for (GCLEdge edge : physicalTopology.edgeSet()) {
            copiedGraph.addEdge(edge.getSource(), edge.getTarget(), new GCLEdge(edge));
            copiedGraph.setEdgeWeight(edge.getSource(), edge.getTarget(), physicalTopology.getEdgeWeight(edge));
        }

        return copiedGraph;
    }

    public static Graph<Node, GCLEdge> discardEndSystems(Graph<Node, GCLEdge> physicalTopology, EndSystem src, EndSystem dest) {
        Set<EndSystem> endSystemsToKeep = new HashSet<>();
        endSystemsToKeep.add(src);
        endSystemsToKeep.add(dest);

        Set<EndSystem> verticesToRemove = new HashSet<>();
        for (Node n : physicalTopology.vertexSet()) {
            if (n instanceof EndSystem endSystem && (!endSystemsToKeep.contains(endSystem))) {
                verticesToRemove.add(endSystem);
            }
        }

        for (EndSystem e : verticesToRemove) {
            physicalTopology.removeVertex(e);
        }

        return physicalTopology;
    }

    public static void pathPenalization(Graph<Node, GCLEdge> physicalTopology, Graph<Node, GCLEdge> esDiscarded, GraphPath<Node, GCLEdge> sp) {
        for (GCLEdge edge : sp.getEdgeList()) {
            esDiscarded.setEdgeWeight(edge.getSource(), edge.getTarget(), Math.min(esDiscarded.getEdgeWeight(edge) * physicalTopology.edgeSet().size(), Double.MAX_VALUE));
        }
    }

    public static void randomizeGraph(Graph<Node, GCLEdge> physicalTopology, String randomizationLWR) {
        if (Objects.equals(randomizationLWR, Constants.RANDOMIZEINTMAX)){
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), randomNumberGenerator.generateRandomWeightWithINTMAX());
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEWMAX)) {
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator(physicalTopology.edgeSet().size());
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), randomNumberGenerator.generateRandomWeightWithWMAX());
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEHEADSORTAILS)) {
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator(physicalTopology.edgeSet().size());
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), randomNumberGenerator.generateRandomWeightWithHeadsOrTails());
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEHEADSORTAILSXORSHIFT)) {
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), RandomNumberGenerator.generateRandomWeightWithHeadsOrTailsWithXORShift(1, physicalTopology.edgeSet().size()));
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEHEADSORTAILSSECURERANDOM)) {
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), RandomNumberGenerator.generateRandomWeightWithHeadsOrTailsWithSecureRandom(1, physicalTopology.edgeSet().size()));
            }
        }
    }

    public static void randomizeVT(Graph<Node, GCLEdge> physicalTopology, String randomizationLWR, Set<GCLEdge> ttEdges) {
        if (Objects.equals(randomizationLWR, Constants.RANDOMIZEINTMAX)){
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                if(!ttEdges.contains(edge)){
                    physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), randomNumberGenerator.generateRandomWeightWithINTMAX());
                }
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEWMAX)) {
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator(physicalTopology.edgeSet().size());
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                if(!ttEdges.contains(edge)) {
                    physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), randomNumberGenerator.generateRandomWeightWithWMAX());
                }
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEHEADSORTAILS)) {
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator(physicalTopology.edgeSet().size());
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                if(!ttEdges.contains(edge)) {
                    physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), randomNumberGenerator.generateRandomWeightWithHeadsOrTails());
                }
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEHEADSORTAILSXORSHIFT)) {
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                if(!ttEdges.contains(edge)) {
                    physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), RandomNumberGenerator.generateRandomWeightWithHeadsOrTailsWithXORShift(1, physicalTopology.edgeSet().size()));
                }
            }
        } else if (Objects.equals(randomizationLWR, Constants.RANDOMIZEHEADSORTAILSSECURERANDOM)) {
            for (GCLEdge edge : physicalTopology.edgeSet()) {
                if(!ttEdges.contains(edge)) {
                    physicalTopology.setEdgeWeight(physicalTopology.getEdgeSource(edge), physicalTopology.getEdgeTarget(edge), RandomNumberGenerator.generateRandomWeightWithHeadsOrTailsWithSecureRandom(1, physicalTopology.edgeSet().size()));
                }
            }
        }
    }

    public static List<Integer> findKForTopologies(int k, int virtualTopologyListSize) {
        List<Integer> kList = new ArrayList<>();
        if (k % virtualTopologyListSize == 0) {
            for (int j = 0; j < virtualTopologyListSize; j++) {
                kList.add(k / virtualTopologyListSize);
            }
        } else {
            for (int j = 0; j < virtualTopologyListSize; j++) {
                kList.add(k / virtualTopologyListSize);
            }
            int z = 0;
            while (z < k % virtualTopologyListSize) {
                kList.set(z, kList.get(z) + 1);
                z++;
            }
        }
        return kList;
    }

    public static List<GraphPath<Node, GCLEdge>> getGPs(List<GraphPath<Node, GCLEdge>> gps, int k){
        if (gps.size() != k) {
            int appPathsSize = gps.size();
            for (int j = 0; j < k - appPathsSize; j++) {
                gps.add(gps.getLast());
            }
        }
        return gps;
    }

    public static List<Unicast> createUnicastForApplicationsHaveExplicitPath(List<Application> applications, Graph<Node, GCLEdge> physicalTopology){
        List<Unicast> unicasts = new ArrayList<>();
        for(Application application: applications){
            try {
                List<GCLEdge> edgeList = new ArrayList<>();

                Node prev = application.getSource();
                for (Node curr : application.getExplicitPath().path()) {
                    edgeList.add(physicalTopology.getEdge(prev, curr));
                    prev = curr;
                }
                edgeList.add(physicalTopology.getEdge(prev, application.getTarget()));


                GraphPath<Node, GCLEdge> gp = new GraphWalk<>(physicalTopology, application.getSource(), application.getTarget(), edgeList, edgeList.size() * Constants.UNITWEIGHT);
                unicasts.add(new Unicast(application, application.getTarget(), gp));

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The specified vertices-Route for " + application.getName() + " do not form a path.");
            }
        }

        return unicasts;
    }
}
