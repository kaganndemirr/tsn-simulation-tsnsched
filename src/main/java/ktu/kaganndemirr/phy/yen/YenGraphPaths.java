package ktu.kaganndemirr.phy.yen;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.*;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.util.GraphSpecificMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;

public class YenGraphPaths {

    private final List<Unicast> ttUnicasts;
    private final List<UnicastCandidates> avbUnicastCandidates;

    public YenGraphPaths(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final int k) {
        avbUnicastCandidates = new ArrayList<>();
        ttUnicasts = new ArrayList<>();

        for (Application app : applications) {
            if (app instanceof SRTApplication) {
                Graph<Node, GCLEdge> copiedGraph = GraphSpecificMethods.copyGraph(physicalTopology);
                Graph<Node, GCLEdge> esDiscarded = GraphSpecificMethods.discardEndSystems(copiedGraph, app.getSource(), app.getTarget());

                YenKShortestPath<Node, GCLEdge> shortestPaths = new YenKShortestPath<>(esDiscarded);

                List<GraphPath<Node, GCLEdge>> appPaths = new ArrayList<>(k);

                //Retrieve the K shortest paths to the destination
                List<GraphPath<Node, GCLEdge>> sp = shortestPaths.getPaths(app.getSource(), app.getTarget(), k);


                //Abort If no such exists as the problem cannot be solved
                if (sp == null) {
                    throw new InputMismatchException("Aborting, could not find a path from " + app.getSource() + " to " + app.getTarget());
                } else {

                    appPaths.addAll(GraphSpecificMethods.getGPs(sp, k));

                    avbUnicastCandidates.add(new UnicastCandidates(app, app.getTarget(), appPaths));
                }
            }
        }
    }

    public List<UnicastCandidates> getAVBRoutingCandidates() {
        return avbUnicastCandidates;
    }

    public List<Unicast> getTTRoutes() {
        return ttUnicasts;
    }
}
