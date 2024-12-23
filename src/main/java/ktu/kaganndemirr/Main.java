package ktu.kaganndemirr;

import ktu.kaganndemirr.application.Application;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.evaluator.AVBLatencyMath;
import ktu.kaganndemirr.output.holders.phy.*;
import ktu.kaganndemirr.output.shapers.phy.*;
import ktu.kaganndemirr.parser.ApplicationParser;
import ktu.kaganndemirr.parser.TopologyParser;
import ktu.kaganndemirr.phy.shortestpath.dijkstra.Dijkstra;
import ktu.kaganndemirr.phy.yen.metaheuristic.*;
import ktu.kaganndemirr.phy.yen.heuristic.*;
import ktu.kaganndemirr.solver.*;
import ktu.kaganndemirr.util.Constants;
import org.apache.commons.cli.*;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getSimpleName());

    //region <Command line options>
    private static final String APP_ARG = "app";
    private static final String NET_ARG = "net";
    private static final String RATE_ARG = "rate";
    private static final String DEBUG_ARG = "debug";
    private static final String K_ARG = "k";
    private static final String ALGORITHM_ARG = "algorithm";
    private static final String THREAD_ARG = "thread";
    private static final String SOLVER_ARG = "solver";
    private static final String DURATION_ARG = "duration";
    private static final String WSMNORMALIZATION_ARG = "wsmnormalization";
    private static final String METHOD_ARG = "method";
    private static final String MTRNAME_ARG = "mtrname";
    private static final String KSPSWLOTHRESHOLD_ARG = "threshold";
    private static final String KSPWLOALGORITHM_ARG = "kspwlo_algorithm";
    private static final String WAVB_ARG = "wAVB";
    private static final String WTT_ARG = "wTT";
    private static final String WLENGTH_ARG = "wLength";
    private static final String WUTIL_ARG = "wUtil";
    private static final String COBJECTIVE_ARG = "mcdmObjective";
    private static final String RANDOMIZATIONLWR_ARG = "randomizationLWR";
    private static final String RANDOMIZATIONCWR_ARG = "randomizationCWR";
    private static final String MHTYPE_ARG = "mhtype";
    private static final String MAXITER_ARG = "maxiter";
    private static final String WPMVERSION_ARG = "wpmversion";
    private static final String WPMVALUETYPE_ARG = "wpmvaluetype";
    private static final String IDLESLOPE_ARG = "idleslope";
    //endregion


    public static void main(String[] args) {
        //region <Default Values>

        //Default value of K
        int k = 50;
        //Default value of rate (mbps)
        int rate = 1000;
        //Metaheuristic Algorithm Duration
        int duration = 60;
        //Default thread number
        int threadNumber = Runtime.getRuntime().availableProcessors();
        //Default Solver
        String solver = "phy";
        //Default Algorithm
        String algorithm = "GRASP";
        //Default Method
        String method = "yen";
        //Default MTR Version
        String mtrname = "v1";
        //Default wsmNormalization
        String wsmNormalization = "max";
        //Default kspwlo threshold
        String kspwloThreshold = "0";
        //Default kspwlo algorithm
        String kspwloAlgorithm = "esx-c";
        //Default wAVB
        String wAVB = "0";
        //Default wTT
        String wTT = "0";
        //Default wLength
        String wLength = "0";
        //Default wUtil
        String wUtil = "0";
        //Default mcdmObjective
        String mcdmObjective = "avbttlength";
        //Default randomization
        String randomizationLWR = "randomizeheadsortails";
        String randomizationCWR = "randomizeweightrandom";
        //Default MH Type
        String mhtype = "duration";
        //Default Max Iter
        int maxIter = 50000;
        //Default wpmVersion
        String wpmVersion = "v1";
        //Default wpmValueType
        String wpmValueType = "actual";
        //Default IdleSlope
        double idleSlope = 0.75;


        //endregion

        //region <Options>
        Option architectureFile = Option.builder(NET_ARG).required().argName("file").hasArg().desc("Use given file as network").build();
        Option applicationFile = Option.builder(APP_ARG).required().argName("file").hasArg().desc("Use given file as application").build();

        Options options = new Options();
        options.addOption(applicationFile);
        options.addOption(architectureFile);
        options.addOption(K_ARG, true, "Value of K for search-space reduction (Default = 50)");
        options.addOption(RATE_ARG, true, "The rate in mbps (Default 1000 mbps)");
        options.addOption(DEBUG_ARG, false, "Debug logging");
        options.addOption(THREAD_ARG, true, "Thread Number(Default = Number of Processor Thread)");
        options.addOption(SOLVER_ARG, true, "Choose solver (Default = ksp)");
        options.addOption(ALGORITHM_ARG, true, "Choose algorithm (Default = GRASP)");
        options.addOption(METHOD_ARG, true, "Choose method (Default = yen)");
        options.addOption(MTRNAME_ARG, true, "Choose default mtr version (Default = v1)");
        options.addOption(DURATION_ARG, true, "Metaheuristic algorithm duration");
        options.addOption(WSMNORMALIZATION_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(KSPSWLOTHRESHOLD_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(KSPWLOALGORITHM_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(WAVB_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(WTT_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(WLENGTH_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(WUTIL_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(COBJECTIVE_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(RANDOMIZATIONLWR_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(RANDOMIZATIONCWR_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(MHTYPE_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(MAXITER_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(WPMVERSION_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(WPMVALUETYPE_ARG, true, "Normalization Method (Default = min-max)");
        options.addOption(IDLESLOPE_ARG, true, "Normalization Method (Default = min-max)");
        //endregion

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            //region <Set Values>

            File net = new File(line.getOptionValue(NET_ARG));
            File app = new File(line.getOptionValue(APP_ARG));

            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

            if (line.hasOption(DEBUG_ARG)) {
                System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
            }

            //Set K
            if (line.hasOption(K_ARG)) {
                k = Integer.parseInt(line.getOptionValue(K_ARG));
            }

            //Set rate
            if (line.hasOption(RATE_ARG)) {
                rate = Integer.parseInt(line.getOptionValue(RATE_ARG));
            }

            //Set Thread
            if (line.hasOption(THREAD_ARG)) {
                threadNumber = Integer.parseInt(line.getOptionValue(THREAD_ARG));
            }

            if (line.hasOption(SOLVER_ARG)) {
                solver = line.getOptionValue(SOLVER_ARG);
            }

            if (line.hasOption(ALGORITHM_ARG)) {
                algorithm = line.getOptionValue(ALGORITHM_ARG);
            }

            //Set Duration
            if (line.hasOption(DURATION_ARG)) {
                duration = Integer.parseInt(line.getOptionValue(DURATION_ARG));
            }

            if (line.hasOption(WSMNORMALIZATION_ARG)) {
                wsmNormalization = line.getOptionValue(WSMNORMALIZATION_ARG);
            }

            if (line.hasOption(METHOD_ARG)) {
                method = line.getOptionValue(METHOD_ARG);
            }

            if (line.hasOption(MTRNAME_ARG)) {
                mtrname = line.getOptionValue(MTRNAME_ARG);
            }

            if (line.hasOption(KSPSWLOTHRESHOLD_ARG)) {
                kspwloThreshold = line.getOptionValue(KSPSWLOTHRESHOLD_ARG);
            }

            if (line.hasOption(KSPWLOALGORITHM_ARG)) {
                kspwloAlgorithm = line.getOptionValue(KSPWLOALGORITHM_ARG);
            }

            if (line.hasOption(WAVB_ARG)) {
                wAVB = line.getOptionValue(WAVB_ARG);
            }

            if (line.hasOption(WTT_ARG)) {
                wTT = line.getOptionValue(WTT_ARG);
            }

            if (line.hasOption(WLENGTH_ARG)) {
                wLength = line.getOptionValue(WLENGTH_ARG);
            }

            if (line.hasOption(WUTIL_ARG)) {
                wUtil= line.getOptionValue(WUTIL_ARG);
            }

            if (line.hasOption(COBJECTIVE_ARG)) {
                mcdmObjective = line.getOptionValue(COBJECTIVE_ARG);
            }

            if (line.hasOption(RANDOMIZATIONLWR_ARG)) {
                randomizationLWR = line.getOptionValue(RANDOMIZATIONLWR_ARG);
            }

            if (line.hasOption(RANDOMIZATIONCWR_ARG)) {
                randomizationCWR = line.getOptionValue(RANDOMIZATIONCWR_ARG);
            }

            if (line.hasOption(MHTYPE_ARG)) {
                mhtype = line.getOptionValue(MHTYPE_ARG);
            }

            if (line.hasOption(MAXITER_ARG)) {
                maxIter = Integer.parseInt(line.getOptionValue(MAXITER_ARG));
            }

            if (line.hasOption(WPMVERSION_ARG)) {
                wpmVersion = line.getOptionValue(WPMVERSION_ARG);
            }

            if (line.hasOption(WPMVALUETYPE_ARG)) {
                wpmValueType = line.getOptionValue(WPMVALUETYPE_ARG);
            }

            if (line.hasOption(IDLESLOPE_ARG)) {
                idleSlope = Double.parseDouble(line.getOptionValue(IDLESLOPE_ARG));
            }


            //endregion

            //Parse Topology
            logger.info("Parsing Topology from {}", net.getName());
            Graph<Node, GCLEdge> graph = TopologyParser.parse(net, idleSlope);
            logger.info("Topology parsed!");

            //endregion

            //Parse Applications
            logger.info("Parsing application set from {}", app.getName());
            List<Application> apps = ApplicationParser.parse(app, rate);
            logger.info("Applications parsed! ");

            //region <Parse Topology Name>
            String applicationName = null;
            String topologyName = null;

            //region Parse BRITE Topology Name
            if (countUnderscores(net.getName()) > 0) {
                Pattern patternTopology = Pattern.compile(".*?_(.*?)_[^_]*$");
                Matcher matcherTopology = patternTopology.matcher(net.getName());
                if (matcherTopology.find()) {
                    topologyName = matcherTopology.group(1);
                }

                Pattern patternApplication = Pattern.compile(".*_([^_]*_\\d+)$");
                Matcher matcherApplication = patternApplication.matcher(app.getName());
                if (matcherApplication.find()) {
                    applicationName = matcherApplication.group(1);
                }

            }
            //endregion

            //region Parse Classic Topology Name
            else {
                Pattern patternTopology = Pattern.compile("(.+?)(?=\\.xml)");
                Matcher matcherTopology = patternTopology.matcher(net.getName());
                if (matcherTopology.find()) {
                    topologyName = matcherTopology.group(1);
                }

                Pattern patternApplication = Pattern.compile("(?<=_)(.*?)(?=\\.xml)");
                Matcher matcherApplication = patternApplication.matcher(app.getName());
                if (matcherApplication.find()) {
                    applicationName = matcherApplication.group(1);
                }
            }
            //endregion
            //region

            //region <Set Solver>
            Solver s;

            switch (solver) {
                case "phy" -> {
                    switch (method) {
                        case "shortestpath" -> {
                            switch (algorithm) {
                                case "dijkstra" -> {
                                    s = new Dijkstra();

                                    logger.info("Solving problem using {}, {}", solver, algorithm);
                                    Solution sol = s.solveSP(graph, apps, new AVBLatencyMath());

                                    PHYShortestPathHolder phyShortestPathHolder = new PHYShortestPathHolder();

                                    phyShortestPathHolder.setTopologyName(topologyName);
                                    phyShortestPathHolder.setApplicationName(applicationName);
                                    phyShortestPathHolder.setMethod(method);
                                    phyShortestPathHolder.setSolver(solver);
                                    phyShortestPathHolder.setAlgorithm(algorithm);

                                    sol.getCost().writeShortestPathResultToFile(phyShortestPathHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found ");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            ShortestPathOutputShaper oS = new ShortestPathOutputShaper(phyShortestPathHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());
                                        }
                                    }
                                }
                            }

                        }
                        case "yen" -> {
                            switch (algorithm) {
                                case "WSMD" -> {
                                    s = new WSMD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, WSMNormalization: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wsmNormalization);
                                    Solution sol = s.solveHWSM(graph, apps, new AVBLatencyMath(), wsmNormalization, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective);

                                    PHYWSMHolder phyWSMHolder = new PHYWSMHolder();
                                    phyWSMHolder.setTopologyName(topologyName);
                                    phyWSMHolder.setApplicationName(applicationName);
                                    phyWSMHolder.setSolver(solver);
                                    phyWSMHolder.setMethod(method);
                                    phyWSMHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMHolder.setAlgorithm(algorithm);
                                    phyWSMHolder.setK(k);
                                    phyWSMHolder.setWAVB(Double.parseDouble(wAVB));
                                    phyWSMHolder.setWTT(Double.parseDouble(wTT));
                                    phyWSMHolder.setWLength(Double.parseDouble(wLength));
                                    phyWSMHolder.setWUtil(Double.parseDouble(wUtil));
                                    phyWSMHolder.setMCDMObjective(mcdmObjective);

                                    sol.getCost().writePHYWSMResultToFile(phyWSMHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMOutputShaper oS = new PHYWSMOutputShaper(phyWSMHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "WPMD" -> {
                                    s = new WPMD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveHWPM(graph, apps, new AVBLatencyMath(), Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, wpmVersion, wpmValueType);

                                    PHYWPMv1Holder phyWPMv1Holder = new PHYWPMv1Holder();
                                    PHYWPMv2Holder phyWPMv2Holder = new PHYWPMv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMv1Holder.setTopologyName(topologyName);
                                        phyWPMv1Holder.setApplicationName(applicationName);
                                        phyWPMv1Holder.setSolver(solver);
                                        phyWPMv1Holder.setMethod(method);
                                        phyWPMv1Holder.setAlgorithm(algorithm);
                                        phyWPMv1Holder.setK(k);
                                        phyWPMv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        phyWPMv2Holder.setTopologyName(topologyName);
                                        phyWPMv2Holder.setApplicationName(applicationName);
                                        phyWPMv2Holder.setSolver(solver);
                                        phyWPMv2Holder.setMethod(method);
                                        phyWPMv2Holder.setAlgorithm(algorithm);
                                        phyWPMv2Holder.setK(k);
                                        phyWPMv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
                                    }


                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMLWRD" -> {
                                    s = new GRASPWSMLWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, WSMNormalization: {}, RandomizationLWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, wsmNormalization, randomizationLWR);
                                    Solution sol = s.solveMWSMLWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, wsmNormalization, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter);

                                    PHYWSMLWRHolder phyWSMLWRHolder = new PHYWSMLWRHolder();
                                    phyWSMLWRHolder.setTopologyName(topologyName);
                                    phyWSMLWRHolder.setApplicationName(applicationName);
                                    phyWSMLWRHolder.setSolver(solver);
                                    phyWSMLWRHolder.setMethod(method);
                                    phyWSMLWRHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMLWRHolder.setAlgorithm(algorithm);
                                    phyWSMLWRHolder.setK(k);
                                    phyWSMLWRHolder.setWAVB(Double.parseDouble(wAVB));
                                    phyWSMLWRHolder.setWTT(Double.parseDouble(wTT));
                                    phyWSMLWRHolder.setWLength(Double.parseDouble(wLength));
                                    phyWSMLWRHolder.setWUtil(Double.parseDouble(wUtil));
                                    phyWSMLWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMLWRHolder.setRandomizationLWR(randomizationLWR);

                                    sol.getCost().writePHYWSMLWRResultToFile(phyWSMLWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMLWROutputShaper oS = new PHYWSMLWROutputShaper(phyWSMLWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "GRASPWPMLWRD" -> {
                                    s = new GRASPWPMLWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMLWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRv1Holder.setSolver(solver);
                                        phyWPMLWRv1Holder.setMethod(method);
                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv1Holder.setK(k);
                                        phyWPMLWRv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv1Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);

                                    }else {
                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRv2Holder.setSolver(solver);
                                        phyWPMLWRv2Holder.setMethod(method);
                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv2Holder.setK(k);
                                        phyWPMLWRv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv2Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
                                    }

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }
                                            else {
                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMCWRD" -> {
                                    s = new GRASPWSMCWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR);
                                    Solution sol = s.solveMWSMCWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, wsmNormalization, mcdmObjective, randomizationCWR, rate, mhtype, maxIter);

                                    PHYWSMCWRHolder phyWSMCWRHolder = new PHYWSMCWRHolder();

                                    phyWSMCWRHolder.setTopologyName(topologyName);
                                    phyWSMCWRHolder.setApplicationName(applicationName);
                                    phyWSMCWRHolder.setSolver(solver);
                                    phyWSMCWRHolder.setMethod(method);
                                    phyWSMCWRHolder.setAlgorithm(algorithm);
                                    phyWSMCWRHolder.setK(k);
                                    phyWSMCWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMCWRHolder.setRandomizationCWR(randomizationCWR);
                                    phyWSMCWRHolder.setWSMNormalization(wsmNormalization);

                                    sol.getCost().writePHYWSMCWRResultToFile(phyWSMCWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMCWROutputShaper oS = new PHYWSMCWROutputShaper(phyWSMCWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "GRASPWPMCWRD" -> {
                                    s = new GRASPWPMCWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMCWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, mcdmObjective, randomizationCWR, rate, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){

                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMCWRv1Holder.setSolver(solver);
                                        phyWPMCWRv1Holder.setMethod(method);
                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv1Holder.setK(k);
                                        phyWPMCWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv1Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);
                                    }else {
                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMCWRv2Holder.setSolver(solver);
                                        phyWPMCWRv2Holder.setMethod(method);
                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv2Holder.setK(k);
                                        phyWPMCWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv2Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
                                    }



                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }else {
                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMLWRCWRD" -> {
                                    s = new GRASPWSMLWRCWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, mcdmObjective: {}, RandomizationLWR: {}, RandomizationCWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationLWR, randomizationCWR);
                                    Solution sol = s.solveMWSMLWRCWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, wsmNormalization, mcdmObjective, randomizationLWR, rate, mhtype, maxIter, randomizationCWR);

                                    PHYWSMLWRCWRHolder phyWSMLWRCWRHolder = new PHYWSMLWRCWRHolder();
                                    phyWSMLWRCWRHolder.setTopologyName(topologyName);
                                    phyWSMLWRCWRHolder.setApplicationName(applicationName);
                                    phyWSMLWRCWRHolder.setSolver(solver);
                                    phyWSMLWRCWRHolder.setMethod(method);
                                    phyWSMLWRCWRHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMLWRCWRHolder.setAlgorithm(algorithm);
                                    phyWSMLWRCWRHolder.setK(k);
                                    phyWSMLWRCWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMLWRCWRHolder.setRandomizationLWR(randomizationLWR);
                                    phyWSMLWRCWRHolder.setRandomizationCWR(randomizationCWR);

                                    sol.getCost().writePHYWSMLWRCWRResultToFile(phyWSMLWRCWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMLWRCWROutputShaper oS = new PHYWSMLWRCWROutputShaper(phyWSMLWRCWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "U" -> {
                                    s = new U(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}", solver, method, algorithm, k);
                                    Solution sol = s.solveHU(graph, apps, new AVBLatencyMath(), rate);

                                    PHYHolder phyHolder = new PHYHolder();

                                    phyHolder.setTopologyName(topologyName);
                                    phyHolder.setApplicationName(applicationName);
                                    phyHolder.setSolver(solver);
                                    phyHolder.setMethod(method);
                                    phyHolder.setAlgorithm(algorithm);
                                    phyHolder.setK(k);

                                    sol.getCost().writePHYResultToFile(phyHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYOutputShaper oS = new PHYOutputShaper(phyHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + solver + ", " + method + ", " + algorithm + " unrecognized!");
                            }
                        }
                        case "pp" -> {
                            switch (algorithm) {
                                case "WSMD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.heuristic.WSMD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil);
                                    Solution sol = s.solveHWSM(graph, apps, new AVBLatencyMath(), wsmNormalization, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective);

                                    PHYWSMHolder phyWSMHolder = new PHYWSMHolder();
                                    phyWSMHolder.setTopologyName(topologyName);
                                    phyWSMHolder.setApplicationName(applicationName);
                                    phyWSMHolder.setSolver(solver);
                                    phyWSMHolder.setMethod(method);
                                    phyWSMHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMHolder.setAlgorithm(algorithm);
                                    phyWSMHolder.setK(k);
                                    phyWSMHolder.setWAVB(Double.parseDouble(wAVB));
                                    phyWSMHolder.setWTT(Double.parseDouble(wTT));
                                    phyWSMHolder.setWLength(Double.parseDouble(wLength));
                                    phyWSMHolder.setWUtil(Double.parseDouble(wUtil));
                                    phyWSMHolder.setMCDMObjective(mcdmObjective);

                                    sol.getCost().writePHYWSMResultToFile(phyWSMHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMOutputShaper oS = new PHYWSMOutputShaper(phyWSMHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "WPMD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.heuristic.WPMD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, mcdmObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, mcdmObjective, wAVB, wTT, wLength, wUtil, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveHWPM(graph, apps, new AVBLatencyMath(), Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, wpmVersion, wpmValueType);

                                    PHYWPMv1Holder phyWPMv1Holder = new PHYWPMv1Holder();
                                    PHYWPMv2Holder phyWPMv2Holder = new PHYWPMv2Holder();
                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMv1Holder.setTopologyName(topologyName);
                                        phyWPMv1Holder.setApplicationName(applicationName);
                                        phyWPMv1Holder.setSolver(solver);
                                        phyWPMv1Holder.setMethod(method);
                                        phyWPMv1Holder.setAlgorithm(algorithm);
                                        phyWPMv1Holder.setK(k);
                                        phyWPMv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMv1ResultToFile(phyWPMv1Holder);
                                    } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                        phyWPMv2Holder.setTopologyName(topologyName);
                                        phyWPMv2Holder.setApplicationName(applicationName);
                                        phyWPMv2Holder.setSolver(solver);
                                        phyWPMv2Holder.setMethod(method);
                                        phyWPMv2Holder.setAlgorithm(algorithm);
                                        phyWPMv2Holder.setK(k);
                                        phyWPMv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMv2ResultToFile(phyWPMv2Holder);
                                    }


                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMv1OutputShaper oS = new PHYWPMv1OutputShaper(phyWPMv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            } else if (Objects.equals(wpmVersion, Constants.WPMVERSIONV2)) {
                                                PHYWPMv2OutputShaper oS = new PHYWPMv2OutputShaper(phyWPMv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMLWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWSMLWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR);
                                    Solution sol = s.solveMWSMLWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, wsmNormalization, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter);

                                    PHYWSMLWRHolder phyWSMLWRHolder = new PHYWSMLWRHolder();
                                    phyWSMLWRHolder.setTopologyName(topologyName);
                                    phyWSMLWRHolder.setApplicationName(applicationName);
                                    phyWSMLWRHolder.setSolver(solver);
                                    phyWSMLWRHolder.setMethod(method);
                                    phyWSMLWRHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMLWRHolder.setAlgorithm(algorithm);
                                    phyWSMLWRHolder.setK(k);
                                    phyWSMLWRHolder.setWAVB(Double.parseDouble(wAVB));
                                    phyWSMLWRHolder.setWTT(Double.parseDouble(wTT));
                                    phyWSMLWRHolder.setWLength(Double.parseDouble(wLength));
                                    phyWSMLWRHolder.setWUtil(Double.parseDouble(wUtil));
                                    phyWSMLWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMLWRHolder.setRandomizationLWR(randomizationLWR);

                                    sol.getCost().writePHYWSMLWRResultToFile(phyWSMLWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMLWROutputShaper oS = new PHYWSMLWROutputShaper(phyWSMLWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "GRASPWPMLWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWPMLWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, wAVB: {}, wTT: {}, wLength: {}, wUtil: {}, RandomizationLWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, wAVB, wTT, wLength, wUtil, randomizationLWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMLWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, Double.parseDouble(wAVB), Double.parseDouble(wTT), Double.parseDouble(wLength), Double.parseDouble(wUtil), rate, mcdmObjective, randomizationLWR, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMLWRv1Holder phyWPMLWRv1Holder = new PHYWPMLWRv1Holder();
                                    PHYWPMLWRv2Holder phyWPMLWRv2Holder = new PHYWPMLWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        phyWPMLWRv1Holder.setTopologyName(topologyName);
                                        phyWPMLWRv1Holder.setApplicationName(applicationName);
                                        phyWPMLWRv1Holder.setSolver(solver);
                                        phyWPMLWRv1Holder.setMethod(method);
                                        phyWPMLWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv1Holder.setK(k);
                                        phyWPMLWRv1Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv1Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv1Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv1Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv1Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMLWRv1ResultToFile(phyWPMLWRv1Holder);

                                    }else {
                                        phyWPMLWRv2Holder.setTopologyName(topologyName);
                                        phyWPMLWRv2Holder.setApplicationName(applicationName);
                                        phyWPMLWRv2Holder.setSolver(solver);
                                        phyWPMLWRv2Holder.setMethod(method);
                                        phyWPMLWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMLWRv2Holder.setK(k);
                                        phyWPMLWRv2Holder.setWAVB(Double.parseDouble(wAVB));
                                        phyWPMLWRv2Holder.setWTT(Double.parseDouble(wTT));
                                        phyWPMLWRv2Holder.setWLength(Double.parseDouble(wLength));
                                        phyWPMLWRv2Holder.setWUtil(Double.parseDouble(wUtil));
                                        phyWPMLWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMLWRv2Holder.setRandomizationLWR(randomizationLWR);
                                        phyWPMLWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMLWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMLWRv2ResultToFile(phyWPMLWRv2Holder);
                                    }

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMLWRv1OutputShaper oS = new PHYWPMLWRv1OutputShaper(phyWPMLWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }
                                            else {
                                                PHYWPMLWRv2OutputShaper oS = new PHYWPMLWRv2OutputShaper(phyWPMLWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMCWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWSMCWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR);
                                    Solution sol = s.solveMWSMCWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, wsmNormalization, mcdmObjective, randomizationCWR, rate, mhtype, maxIter);

                                    PHYWSMCWRHolder phyWSMCWRHolder = new PHYWSMCWRHolder();

                                    phyWSMCWRHolder.setTopologyName(topologyName);
                                    phyWSMCWRHolder.setApplicationName(applicationName);
                                    phyWSMCWRHolder.setSolver(solver);
                                    phyWSMCWRHolder.setMethod(method);
                                    phyWSMCWRHolder.setAlgorithm(algorithm);
                                    phyWSMCWRHolder.setK(k);
                                    phyWSMCWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMCWRHolder.setRandomizationCWR(randomizationCWR);
                                    phyWSMCWRHolder.setWSMNormalization(wsmNormalization);

                                    sol.getCost().writePHYWSMCWRResultToFile(phyWSMCWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMCWROutputShaper oS = new PHYWSMCWROutputShaper(phyWSMCWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                case "GRASPWPMCWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWPMCWRD(k);

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion);
                                    }else {
                                        logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, MCDMObjective: {}, RandomizationCWR: {}, wpmVersion: {}, wpmValueType: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationCWR, wpmVersion, wpmValueType);
                                    }

                                    Solution sol = s.solveMWPMCWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, mcdmObjective, randomizationCWR, rate, mhtype, maxIter, wpmVersion, wpmValueType);

                                    PHYWPMCWRv1Holder phyWPMCWRv1Holder = new PHYWPMCWRv1Holder();
                                    PHYWPMCWRv2Holder phyWPMCWRv2Holder = new PHYWPMCWRv2Holder();

                                    if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){

                                        phyWPMCWRv1Holder.setTopologyName(topologyName);
                                        phyWPMCWRv1Holder.setApplicationName(applicationName);
                                        phyWPMCWRv1Holder.setSolver(solver);
                                        phyWPMCWRv1Holder.setMethod(method);
                                        phyWPMCWRv1Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv1Holder.setK(k);
                                        phyWPMCWRv1Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv1Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv1Holder.setWPMVersion(wpmVersion);

                                        sol.getCost().writePHYWPMCWRv1ResultToFile(phyWPMCWRv1Holder);
                                    }else {
                                        phyWPMCWRv2Holder.setTopologyName(topologyName);
                                        phyWPMCWRv2Holder.setApplicationName(applicationName);
                                        phyWPMCWRv2Holder.setSolver(solver);
                                        phyWPMCWRv2Holder.setMethod(method);
                                        phyWPMCWRv2Holder.setAlgorithm(algorithm);
                                        phyWPMCWRv2Holder.setK(k);
                                        phyWPMCWRv2Holder.setMCDMObjective(mcdmObjective);
                                        phyWPMCWRv2Holder.setRandomizationCWR(randomizationCWR);
                                        phyWPMCWRv2Holder.setWPMVersion(wpmVersion);
                                        phyWPMCWRv2Holder.setWPMValueType(wpmValueType);

                                        sol.getCost().writePHYWPMCWRv2ResultToFile(phyWPMCWRv2Holder);
                                    }



                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            if(Objects.equals(wpmVersion, Constants.WPMVERSIONV1)){
                                                PHYWPMCWRv1OutputShaper oS = new PHYWPMCWRv1OutputShaper(phyWPMCWRv1Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }else {
                                                PHYWPMCWRv2OutputShaper oS = new PHYWPMCWRv2OutputShaper(phyWPMCWRv2Holder);

                                                oS.writeSolutionToFile(s.getSolution());

                                                oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                                oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                                oS.writeDurationMap(s.getDurationMap());

                                                oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                                oS.writeAVBTTIntersectionToFile(s.getSolution());

                                                oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                                oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                            }


                                        }
                                    }
                                }
                                case "GRASPWSMLWRCWRD" -> {
                                    s = new ktu.kaganndemirr.phy.pp.metaheuristic.GRASPWSMLWRCWRD(k);

                                    logger.info("Solving problem using {}, {}, {}, K: {}, Dur: {}s, mcdmObjective: {}, RandomizationLWR: {}, RandomizationCWR: {}", solver, method, algorithm, k, Duration.ofSeconds(duration).toSeconds(), mcdmObjective, randomizationLWR, randomizationCWR);
                                    Solution sol = s.solveMWSMLWRCWR(graph, apps, new AVBLatencyMath(), Duration.ofSeconds(duration), threadNumber, wsmNormalization, mcdmObjective, randomizationLWR, rate, mhtype, maxIter, randomizationCWR);

                                    PHYWSMLWRCWRHolder phyWSMLWRCWRHolder = new PHYWSMLWRCWRHolder();
                                    phyWSMLWRCWRHolder.setTopologyName(topologyName);
                                    phyWSMLWRCWRHolder.setApplicationName(applicationName);
                                    phyWSMLWRCWRHolder.setSolver(solver);
                                    phyWSMLWRCWRHolder.setMethod(method);
                                    phyWSMLWRCWRHolder.setWSMNormalization(wsmNormalization);
                                    phyWSMLWRCWRHolder.setAlgorithm(algorithm);
                                    phyWSMLWRCWRHolder.setK(k);
                                    phyWSMLWRCWRHolder.setMCDMObjective(mcdmObjective);
                                    phyWSMLWRCWRHolder.setRandomizationLWR(randomizationLWR);
                                    phyWSMLWRCWRHolder.setRandomizationCWR(randomizationCWR);

                                    sol.getCost().writePHYWSMLWRCWRResultToFile(phyWSMLWRCWRHolder);

                                    if (sol.getRouting() == null || sol.getRouting().isEmpty()) {
                                        logger.info("No solution could be found!");
                                    } else {
                                        if (sol.getCost().getTotalCost() == Double.MAX_VALUE) {
                                            logger.info("Found No solution : {}", sol.getCost().toDetailedString());
                                        } else {
                                            logger.info("Found solution : {}", sol.getCost().toDetailedString());

                                            PHYWSMLWRCWROutputShaper oS = new PHYWSMLWRCWROutputShaper(phyWSMLWRCWRHolder);

                                            oS.writeSolutionToFile(s.getSolution());

                                            oS.writeWCDsToFile(sol.getCost().getaWCDMap());

                                            oS.writeLinkUtilizationsToFile(s.getSolution(), graph, rate);

                                            oS.writeDurationMap(s.getDurationMap());

                                            oS.writeAVBCandidatesTTIntersectionToFile(s.getAVBUnicastCandidates(), s.getTTUnicasts());

                                            oS.writeAVBTTIntersectionToFile(s.getSolution());

                                            oS.writeLinkAVBTTIntersectionToFile(graph, s.getSolution());

                                            oS.writeAVBCandidateRoutesToFile(s.getAVBUnicastCandidates());
                                        }
                                    }
                                }
                                default -> throw new InputMismatchException("Aborting: " + solver + ", " + method + ", " + algorithm + " unrecognized!");
                            }
                        }
                        default -> throw new InputMismatchException("Aborting: Solver " + solver + ", " + method + " unrecognized!");
                    }
                }
                default -> throw new InputMismatchException("Aborting: " + solver + " unrecognized!");
            }
            //endregion

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static int countUnderscores(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                count++;
            }
        }
        return count;
    }

}
