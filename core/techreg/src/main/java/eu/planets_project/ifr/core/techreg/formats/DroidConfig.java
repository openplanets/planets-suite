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
package eu.planets_project.ifr.core.techreg.formats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import eu.planets_project.ifr.core.common.conf.Configuration;
import eu.planets_project.ifr.core.common.conf.ConfigurationException;
import eu.planets_project.ifr.core.common.conf.ServiceConfig;

/**
 * Droid configuration settings.
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
class DroidConfig {
	/** The logger */
    private static Logger log = Logger.getLogger(DroidConfig.class.getName());

    /** Properties keys for DROID sig file information */
    private static final String COMMON_CONF_FILE_NAME = "Common";
    private static final String SIG_FILE_LOC_KEY = "droid.sigfile.location";
    private static final String SIG_FILE_NAME_KEY = "droid.sigfile.name";
    /**
     * @return The location of the DROID signature file taken from the
     * 		   Droid configuration properties file
     */
    public static String getSigFileLocation() {
    	// String to hold the location
        String sigFileLocation = null;
        // Get the configuration object from the ServiceConfig util
        try {
        Configuration conf = ServiceConfig.getConfiguration(COMMON_CONF_FILE_NAME);
        // Create the file name from the properties
        sigFileLocation = conf.getString(SIG_FILE_LOC_KEY) +
        		File.separator + conf.getString(SIG_FILE_NAME_KEY);
        } catch( ConfigurationException e ) {
        	log.severe("Could not find configuration file! "+e);
        }
        log.info("DROID Signature File location:" + sigFileLocation);
        // Check if the sigFileLocation is sane, and override with internal resource if not:
        File sfl = null;
        if( sigFileLocation != null ) sfl = new File(sigFileLocation);
        if( sfl == null || ! sfl.exists() || ! sfl.isFile() ) {
			try {
	        	File tmp =  File.createTempFile("DroidSigFile", "xml");
	        	tmp.deleteOnExit();
	        	IOUtils.copy( DroidConfig.class.getResourceAsStream("/droid/DROID_SignatureFile.xml"), new FileOutputStream(tmp));
	        	sigFileLocation = tmp.getAbsolutePath();
	        	log.info("Wrote cached Droid sig file to "+sigFileLocation);
			} catch (IOException e) {
				e.printStackTrace();
				log.severe("Could not generate external Droid Sig File.");
			}
        }
        return sigFileLocation;
    }

}
