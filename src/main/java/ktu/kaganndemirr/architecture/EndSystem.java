package ktu.kaganndemirr.architecture;

public class EndSystem extends Node {

    public EndSystem(String name) {
        this.name = name;
    }

    public EndSystem(Node n) {
        name = n.name;
    }
}
