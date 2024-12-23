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

import java.util.*;

public class PPGraphPaths {

    private final List<Unicast> ttUnicasts;
    private final List<UnicastCandidates> avbUnicastCandidates;

    public PPGraphPaths(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications, final int k) {
        avbUnicastCandidates = new ArrayList<>();
        ttUnicasts = new ArrayList<>();

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

    public List<UnicastCandidates> getAVBRoutingCandidates() {
        return avbUnicastCandidates;
    }

    public List<Unicast> getTTRoutes() {
        return ttUnicasts;
    }

}
