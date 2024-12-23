package ktu.kaganndemirr.architecture;

import java.util.Objects;

public abstract class Node {
    protected String name;

    @Override
    public boolean equals(Object other) {
        boolean result;
        if (other == null || getClass() != other.getClass()) {
            result = false;
        } else {
            Node otherNode = (Node) other;
            result = name.equals(otherNode.name);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
