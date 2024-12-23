package ktu.kaganndemirr.route;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.Node;

public abstract class Route {
    //The Application
    protected Application aApp;
    //The Destination Node
    protected Node aDestNode;

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Route r) {
            return aApp.equals(r.aApp) && aDestNode.equals(r.aDestNode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return aApp.hashCode() + aDestNode.hashCode();
    }

    @Override
    public String toString() {
        return aApp.toString() + "->" + aDestNode;
    }

    public Application getApplication() {
        return aApp;
    }

    public Node getDestNode() {
        return aDestNode;
    }


}
