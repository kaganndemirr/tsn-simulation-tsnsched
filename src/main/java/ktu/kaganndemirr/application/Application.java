package ktu.kaganndemirr.application;

import ktu.kaganndemirr.architecture.EndSystem;

import java.util.List;
import java.util.Objects;

public abstract class Application {
    protected String name;
    protected int pcp;
    protected String type;
    protected double deadline;
    protected double frameSizeByte;
    protected int numberOfFrames;
    protected EndSystem source;
    protected EndSystem target;
    protected ExplicitPath explicitPathpath;
    protected double CMI;
    protected double messageSizeMbps;

    protected Application(String name, int pcp, String type, double deadline, double frameSizeByte, int numberOfFrames, EndSystem source, EndSystem target, ExplicitPath explicitPathpath, double CMI, double messageSizeMbps) {
        this.name = name;
        this.pcp = pcp;
        this.type = type;
        this.deadline = deadline;
        this.frameSizeByte = frameSizeByte;
        this.numberOfFrames = numberOfFrames;
        this.source = source;
        this.target = target;
        this.explicitPathpath = explicitPathpath;
        this.CMI = CMI;
        this.messageSizeMbps = messageSizeMbps;
    }

    public String getName(){
        return name;
    }

    public int getPCP(){
        return pcp;
    }

    public String getType(){
        return type;
    }

    public double getDeadline(){
        return deadline;
    }

    public double getFrameSizeByte(){
        return frameSizeByte;
    }

    public int getNumberOfFrames(){
        return numberOfFrames;
    }

    public EndSystem getSource() {
        return source;
    }

    public EndSystem getTarget() {
        return target;
    }

    public ExplicitPath getExplicitPath(){
        return explicitPathpath;
    }

    public double getCMI(){
        return CMI;
    }

    public double getMessageSizeMbps(){
        return messageSizeMbps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Application a) {
            return name.equals(a.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
