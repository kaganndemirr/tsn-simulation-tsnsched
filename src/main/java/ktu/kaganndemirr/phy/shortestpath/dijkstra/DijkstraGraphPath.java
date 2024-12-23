package ktu.kaganndemirr.phy.shortestpath.dijkstra;

import ktu.kaganndemirr.application.SRTApplication;
import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.application.TTApplication;
import ktu.kaganndemirr.architecture.*;
import ktu.kaganndemirr.route.Unicast;
import ktu.kaganndemirr.util.GraphSpecificMethods;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.*;

public class DijkstraGraphPath {

    private final List<Unicast> ttUnicasts;
    private final List<Unicast> avbUnicasts;

    public DijkstraGraphPath(final Graph<Node, GCLEdge> physicalTopology, final List<Application> applications) {

        avbUnicasts = new ArrayList<>();
        ttUnicasts = new ArrayList<>();

        for (Application app : applications) {
            if (app instanceof SRTApplication) {
                Graph<Node, GCLEdge> copiedGraph = GraphSpecificMethods.copyGraph(physicalTopology);
                Graph<Node, GCLEdge> esDiscarded = GraphSpecificMethods.discardEndSystems(copiedGraph, app.getSource(), app.getTarget());

                DijkstraShortestPath<Node, GCLEdge> shortestPath = new DijkstraShortestPath<>(esDiscarded);

                //Retrieve the dijkstra shortest path to the destination
                GraphPath<Node, GCLEdge> sp = shortestPath.getPath(app.getSource(), app.getTarget());

                //Abort If no such exists as the problem cannot be solved
                if (sp == null) {
                    throw new InputMismatchException("Aborting, could not find a path from " + app.getSource() + " to " + app.getTarget());
                } else {
                    avbUnicasts.add(new Unicast(app, app.getTarget(), sp));
                }
            }
        }
    }

    public List<Unicast> getAVBUnicasts() {
        return avbUnicasts;
    }

    public List<Unicast> getTTUnicasts() {
        return ttUnicasts;
    }

}
