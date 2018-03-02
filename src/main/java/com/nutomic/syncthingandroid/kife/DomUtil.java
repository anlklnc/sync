package com.nutomic.syncthingandroid.kife;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Anıl Kılınç on 5.6.2017.
 * Config.xml içinde yapılacak değişiklikler için gerekli fonksiyonları içerir.
 */

public class DomUtil {

    public static Element newElement(Document d, String name) {
        return d.createElement(name);
    }

    public static Element newElement(Document dom, String name, String text) {
        Element e = dom.createElement(name);
        setText(e, text);
        return e;
    }

    public static void addAttr(Element e, String label, String value) {
        e.setAttribute(label, value);
    }

    public static void addElement(Element parent, Element child){
        parent.appendChild(child);
    }

    public static void setText(Element e, String s) {
        e.setTextContent(s);
    }

    public static Element findElement(Document dom, String name, boolean depthFirst) {
        Element e = null;
        Element parent = dom.getDocumentElement();
        if(depthFirst) {
            e = (Element)parent.getElementsByTagName(name).item(0);
        }else {
            NodeList nl = parent.getChildNodes();
            for(int i=0; i<nl.getLength(); i++) {
                Node n = nl.item(i);
                if(n.getNodeName().equals(name)) {
                    e = (Element)n;
                    break;
                }
            }
        }
        return e;
    }

    public static ArrayList<Element> getElements(Document dom, String name) {
        Element parent = dom.getDocumentElement();
        NodeList list = parent.getElementsByTagName(name);
        ArrayList<Element> result = new ArrayList<>(list.getLength());
        for(int i=0; i<list.getLength(); i++) {
            Element e = (Element)list.item(i);
            result.add(e);
        }
        return result;
    }

    public static ArrayList<Element> getChildElements(Element root, String name) {

        ArrayList<Element> result = new ArrayList<>();

        NodeList nl = root.getChildNodes();
        for (int i=0; i<nl.getLength(); i++){
            Node n = nl.item(i);
            if(n.getNodeName().equals(name)) {
                result.add((Element)n);
            }
        }
        return result;
    }

    public static void addDeviceElement(Document dom, KifeDevice device) {
        Element e = newElement(dom, "device");
        addAttr(e, "id", device.deviceID);
        addAttr(e, "name", device.name);
        addAttr(e, "compression", device.compression);
        //...
    }

    public static void addFolderElement() {

    }

    public static void append(Element parent, Element child) {
        parent.appendChild(child);
    }

    public static String print(Document d) {
        try {
            DOMSource domSource = new DOMSource(d);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            return "Dom Error!";
        }
    }

    public static String print(Node node) {
        DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
        return serializer.writeToString(node);
    }
}
