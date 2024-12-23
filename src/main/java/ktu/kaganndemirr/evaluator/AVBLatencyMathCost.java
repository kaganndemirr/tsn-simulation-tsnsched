package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.output.holders.phy.*;
import ktu.kaganndemirr.route.Multicast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AVBLatencyMathCost implements Cost, Comparator<AVBLatencyMathCost> {
    private static Logger logger = LoggerFactory.getLogger(AVBLatencyMathCost.class.getSimpleName());

    private double obj1;
    private double obj2;
    private double obj3;
    private boolean isUsed;
    private final Map<Multicast, Double> aWCDMap = new HashMap<>();

    public AVBLatencyMathCost() {
        reset();
    }

    public void setWCD(Multicast m, Double wcd) {
        aWCDMap.put(m, wcd);
    }

    public void add(Objective e, double value) {
        isUsed = true;
        switch (e) {
            case one -> obj1 += value;
            case two -> obj2 += value;
            case three -> obj3 += value;
        }
    }

    @Override
    public double getTotalCost() {
        if (!isUsed) {
            return Double.MAX_VALUE;
        }
        double w1 = 10000;
        double w2 = 3.0;
        double w3 = 1.0;
        return w1 * obj1 + w2 * obj2 + w3 * obj3;
    }

    @Override
    public void reset() {
        isUsed = false;
        obj1 = 0.0;
        obj2 = 0.0;
        obj3 = 0.0;
    }

    @Override
    public int compare(AVBLatencyMathCost o1, AVBLatencyMathCost o2) {
        return (int) Math.round(o1.getTotalCost() - o2.getTotalCost());
    }

    public enum Objective {
        one, two, three;
    }

    public String toString() {
        return getTotalCost() + " (current o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + ")";
    }

    public String toDetailedString() {
        return "Total : " + this + " | o1 " + obj1 + ", o2 " + obj2 + ", o3 " + obj3 + " -- " + aWCDMap + " --";
    }

    @Override
    public Map<Multicast, Double> getaWCDMap() {
        return aWCDMap;
    }

    @Override
    public void writeShortestPathResultToFile(PHYShortestPathHolder phyShortestPathHolder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyShortestPathHolder.getSolver(), phyShortestPathHolder.getMethod(), phyShortestPathHolder.getAlgorithm(), phyShortestPathHolder.getTopologyName() + "_" + phyShortestPathHolder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyShortestPathHolder.getSolver(), phyShortestPathHolder.getMethod(), phyShortestPathHolder.getAlgorithm()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyShortestPathHolder.getTopologyName() + "_" + phyShortestPathHolder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWSMResultToFile(PHYWSMHolder phyWSMHolder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWSMHolder.getSolver(), phyWSMHolder.getMethod(), phyWSMHolder.getAlgorithm(), phyWSMHolder.getWSMNormalization(), phyWSMHolder.getMCDMObjective(), String.valueOf(phyWSMHolder.getWAVB()), String.valueOf(phyWSMHolder.getWTT()), String.valueOf(phyWSMHolder.getWLength()), String.valueOf(phyWSMHolder.getWUtil()), String.valueOf(phyWSMHolder.getK()), phyWSMHolder.getTopologyName() + "_" + phyWSMHolder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWSMHolder.getSolver(), phyWSMHolder.getMethod(), phyWSMHolder.getAlgorithm(), phyWSMHolder.getWSMNormalization(), phyWSMHolder.getMCDMObjective(), String.valueOf(phyWSMHolder.getWAVB()), String.valueOf(phyWSMHolder.getWTT()), String.valueOf(phyWSMHolder.getWLength()), String.valueOf(phyWSMHolder.getWUtil()), String.valueOf(phyWSMHolder.getK())).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWSMHolder.getTopologyName() + "_" + phyWSMHolder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWPMv1ResultToFile(PHYWPMv1Holder phyWPMv1Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMv1Holder.getSolver(), phyWPMv1Holder.getMethod(), phyWPMv1Holder.getAlgorithm(), phyWPMv1Holder.getMCDMObjective(), String.valueOf(phyWPMv1Holder.getWAVB()), String.valueOf(phyWPMv1Holder.getWTT()), String.valueOf(phyWPMv1Holder.getWLength()), String.valueOf(phyWPMv1Holder.getWUtil()), phyWPMv1Holder.getMethod(), String.valueOf(phyWPMv1Holder.getK()), phyWPMv1Holder.getWPMVersion(), phyWPMv1Holder.getTopologyName() + "_" + phyWPMv1Holder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWPMv1Holder.getSolver(), phyWPMv1Holder.getMethod(), phyWPMv1Holder.getAlgorithm(), phyWPMv1Holder.getMCDMObjective(), String.valueOf(phyWPMv1Holder.getWAVB()), String.valueOf(phyWPMv1Holder.getWTT()), String.valueOf(phyWPMv1Holder.getWLength()), String.valueOf(phyWPMv1Holder.getWUtil()), phyWPMv1Holder.getMethod(), String.valueOf(phyWPMv1Holder.getK()), phyWPMv1Holder.getWPMVersion()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWPMv1Holder.getTopologyName() + "_" + phyWPMv1Holder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWPMv2ResultToFile(PHYWPMv2Holder phyWPMv2Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMv2Holder.getSolver(), phyWPMv2Holder.getMethod(), phyWPMv2Holder.getAlgorithm(), phyWPMv2Holder.getMCDMObjective(), String.valueOf(phyWPMv2Holder.getWAVB()), String.valueOf(phyWPMv2Holder.getWTT()), String.valueOf(phyWPMv2Holder.getWLength()), String.valueOf(phyWPMv2Holder.getWUtil()), String.valueOf(phyWPMv2Holder.getK()), phyWPMv2Holder.getWPMVersion(), phyWPMv2Holder.getWPMValueType(), phyWPMv2Holder.getTopologyName() + "_" + phyWPMv2Holder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWPMv2Holder.getSolver(), phyWPMv2Holder.getMethod(), phyWPMv2Holder.getAlgorithm(), phyWPMv2Holder.getMCDMObjective(), String.valueOf(phyWPMv2Holder.getWAVB()), String.valueOf(phyWPMv2Holder.getWTT()), String.valueOf(phyWPMv2Holder.getWLength()), String.valueOf(phyWPMv2Holder.getWUtil()), String.valueOf(phyWPMv2Holder.getK()), phyWPMv2Holder.getWPMVersion(), phyWPMv2Holder.getWPMValueType()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWPMv2Holder.getTopologyName() + "_" + phyWPMv2Holder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWPMLWRv1ResultToFile(PHYWPMLWRv1Holder phyWPMLWRv1Holder){
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMLWRv1Holder.getSolver(), phyWPMLWRv1Holder.getMethod(), phyWPMLWRv1Holder.getAlgorithm(), phyWPMLWRv1Holder.getMCDMObjective(), String.valueOf(phyWPMLWRv1Holder.getWAVB()), String.valueOf(phyWPMLWRv1Holder.getWTT()), String.valueOf(phyWPMLWRv1Holder.getWLength()), String.valueOf(phyWPMLWRv1Holder.getWUtil()), String.valueOf(phyWPMLWRv1Holder.getK()), phyWPMLWRv1Holder.getWPMVersion(), phyWPMLWRv1Holder.getTopologyName() + "_" + phyWPMLWRv1Holder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWPMLWRv1Holder.getSolver(), phyWPMLWRv1Holder.getMethod(), phyWPMLWRv1Holder.getAlgorithm(), phyWPMLWRv1Holder.getMCDMObjective(), String.valueOf(phyWPMLWRv1Holder.getWAVB()), String.valueOf(phyWPMLWRv1Holder.getWTT()), String.valueOf(phyWPMLWRv1Holder.getWLength()), String.valueOf(phyWPMLWRv1Holder.getWUtil()), String.valueOf(phyWPMLWRv1Holder.getK()), phyWPMLWRv1Holder.getWPMVersion()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWPMLWRv1Holder.getTopologyName() + "_" + phyWPMLWRv1Holder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWPMLWRv2ResultToFile(PHYWPMLWRv2Holder phyWPMLWRv2Holder){
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMLWRv2Holder.getSolver(), phyWPMLWRv2Holder.getMethod(), phyWPMLWRv2Holder.getAlgorithm(), phyWPMLWRv2Holder.getMCDMObjective(), String.valueOf(phyWPMLWRv2Holder.getWAVB()), String.valueOf(phyWPMLWRv2Holder.getWTT()), String.valueOf(phyWPMLWRv2Holder.getWLength()), String.valueOf(phyWPMLWRv2Holder.getWUtil()), String.valueOf(phyWPMLWRv2Holder.getK()), phyWPMLWRv2Holder.getWPMVersion(), phyWPMLWRv2Holder.getWPMValueType(), phyWPMLWRv2Holder.getTopologyName() + "_" + phyWPMLWRv2Holder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWPMLWRv2Holder.getSolver(), phyWPMLWRv2Holder.getMethod(), phyWPMLWRv2Holder.getAlgorithm(), phyWPMLWRv2Holder.getMCDMObjective(), String.valueOf(phyWPMLWRv2Holder.getWAVB()), String.valueOf(phyWPMLWRv2Holder.getWTT()), String.valueOf(phyWPMLWRv2Holder.getWLength()), String.valueOf(phyWPMLWRv2Holder.getWUtil()), String.valueOf(phyWPMLWRv2Holder.getK()), phyWPMLWRv2Holder.getWPMVersion(), phyWPMLWRv2Holder.getWPMValueType()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWPMLWRv2Holder.getTopologyName() + "_" + phyWPMLWRv2Holder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWSMLWRResultToFile(PHYWSMLWRHolder phyWSMLWRHolder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWSMLWRHolder.getSolver(), phyWSMLWRHolder.getAlgorithm(), phyWSMLWRHolder.getRandomizationLWR(), phyWSMLWRHolder.getWSMNormalization(), phyWSMLWRHolder.getMCDMObjective(), String.valueOf(phyWSMLWRHolder.getWAVB()), String.valueOf(phyWSMLWRHolder.getWTT()), String.valueOf(phyWSMLWRHolder.getWLength()), String.valueOf(phyWSMLWRHolder.getWUtil()), phyWSMLWRHolder.getMethod(), String.valueOf(phyWSMLWRHolder.getK()), phyWSMLWRHolder.getTopologyName() + "_" + phyWSMLWRHolder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWSMLWRHolder.getSolver(), phyWSMLWRHolder.getAlgorithm(), phyWSMLWRHolder.getRandomizationLWR(), phyWSMLWRHolder.getWSMNormalization(), phyWSMLWRHolder.getMCDMObjective(), String.valueOf(phyWSMLWRHolder.getWAVB()), String.valueOf(phyWSMLWRHolder.getWTT()), String.valueOf(phyWSMLWRHolder.getWLength()), String.valueOf(phyWSMLWRHolder.getWUtil()), phyWSMLWRHolder.getMethod(), String.valueOf(phyWSMLWRHolder.getK())).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWSMLWRHolder.getTopologyName() + "_" + phyWSMLWRHolder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWSMLWRCWRResultToFile(PHYWSMLWRCWRHolder phyWSMLWRCWRHolder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWSMLWRCWRHolder.getSolver(), phyWSMLWRCWRHolder.getAlgorithm(), phyWSMLWRCWRHolder.getRandomizationLWR(), phyWSMLWRCWRHolder.getRandomizationCWR(), phyWSMLWRCWRHolder.getWSMNormalization(), phyWSMLWRCWRHolder.getMCDMObjective(), phyWSMLWRCWRHolder.getMethod(), String.valueOf(phyWSMLWRCWRHolder.getK()), phyWSMLWRCWRHolder.getTopologyName() + "_" + phyWSMLWRCWRHolder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWSMLWRCWRHolder.getSolver(), phyWSMLWRCWRHolder.getAlgorithm(), phyWSMLWRCWRHolder.getRandomizationLWR(), phyWSMLWRCWRHolder.getRandomizationCWR(), phyWSMLWRCWRHolder.getWSMNormalization(), phyWSMLWRCWRHolder.getMCDMObjective(), phyWSMLWRCWRHolder.getMethod(), String.valueOf(phyWSMLWRCWRHolder.getK())).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWSMLWRCWRHolder.getTopologyName() + "_" + phyWSMLWRCWRHolder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYWSMCWRResultToFile(PHYWSMCWRHolder phyWSMCWRHolder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWSMCWRHolder.getSolver(), phyWSMCWRHolder.getMethod(), phyWSMCWRHolder.getAlgorithm(), phyWSMCWRHolder.getRandomizationCWR(), phyWSMCWRHolder.getWSMNormalization(), phyWSMCWRHolder.getMCDMObjective(), String.valueOf(phyWSMCWRHolder.getK()), phyWSMCWRHolder.getTopologyName() + "_" + phyWSMCWRHolder.getApplicationName()).toString();

        mainFolderOutputLocation = Paths.get(mainFolderOutputLocation, phyWSMCWRHolder.getTopologyName() + "_" + phyWSMCWRHolder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWSMCWRHolder.getSolver(), phyWSMCWRHolder.getMethod(), phyWSMCWRHolder.getAlgorithm(), phyWSMCWRHolder.getRandomizationCWR(), phyWSMCWRHolder.getWSMNormalization(), phyWSMCWRHolder.getMCDMObjective(), String.valueOf(phyWSMCWRHolder.getK())).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWSMCWRHolder.getTopologyName() + "_" + phyWSMCWRHolder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePHYWPMCWRv1ResultToFile(PHYWPMCWRv1Holder phyWPMCWRv1Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getSolver(), phyWPMCWRv1Holder.getMethod(), phyWPMCWRv1Holder.getAlgorithm(), phyWPMCWRv1Holder.getRandomizationCWR(), phyWPMCWRv1Holder.getMCDMObjective(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getWPMVersion(), phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName()).toString();

        mainFolderOutputLocation = Paths.get(mainFolderOutputLocation, phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWPMCWRv1Holder.getSolver(), phyWPMCWRv1Holder.getMethod(), phyWPMCWRv1Holder.getAlgorithm(), phyWPMCWRv1Holder.getRandomizationCWR(), phyWPMCWRv1Holder.getMCDMObjective(), String.valueOf(phyWPMCWRv1Holder.getK()), phyWPMCWRv1Holder.getWPMVersion()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWPMCWRv1Holder.getTopologyName() + "_" + phyWPMCWRv1Holder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePHYWPMCWRv2ResultToFile(PHYWPMCWRv2Holder phyWPMCWRv2Holder) {
        String mainFolderOutputLocation = Paths.get("outputs", phyWPMCWRv2Holder.getSolver(), phyWPMCWRv2Holder.getMethod(), phyWPMCWRv2Holder.getAlgorithm(), phyWPMCWRv2Holder.getRandomizationCWR(), phyWPMCWRv2Holder.getMCDMObjective(), String.valueOf(phyWPMCWRv2Holder.getK()), phyWPMCWRv2Holder.getWPMVersion(), phyWPMCWRv2Holder.getWPMValueType(), phyWPMCWRv2Holder.getTopologyName() + "_" + phyWPMCWRv2Holder.getApplicationName()).toString();

        mainFolderOutputLocation = Paths.get(mainFolderOutputLocation, phyWPMCWRv2Holder.getTopologyName() + "_" + phyWPMCWRv2Holder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyWPMCWRv2Holder.getSolver(), phyWPMCWRv2Holder.getMethod(), phyWPMCWRv2Holder.getAlgorithm(), phyWPMCWRv2Holder.getRandomizationCWR(), phyWPMCWRv2Holder.getMCDMObjective(), String.valueOf(phyWPMCWRv2Holder.getK()), phyWPMCWRv2Holder.getWPMVersion(), phyWPMCWRv2Holder.getWPMValueType()).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyWPMCWRv2Holder.getTopologyName() + "_" + phyWPMCWRv2Holder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writePHYResultToFile(PHYHolder phyHolder) {
        String mainFolderOutputLocation = Paths.get(".outputs", phyHolder.getSolver(), phyHolder.getAlgorithm(), phyHolder.getMethod(), String.valueOf(phyHolder.getK()), phyHolder.getTopologyName() + "_" + phyHolder.getApplicationName()).toString();

        new File(mainFolderOutputLocation).mkdirs();

        String resultFileOutputLocation = Paths.get("outputs", phyHolder.getSolver(), phyHolder.getAlgorithm(), phyHolder.getMethod(), String.valueOf(phyHolder.getK())).toString();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultFileOutputLocation, "Results.txt").toString(), true));
            writer.write(phyHolder.getTopologyName() + "_" + phyHolder.getApplicationName() + "\n");
            writer.write("cost = " + getTotalCost() + ", o1 = " + obj1 + ", o2 = " + obj2 + ", o3 = " + obj3 + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
