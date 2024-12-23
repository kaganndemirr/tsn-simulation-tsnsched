package ktu.kaganndemirr.phy.pp;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.*;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.route.UnicastCandidates;
import ktu.kaganndemirr.util.GraphSpecificMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

public class PPRandomizedGraphPaths {

    private List<Unicast> ttUnicasts;
    private List<UnicastCandidates> avbUnicastCandidates;

    public PPRandomizedGraphPaths(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications) {
        ttUnicasts = new ArrayList<>();
    }

    public PPRandomizedGraphPaths(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final int k, final String randomizationLWR) {
        avbUnicastCandidates = new ArrayList<>();

        GraphSpecificMethods.randomizeGraph(physicalTopology, randomizationLWR);


        for (Application app : applications) {
            if (app instanceof SRTApplication) {
                Graph<Node, GCLEdge> copiedGraph = GraphSpecificMethods.copyGraph(physicalTopology);
                Graph<Node, GCLEdge> esDiscarded = GraphSpecificMethods.discardEndSystems(copiedGraph, app.getSource(), app.getTarget());

                ArrayList<GraphPath<Node, GCLEdge>> appPaths = new ArrayList<>(k);

                for (int i = 0; i < k; i++) {

                    DijkstraShortestPath<Node, GCLEdge> shortestPath = new DijkstraShortestPath<>(esDiscarded);

                    //Retrieve the K shortest paths to the destination
                    GraphPath<Node, GCLEdge> sp = shortestPath.getPath(app.getSource(), app.getTarget());

                    if (sp == null) {
                        throw new InputMismatchException("Aborting, could not find a path from " + app.getSource() + " to " + app.getTarget());
                    }

                    appPaths.add(sp);

                    GraphSpecificMethods.pathPenalization(physicalTopology, esDiscarded, sp);

                }

                avbUnicastCandidates.add(new UnicastCandidates(app, app.getTarget(), appPaths));
            }
        }
    }

    public List<UnicastCandidates> getAVBUnicastCandidates() {
        return avbUnicastCandidates;
    }

    public List<Unicast> getTTUnicasts() {
        return ttUnicasts;
    }
}
