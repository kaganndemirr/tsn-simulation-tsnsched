package ktu.kaganndemirr.route;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import org.jgrapht.GraphPath;

public class Unicast extends Route {
    //The ArrayList of GraphPaths (One for each destination)
    private GraphPath<Node, GCLEdge> aRoute;

    public Unicast(Application app, Node destNode, GraphPath<Node, GCLEdge> route) {
        aApp = app;
        aDestNode = destNode;
        aRoute = route;
    }

    public GraphPath<Node, GCLEdge> getRoute() {
        return aRoute;
    }

    public void setRoute(GraphPath<Node, GCLEdge> gp){
        aRoute = gp;
    }
}
