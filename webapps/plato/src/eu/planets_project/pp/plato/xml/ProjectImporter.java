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

package eu.planets_project.pp.plato.xml;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.NodeCreateRule;
import org.apache.commons.logging.Log;
import org.dom4j.io.XMLWriter;
import org.jboss.annotation.ejb.cache.Cache;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xml.sax.SAXException;

import eu.planets_project.pp.plato.action.interfaces.IProjectImport;
import eu.planets_project.pp.plato.model.Alternative;
import eu.planets_project.pp.plato.model.AlternativesDefinition;
import eu.planets_project.pp.plato.model.CollectionProfile;
import eu.planets_project.pp.plato.model.Decision;
import eu.planets_project.pp.plato.model.DetailedExperimentInfo;
import eu.planets_project.pp.plato.model.DigitalObject;
import eu.planets_project.pp.plato.model.Evaluation;
import eu.planets_project.pp.plato.model.ExecutablePlanDefinition;
import eu.planets_project.pp.plato.model.FormatInfo;
import eu.planets_project.pp.plato.model.ImportanceWeighting;
import eu.planets_project.pp.plato.model.Parameter;
import eu.planets_project.pp.plato.model.Plan;
import eu.planets_project.pp.plato.model.PlanDefinition;
import eu.planets_project.pp.plato.model.PlanProperties;
import eu.planets_project.pp.plato.model.PlanState;
import eu.planets_project.pp.plato.model.Policy;
import eu.planets_project.pp.plato.model.PolicyNode;
import eu.planets_project.pp.plato.model.PreservationActionDefinition;
import eu.planets_project.pp.plato.model.ProjectBasis;
import eu.planets_project.pp.plato.model.RequirementsDefinition;
import eu.planets_project.pp.plato.model.ResourceDescription;
import eu.planets_project.pp.plato.model.SampleObject;
import eu.planets_project.pp.plato.model.SampleRecordsDefinition;
import eu.planets_project.pp.plato.model.Transformation;
import eu.planets_project.pp.plato.model.TriggerDefinition;
import eu.planets_project.pp.plato.model.Values;
import eu.planets_project.pp.plato.model.XcdlDescription;
import eu.planets_project.pp.plato.model.measurement.MeasurableProperty;
import eu.planets_project.pp.plato.model.measurement.Measurement;
import eu.planets_project.pp.plato.model.measurement.MeasurementInfo;
import eu.planets_project.pp.plato.model.measurement.Metric;
import eu.planets_project.pp.plato.model.scales.BooleanScale;
import eu.planets_project.pp.plato.model.scales.FloatRangeScale;
import eu.planets_project.pp.plato.model.scales.FloatScale;
import eu.planets_project.pp.plato.model.scales.FreeStringScale;
import eu.planets_project.pp.plato.model.scales.IntRangeScale;
import eu.planets_project.pp.plato.model.scales.IntegerScale;
import eu.planets_project.pp.plato.model.scales.OrdinalScale;
import eu.planets_project.pp.plato.model.scales.PositiveFloatScale;
import eu.planets_project.pp.plato.model.scales.PositiveIntegerScale;
import eu.planets_project.pp.plato.model.scales.YanScale;
import eu.planets_project.pp.plato.model.transform.NumericTransformer;
import eu.planets_project.pp.plato.model.transform.OrdinalTransformer;
import eu.planets_project.pp.plato.model.tree.Leaf;
import eu.planets_project.pp.plato.model.tree.Node;
import eu.planets_project.pp.plato.model.tree.ObjectiveTree;
import eu.planets_project.pp.plato.model.tree.PolicyTree;
import eu.planets_project.pp.plato.model.tree.TemplateTree;
import eu.planets_project.pp.plato.model.tree.TreeNode;
import eu.planets_project.pp.plato.model.values.BooleanValue;
import eu.planets_project.pp.plato.model.values.FloatRangeValue;
import eu.planets_project.pp.plato.model.values.FloatValue;
import eu.planets_project.pp.plato.model.values.FreeStringValue;
import eu.planets_project.pp.plato.model.values.IntRangeValue;
import eu.planets_project.pp.plato.model.values.IntegerValue;
import eu.planets_project.pp.plato.model.values.OrdinalValue;
import eu.planets_project.pp.plato.model.values.PositiveFloatValue;
import eu.planets_project.pp.plato.model.values.PositiveIntegerValue;
import eu.planets_project.pp.plato.model.values.YanValue;
import eu.planets_project.pp.plato.services.characterisation.jhove.JHoveAdaptor;
import eu.planets_project.pp.plato.util.FileUtils;
import eu.planets_project.pp.plato.util.OS;
import eu.planets_project.pp.plato.util.PlatoLogger;
import eu.planets_project.pp.plato.xml.plato.BinaryDataWrapper;
import eu.planets_project.pp.plato.xml.plato.ChangeLogFactory;
import eu.planets_project.pp.plato.xml.plato.ExperimentWrapper;
import eu.planets_project.pp.plato.xml.plato.GoDecisionFactory;
import eu.planets_project.pp.plato.xml.plato.NodeContentWrapper;
import eu.planets_project.pp.plato.xml.plato.OrdinalTransformerMappingFactory;
import eu.planets_project.pp.plato.xml.plato.RecommendationWrapper;
import eu.planets_project.pp.plato.xml.plato.SampleAggregationModeFactory;
import eu.planets_project.pp.plato.xml.plato.TransformationModeFactory;
import eu.planets_project.pp.plato.xml.plato.TriggerFactory;

