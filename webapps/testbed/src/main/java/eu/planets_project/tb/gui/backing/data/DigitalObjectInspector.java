/*******************************************************************************
 * Copyright (c) 2007, 2010 The Planets Project Partners.
 *
 * All rights reserved. This program and the accompanying 
 * materials are made available under the terms of the 
 * Apache License, Version 2.0 which accompanies 
 * this distribution, and is available at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package eu.planets_project.tb.gui.backing.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.storage.api.DataRegistry;
import eu.planets_project.ifr.core.storage.api.DataRegistryFactory;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.tb.gui.backing.ServiceBrowser;
import eu.planets_project.tb.gui.backing.service.FormatBean;
import eu.planets_project.tb.gui.util.JSFUtil;
import eu.planets_project.tb.impl.services.wrappers.CharacteriseWrapper;
import eu.planets_project.tb.impl.services.wrappers.IdentifyWrapper;

/**
 * @author AnJackson
 *
 */
public class DigitalObjectInspector {
    
    static private Log log = LogFactory.getLog(DigitalObjectInspector.class);
    
    // The data sources are managed here:
    DataRegistry dataReg = DataRegistryFactory.getDataRegistry();
    
    private String dobUri;

    /**
     * @return the dobUri
     */
    public String getDobUri() {
        return this.dobUri;
    }

    /**
     * -Dfile.encoding=UTF8
     * @param dobUri the dobUri to set
     */
    public void setDobUri(String dobUri) {
        dobUri = uriEncoder(dobUri).toASCIIString();
        this.dobUri = dobUri;
    }
    
    /**
     * @return
     */
    public DigitalObjectTreeNode getDob() {
        if( this.dobUri == null ) return null;
        // Create as a URI:
        URI item = DigitalObjectInspector.uriEncoder(this.dobUri);
        if( item == null ) return null;
        // Lookup and return:
        DigitalObjectTreeNode itemNode = new DigitalObjectTreeNode(item, this.dataReg );
        return itemNode;
    }

    /**
     * TODO Should be able to avoid this hack workaround.
     * How to set the page encoding correctly?
     * <?xml version="1.0" encoding="UTF-8"?>
     * @param in
     * @return
     */
    /*
    public static String fixBadEncoding( String in ) { 
        return in;
        if( in == null ) return in;
        try {
            String out = new String( in.getBytes("ISO-8859-1"), "UTF-8" );
            return out;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return in;
        }
    }
        */
    
    /**
     * There are some characters that are allowed in URLs and should not be escaped as %xx
     * e.g. blanks. These are not allowed in URIs.
     * This method uses the URI constructor to fix those escapings. 
     * @param uri
     * @return
     */
    public static URI uriEncoder( String uri ) {
        if( uri == null || uri.length() == 0 ) return null;
        try {
            URI nuri = new URI("planets", uri.replaceFirst("planets:", ""), null);
            return nuri;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /* -------------------- Additional code for deeper inspection --------------------- */
    
    /**
     * @return
     */
    public List<SelectItem> getCharacteriseServiceList() {
        log.info("IN: getCharacteriseServiceList");
        ServiceBrowser sb = (ServiceBrowser)JSFUtil.getManagedObject("ServiceBrowser");
/*
        String input = this.getInputFormat();
        if( ! this.isInputSet() ) input = null;
        String output = this.getOutputFormat();
        if( ! this.isOutputSet() ) output = null;
*/
        List<ServiceDescription> sdl = sb.getCharacteriseServices();

        return ServiceBrowser.mapServicesToSelectList( sdl );
    }

    /** */
    private String characteriseService;
    /** */
    private String identifyService;

    /**
     * @return the characteriseService
     */
    public String getCharacteriseService() {
        return this.characteriseService;
    }

    /**
     * @param characteriseService the characteriseService to set
     */
    public void setCharacteriseService(String characteriseService) {
        this.characteriseService = characteriseService;
    }
    
    private CharacteriseResult runCharacteriseService() {
        log.info("Looking for properties using: "+this.getCharacteriseService());
        // Return nothing if no service is selected:
        if( this.getCharacteriseService() == null ) return null;
        // Run the service:
        try {
            Characterise chr = new CharacteriseWrapper(new URL(this.getCharacteriseService()));
            DigitalObject dob = this.getDob().getDob();
            log.info("Got digital object: "+dob);
            CharacteriseResult cr = chr.characterise( dob, null);
            return cr;
        } catch( Exception e ) {
            log.error("FAILED! "+e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return
     */
    public List<Property> getCharacteriseProperties() {
    	List<Property> ret = new ArrayList<Property>();
        CharacteriseResult cr = this.runCharacteriseService();
        if( cr == null ) return null;
        if( cr.getProperties() != null ) {
            log.info("Got properties: "+cr.getProperties().size());
        }else{
        	return ret;
        }
        //iterate over the properties and truncate when more than 500 chars 
        for(Property p : cr.getProperties()){
        	if(p.getValue().length()>500){
        		//add a truncated value String
        		ret.add(new Property(p.getUri(),p.getName(),p.getValue().substring(0, 500)+"[..]"));
        	}else{
        		//add the original result
        		ret.add(p);
        	}
        }
        return ret;
    }
    
    /**
     * @return
     */
    public String getCharacteriseServiceReport() {
        CharacteriseResult cr = this.runCharacteriseService();
        if( cr == null ) return null;
        return ""+cr.getReport();
    }

    
    /**
     * @return the identifyService
     */
    public String getIdentifyService() {
        return this.identifyService;
    }

    /**
     * @param identifyService the identifyService to set
     */
    public void setIdentifyService(String identifyService) {
        this.identifyService = identifyService;
    }
    
    private IdentifyResult runIdentifyService() {
        if( this.getIdentifyService() == null ) return null;
        try {
            Identify id = new IdentifyWrapper(new URL( this.getIdentifyService()));
            IdentifyResult ir = id.identify(this.getDob().getDob(), null);
            return ir;
        } catch( Exception e ) {
            log.error("FAILED! "+e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * @return
     */
    public String getIdentifyResult() {
        IdentifyResult ir = this.runIdentifyService();
        if( ir == null ) return null;
        String result = "";
        for( URI type : ir.getTypes() ) {
            result += type+" ";
        }
        return result;
    }
    
    /**
     * @return
     */
    public List<FormatBean> getIdentifyResultList() {
        IdentifyResult ir = this.runIdentifyService();
        if( ir == null ) return null;
        List<FormatBean> fmts = new ArrayList<FormatBean>();
        for( URI type : ir.getTypes() ) {
            FormatBean fb = new FormatBean( ServiceBrowser.fr.getFormatForUri( type ) );
            fmts.add(fb);
        }
        return fmts;
    }
    
    /**
     * @return
     */
    public String getIdentifyServiceReport() {
        IdentifyResult ir = this.runIdentifyService();
        if( ir == null ) return null;
        return ""+ir.getReport();
    }
    

}
