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
/**
 * 
 */
package eu.planets_project.tb.impl.data;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.storage.api.DataRegistry;
import eu.planets_project.ifr.core.storage.api.DataRegistryFactory;
import eu.planets_project.ifr.core.storage.api.DigitalObjectManager;
import eu.planets_project.tb.api.data.DigitalObjectReference;

/**
 * 
 * This class provides the access point for mapping between the DigitalObjectManager interface and the TB, by
 * creating the DigitalObjectReference beans that are used in the TB interface to explore the DOMs.
 * 
 * The DigitalObjectMultiManager does the actual work.
 * 
 * @author AnJackson
 *
 */
public class DigitalObjectDirectoryLister {
    private static Log log = LogFactory.getLog(DigitalObjectDirectoryLister.class);
    
    // The data sources are managed here:
    DataRegistry dataReg = DataRegistryFactory.getDataRegistry();
    
    /**
     * @return
     */
    public DigitalObjectReference getRootDigitalObject() {
        return new DigitalObjectReference( null );
    }
    
    /**
     * List the contents of one URI as a list or URIs.
     * 
     * @param puri The Planets URI to list. Should point to a directory.
     * @return Returns null if URI is a file or is invalid.
     */
    public DigitalObjectReference[] list( URI puri ) {
        // List from the appropriate registry.
    	log.info("Calling Data Registry List for " + puri);
        List<URI> childs = this.dataReg.list(puri);
        
        if( childs == null ) return new DigitalObjectReference[0];
        
        // Create a DigitalObject for each URI.
        DigitalObjectReference[] dobs = new DigitalObjectReference[childs.size()];
        for( int i = 0; i < childs.size(); i ++ ) {
            // Create a DOB from the URI:
            dobs[i] = new DigitalObjectReference( childs.get(i) );
            
            // Mark that DigitalObject as a Directory if listing it returns NULL:
        	log.info("Calling Data Registry List for " + childs.get(i));
            List<URI> grandchilds = this.dataReg.list(childs.get(i));
            if( grandchilds == null ) {
                dobs[i].setDirectory(false);
            } else {
                dobs[i].setDirectory(true);
            }
        }
        
        // Return the array of Digital Objects:
        return dobs;
    }
    

    /**
     * Can the current user access this resource?
     * @param puri
     * @return
     */
    public boolean canAccessURI( URI puri ) {
        // If the URI sanitiser does not change the URI, then its okay:
        //if( puri == this.checkURI(puri)) return true;
        //return false;
        return true;
    }
    
    /**
     * Utility to get hold of the DataManagerLocal for a URI;
     * @param puri
     * @return
     */
    public DigitalObjectManager getDataManager( URI puri ) {
        return this.dataReg;
    }
    
    
}
