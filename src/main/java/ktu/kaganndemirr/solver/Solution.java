package ktu.kaganndemirr.solver;

import ktu.kaganndemirr.evaluator.Cost;
import ktu.kaganndemirr.route.Multicast;

import java.util.List;

public class Solution {
    private final Cost aCost;
    private final List<Multicast> aRouting;

    public Solution(Cost c, List<Multicast> m) {
        aCost = c;
        aRouting = m;
    }

    public List<Multicast> getRouting() {
        return aRouting;
    }

    public Cost getCost() {
        return aCost;
    }
}
