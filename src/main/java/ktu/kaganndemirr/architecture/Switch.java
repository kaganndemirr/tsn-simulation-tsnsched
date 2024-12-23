package ktu.kaganndemirr.architecture;

public class Switch extends Node {

    public Switch(String name) {
        this.name = name;
    }

    public Switch(Node n) {
        name = n.name;
    }
}
