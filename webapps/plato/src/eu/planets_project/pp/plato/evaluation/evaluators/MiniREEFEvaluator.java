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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import at.tuwien.minireef.ResultSet;
import eu.planets_project.pp.plato.evaluation.EvaluatorException;
import eu.planets_project.pp.plato.evaluation.IActionEvaluator;
import eu.planets_project.pp.plato.evaluation.IStatusListener;
import eu.planets_project.pp.plato.model.Alternative;
import eu.planets_project.pp.plato.model.FormatInfo;
import eu.planets_project.pp.plato.model.scales.Scale;
import eu.planets_project.pp.plato.model.values.BooleanValue;
import eu.planets_project.pp.plato.model.values.Value;
import eu.planets_project.pp.plato.services.action.IMigrationAction;
import eu.planets_project.pp.plato.util.MeasurementInfoUri;
import eu.planets_project.pp.plato.util.PlatoLogger;

public class MiniREEFEvaluator extends EvaluatorBase implements IActionEvaluator {
    
    private static final Log log = PlatoLogger.getLogger(MiniREEFEvaluator.class);

    private static final String P2_RESOURCE_FORMAT_LICENSE_RIGHTS_OPEN = "http://p2-registry.ecs.soton.ac.uk/pronom/risk_categories/rights/open";
    private static final String P2_RESOURCE_FORMAT_LICENSE_RIGHTS_IPR_PROTECTED = "http://p2-registry.ecs.soton.ac.uk/pronom/risk_categories/rights/ipr_protected";
    private static final String P2_RESOURCE_FORMAT_LICENSE_RIGHTS_PROPRIETARY = "http://p2-registry.ecs.soton.ac.uk/pronom/risk_categories/rights/proprietary";

    private Map<String, String> statements = new HashMap<String, String>();

    private static final String DESCRIPTOR_FILE = "data/evaluation/measurementsMiniReef.xml";
    
    public MiniREEFEvaluator(){
        // load information about measurements
        loadMeasurementsDescription(DESCRIPTOR_FILE);
        
        addStatements();
    }
    
