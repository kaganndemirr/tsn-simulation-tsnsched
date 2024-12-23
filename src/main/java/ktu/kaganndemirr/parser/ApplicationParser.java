package ktu.kaganndemirr.parser;

import ktu.kaganndemirr.application.*;
import ktu.kaganndemirr.architecture.Switch;
import ktu.kaganndemirr.util.Constants;
import ktu.kaganndemirr.architecture.EndSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ApplicationParser {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationParser.class.getSimpleName());

    /**
     * Parses the applications from an XML file
     *
     * @param f the XML formatted {@link File}
     * @return a List of {@link Application}s
     */
    public static List<Application> parse(File f) {

        List<Application> applications = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom;

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(f);
            Element docEle = dom.getDocumentElement();

            //Get node list of AVBApplicationElements
            NodeList nl = docEle.getElementsByTagName("Application");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element appEle = (Element) nl.item(i);
                    Application app = getApplication(appEle);
                    //Add it to the application list
                    applications.add(app);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }

        return applications;
    }

    
    private static Application getApplication(Element appEle) {
        String name = appEle.getAttribute("name");

        int pcp = parsePCP(appEle);

        double deadline = parseDeadline(appEle);

        //Parse PayloadSize
        int frameSizeByte = parseFrameSizeByte(appEle);

        //Parse NoOfFrames
        int numberOfFrames = parseNumberOfFrames(appEle);

        //Parse Source
        EndSystem source = parseSource(appEle);

        //Parse Destinations
        EndSystem target = parseTarget(appEle);

        ExplicitPath explicitPath = parseExplicitPath(appEle);

        double CMI = parseCMI(appEle);
            double messageSizeMbps = getMessageSizeMbps(frameSizeByte, numberOfFrames, CMI);

        if(pcp == Constants.TTPCP){
            return new TTApplication(name, pcp, Constants.TTTYPE, deadline, frameSizeByte, numberOfFrames, source, target, CMI, messageSizeMbps, explicitPath);
        }
        else{
            return new SRTApplication(name, pcp, Constants.TTTYPE, deadline, frameSizeByte, numberOfFrames, source, target, CMI, messageSizeMbps, explicitPath);
        }

    }

    private static int parsePCP(Element ele) {
        return Integer.parseInt(ele.getElementsByTagName("PCP").item(0).getFirstChild().getNodeValue());
    }

    private static int parseDeadline(Element ele) {
        return Integer.parseInt(ele.getElementsByTagName("Deadline").item(0).getFirstChild().getNodeValue());
    }

    private static int parseFrameSizeByte(Element ele) {
        return Integer.parseInt(ele.getElementsByTagName("FrameSize").item(0).getFirstChild().getNodeValue());
    }

    private static int parseNumberOfFrames(Element ele) {
        return Integer.parseInt(ele.getElementsByTagName("NumberOfFrames").item(0).getFirstChild().getNodeValue());
    }

    private static EndSystem parseSource(Element ele) {
        return new EndSystem(((Element) ele.getElementsByTagName("Source").item(0)).getAttribute("name"));
    }

    private static EndSystem parseTarget(Element ele) {
        return new EndSystem(((Element) ele.getElementsByTagName("Target").item(0)).getAttribute("name"));
    }

    private static double parseCMI(Element ele) {
        return Integer.parseInt(ele.getElementsByTagName("CMI").item(0).getFirstChild().getNodeValue());
    }

    private static ExplicitPath parseExplicitPath(Element ele) {
        List<Switch> explicitPath = new ArrayList<>();
        Element path = (Element) ele.getElementsByTagName("Path").item(0);

        if (path != null) {
            NodeList switchNode = path.getElementsByTagName("Switch");
            for (int u = 0; u < switchNode.getLength(); u++) {
                explicitPath.add(new Switch(((Element) switchNode.item(u)).getAttribute("name")));
            }
        }

        return new ExplicitPath(explicitPath);
    }

    private static double getMessageSizeMbps(int payloadSize, int noOfFrames, double interval){
        return (payloadSize * 8.0) * noOfFrames / interval;
    }

    public static List<Application> findApplicationsHaveNotExplicitPath(List<Application> applications){
        List<Application> localApplications = new ArrayList<>();
        for(Application app: applications){
            if(app.getExplicitPath().path().isEmpty()){
                localApplications.add(app);
            }
        }
        return localApplications;
    }
}