@Stateful
@Scope(ScopeType.SESSION)
@Name("projectImport")
@Cache(org.jboss.ejb3.cache.NoPassivationCache.class)
public class ProjectImporter implements IProjectImport, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4909854324732570490L;

    private static Log log = PlatoLogger.getLogger(ProjectImporter.class);

    /**
     * Imports the given file. This function is only used for test purposes in
     * {@link #main(String[])}
     */
    public List<Plan> importProjects(String file) throws IOException,
            SAXException {
        FileInputStream in = new FileInputStream(file);
        return importProjects(in);
    }

    public int importAllProjectsFromDir(File dir) {
        int count = 0;
        for (String s : dir.list()) {
            log.debug("importing file: " + s);
            try {
                String file = dir.getAbsolutePath() + File.separator + s;
                for (Plan p : importProjects(file)) {
                    em.persist(p);
                    count++;
                }
            } catch (IOException e) {
                log.error("IMPORT FAILED: could not import file " + s, e);
            } catch (SAXException e) {
                log.error("IMPORT FAILED: could not import file " + s, e);
            }
        }
        return count;
    }

    private List<Plan> plans;

    private List<TemplateTree> templates;

    private String fileVersion;

    private List<String> appliedTransformations;

    @In
    EntityManager em;

    /**
     * can be used to set data for a specific ByteStream, for example for
     * two-pass XML import. but: NOT USED AT THE MOMENT, and probably will not
     * be needed soon!
     * 
     * @param byteStreamID
     * @param data
     *            private void insertData(int byteStreamID, byte[] data) {
     *            ByteStream b = em.find(ByteStream.class, byteStreamID); if (b
     *            == null) {
     *            log.error("INCONSISTENCY: bytestream with ID "+byteStreamID
     *            +" not found!"); return; } b.setData(data); em.persist(b);
     *            em.flush(); b = null; em.clear(); }
     */

    /**
     * Used by the digester every time a project has been parsed.
     * 
     * @param p
     */
    public void setProject(Plan p) {
        plans.add(p);
    }

    /**
     * Used by the digester every time a template has been parsed.
     * 
     * @param t
     */
    public void setTemplate(TemplateTree t) {
        templates.add(t);
    }

    @Destroy
    @Remove
    public void destroy() {

    }

    /**
     * Detect the version of the given XML representation of plans. If the
     * version of the XML representation is not up to date, necessary
     * transformations are applied.
     * 
     * @param importData
     * @return null if the transformation fails, otherwise an up to date XML
     *         representation
     * @throws IOException
     *             if parsing the XML representation fails
     * @throws SAXException
     *             if parsing the XML representation fails
     */
    public String getCurrentVersionData(InputStream in, String tempPath) throws IOException,
            SAXException {
        appliedTransformations = new ArrayList<String>();

        String originalFile = tempPath + "_original.xml";
        OutputStream out = new BufferedOutputStream(new FileOutputStream(originalFile));
        FileUtils.writeToFile(in, out);
        out.close();

        /** check for the version of the file **/
        fileVersion = "xxx";
        Digester d = new Digester();
        d.setValidating(false);
        StrictErrorHandler errorHandler = new StrictErrorHandler();
        d.setErrorHandler(errorHandler);
        d.push(this);
        // to read the version we have to support all versions:
        d.addSetProperties("*/projects", "version", "fileVersion");
        // manually migrated projects may have the file version in the node
        // projects/project
        d.addSetProperties("*/projects/project", "version", "fileVersion");
        // pre V1.3 version info was stored in the project node
        d.addSetProperties("*/project", "version", "fileVersion");
        // since V1.9 the root node is plans:
        d.addSetProperties("*/plans", "version", "fileVersion");

        InputStream inV = new FileInputStream(originalFile); 
        d.parse(inV);
        inV.close();
        /** this could be more sophisticated, but for now this is enough **/
        String version = "1.0";
        if (fileVersion != null) {
            version = fileVersion;
        }
        
        String fileTo = originalFile;
        String fileFrom = originalFile;
        
        boolean success = true;
        if ("xxx".equals(version)) {
            fileFrom = fileTo; 
            fileTo = fileFrom + "_V1.3.xml";
            /** this is an old export file, transform it to the 1.3 schema **/
            success = transformXmlData(fileFrom, fileTo, "data/xslt/Vxxx-to-V1.3.xsl");
            appliedTransformations.add("Vxxx-to-V1.3.xsl");
            version = "1.3";
        }
        if (success && "1.3".equals(version)) {
            fileFrom = fileTo; 
            fileTo = fileFrom + "_V1.9.xml";
            success = transformXmlData(fileFrom, fileTo, "data/xslt/V1.3-to-V1.9.xsl");
            appliedTransformations.add("V1.3-to-V1.9.xsl");
            version = "1.9";
        }
        // with release of Plato 2.0 and its schema ProjectExporter creates
        // documents with version 2.0
        if (success && "1.9".equals(version)) {
            version = "2.0";
        }
        if (success && "2.0".equals(version)) {
            // transform the document to version 2.1
            fileFrom = fileTo; 
            fileTo = fileFrom + "_V2.1.xml";
            success = transformXmlData(fileFrom, fileTo, "data/xslt/V2.0-to-V2.1.xsl");
            appliedTransformations.add("V2.0-to-V2.1.xsl");
            version = "2.1";
        }
        if (success && "2.1".equals(version)) {
            // transform the document to version 2.1.2
            fileFrom = fileTo; 
            fileTo = fileFrom + "_V2.1.2.xml";
            success = transformXmlData(fileFrom, fileTo, "data/xslt/V2.1-to-V2.1.2.xsl");
            appliedTransformations.add("V2.1-to-V2.1.2.xsl");
            version = "2.1.2";
        }
        if (success && "2.1.1".equals(version)) {
            // transform the document to version 2.1.2
            fileFrom = fileTo; 
            fileTo = fileFrom + "_V2.1.2.xml";
            success = transformXmlData(fileFrom, fileTo, "data/xslt/V2.1.1-to-V2.1.2.xsl");
            appliedTransformations.add("V2.1.1-to-V2.1.2.xsl");
            version = "2.1.2";
        }

        if (success && "2.1.2".equals(version)) {
            // transform the document to version 3.0.0
            fileFrom = fileTo; 
            fileTo = fileFrom + "_V3.0.0.xml";
            success = transformXmlData(fileFrom, fileTo, "data/xslt/V2.1.2-to-V3.0.0.xsl");
            appliedTransformations.add("V2.1.2-to-V3.0.0.xsl");
            version = "3.0.0";
        }

        if (success) {
            return fileTo;
        } else {
            return null;
        }
    }

    public boolean transformXmlData(String fromFile, String toFile, String xslFile)
            throws IOException {
        try {
            InputStream xsl = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslFile);
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory
                    .newTransformer(new StreamSource(xsl));// schemaUrl.openStream()

            OutputStream transformedOut = new FileOutputStream(toFile);
            Result outputTarget = new StreamResult(transformedOut);

            Source xmlSource = new StreamSource(new FileInputStream(fromFile));

            transformer.transform(xmlSource, outputTarget);
            transformedOut.close();
            return true;

        } catch (TransformerConfigurationException e) {
            log.debug(e);
        } catch (TransformerFactoryConfigurationError e) {
            log.debug(e);
        } catch (TransformerException e) {
            log.debug(e);
        } 
        return false;

    }
    
    /**
     * This method takes a template xml and stores the templates in the template library. The xml is of the form:
     * 
     * <templates>
     *   <template name="Public Fragments">
     *     <node name="Template 1" weight="0.0" single="false" lock="false">
     *         <node name="Interactive multimedia presentations" weight="0.0" single="false" lock="false">
     *      ...
     *   </template>
     * </templates>  
     *  
     *  We go through the templates //templates/template/node and store them in the respective template library, in this
     *  case 'Public Fragments'
     * 
     * @param xml
     * @throws SAXException
     * @throws IOException
     */
    public void storeTemplatesInLibrary(byte[] xml) throws SAXException, IOException {
        
        List<TemplateTree> templates = importTemplates(xml);
        /*
         * store all templates
         */
        for (TemplateTree template : templates) {
            
            // we get the template tree ("Public Templates") from the database 
            TemplateTree tdb;
            try {
                tdb = (TemplateTree) em.createQuery("select n from TemplateTree n where name = :name")
                    .setParameter("name", template.getName())
                    .getSingleResult();
            } catch(NoResultException e) {
                tdb = new TemplateTree(template.getName(), null);
            }
            
            if (tdb != null) {
            
                // we get the templates and add them to the tree
                // and store them
                for (TreeNode n : template.getRoot().getChildren()) {
                    tdb.getRoot().addChild(n);
                    em.persist(n);
                }

                em.persist(em.merge(tdb));
                em.flush();

            } 
        } 
    }
    

    /**
     * Imports the XML representation of templates.
     * 
     * @return list of read templates.
     */
    public List<TemplateTree> importTemplates(byte[] in) throws IOException,
            SAXException {

        Digester digester = new Digester();
        //digester.setValidating(true);
        StrictErrorHandler errorHandler = new StrictErrorHandler();
        digester.setErrorHandler(errorHandler);

        // At the moment XML files for template tree's are only used internally, 
        // later we will define a schema and use it also for validation

        digester.push(this);

        digester.addObjectCreate("*/template", TemplateTree.class);
        digester.addSetProperties("*/template");
        digester.addSetRoot("*/template", "setTemplate");
//        digester.addSetNext("*/template/name", "setName");
//        digester.addSetNext("*/template/owner", "setOwner");

        ProjectImporter.addTreeParsingRulesToDigester(digester);


        digester.addObjectCreate("*/template/node", Node.class);
        digester.addSetProperties("*/template/node");
        digester.addSetNext("*/template/node", "addChild");
        
        digester.setUseContextClassLoader(true);

        this.templates = new ArrayList<TemplateTree>();
        digester.parse(new ByteArrayInputStream(in));
        /*
         * for (TemplateTree t : this.templates) { log.info(t.getName() +
         * t.getOwner()); }
         */
        return this.templates;
    }

    /**
     * Imports the XML representation of plans from the given inputstream.
     * 
     * @return list of read plans
     */
    public List<Plan> importProjects(InputStream in) throws IOException, SAXException {
        String tempPath = OS.getTmpPath() + "import_xml" + System.currentTimeMillis() + "/";
        File tempDir = new File(tempPath);
        tempDir.mkdirs();
        try {
            String currentVersionFile = getCurrentVersionData(in, tempPath);
            
            if (currentVersionFile == null) {
                log.error("Failed to migrate plans.");
                return this.plans;
            }
    
            Digester digester = new Digester();
//            digester.setValidating(true);
            StrictErrorHandler errorHandler =  new StrictErrorHandler();
            digester.setErrorHandler(errorHandler);
            digester.setNamespaceAware(true);
//            digester.setSchemaLanguage("http://www.w3.org/2001/XMLSchema");
//            digester.setSchema("http://localhost:8080/plato/schema/plato-2.1.xsd");
    
            /*
             * It is NOT sufficient to use setValidating(true) and digester.setSchema("data/schemas/plato.xsd")!
             * the following parameters have to be set and a special error handler is necessary
             */
             try {
                digester.setFeature("http://xml.org/sax/features/validation", true);
                digester.setFeature("http://apache.org/xml/features/validation/schema",true);
//    
                digester.setFeature("http://xml.org/sax/features/namespaces", true);
//                digester.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
                /*
                 * And provide the relative path to the xsd-schema:
                 */
                digester.setProperty(
                        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                        "http://www.w3.org/2001/XMLSchema");
                URL platoSchema = Thread.currentThread().getContextClassLoader().getResource("data/schemas/plato-3.0.xsd");
                URL wdtSchema = Thread.currentThread().getContextClassLoader().getResource("data/schemas/planets_wdt-1.0.xsd");
                digester.setProperty(
                        "http://apache.org/xml/properties/schema/external-schemaLocation",
                        "http://www.planets-project.eu/plato " + platoSchema + " http://www.planets-project.eu/wdt " + wdtSchema);
                //http://localhost:8080/plato/schema/planets_wdt-1.0.xsd
            } catch (ParserConfigurationException e) {
                log.debug("Cannot import XML file: Configuration of parser failed.", e);
                throw new SAXException("Cannot import XML file: Configuration of parser failed.");
            }
    
            digester.push(this);
            // start with a new file
            digester.addObjectCreate("*/plan", Plan.class);
            digester.addSetProperties("*/plan");
            digester.addSetRoot("*/plan", "setProject");
    
            digester.addFactoryCreate("*/changelog", ChangeLogFactory.class);
            digester.addSetNext("*/changelog", "setChangeLog");
    
            digester.addObjectCreate("*/plan/state", PlanState.class);
            digester.addSetProperties("*/plan/state");
            digester.addSetNext("*/plan/state", "setState");
    
    
            digester.addObjectCreate("*/plan/properties", PlanProperties.class);
            digester.addSetProperties("*/plan/properties");
            digester.addSetNext("*/plan/properties", "setPlanProperties");
            digester.addCallMethod("*/plan/properties/description", "setDescription", 0);
            digester.addCallMethod("*/plan/properties/owner", "setOwner", 0);
    
            addCreateUpload(digester, "*/plan/properties/report", "setReportUpload", DigitalObject.class);
    
            digester.addObjectCreate("*/plan/basis", ProjectBasis.class);
            digester.addSetProperties("*/plan/basis");
            digester.addSetNext("*/plan/basis", "setProjectBasis");
            digester.addCallMethod("*/plan/basis/applyingPolicies", "setApplyingPolicies", 0);
            digester.addCallMethod("*/plan/basis/designatedCommunity", "setDesignatedCommunity", 0);
            digester.addCallMethod("*/plan/basis/mandate", "setMandate", 0);
    
            digester.addCallMethod("*/plan/basis/documentTypes", "setDocumentTypes", 0);
            digester.addCallMethod("*/plan/basis/identificationCode", "setIdentificationCode", 0);
            digester.addCallMethod("*/plan/basis/organisationalProcedures", "setOrganisationalProcedures", 0);
            digester.addCallMethod("*/plan/basis/planningPurpose", "setPlanningPurpose", 0);
            digester.addCallMethod("*/plan/basis/planRelations", "setPlanRelations", 0);
            digester.addCallMethod("*/plan/basis/preservationRights", "setPreservationRights", 0);
            digester.addCallMethod("*/plan/basis/referenceToAgreements", "setReferenceToAgreements", 0);
    
            // define common rule for triggers, for all */triggers/...!
            // also used for PlanDefinition
            digester.addObjectCreate("*/triggers", TriggerDefinition.class);
            digester.addSetNext("*/triggers","setTriggers");
            // every time a */triggers/trigger is encountered:
            digester.addFactoryCreate("*/triggers/trigger",TriggerFactory.class);
            digester.addSetNext("*/triggers/trigger","setTrigger");
            
            //
            // Policy Tree
            //
            digester.addObjectCreate("*/plan/basis/policyTree", PolicyTree.class);
            digester.addSetProperties("*/plan/basis/policyTree");
            digester.addSetNext("*/plan/basis/policyTree", "setPolicyTree");
    
            digester.addObjectCreate("*/plan/basis/policyTree/policyNode", PolicyNode.class);
            digester.addSetProperties("*/plan/basis/policyTree/policyNode");
            digester.addSetNext("*/plan/basis/policyTree/policyNode", "setRoot");
    
            digester.addObjectCreate("*/policyNode/policyNode", PolicyNode.class);
            digester.addSetProperties("*/policyNode/policyNode");
            digester.addSetNext("*/policyNode/policyNode", "addChild");
    
            digester.addObjectCreate("*/policyNode/policy", Policy.class);
            digester.addSetProperties("*/policyNode/policy");
            digester.addSetNext("*/policyNode/policy", "addChild");
    
            //
            // Sample Records
            //
    
            digester.addObjectCreate("*/plan/sampleRecords", SampleRecordsDefinition.class);
            digester.addSetProperties("*/plan/sampleRecords");
            digester.addSetNext("*/plan/sampleRecords", "setSampleRecordsDefinition");
    
            digester.addCallMethod("*/plan/sampleRecords/samplesDescription", "setSamplesDescription", 0);
    
            // - records
            digester.addObjectCreate("*/record", SampleObject.class);
            digester.addSetProperties("*/record");
            digester.addSetNext("*/record", "addRecord");
    
            digester.addCallMethod("*/record/description", "setDescription", 0);
            digester.addCallMethod("*/record/originalTechnicalEnvironment", "setOriginalTechnicalEnvironment", 0);
    
            digester.addObjectCreate("*/record/data", BinaryDataWrapper.class);
            digester.addSetTop("*/record/data", "setData");
            digester.addCallMethod("*/record/data", "setFromBase64Encoded", 0);
    
            // set up an general rule for all jhove strings!
            digester.addObjectCreate("*/jhoveXML", BinaryDataWrapper.class);
            digester.addSetTop("*/jhoveXML", "setString");
            digester.addCallMethod("*/jhoveXML", "setFromBase64Encoded", 0);
            digester.addCallMethod("*/jhoveXML", "setMethodName", 1, new String[]{"java.lang.String"});
            digester.addObjectParam("*/jhoveXML", 0, "setJhoveXMLString");
            
            // set up general rule for all fitsXMLs
            digester.addObjectCreate("*/fitsXML", BinaryDataWrapper.class);
            digester.addSetTop("*/fitsXML", "setString");
            digester.addCallMethod("*/fitsXML", "setFromBase64Encoded", 0);
            digester.addCallMethod("*/fitsXML", "setMethodName", 1, new String[]{"java.lang.String"});
            digester.addObjectParam("*/fitsXML", 0, "setFitsXMLString");
            
            digester.addObjectCreate("*/record/formatInfo", FormatInfo.class);
            digester.addSetProperties("*/record/formatInfo");
            digester.addSetNext("*/record/formatInfo", "setFormatInfo");
            
            addCreateUpload(digester, "*/record/xcdlDescription", "setXcdlDescription", XcdlDescription.class);
    
            // - collection profile
            digester.addObjectCreate("*/plan/sampleRecords/collectionProfile", CollectionProfile.class);
            digester.addSetProperties("*/plan/sampleRecords/collectionProfile");
            digester.addSetNext("*/plan/sampleRecords/collectionProfile", "setCollectionProfile");
    
            digester.addCallMethod("*/plan/sampleRecords/collectionProfile/collectionID", "setCollectionID", 0);
            digester.addCallMethod("*/plan/sampleRecords/collectionProfile/description", "setDescription", 0);
            digester.addCallMethod("*/plan/sampleRecords/collectionProfile/numberOfObjects", "setNumberOfObjects", 0);
            digester.addCallMethod("*/plan/sampleRecords/collectionProfile/typeOfObjects", "setTypeOfObjects", 0);
            digester.addCallMethod("*/plan/sampleRecords/collectionProfile/expectedGrowthRate", "setExpectedGrowthRate", 0);
            digester.addCallMethod("*/plan/sampleRecords/collectionProfile/retentionPeriod", "setRetentionPeriod", 0);
    
            // requirements definition
            digester.addObjectCreate("*/plan/requirementsDefinition", RequirementsDefinition.class);
            digester.addSetProperties("*/plan/requirementsDefinition");
            digester.addSetNext("*/plan/requirementsDefinition", "setRequirementsDefinition");
    
            digester.addCallMethod("*/plan/requirementsDefinition/description", "setDescription", 0);
    
            // - uploads
            digester.addObjectCreate("*/plan/requirementsDefinition/uploads", ArrayList.class);
            digester.addSetNext("*/plan/requirementsDefinition/uploads", "setUploads");
            addCreateUpload(digester, "*/plan/requirementsDefinition/uploads/upload", "add", DigitalObject.class);
    
            // alternatives
            digester.addObjectCreate("*/plan/alternatives", AlternativesDefinition.class);
            digester.addSetProperties("*/plan/alternatives");
            digester.addCallMethod("*/plan/alternatives/description", "setDescription", 0);
            digester.addSetNext("*/plan/alternatives", "setAlternativesDefinition");
    
            digester.addObjectCreate("*/plan/alternatives/alternative", Alternative.class);
            digester.addSetProperties("*/plan/alternatives/alternative");
            digester.addSetNext("*/plan/alternatives/alternative", "addAlternative");
            // - action
            digester.addObjectCreate("*/plan/alternatives/alternative/action", PreservationActionDefinition.class);
            digester.addSetProperties("*/plan/alternatives/alternative/action");
            digester.addBeanPropertySetter("*/plan/alternatives/alternative/action/descriptor");
            digester.addBeanPropertySetter("*/plan/alternatives/alternative/action/parameterInfo");
            
            digester.addSetNext("*/plan/alternatives/alternative/action", "setAction");
    
            digester.addCallMethod("*/plan/alternatives/alternative/description", "setDescription", 0);
    
            // - - params
            digester.addObjectCreate("*/plan/alternatives/alternative/action/params", LinkedList.class);
            digester.addSetNext("*/plan/alternatives/alternative/action/params", "setParams");
    
            digester.addObjectCreate("*/plan/alternatives/alternative/action/params/param", Parameter.class);
            digester.addSetProperties("*/plan/alternatives/alternative/action/params/param");
            digester.addSetNext("*/plan/alternatives/alternative/action/params/param", "add");
            // - resource description
            digester.addObjectCreate("*/resourceDescription", ResourceDescription.class);
            digester.addSetProperties("*/resourceDescription");
            digester.addSetNext("*/resourceDescription", "setResourceDescription");
    
            digester.addCallMethod("*/resourceDescription/configSettings", "setConfigSettings", 0);
            digester.addCallMethod("*/resourceDescription/necessaryResources", "setNecessaryResources", 0);
            digester.addCallMethod("*/resourceDescription/reasonForConsidering", "setReasonForConsidering", 0);
    
            // - experiment
            digester.addObjectCreate("*/experiment", ExperimentWrapper.class);
            digester.addSetProperties("*/experiment");
            digester.addSetNext("*/experiment", "setExperiment");
            digester.addCallMethod("*/experiment/description", "setDescription", 0);
            digester.addCallMethod("*/experiment/settings", "setSettings", 0);
    
            addCreateUpload(digester, "*/experiment/results/result", null, DigitalObject.class);
            addCreateUpload(digester, "*/result/xcdlDescription", "setXcdlDescription", XcdlDescription.class);
    
            // call function addUpload of ExperimentWrapper
            CallMethodRule r = new CallMethodRule(1, "addResult", 2);  //method with two params
            // every time  */experiment/uploads/upload  is encountered
            digester.addRule("*/experiment/results/result", r);
            // use attribute "key" as first param
            digester.addCallParam("*/experiment/results/result", 0 , "key");
            // and the object on stack (DigitalObject) as the second
            digester.addCallParam("*/experiment/results/result",1,true);
    
    //        addCreateUpload(digester, "*/experiment/xcdlDescriptions/xcdlDescription", null, XcdlDescription.class);
    //        // call function addXcdlDescription of ExperimentWrapper
    //        r = new CallMethodRule(1, "addXcdlDescription", 2);  //method with two params
    //        // every time  */experiment/xcdlDescriptions/xcdlDescription  is encountered
    //        digester.addRule("*/experiment/xcdlDescriptions/xcdlDescription", r);
    //        // use attribute "key" as first param
    //        digester.addCallParam("*/experiment/xcdlDescriptions/xcdlDescription", 0 , "key");
    //        // and the object on stack (DigitalObject) as the second
    //        digester.addCallParam("*/experiment/xcdlDescriptions/xcdlDescription",1,true);
    
            
            digester.addObjectCreate("*/experiment/detailedInfos/detailedInfo", DetailedExperimentInfo.class);
            digester.addSetProperties("*/experiment/detailedInfos/detailedInfo");
            digester.addBeanPropertySetter("*/experiment/detailedInfos/detailedInfo/programOutput");
            digester.addBeanPropertySetter("*/experiment/detailedInfos/detailedInfo/cpr");
            
            // call function "addDetailedInfo" of ExperimentWrapper
            r = new CallMethodRule(1, "addDetailedInfo", 2);  //method with two params
            // every time  */experiment/detailedInfos/detailedInfo is encountered
            digester.addRule("*/experiment/detailedInfos/detailedInfo", r);
            // use attribute "key" as first param
            digester.addCallParam("*/experiment/detailedInfos/detailedInfo", 0 , "key");
            // and the object on stack as second parameter
            digester.addCallParam("*/experiment/detailedInfos/detailedInfo",1,true);
    
            // read contained measurements:
            digester.addObjectCreate("*/detailedInfo/measurements/measurement", Measurement.class);
            digester.addSetNext("*/detailedInfo/measurements/measurement", "put");
            // values are defined with wild-cards, and therefore set automatically        
            digester.addObjectCreate("*/measurement/property", MeasurableProperty.class);
            digester.addSetProperties("*/measurement/property");
            digester.addSetNext("*/measurement/property", "setProperty");
            // scales are defined with wild-cards, and therefore set automatically 
            
            /*
             * for each value type a set of rules
             * because of FreeStringValue we need to store the value as XML-element
             * instead of an attribute
             * naming them "ResultValues" wasn't nice too
             */
            addCreateValue(digester, BooleanValue.class, "setValue");
            addCreateValue(digester, FloatRangeValue.class, "setValue");
            addCreateValue(digester, IntegerValue.class, "setValue");
            addCreateValue(digester, IntRangeValue.class, "setValue");
            addCreateValue(digester, OrdinalValue.class, "setValue");
            addCreateValue(digester, PositiveFloatValue.class, "setValue");
            addCreateValue(digester, PositiveIntegerValue.class, "setValue");
            addCreateValue(digester, YanValue.class, "setValue");
            addCreateValue(digester, FreeStringValue.class, "setValue");
    
            
            // go no go decision
            digester.addObjectCreate("*/plan/decision", Decision.class);
            digester.addSetProperties("*/plan/decision");
            digester.addSetNext("*/plan/decision", "setDecision");
    
            digester.addCallMethod("*/plan/decision/actionNeeded", "setActionNeeded", 0);
            digester.addCallMethod("*/plan/decision/reason", "setReason", 0);
    
            digester.addFactoryCreate("*/plan/decision/goDecision", GoDecisionFactory.class);
            digester.addSetNext("*/plan/decision/goDecision", "setDecision");
    
    
            // evaluation
            digester.addObjectCreate("*/plan/evaluation", Evaluation.class);
            digester.addSetProperties("*/plan/evaluation");
            digester.addSetNext("*/plan/evaluation", "setEvaluation");
    
            digester.addCallMethod("*/plan/evaluation/comment", "setComment", 0);
    
            // importance weighting
            digester.addObjectCreate("*/plan/importanceWeighting", ImportanceWeighting.class);
            digester.addSetProperties("*/plan/importanceWeighting");
            digester.addSetNext("*/plan/importanceWeighting", "setImportanceWeighting");
    
            digester.addCallMethod("*/plan/importanceWeighting/comment", "setComment", 0);
    
            // recommendation
            digester.addObjectCreate("*/plan/recommendation", RecommendationWrapper.class);
            digester.addSetProperties("*/plan/recommendation");
            digester.addSetNext("*/plan/recommendation", "setRecommendation");
    
            digester.addCallMethod("*/plan/recommendation/reasoning", "setReasoning", 0);
            digester.addCallMethod("*/plan/recommendation/effects", "setEffects", 0);
    
            // transformation
            digester.addObjectCreate("*/plan/transformation", Transformation.class);
            digester.addSetProperties("*/plan/transformation");
            digester.addSetNext("*/plan/transformation", "setTransformation");
    
            digester.addCallMethod("*/plan/transformation/comment", "setComment", 0);
    
    
            // Tree
            /* Some rules for tree parsing are necessary for importing templates too,
             * that's why they are added by this static method. */
            ProjectImporter.addTreeParsingRulesToDigester(digester);
    
            digester.addObjectCreate("*/leaf/evaluation", HashMap.class);
            digester.addSetNext("*/leaf/evaluation", "setValueMap");
            /*
             *  The valueMap has an entry for each (considered) alternative ...
             *  and for each alternative there is a list of values, one per SampleObject.
             *  Note: The digester uses a stack, therefore the rule to put the list of values to the valueMap
             *  must be added after the rule for adding the values to the list.
             */
    
            /*
             * 2. and for each alternative there is a list of values, one per SampleObject
             */
            digester.addObjectCreate("*/leaf/evaluation/alternative", Values.class);
            digester.addCallMethod("*/leaf/evaluation/alternative/comment", "setComment", 0);
    
            /*
             *  for each result-type a set of rules
             *  they are added to the valueMap by the rules above
             */
            addCreateResultValue(digester, BooleanValue.class);
            addCreateResultValue(digester, FloatValue.class);
            addCreateResultValue(digester, FloatRangeValue.class);
            addCreateResultValue(digester, IntegerValue.class);
            addCreateResultValue(digester, IntRangeValue.class);
            addCreateResultValue(digester, OrdinalValue.class);
            addCreateResultValue(digester, PositiveFloatValue.class);
            addCreateResultValue(digester, PositiveIntegerValue.class);
            addCreateResultValue(digester, YanValue.class);
            addCreateResultValue(digester, FreeStringValue.class);
    
            /*
             * 1. The valueMap has an entry for each (considered) alternative ...
             */
            // call put of the ValueMap (HashMap)
            r = new CallMethodRule(1, "put", 2);
            digester.addRule("*/leaf/evaluation/alternative", r);
            digester.addCallParam("*/leaf/evaluation/alternative", 0 , "key");
            digester.addCallParam("*/leaf/evaluation/alternative",1,true);
            
    //        digester.addObjectCreate("*/plan/executablePlan/planWorkflow", ExecutablePlanContentWrapper.class);
    //        digester.addSetProperties("*/plan/executablePlan/planWorkflow");
    //        digester.addSetNext("*/plan/executablePlan/planWorkflow", "setRecommendation");
    
            // Executable plan definition
            digester.addObjectCreate("*/plan/executablePlan", ExecutablePlanDefinition.class);
            digester.addSetProperties("*/plan/executablePlan");
            digester.addSetNext("*/plan/executablePlan", "setExecutablePlanDefinition");
            
            //
            // Import Planets executable plan if present
            //
            try {
                // object-create rules are called at the beginning element-tags, in the same order as defined 
                // first create the wrapper
                digester.addObjectCreate("*/plan/executablePlan/planWorkflow", NodeContentWrapper.class);
                // then an element for workflowConf
                digester.addRule("*/plan/executablePlan/planWorkflow/workflowConf", new NodeCreateRule()); 
                
                // CallMethod and SetNext rules are called at closing element-tags, (last in - first out!)
                
                CallMethodRule rr = new CallMethodRule(1, "setNodeContent", 2);
                digester.addRule("*/plan/executablePlan/planWorkflow/workflowConf", rr);
                // right below the wrapper is an instance of ExecutablePlanDefinition  
                digester.addCallParam("*/plan/executablePlan/planWorkflow/workflowConf", 0 , 1);
                // provide the name of the setter method
                digester.addObjectParam("*/plan/executablePlan/planWorkflow/workflowConf", 1, "setExecutablePlan");
    
                // the generated node is not accessible as CallParam (why?!?), but available for addSetNext
                digester.addSetNext("*/plan/executablePlan/planWorkflow/workflowConf", "setNode");

            } catch (ParserConfigurationException e) {
                PlatoLogger.getLogger(this.getClass()).error(e.getMessage(),e);
            }

            //
            // Import EPrints executable plan if present
            //
            try {
                
                digester.addObjectCreate("*/plan/executablePlan/eprintsPlan", NodeContentWrapper.class);
                // then an element for workflowConf
                digester.addRule("*/plan/executablePlan/eprintsPlan", new NodeCreateRule());
                
                CallMethodRule rr2 = new CallMethodRule(1, "setNodeContentEPrintsPlan", 2);
                digester.addRule("*/plan/executablePlan/eprintsPlan", rr2);
                // right below the wrapper is an instance of ExecutablePlanDefinition  
                digester.addCallParam("*/plan/executablePlan/eprintsPlan", 0 , 1);
                // provide the name of the setter method
                digester.addObjectParam("*/plan/executablePlan/eprintsPlan", 1, "setEprintsExecutablePlan");
                
                digester.addSetNext("*/plan/executablePlan/eprintsPlan", "setNode");
                
            } catch (ParserConfigurationException e) {
                PlatoLogger.getLogger(this.getClass()).error(e.getMessage(),e);
            }
            
            digester.addCallMethod("*/plan/executablePlan/objectPath", "setObjectPath", 0);
            digester.addCallMethod("*/plan/executablePlan/toolParameters", "setToolParameters", 0);
            digester.addCallMethod("*/plan/executablePlan/triggersConditions", "setTriggersConditions", 0);
            digester.addCallMethod("*/plan/executablePlan/validateQA", "setValidateQA", 0);
            
            // Plan definition        
            digester.addObjectCreate("*/plan/planDefinition", PlanDefinition.class);
            digester.addSetProperties("*/plan/planDefinition");
            digester.addSetNext("*/plan/planDefinition", "setPlanDefinition");
    
            digester.addCallMethod("*/plan/planDefinition/costsIG", "setCostsIG", 0);
            digester.addCallMethod("*/plan/planDefinition/costsPA", "setCostsPA", 0);
            digester.addCallMethod("*/plan/planDefinition/costsPE", "setCostsPE", 0);
            digester.addCallMethod("*/plan/planDefinition/costsQA", "setCostsQA", 0);
            digester.addCallMethod("*/plan/planDefinition/costsREI", "setCostsREI", 0);
            digester.addCallMethod("*/plan/planDefinition/costsRemarks", "setCostsRemarks", 0);
            digester.addCallMethod("*/plan/planDefinition/costsRM", "setCostsRM", 0);
            digester.addCallMethod("*/plan/planDefinition/costsTCO", "setCostsTCO", 0);
            digester.addCallMethod("*/plan/planDefinition/responsibleExecution", "setResponsibleExecution", 0);
            digester.addCallMethod("*/plan/planDefinition/responsibleMonitoring", "setResponsibleMonitoring", 0);
    
            digester.addObjectCreate("*/plan/planDefinition/triggers", TriggerDefinition.class);
            digester.addSetNext("*/plan/planDefinition/triggers","setTriggers");
            // every time a */plan/basis/triggers/trigger is encountered:
            digester.addFactoryCreate("*/plan/planDefinition/triggers/trigger",TriggerFactory.class);
            digester.addSetNext("*/plan/planDefinition/triggers/trigger","setTrigger");
            
            digester.setUseContextClassLoader(true);
            this.plans = new ArrayList<Plan>();
    
            // finally parse the XML representation with all created rules
            digester.parse(new FileInputStream(currentVersionFile));
    
            for (Plan plan : plans) {
                String projectName = plan.getPlanProperties().getName();
                if ((projectName != null) &&
                    (!"".equals(projectName))) {
                    /*
                     * establish links from values to scales
                     */
                    plan.getTree().initValues(
                            plan.getAlternativesDefinition().getConsideredAlternatives(),
                            plan.getSampleRecordsDefinition().getRecords().size(),
                            true);
                    /*
                     * establish references of Experiment.uploads
                     */
                    HashMap<String, SampleObject> records = new HashMap<String, SampleObject>();
                    for (SampleObject record : plan.getSampleRecordsDefinition().getRecords()) {
                        records.put(record.getShortName(), record);
                    }
                    for (Alternative alt : plan.getAlternativesDefinition().getAlternatives()) {
                        if ((alt.getExperiment() != null) && (alt.getExperiment() instanceof ExperimentWrapper )) {
                            alt.setExperiment(((ExperimentWrapper)alt.getExperiment()).getExperiment(records));
                        }
                    }
                  
                    // DESCRIBE all DigitalObjects with Jhove.
                    for (SampleObject record : plan.getSampleRecordsDefinition().getRecords()) {
                        if (record.isDataExistent()) {
                            // characterise
                            
                            try {
                                record.setJhoveXMLString(new JHoveAdaptor().describe(record));
                            } catch(Throwable e) {
                                log.error("Error running Jhove for record " + record.getShortName() + ". " + e.getMessage(), e);  
                            }
                            for (Alternative alt : plan.getAlternativesDefinition().getAlternatives()) {
                                DigitalObject result = alt.getExperiment().getResults().get(record);
                                if (result != null && result.isDataExistent()) {
                                    try {
                                        result.setJhoveXMLString(new JHoveAdaptor().describe(result));
                                    } catch(Throwable e) {
                                        log.error("Error running Jhove for record " + record.getShortName() 
                                                + ", alternative " + alt.getName() + ". " + e.getMessage(), e);  
                                    }

                                }
                            }
                        }
                    }
                    
                    
                    // CHECK NUMERIC TRANSFORMER THRESHOLDS
                    for (Leaf l: plan.getTree().getRoot().getAllLeaves()) {
                        eu.planets_project.pp.plato.model.transform.Transformer t = l.getTransformer();
                        if (t != null && t instanceof NumericTransformer) {
                            NumericTransformer nt = (NumericTransformer) t;
                            if (!nt.checkOrder()) {
                                StringBuffer sb = new StringBuffer("NUMERICTRANSFORMER THRESHOLD ERROR ");
                                sb.append(l.getName())
                                  .append("::NUMERICTRANSFORMER:: ");
                                sb.append(nt.getThreshold1()).append(" ")
                                  .append(nt.getThreshold2()).append(" ")
                                  .append(nt.getThreshold3()).append(" ")
                                  .append(nt.getThreshold4()).append(" ")
                                  .append(nt.getThreshold5());
                                log.error(sb.toString());
                            }
                        }
                    }
                    
                    /*
                     * establish references to selected alternative
                     */
                    HashMap<String, Alternative> alternatives = new HashMap<String, Alternative>();
                    for (Alternative alt : plan.getAlternativesDefinition().getAlternatives()) {
                        alternatives.put(alt.getName(), alt);
                    }
                    if ((plan.getRecommendation() != null) && (plan.getRecommendation() instanceof RecommendationWrapper)) {
                        plan.setRecommendation(((RecommendationWrapper)plan.getRecommendation()).getRecommendation(alternatives));
                    }
                    if ((plan.getState().getValue() == PlanState.ANALYSED) &&
                        ((plan.getRecommendation() == null)||(plan.getRecommendation().getAlternative()== null))) {
                        /*
                         * This project is NOT completely analysed
                         */
                        plan.getState().setValue(PlanState.ANALYSED - 1);
                    }
                } else {
                    throw new SAXException("Could not find any project data.");
                }
            }
        } finally {
            OS.deleteDirectory(tempDir);
            /*
             * Importing big plans results in an increasing memory consumption
             * strange: The rise of memory consumption occurs when persisting the loaded project
             * NOT during parsing with the digester
             */
            System.gc();
        }

        return this.plans;
    }

    /**
     * 
     * @param args
     *            first entry is the filename of the xml-file to be imported
     *            second entry is the name of the output-file
     */
    public static void main(String[] args) {

        ProjectImporter projectImp = new ProjectImporter();
        ProjectExporter exporter = new ProjectExporter();
        try {
            for (Plan plan : projectImp.importProjects(args[0])) {
                System.out.println("Imported : "
                        + plan.getPlanProperties().getName());
                // export the imported project
                FileOutputStream out = new FileOutputStream(args[1]);
                XMLWriter writer = new XMLWriter(out,
                        ProjectExporter.prettyFormat);
                writer.write(exporter.exportToXml(plan));
                writer.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a rule for reading an upload entry for the given location
     * <code>pattern</code>, and use the <code>method</code> to set the upload
     * object.
     * 
     * @param digester
     * @param pattern
     * @param method
     */
    private static void addCreateUpload(Digester digester, String pattern,
            String method, Class objectType) {
        digester.addObjectCreate(pattern, objectType);
        digester.addSetProperties(pattern);
        if ((method != null) && (!"".equals(method)))
            digester.addSetNext(pattern, method);
        /*
         * Note: It is not possible to read element data, process it and pass it
         * to a function with a simple digester Rule, neither you can define a
         * factory to read the data of an element.
         * 
         * So we have to do it the other way round: (remember: the function
         * added last is executed first!)
         */
        // 1. Create a BinaryDataWrapper if a <data> element is encountered
        digester.addObjectCreate(pattern + "/data", BinaryDataWrapper.class);
        // 3. Finally call setData on the BinaryDataWrapper(!) on top with the
        // object next to top as argument
        // The BinaryDataWrapper will call setData on to object next to top with
        // the previously read and decoded data
        digester.addSetTop(pattern + "/data", "setData");
        // 2. Call setFromBase64Encoded on the BinaryDataWrapper to read the
        // elements content
        digester.addCallMethod(pattern + "/data", "setFromBase64Encoded", 0);

    }

    public void setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
    }

    /**
     * This method adds rules for name, properties, scales, modes and mappings
     * only! Rules for importing measured values of alternatives are defined
     * seperately in importProjects()! (Refactored to its own method by Kevin)
     */
    private static void addTreeParsingRulesToDigester(Digester digester) {
        digester.addObjectCreate("*/plan/tree", ObjectiveTree.class);
        digester.addSetProperties("*/plan/tree");
        digester.addSetNext("*/plan/tree", "setTree");

        digester.addObjectCreate("*/node/node", Node.class);
        digester.addSetProperties("*/node/node");
        digester.addSetNext("*/node/node", "addChild");

        digester.addCallMethod("*/node/description", "setDescription", 0);

        digester.addObjectCreate("*/plan/tree/node", Node.class);
        digester.addSetProperties("*/plan/tree/node");
        digester.addSetNext("*/plan/tree/node", "setRoot");

        digester.addObjectCreate("*/leaf", Leaf.class);
        digester.addSetProperties("*/leaf");
        digester.addSetNext("*/leaf", "addChild");
        digester.addFactoryCreate("*/leaf/aggregationMode",
                SampleAggregationModeFactory.class);
        digester.addSetNext("*/leaf/aggregationMode", "setAggregationMode");

        digester.addCallMethod("*/leaf/description", "setDescription", 0);

        digester.addObjectCreate("*/measurementInfo", MeasurementInfo.class);
        digester.addSetNext("*/measurementInfo", "setMeasurementInfo");
        addPropertyRules(digester, "*/measurementInfo/property");
        // and the selected metric
        addMetricRules(digester, "*/measurementInfo/metric", "setMetric");

        /*
         * for each scale-type a set of rules
         */
        addCreateScale(digester, BooleanScale.class);
        addCreateScale(digester, FloatRangeScale.class);
        addCreateScale(digester, FloatScale.class);
        addCreateScale(digester, IntegerScale.class);
        addCreateScale(digester, IntRangeScale.class);
        addCreateScale(digester, OrdinalScale.class);
        addCreateScale(digester, PositiveFloatScale.class);
        addCreateScale(digester, PositiveIntegerScale.class);
        addCreateScale(digester, YanScale.class);
        addCreateScale(digester, FreeStringScale.class);

        /*
         * for each transformer type a set of rules
         */
        digester.addObjectCreate("*/leaf/numericTransformer",
                NumericTransformer.class);
        digester.addSetProperties("*/leaf/numericTransformer");
        digester.addFactoryCreate("*/leaf/numericTransformer/mode",
                TransformationModeFactory.class);
        digester.addSetNext("*/leaf/numericTransformer/mode", "setMode");
        digester
                .addBeanPropertySetter(
                        "*/leaf/numericTransformer/thresholds/threshold1",
                        "threshold1");
        digester
                .addBeanPropertySetter(
                        "*/leaf/numericTransformer/thresholds/threshold2",
                        "threshold2");
        digester
                .addBeanPropertySetter(
                        "*/leaf/numericTransformer/thresholds/threshold3",
                        "threshold3");
        digester
                .addBeanPropertySetter(
                        "*/leaf/numericTransformer/thresholds/threshold4",
                        "threshold4");
        digester
                .addBeanPropertySetter(
                        "*/leaf/numericTransformer/thresholds/threshold5",
                        "threshold5");
        digester.addSetNext("*/leaf/numericTransformer", "setTransformer");

        // digester.addObjectCreate("*/numericTransformer/thresholds",
        // LinkedHashMap.class);
        // digester.addSetNext("*/numericTransformer/thresholds",
        // "setThresholds");
        // digester.addFactoryCreate("*/thresholds/threshold",
        // NumericTransformerThresholdFactory.class);

        digester.addObjectCreate("*/leaf/ordinalTransformer",
                OrdinalTransformer.class);
        digester.addSetProperties("*/leaf/ordinalTransformer");
        digester.addSetNext("*/leaf/ordinalTransformer", "setTransformer");

        digester.addObjectCreate("*/ordinalTransformer/mappings",
                LinkedHashMap.class);
        digester.addSetNext("*/ordinalTransformer/mappings", "setMapping");
        digester.addFactoryCreate("*/mappings/mapping",
                OrdinalTransformerMappingFactory.class);

        digester.addRule("*/mappings/mapping", new CallMethodRule(1, "put", 2)); // method
                                                                                 // with
                                                                                 // two
                                                                                 // params
        digester.addCallParam("*/mappings/mapping", 0, "ordinal"); // use
                                                                   // attribute
                                                                   // "ordinal"
                                                                   // as first
                                                                   // argument
        digester.addCallParam("*/mappings/mapping", 1, true); // and the object
                                                              // on the stack as
                                                              // second
    }

    private static void addCreateResultValue(Digester digester, Class c) {
        String name = c.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        String pattern = "*/" + name.replace("Value", "Result");
        digester.addObjectCreate(pattern, c);
        digester.addSetProperties(pattern);
        digester.addBeanPropertySetter(pattern + "/value");
        digester.addBeanPropertySetter(pattern + "/comment");        
        digester.addSetNext(pattern, "add");
    }

    private static void addCreateValue(Digester digester, Class c,
            String setNextMethod) {
        String name = c.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        String pattern = "*/" + name;
        digester.addObjectCreate(pattern, c);
        // digester.addSetProperties(pattern);
        digester.addBeanPropertySetter(pattern + "/value");
        digester.addSetNext(pattern, setNextMethod);
    }

    private static void addCreateScale(Digester digester, Class c) {
        String name = c.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        String pattern = "*/" + name;
        digester.addObjectCreate(pattern, c);
        digester.addSetProperties(pattern);
        digester.addSetNext(pattern, "setScale");
    }

    private static void addPropertyRules(Digester digester, String pattern) {
        digester.addObjectCreate(pattern, MeasurableProperty.class);
        digester.addSetNext(pattern, "setProperty");
        digester.addSetProperties(pattern);
        // there is no property categoryAsString, but a setter ...
        digester.addBeanPropertySetter( pattern + "/category", "categoryAsString");
        digester.addBeanPropertySetter(pattern + "/propertyId");
        digester.addBeanPropertySetter(pattern + "/name");
        digester.addBeanPropertySetter(pattern + "/description");
        
        // scale is created automatically with global rule
//        digester.addObjectCreate(pattern + "/possibleMetrics", ArrayList.class);
//        digester.addSetNext(pattern + "/possibleMetrics", "setPossibleMetrics");
//        addMetricRules(digester, "*/possibleMetrics/metric", "add");

    }
    private static void addMetricRules(Digester digester, String pattern,
            String method) {
        digester.addObjectCreate(pattern, Metric.class);
        digester.addSetProperties(pattern);
        digester.addBeanPropertySetter(pattern + "/metricId");
        digester.addBeanPropertySetter(pattern + "/name");
        digester.addBeanPropertySetter(pattern + "/description");
        // scale is created automatically with "global" rule
        digester.addSetNext(pattern, method);
    }

    public List<String> getAppliedTransformations() {
        return appliedTransformations;
    }
}
