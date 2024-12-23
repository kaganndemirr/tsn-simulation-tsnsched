package ktu.kaganndemirr.output.holders.phy;

public class PHYWPMCWRv1Holder {
    private String topologyName = null;
    private String applicationName = null;
    private String solver;
    private String method;
    private String algorithm;
    private int k;
    private String mcdmObjective;
    private String randomizationCWR;
    private String wpmVersion;

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

    public String getMCDMObjective() {
        return mcdmObjective;
    }

    public void setMCDMObjective(String mcdmObjective) {
        this.mcdmObjective = mcdmObjective;
    }

    public String getRandomizationCWR() {
        return randomizationCWR;
    }

    public void setRandomizationCWR(String randomizationCWR) {
        this.randomizationCWR = randomizationCWR;
    }

    public String getWPMVersion() {
        return wpmVersion;
    }

    public void setWPMVersion(String wpmVersion) {
        this.wpmVersion = wpmVersion;
    }

}




