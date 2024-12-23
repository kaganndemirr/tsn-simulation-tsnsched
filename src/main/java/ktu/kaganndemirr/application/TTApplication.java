package ktu.kaganndemirr.application;

import ktu.kaganndemirr.architecture.EndSystem;

import java.util.List;
import java.util.Objects;

public class TTApplication extends Application {
    public TTApplication(String name, int pcp, String type, double deadline, double frameSizeByte, int numberOfFrames, EndSystem source, EndSystem target, double CMI, double messageSizeMbps, ExplicitPath explicitPath) {
        super(name, pcp, type, deadline, frameSizeByte, numberOfFrames, source, target, explicitPath, CMI, messageSizeMbps);
    }

    public String toString() {
        return String.format("App Name: %s, PCP: %d, Type: %s, Deadline: %f(us), Frame Size: %f (%f x %d) B, Message Size: %f (Mbps),  Source: %s, Targets: %s, CMI: %f(us)", name, pcp, type, deadline, (frameSizeByte * numberOfFrames), frameSizeByte, numberOfFrames, messageSizeMbps, source, target, CMI);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (obj == null || getClass() != obj.getClass()) {
            result = false;
        } else {
            TTApplication objApp = (TTApplication) obj;
            result = Objects.equals(name, objApp.getName()) && (source == objApp.getSource()) && (target == objApp.getTarget());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name) + Objects.hash(source) + Objects.hash(target);
    }
}