    public HashMap<MeasurementInfoUri, Value> evaluate(
            Alternative alternative,
            List<MeasurementInfoUri> measurementInfoUris,
            IStatusListener listener) throws EvaluatorException{
        
        HashMap<MeasurementInfoUri, Value> results = new HashMap<MeasurementInfoUri, Value>();

        if (alternative.getAction() == null) {
            return results;
        }
        
        // prepare commonly used parameters
        Map<String, String> params = new HashMap<String, String>();

        // PUID: use the PUID from action.targetformat
        String puid = "Target format PUID undefined for this action";
        FormatInfo targetInfo = alternative.getAction().getTargetFormatInfo();
        if (targetInfo != null && !"".equals(targetInfo.getPuid())) {
            puid = targetInfo.getPuid();
        }
        params.put("PUID", puid);
        
        
        for (MeasurementInfoUri info: measurementInfoUris) {
            Scale scale = descriptor.getMeasurementScale(info);
            if (scale == null)  {
                // This means that I am not entitled to evaluate this measurementInfo and therefore supposed to skip it:
                continue;
            }
            
            Value value = scale.createValue();
            String propertyURI = info.getAsURI();
            
            if (ACTION_BATCH_SUPPORT.equals(propertyURI)) {
                if (alternative.getAction() instanceof IMigrationAction) {
                    // this alternative is wrapped as service and therefore provides batch support 
                    value.parse("Yes");
                    value.setComment("this alternative is wrapped as service and therefore provides batch support");
                }
            }
            

            String statement = statements.get(propertyURI);
            if (statement == null) {
                // this leaf cannot be evaluated by MiniREEF - skip it
                continue;
            } 
            
            String result = null;
            
            // add additional params if necessary
            // ...
            ResultSet resultSet = MiniREEFResolver.getInstance().resolve(statement, params);
            listener.updateStatus("MiniREEF is attempting to evaluate "+propertyURI);

            if (resultSet == null) {
                // this should not happen, if MiniREEF is properly configured
                listener.updateStatus("querying MiniREEF/P2 knowledge base failed for statement: " + statement);
                // skip this leaf
                continue;
            }
            
            // evaluation was successful! 
            if (propertyURI.startsWith(FORMAT_NUMBEROFTOOLS)){
                // _measure_ is the number of tools found 
                result = "" + resultSet.size();
                value.parse(result);
                // add names of tools as comment
                value.setComment(toCommaSeparated(resultSet.getColResults("swname")) + 
                       "; - according to miniREEF/P2 knowledge base");
                listener.updateStatus("MiniREEF evaluated "+propertyURI);
            } else if ((FORMAT_LICENSE_OPEN.equals(propertyURI))||
                       (FORMAT_LICENSE_IPR_PROTECTED.equals(propertyURI))||
                       (FORMAT_LICENSE_PROPRIETARY.equals(propertyURI))) {
                if (resultSet.size() > 0) {
                    // we query for rights information in general, this way we can clarify the deduced result
                    // e.g. open = false, comment: "Format is encumbered by IPR"
                    boolean open = false;
                    boolean ipr = false;
                    boolean proprietary = false;
                    String comment = "";
                    for (int i=0; i < resultSet.size(); i++) {
                        // collect all results, even though there should be only one, the user can take care of it 
                        List<String> vals = resultSet.getRow(i);
                        comment = comment + vals.get(0)+"\n";
                        String type = vals.get(1);
                        if (P2_RESOURCE_FORMAT_LICENSE_RIGHTS_IPR_PROTECTED.equals(type)) {
                            ipr = true;
                        } else if (P2_RESOURCE_FORMAT_LICENSE_RIGHTS_PROPRIETARY.equals(type)) {
                            proprietary = true;
                        }else if (P2_RESOURCE_FORMAT_LICENSE_RIGHTS_OPEN.equals(type)) {
                            open = true;
                        }
                    }
                    if (resultSet.size() > 1) {
                        comment = comment + "more than one right category applies to this format, check for reason of this conflict.\n";
                    }
                    boolean boolResult = false;
                    if (FORMAT_LICENSE_OPEN.equals(propertyURI)) {
                        boolResult = open;
                    } else if (FORMAT_LICENSE_IPR_PROTECTED.equals(propertyURI)) {
                        boolResult = ipr;
                    } else if (FORMAT_LICENSE_PROPRIETARY.equals(propertyURI)) {
                        boolResult = proprietary;
                    }
                    value = scale.createValue();
                    ((BooleanValue)value).bool(boolResult);
                    value.setComment(comment + "according to MiniREEF/P2 knowledge base");
                    listener.updateStatus("MiniREEF evaluated "+propertyURI);
                }
                listener.updateStatus("P2 does not contain enough information to evaluate "+propertyURI+" for this format.");
            } else if ((FORMAT_COMPLEXITY.equals(propertyURI)) || 
                       (FORMAT_DISCLOSURE.equals(propertyURI)) || 
                       (FORMAT_UBIQUITY.equals(propertyURI))   ||
                       (FORMAT_DOCUMENTATION_QUALITY.equals(propertyURI))||
                       (FORMAT_STABILITY.equals(propertyURI))||
                       (FORMAT_LICENSE.equals(propertyURI)))  {
                if (resultSet.size()>0) {
                    String text = resultSet.getRow(0).get(0);
                    if (text.trim().length() > 0) {
                        value = scale.createValue();
                        value.parse(text);
                        value.setComment("according to miniREEF/P2 knowledge base");
                    }
                    listener.updateStatus("MiniREEF evaluated "+propertyURI);
                } else {
                    listener.updateStatus("P2 does not contain enough information to evaluate "+propertyURI+" for this format.");
                }
                
            } 
            // put measure to result map
            results.put(info, value);
        }
        
        return results;
    }
    
