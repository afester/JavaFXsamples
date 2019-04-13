package afester.javafx.examples.board;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Board {

    
    private double width = 160.0;
    private double height = 100.0;
    
    private Map<String, Part> parts = new HashMap<>();
    private List<Net> nets = new ArrayList<>();    

    public void addDevice(Part pkg) {
        parts.put(pkg.getName(), pkg);
    }

    public Map<String, Part> getParts() {
        return parts;
    }

    public List<Net> getNets() {
        return nets;
    }

//    public Double getWidth() {
//        return width;
//    }
//
//    public Double getHeight() {
//        return height;
//    }

    public Part getDevice(String partName) {
        return parts.get(partName);
    }

    public void addNet(Net net) {
        nets.add(net);
    }

    private class IntVal {
        public int val = 0;
    }
    

    public void save(File result) {
        System.err.println("Saving " + result.getAbsolutePath());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dbFactory.setValidating(false);
        try {
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // The root node represents the complete board
            Element rootNode = doc.createElement("breadboard");
            rootNode.setAttribute("schematic", "small.sch");
//            rootNode.setAttribute("width",  Double.toString(getWidth()));
//            rootNode.setAttribute("height", Double.toString(getHeight()));
            doc.appendChild(rootNode);

            IntVal junctionId = new IntVal();
            parts.forEach( (k, v) -> {
                Element partNode = doc.createElement("part");
                partNode.setAttribute("name", v.getName());
                partNode.setAttribute("x", Double.toString(v.getLayoutX()));
                partNode.setAttribute("y", Double.toString(v.getLayoutY()));
                partNode.setAttribute("rotation", Double.toString(v.getRotate()));

                for (PartShape ps : v.getShapes()) {
                    Node shapeNode = ps.getXML(doc);
                    partNode.appendChild(shapeNode);
                }

                for (Pad ps : v.getPads()) {
                    ps.setId(junctionId.val++);
                    Node padNode = ps.getXML(doc);
                    partNode.appendChild(padNode);
                }

                rootNode.appendChild(partNode);
            });

            nets.forEach(n -> {
                Element netNode = doc.createElement("net");
                netNode.setAttribute("name", n.getName());

                // Junctions are just points which connect traces WITHIN a net
                for (Junction j : n.getJunctions()) {
                    j.setId(junctionId.val++);
                    Node junctionNode = j.getXML(doc);
                    netNode.appendChild(junctionNode);
                }

                // Traces are direct lines which connect two Junctions and/or Pads
                for (Trace t : n.getTraces()) {
                    Node traceNode = t.getXML(doc);
                    netNode.appendChild(traceNode);
                }

                rootNode.appendChild(netNode);
            });

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            FileOutputStream fos = new FileOutputStream(result);
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            // Writer out = new FileWriter(result);
            tf.transform(new DOMSource(doc), new StreamResult(out));
            out.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.err.println("Saved " + result.getAbsolutePath());
    }

    public void load(File file) {
        System.err.println("Loading " + file.getAbsolutePath());

        XPath xPath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        try {
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            
            Element breadboardNode = (Element) xPath.evaluate("/breadboard", doc, XPathConstants.NODE);
            //String widthAttr = breadboardNode.getAttribute("width");
            //String heightAttr = breadboardNode.getAttribute("height");
            String schematic = breadboardNode.getAttribute("schematic");

            //width = Double.parseDouble(widthAttr);
            //height = Double.parseDouble(heightAttr);

            Map<String, Junction> junctions = new HashMap<>();
            Map<String, Pad> pads = new HashMap<>();

            NodeList partNodes = (NodeList) xPath.evaluate("part", breadboardNode, XPathConstants.NODESET);
            for (int i = 0; i < partNodes.getLength(); ++i) {
                Element partNode = (Element) partNodes.item(i);
                String partName = partNode.getAttribute("name");
                Double rotation = Double.parseDouble(partNode.getAttribute("rotation"));
                Double xpos = Double.parseDouble(partNode.getAttribute("x"));
                Double ypos = Double.parseDouble(partNode.getAttribute("y"));

                Part part = new Part(partName);
                part.setLayoutX(xpos);
                part.setLayoutY(ypos);
                part.setRotate(rotation);

                NodeList padNodes = (NodeList) xPath.evaluate("./pad", partNode, XPathConstants.NODESET);
                for (int j = 0; j < padNodes.getLength(); ++j) {
                    Element padNode = (Element) padNodes.item(j);
                    // String padName = padNode.getAttribute("name");
                    String padId = padNode.getAttribute("id");
                    Double padX = Double.parseDouble(padNode.getAttribute("x"));
                    Double padY = Double.parseDouble(padNode.getAttribute("y"));

                    Pad junction = new Pad(part, padId /*padName*/, /* "", "", */ padX, padY);
                    pads.put(padId, junction);
                    part.addPad(junction, padId);
                }

                NodeList lineNodes = (NodeList) xPath.evaluate("./line", partNode, XPathConstants.NODESET);
                for (int j = 0; j < lineNodes.getLength(); ++j) {
                    Element lineNode = (Element) lineNodes.item(j);
                    Double x1 = Double.parseDouble(lineNode.getAttribute("x1"));
                    Double y1 = Double.parseDouble(lineNode.getAttribute("y1"));
                    Double x2 = Double.parseDouble(lineNode.getAttribute("x2"));
                    Double y2 = Double.parseDouble(lineNode.getAttribute("y2"));
                    Double width = Double.parseDouble(lineNode.getAttribute("width"));

                    part.addShape(new PartLine(x1, y1, x2, y2, width));
                }

                NodeList textNodes = (NodeList) xPath.evaluate("./text", partNode, XPathConstants.NODESET);
                for (int j = 0; j < textNodes.getLength(); ++j) {
                    Element textNode = (Element) textNodes.item(j);
                    Double x = Double.parseDouble(textNode.getAttribute("x"));
                    Double y = Double.parseDouble(textNode.getAttribute("y"));
                    Double size = Double.parseDouble(textNode.getAttribute("size"));
                    // String layer = textNode.getAttribute("layer");
                    String text = textNode.getTextContent();
//                    if (text.isEmpty()) {
//                        text = "???";
//                    }
                    part.addShape(new PartText(x, y, text, size));
                }

                System.err.printf("Part: %s\n", part);
                addDevice(part);
                part.createNode();
            }

            NodeList netNodes = (NodeList) xPath.evaluate("net", breadboardNode, XPathConstants.NODESET);
            for (int i = 0; i < netNodes.getLength(); ++i) {
                Element netNode = (Element) netNodes.item(i);
                String netName = netNode.getAttribute("name");
                Net net = new Net(netName); // , netPads);

                // List<Pad> netPads = new ArrayList<>();
                NodeList junctionNodes = (NodeList) xPath.evaluate("./junction", netNode, XPathConstants.NODESET);
                for (int j = 0; j < junctionNodes.getLength(); ++j) {
                    Element junctionNode = (Element) junctionNodes.item(j);
                    String junctionId = junctionNode.getAttribute("id");
                    Double xpos = Double.parseDouble(junctionNode.getAttribute("x"));
                    Double ypos = Double.parseDouble(junctionNode.getAttribute("y"));

                    Junction junction = new Junction(xpos, ypos);
                    junctions.put(junctionId, junction);
                    System.err.printf("  %s\n", junction);
                    
                    net.addJunction(junction);
                }

                NodeList airwireNodes = (NodeList) xPath.evaluate("./airwire", netNode, XPathConstants.NODESET);
                for (int j = 0; j < airwireNodes.getLength(); ++j) {
                    Element airwireNode = (Element) airwireNodes.item(j);
                    String fromId = airwireNode.getAttribute("from");
                    String toId = airwireNode.getAttribute("to");

                    Junction from = junctions.get(fromId);
                    if (from == null) from = pads.get(fromId);  // TODO: This is a bad hack!!!!
                    Junction to = junctions.get(toId);
                    if (to == null) to = pads.get(toId);        // TODO: This is a bad hack!!!!
                    System.err.printf("  AW: %s -> %s\n", from, to);

                    net.addTrace(new AirWire(from, to));
                }

                NodeList traceNodes = (NodeList) xPath.evaluate("./trace", netNode, XPathConstants.NODESET);
                for (int j = 0; j < traceNodes.getLength(); ++j) {
                    Element traceNode = (Element) traceNodes.item(j);
                    String fromId = traceNode.getAttribute("from");
                    String toId = traceNode.getAttribute("to");

                    Junction from = junctions.get(fromId);
                    if (from == null) from = pads.get(fromId);      // TODO: This is a bad hack!!!!
                    Junction to = junctions.get(toId);
                    if (to == null) to = pads.get(toId);          // TODO: This is a bad hack!!!!
                    System.err.printf("  T : %s -> %s\n", from, to);

                    net.addTrace(new Trace(from, to));
                }

                System.err.printf("Net: %s\n", net);

                addNet(net);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

}