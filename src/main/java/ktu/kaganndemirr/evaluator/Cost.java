package ktu.kaganndemirr.evaluator;

import ktu.kaganndemirr.output.holders.phy.*;
import ktu.kaganndemirr.route.Multicast;

import java.util.Map;

public interface Cost {

    void reset();

    double getTotalCost();

    String toDetailedString();

    Map<Multicast, Double> getaWCDMap();

    void writeShortestPathResultToFile(PHYShortestPathHolder phyShortestPathHolder);

    void writePHYWSMResultToFile(PHYWSMHolder phyWSMHolder);

    void writePHYWPMv1ResultToFile(PHYWPMv1Holder phyWPMHolder);

    void writePHYWPMv2ResultToFile(PHYWPMv2Holder phyWPMHolder);

    void writePHYWSMLWRResultToFile(PHYWSMLWRHolder phyWSMLWRHolder);

    void writePHYWPMLWRv1ResultToFile(PHYWPMLWRv1Holder phyWPMLWRv1Holder);

    void writePHYWPMLWRv2ResultToFile(PHYWPMLWRv2Holder phyWPMLWRv2Holder);

    void writePHYWSMLWRCWRResultToFile(PHYWSMLWRCWRHolder phyWSMLWRCWRHolder);

    void writePHYWSMCWRResultToFile(PHYWSMCWRHolder phyWSMCWRHolder);

    void writePHYWPMCWRv1ResultToFile(PHYWPMCWRv1Holder phyWPMCWRv1Holder);

    void writePHYWPMCWRv2ResultToFile(PHYWPMCWRv2Holder phyWPMCWRv2Holder);

    void writePHYResultToFile(PHYHolder phyHolder);

}