    private String toCommaSeparated(List<String> list) {
        if (list == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        Iterator<String> iter = list.iterator();
        while(iter.hasNext())  {
            b.append(iter.next());
            if (iter.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
    
    /**
     * add a set of available ARQ - SPARQL statements
     * at the moment only results with one column can be handled
     * 
     * You can define parameters by wrapping an identifier with '$' 
     * - they will be replaced later on with supplied values   
     */
    private void addStatements() {
        // action://format/numberOfTools
        String statement=
            "SELECT distinct ?swname " +
            "WHERE { ?sw ?link ?format . " +
            "        ?link rdf:type <http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink> . " +
            "        ?format pronom:FileFormatIdentifier ?ident . " +
            "        ?ident  pronom:Identifier \"$PUID$\" ." +
            "        ?ident  pronom:IdentifierType \"PUID\" ." +
            "        ?sw pronom:SoftwareName  ?swname } ";

        statements.put(FORMAT_NUMBEROFTOOLS, statement);
        
        // action://format/numberOfTools/save : "http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink/Save"
        statement=
            "SELECT distinct ?swname " +
            "WHERE { ?sw ?link ?format . " +
            "        ?link rdf:type <http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink/Save> . " +
            "        ?format pronom:FileFormatIdentifier ?ident . " +
            "        ?ident  pronom:Identifier \"$PUID$\" ." +
            "        ?ident  pronom:IdentifierType \"PUID\" ." +
            "        ?sw pronom:SoftwareName  ?swname } ";

        statements.put(FORMAT_NUMBEROFTOOLS_SAVE, statement);
        
        // action://format/numberOfTools/open : "http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink/Save"
        statement=
            "SELECT distinct ?swname " +
            "WHERE { ?sw ?link ?format . " +
            "        ?link rdf:type <http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink/Open> . " +
            "        ?format pronom:FileFormatIdentifier ?ident . " +
            "        ?ident  pronom:Identifier \"$PUID$\" ." +
            "        ?ident  pronom:IdentifierType \"PUID\" ." +
            "        ?sw pronom:SoftwareName  ?swname } ";

        statements.put(FORMAT_NUMBEROFTOOLS_OPEN, statement);

        // action://format/numberOfTools/other : "http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink/Other"
        statement=
            "SELECT distinct ?swname " +
            "WHERE { ?sw ?link ?format . " +
            "        ?link rdf:type <http://p2-registry.ecs.soton.ac.uk/pronom/SoftwareLink/Other> . " +
            "        ?format pronom:FileFormatIdentifier ?ident . " +
            "        ?ident  pronom:Identifier \"$PUID$\" ." +
            "        ?ident  pronom:IdentifierType \"PUID\" ." +
            "        ?sw pronom:SoftwareName  ?swname } ";

        statements.put(FORMAT_NUMBEROFTOOLS_OTHERS, statement);
        
        statement = 
            "SELECT ?d WHERE { " +
            " ?format pronom:FormatDisclosure ?d . " +
            " ?format pronom:FileFormatIdentifier ?ident . "+
            " ?ident pronom:IdentifierType \"PUID\" . " +
            " ?ident pronom:Identifier \"$PUID$\" }";
        statements.put(FORMAT_DISCLOSURE,statement);
        
        // p2 is used to add information about ubiquity
        statement = 
            "SELECT ?d  WHERE {  " + 
            "?format p2-additional:ubiquity ?u . " +
            "?u rdfs:comment ?d . "  +
            "?format pronom:IsSupertypeOf ?pronomformat . " +
            "?pronomformat pronom:FileFormatIdentifier ?ident ." +
            "?ident pronom:IdentifierType \"PUID\" ." +
            "?ident pronom:Identifier \"$PUID$\" }";
        statements.put(FORMAT_UBIQUITY, statement);

        // p2 is used to add information about ubiquity
        statement = 
            "SELECT ?d  WHERE {  " + 
            "?format p2-additional:complexity ?u . " +
            "?u rdfs:comment ?d . "  +
            "?format pronom:IsSupertypeOf ?pronomformat . " +
            "?pronomformat pronom:FileFormatIdentifier ?ident ." +
            "?ident pronom:IdentifierType \"PUID\" ." +
            "?ident pronom:Identifier \"$PUID$\" }";
        statements.put(FORMAT_COMPLEXITY, statement);

        
        statement = 
            "SELECT ?d  WHERE {  " + 
            "?format p2-additional:documentation_quality ?q . " +
            "?q rdfs:comment ?d . "  +
            "?format  pronom:FileFormatIdentifier ?ident ." +
            "?ident pronom:IdentifierType \"PUID\" ." +
            "?ident pronom:Identifier \"$PUID$\" " +      
             " }";
        statements.put(FORMAT_DOCUMENTATION_QUALITY, statement);
        
        // pronom(!) is used to add information about stability
        statement = 
            "SELECT ?d  WHERE {  " + 
            "?format pronom:stability ?u . " +
            "?u rdfs:comment ?d . "  +
            "?format pronom:IsSupertypeOf ?pronomformat . " +
            "?pronomformat pronom:FileFormatIdentifier ?ident ." +
            "?ident pronom:IdentifierType \"PUID\" ." +
            "?ident pronom:Identifier \"$PUID$\" }";
        statements.put(FORMAT_STABILITY, statement);
        
        /**
         * we use the same query for information on rights,
         * and select the comment and rdf:resource, this way we can provide more detailed information,
         * if a right model does not apply to the format  
         */
        // pronom(!) is used to add information about rights!
        String selectRights =
            "SELECT DISTINCT ?d ?u WHERE {  " + 
            "?format pronom:rights ?u . " +
            "?u rdfs:comment ?d . "  +
            "?format pronom:IsSupertypeOf ?pronomformat . " +
            "?pronomformat pronom:FileFormatIdentifier ?ident ." +
            "?ident pronom:IdentifierType \"PUID\" ." +
            "?ident pronom:Identifier \"$PUID$\" }";
        statements.put(FORMAT_LICENSE, selectRights);

        // pronom(!) is used to add information about rights!
        statements.put(FORMAT_LICENSE_IPR_PROTECTED, selectRights);

        // pronom(!) is used to add information about rights!
        statements.put(FORMAT_LICENSE_OPEN, selectRights);
    }
    
    
}
