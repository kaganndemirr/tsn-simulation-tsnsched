package ktu.kaganndemirr.application;

import ktu.kaganndemirr.architecture.Switch;

import java.util.List;

public record ExplicitPath(List<Switch> path) {

    public String toString() {
        return path.toString();
    }
}
