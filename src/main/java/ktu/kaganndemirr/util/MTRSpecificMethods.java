package ktu.kaganndemirr.util;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.Switch;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MTRSpecificMethods {
    public static Set<GCLEdge> extractTTRoutes(Graph<Node, GCLEdge> physicalTopology, List<Application> applications) {
        Set<GCLEdge> ttEdges = new HashSet<>();
        for (Application app : applications) {
            if (app instanceof TTApplication ttApp) {
                Node prev = ttApp.getSource();
                for (Node curr : ttApp.getExplicitPath().path()) {
                    ttEdges.add(physicalTopology.getEdge(prev, curr));
                    prev = curr;
                }
                ttEdges.add(physicalTopology.getEdge(prev, ttApp.getTarget()));
            }
        }
        return ttEdges;
    }

    public static List<Graph<Node, GCLEdge>> createVirtualTopologies(Graph<Node, GCLEdge> topology, Set<GCLEdge> ttEdges) {
        //Physical Topology
        Graph<Node, GCLEdge> physicalTopology = GraphSpecificMethods.copyGraph(topology);

        //Virtual Topology
        Graph<Node, GCLEdge> virtualTopology = GraphSpecificMethods.copyGraph(topology);

        for (GCLEdge edge : physicalTopology.edgeSet()) {
            if (ttEdges.contains(edge)) {
                virtualTopology.setEdgeWeight(edge.getSource(), edge.getTarget(), topology.edgeSet().size());
            }
        }

        List<Graph<Node, GCLEdge>> virtualTopologyList = new ArrayList<>();
        virtualTopologyList.add(physicalTopology);
        virtualTopologyList.add(virtualTopology);

        return virtualTopologyList;
    }
}
