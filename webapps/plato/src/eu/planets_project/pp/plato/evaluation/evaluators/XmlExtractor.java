/*******************************************************************************
 * Copyright (c) 2006-2010 Vienna University of Technology, 
 * Department of Software Technology and Interactive Systems
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0 
 *******************************************************************************/
package eu.planets_project.pp.plato.evaluation.evaluators;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.planets_project.pp.plato.model.scales.Scale;
import eu.planets_project.pp.plato.model.values.Value;
import eu.planets_project.pp.plato.util.PlatoLogger;

/**
 * This is a generic helper class that takes an XPath expression and uses
 * it to search a specified XML and extract a @link {@link Value} 
 * @author cb
 *
 */
public class XmlExtractor {
    
    private NamespaceContext namespaceContext;
    
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public void setNamespaceContext(NamespaceContext context) {
        this.namespaceContext = context;
    }

    public Value extractValue(Document xml, Scale scale, String xpath, String commentXPath) {
        try {
            Document pcdlDoc = xml;
            String text = extractTextInternal(pcdlDoc,xpath);
            Value v = scale.createValue();
            v.parse(text);
            if (commentXPath != null) {
                String comment = extractTextInternal(pcdlDoc,commentXPath);
                v.setComment(comment);
            }
            return v;
            
        } catch (Exception e) {
            PlatoLogger.getLogger(this.getClass()).error(
                    "Could not parse XML " +
                    " searching for path "+xpath+
                    ": "+e.getMessage(),
                    e);  
            return null;
        } 
    }
    
    public String extractText(Document xml, String xpath) {
        try {
            Document pcdlDoc = xml;
            String text = extractTextInternal(pcdlDoc,xpath);
            return text;
        } catch (Exception e) {
            PlatoLogger.getLogger(this.getClass()).error(
                    "Could not parse XML " +
                    " searching for path "+xpath+
                    ": "+e.getMessage(),
                    e);  
            return null;
        }         
    }
    

    public Document getDocument(InputSource xml)
            throws ParserConfigurationException, SAXException, IOException {
        // extract value via XPath
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document pcdlDoc = builder.parse(xml);
        return pcdlDoc;
    }
    
    public Value extractAttributeValue() {
        return null;
    }
    
    /**
     * very useful: {@link http://www.ibm.com/developerworks/library/x-javaxpathapi.html}
     * @param doc
     * @param path
     * @param scale
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    private  String extractTextInternal(Document doc, String path) 
    throws ParserConfigurationException, SAXException, IOException, XPathExpressionException 
    {
       
        XPathFactory factory = XPathFactory.newInstance();
        
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(namespaceContext);
        XPathExpression expr = xpath.compile(path);
        try {
            String result = (String) expr.evaluate(doc, XPathConstants.STRING);
            return result;
        } catch (Exception e) {
            PlatoLogger.getLogger(this.getClass()).error("XML extraction for path "+path+" failed: "+e.getMessage(),e);
            return "XML extraction for path "+path+" failed: "+e.getMessage();
        }
    }

    public HashMap<String, String> extractValues(Document xml, String path) {
        try {
            HashMap<String, String> resultMap = new HashMap<String, String>();
            
            XPathFactory factory = XPathFactory.newInstance();
            
            XPath xpath = factory.newXPath();
            xpath.setNamespaceContext(namespaceContext);
            XPathExpression expr = xpath.compile(path);
            
            NodeList list = (NodeList) expr.evaluate(xml,  XPathConstants.NODESET);
            if (list != null) {
                for (int i = 0; i < list.getLength(); i++) {
                    Node n = list.item(i);
                    String content = n.getTextContent();
                    if (content != null) {
                        resultMap.put(n.getLocalName(), content);    
                    }
                }
            }
            return resultMap;
        } catch (Exception e) {
            PlatoLogger.getLogger(this.getClass()).error(
                    "Could not parse XML " +
                    " searching for path "+path+
                    ": "+e.getMessage(),
                    e);  
            return null;
        } 
    }
    
}
