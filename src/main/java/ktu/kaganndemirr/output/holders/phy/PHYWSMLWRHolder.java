package ktu.kaganndemirr.output.holders.phy;

public class PHYWSMLWRHolder {
    private String topologyName = null;
    private String applicationName = null;
    private String solver;
    private String method;
    private String algorithm;
    private double wAVB;
    private double wTT;
    private double wLength;
    private double wUtil;
    private String wsmNormalization;
    private int k;
    private String mcdmObjective;
    private String randomizationLWR;

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getSolver() {
        return solver;
    }

    public void setSolver(String solver) {
        this.solver = solver;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getWSMNormalization() {
        return wsmNormalization;
    }

    public void setWSMNormalization(String wsmNormalization) {
        this.wsmNormalization = wsmNormalization;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getMCDMObjective() {
        return mcdmObjective;
    }

    public void setMCDMObjective(String mcdmObjective) {
        this.mcdmObjective = mcdmObjective;
    }

    public String getRandomizationLWR() {
        return randomizationLWR;
    }

    public void setRandomizationLWR(String randomizationCWR) {
        this.randomizationLWR = randomizationLWR;
    }

    public double getWAVB() {
        return wAVB;
    }

    public void setWAVB(double wAVB) {
        this.wAVB = wAVB;
    }

    public double getWTT() {
        return wTT;
    }

    public void setWTT(double wTT) {
        this.wTT = wTT;
    }

    public double getWLength() {
        return wLength;
    }

    public void setWLength(double wLength) {
        this.wLength = wLength;
    }

    public double getWUtil() {
        return wUtil;
    }

    public void setWUtil(double wUtil) {
        this.wUtil = wUtil;
    }

}



