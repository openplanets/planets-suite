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
package eu.planets_project.tb.api.data.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.tb.api.model.Experiment;


/**
 * This is the Testbed binary file-store wrapper.  It takes copies of submitted resources 
 * and hold them, each with unique keys.  The unique key must be used to retrieve the files.
 * 
 * @author Andrew Lindley, ARC. Andrew Jackson, BL.
 */
public interface DataHandler {

	/**
	 * Creates a temporary File object in the Tomcats's externally available directory
	 * This might be used for exposing config files, etc. temporarily for download while the backing information is persisted in the db model.
	 * This should only be used for information that doesn't belong in any data registry!
	 * @return
	 */
	public File createTempFileInExternallyAccessableDir() throws IOException; 
	/**
	 * Extracts the external format for this given temporary file as e.g. created by 'createTempFileInExternallyAccessableDir'
	 * @param tempFileInExternalDir
	 * @return
	 * @throws URISyntaxException
	 * @throws FileNotFoundException if the file was already deleted or can't be found
	 */
	public URI getHttpFileRef(File tempFileInExternalDir) throws URISyntaxException, FileNotFoundException;	
	
    /* --- Adding to the repository -- */

    /**
     * This adds a file to the store, and returns it's key.
     */
    public URI storeFile(File f) throws IOException, FileNotFoundException;

    /**
     * 
     * @param u
     * @return
     */
    public URI storeUriContent(URI u) throws IOException, URISyntaxException;
    
    /**
     * 
     * @param in
     * @param name
     * @return
     */
    public URI storeBytestream(InputStream in, String name) throws IOException;
    
    /**
     * @param b
     * @param outputFileName
     * @return
     */
    public URI storeBytearray(byte[] b, String outputFileName) throws IOException;
    
    /**
     * @param The new DigitalObject to be stored in the user's default TB results space.
     * @param The Experiment this digital object was created by.
     * @return The URI of the new storage location.
     */
    public URI storeDigitalObject( DigitalObject dob, Experiment exp );
    
    
    /* -- Getting data back from the repository -- */
    
    /**
     * @param the id to look up.
     * @return a wrapped up Digital Object.
     */
    public DigitalObjectRefBean get( String id ) throws FileNotFoundException;
    
    /**
	 * returns a DigitalObject representation for all local file refs that we're able to find
	 * @param localFileRefs
	 * @return
	 */
    //public List<DigitalObject> convertFileRefsToDigos(Collection<String> localFileRefs);
    
	/**
	 * Copies a file from its local location as a temp file into the system's externally accessible directory
	 * There the file is accessible even if the Testbed stopped. File is deleted on JVM shutdown.
	 * Other solution: download.jsp and DataHandler.get(tbURI).getDownloadUri()
	 * @param localFileRef
	 * @return
	 * @throws IOException
	 */
    public File copyLocalFileAsTempFileInExternallyAccessableDir(String localFileRef)throws IOException;

	/**
	 * This method returns a List of DigitalObjects (content by reference) representation for all local file refs that we're able to access
	 * The boolean trigger allows to expose these files as temporary files in an externally reachable location and adds its content by reference
	 * to this URL. The original file location is kept within the originator field.
	 * @param localFileRefs
	 * @return
	 */
    public List<DigitalObject> convertFileRefsToURLAccessibleDigos(Collection<String> localFileRefs);
    
}
