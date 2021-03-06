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
package at.tuwien.minimee.emulation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import at.tuwien.minimee.ActionService;
import at.tuwien.minimee.model.ToolConfig;
import eu.planets_project.pp.plato.services.PlatoServiceException;
import eu.planets_project.pp.plato.util.PlatoLogger;

/**
 * Currently unused emulation connector that provided the first remote
 * access to GRATE. Now not needed since this became part of the Planets IF
 * @author cbu
 *
 */
@Deprecated
public class EmulationService extends ActionService {

    /**
     * Currently not exposed as a web service since miniMEE
     * has been integrated with Plato.
     * This starts a session with GRATE
     * @param samplename filename of the object to be rendered remotely
     * @param data the file to be rendered remotely
     * @param toolID pointing to the corresponding minimee configuration
     * @return a URL to be posted to the browser for opening a GRATE session.
     * This URL points to a GRATE session that contains the object readily waiting
     * to be rendered, already injected into the appropriate environment.
     * @throws PlatoServiceException if the connection to the GRATE server failed
     */
    public String startSession (String samplename, byte[] data, String toolID) throws PlatoServiceException{
        ToolConfig config = getToolConfig(toolID);
        
        String response;
        try {
            HttpClient client = new HttpClient();
            MultipartPostMethod mPost = new MultipartPostMethod(config.getTool().getExecutablePath());
            client.setConnectionTimeout(8000);


            // MultipartPostMethod needs a file instance
            File sample = File.createTempFile(samplename+System.nanoTime(), "tmp");
            OutputStream out = new BufferedOutputStream(new FileOutputStream(sample));
            out.write(data);
            out.close();
            
            mPost.addParameter("datei", samplename, sample);
            
            int statusCode = client.executeMethod(mPost);
            
            response = mPost.getResponseBodyAsString();
            
            return response+ config.getParams();

        } catch (HttpException e) {
            throw new PlatoServiceException("Could not connect to GRATE.", e);
        } catch (FileNotFoundException e) {
            throw new PlatoServiceException("Could not create temp file.", e);
        } catch (IOException e) {
            throw new PlatoServiceException("Could not connect to GRATE.", e);
        }        
        
    }

    
    /**
     * A small test method
     * @param args not used
     */
    public static void main(String[] args) {
        String url = "http://planets.ruf.uni-freiburg.de/~randy/plato_interface/plato_uploader.php";
        EmulationService emu = new EmulationService();
        File sample = new File("D:/projects/ifs/workspace/plato/data/samples/polarbear1.jpg");

        try {
            byte[] data = getBytesFromFile(sample);
            
            String sessionid = emu.startSession("polarbear1.jpg", data, url);
            System.out.println(sessionid);
        } catch (IOException e) {
            PlatoLogger.getLogger(EmulationService.class).error(e.getMessage(),e);
        } catch (PlatoServiceException e) {
            PlatoLogger.getLogger(EmulationService.class).error(e.getMessage(),e);
        }
        
    }
    
    /**
     * utility method to read a bytestream from a file
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
