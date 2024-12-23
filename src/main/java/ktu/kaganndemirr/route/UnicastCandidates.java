package ktu.kaganndemirr.route;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

import java.util.List;

public class UnicastCandidates extends Route {
    //The ArrayList of GraphPaths making up all the
    private final List<GraphPath<Node, GCLEdge>> aRouting;

    public UnicastCandidates(Application app, Node destNode, List<GraphPath<Node, GCLEdge>> paths) {
        aApp = app;
        aDestNode = destNode;
        aRouting = paths;
    }

    public List<GraphPath<Node, GCLEdge>> getCandidates() {
        return aRouting;
    }
}
