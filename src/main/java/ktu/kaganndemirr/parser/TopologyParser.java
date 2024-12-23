package ktu.kaganndemirr.parser;

import ktu.kaganndemirr.architecture.Switch;
import ktu.kaganndemirr.architecture.EndSystem;
import ktu.kaganndemirr.architecture.GCLEdge;
import ktu.kaganndemirr.architecture.Node;
import ktu.kaganndemirr.util.Constants;
import weka.core.Debug.DBO;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
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
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Objects;

public class TopologyParser {

    private static final Logger logger = LoggerFactory.getLogger(TopologyParser.class.getSimpleName());

    public static AbstractBaseGraph<Node, GCLEdge> parse(File f, double idleSlope) {
        AbstractBaseGraph<Node, GCLEdge> graph = new SimpleDirectedWeightedGraph<>(GCLEdge.class);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom;

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(f);
            Element graphEle = dom.getDocumentElement();
            Map<String, Node> nodeMap = new HashMap<>();

            //Parse nodes and create graph-vertices accordingly
            NodeList nl = graphEle.getElementsByTagName("node");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String nodeName = ((Element) nl.item(i)).getAttribute("name");
                    String typeName = ((Element) nl.item(i)).getAttribute("type");
                    Node n;
                    if (Objects.equals(typeName, Constants.ENDSYSTEM)) {
                        n = new EndSystem(nodeName);
                    } else {
                        n = new Switch(nodeName);
                    }
                    nodeMap.put(nodeName, n);
                    graph.addVertex(n);
                }
            }

            //Parse edges and create graph-edges accordingly
            nl = graphEle.getElementsByTagName("edge");
            if (nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String source = ((Element) nl.item(i)).getAttribute("source");
                    source = source.toUpperCase();

                    String target = ((Element) nl.item(i)).getAttribute("target");
                    target = target.toUpperCase();

                    double weight = Double.parseDouble(((Element) nl.item(i)).getAttribute("weight"));

                    int rate = Integer.parseInt(((Element) nl.item(i)).getAttribute("rate"));

                    GCLEdge edge = new GCLEdge(rate, idleSlope);
                    graph.addEdge(nodeMap.get(source), nodeMap.get(target), edge);
                    graph.setEdgeWeight(edge, weight);
                }
            }
            nodeMap.clear();
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();

        }
        return graph;
    }
}
