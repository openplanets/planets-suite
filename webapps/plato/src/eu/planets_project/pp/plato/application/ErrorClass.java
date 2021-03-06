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

package eu.planets_project.pp.plato.application;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

import eu.planets_project.pp.plato.model.Plan;

/**
 * @author Florian Motlik
 * 
 * Class for storing Errors to display on admin-utils page
 * 
 * @see eu.planets_project.pp.plato.application.Messages
 * @see eu.planets_project.pp.plato.action.interfaces.IMessages
 */

public class ErrorClass implements Serializable {

    private static final long serialVersionUID = 508829244883298069L;

    /**
     * Type of error. In case of an exception this is the exception class (e.g.
     * NullPointerException)
     */
    private String type = "";

    /**
     * Error message.
     */
    private String message = "";

    /**
     * Time when error occurred.
     */
    private String timestamp = "";

    private String sessionID = "";

    private String user = "";

    private String step = "";

    private String site;

    private int projectId;

    private String projectName;
    
    private String userAgent;
    
    public String getUserAgent() {
        return userAgent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getStep() {
        return step;
    }

    public String getUser() {
        return user;
    }

    /**
     * Default constructor
     * 
     * @param type
     *            error type. In case of an exception this is the canonical name
     *            of the exception class.
     * @param message
     *            message type
     * @param sessionID
     *            session id
     */
    public ErrorClass(String type, String message, String sessionID,
            String user, String site, Plan selectedPlan) {
        this.type = type;
        this.message = message;
        SimpleDateFormat format = new SimpleDateFormat("dd.MMMM.yyyy kk:mm:ss");
        this.timestamp = format.format(new Date(System.currentTimeMillis()));
        this.sessionID = sessionID;
        this.user = user;
        this.site = site;
        
        Map<String,String> headers = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
        String userAgent =  headers.get("user-agent");
        
        if (userAgent == null) {
            userAgent = "Unknown";
        }
        
        this.userAgent = userAgent;
        
        if (selectedPlan != null) {
            this.step = selectedPlan.getStateName();
            this.projectId = selectedPlan.getId();
            this.projectName = selectedPlan.getPlanProperties().getName();
        }
    }

    public String getSite() {
        return site;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

}