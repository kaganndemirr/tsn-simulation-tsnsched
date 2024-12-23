package ktu.kaganndemirr.output.holders.phy;

public class PHYHolder {
    private String topologyName = null;
    private String applicationName = null;
    private String solver;
    private String method;
    private String algorithm;
    private int k;

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

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

}

