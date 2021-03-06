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
package eu.planets_project.tb.gui.backing.exp.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import eu.planets_project.tb.impl.model.exec.ExecutionRecordImpl;

/**
 * @author AnJackson
 *
 */
public class ViewResultBean {
    URL endpoint;
    String sessionId;
    URL viewUrl;
    
    public ViewResultBean( String endpoint, String sessionId, String viewUrl ) {
        try {
            this.endpoint = new URL( endpoint );
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        this.sessionId = sessionId;
        try {
            this.viewUrl = new URL( viewUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    public ViewResultBean( URL endpoint, String sessionId, URL viewUrl ) {
        this.endpoint = endpoint;
        this.sessionId = sessionId;
        this.viewUrl =viewUrl;
    }

    /**
     * @return the endpoint
     */
    public URL getEndpoint() {
        return endpoint;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return the viewUrl
     */
    public URL getViewUrl() {
        return viewUrl;
    }

    
    /* ---- */
    
    public static List<ViewResultBean> createResultsFromExecutionRecords( List<ExecutionRecordImpl> execs ) {
        List<ViewResultBean> vurl = new ArrayList<ViewResultBean>();
        for( ExecutionRecordImpl exec : execs ) {
            ViewResultBean vrb = ViewResultBean.createViewResultBeanFromExecutionRecord(exec);
            if( vrb != null ) {
                vurl.add( vrb );
            }
        }
        return vurl;
    }
    
    public static ViewResultBean createViewResultBeanFromExecutionRecord( ExecutionRecordImpl exec ) {
        Properties p = null;
        try {
            p = exec.getPropertiesListResult();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if( p != null ) {
            return 
               new ViewResultBean( 
                    p.getProperty( ExecutionRecordImpl.RESULT_PROPERTY_CREATEVIEW_ENDPOINT_URL),
                    p.getProperty( ExecutionRecordImpl.RESULT_PROPERTY_CREATEVIEW_SESSION_ID ),
                    p.getProperty( ExecutionRecordImpl.RESULT_PROPERTY_CREATEVIEW_VIEW_URL ) 
            );
        }
        /*
        for( ExecutionStageRecordImpl stage : exec.getStages() ) {
            if( stage.getStage().equals( IdentifyWorkflow.STAGE_IDENTIFY )) {
                for( MeasurementImpl m : stage.getMeasurements() ) {
                    if( m.getIdentifier().equals(TecRegMockup.URI_DO_PROP_ROOT+"basic/format")) {
                        frb.add(new FormatResultBean(m.getValue()));
                    }
                }
            }
        }
        */
        return null;
    }
    
}
