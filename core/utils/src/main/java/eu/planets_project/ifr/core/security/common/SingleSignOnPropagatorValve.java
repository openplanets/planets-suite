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
package eu.planets_project.ifr.core.security.common;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

//import eu.planets_project.ifr.core.storage.gui.login.LoginBean;

/**
 * @author AnJackson
 *
 * This code has been placed here so that it ends in the server/lib directory.
 * It needs to be there in order to be picked up during the initialisation of a servlet, before it's jars have been loaded.
 * It may be of general use, in setting up the DR session.
 * 
 * See the SingleSignOn Valve of inspiration: {@link "http://svn.apache.org/repos/asf/tomcat/container/tc5.5.x/catalina/src/share/org/apache/catalina/authenticator/SingleSignOn.java"}
 * 
 * The OpenID login would be another example of code where we can propagate the login: OpenIDLoginAction in IF/security
 * 
 */
public class SingleSignOnPropagatorValve extends ValveBase {
    // A Planets Logger:
    private static Logger log = Logger.getLogger(SingleSignOnPropagatorValve.class.getName());
    
    // The parameters that will store the username and password, usually j_username and j_password.
    private static String USER_PARAM = "josso_username";
    private static String PASS_PARAM = "josso_password";


    /**
     * {@inheritDoc}
     * @see org.apache.catalina.valves.ValveBase#invoke(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response)
     */
    public void invoke(Request request, Response response) throws IOException,
    ServletException { 

        // Find the username and password, if they are there...
        HttpServletRequest hRequest = (HttpServletRequest)request.getRequest();
        String username = (String)hRequest.getParameter(USER_PARAM);
        String password = (String)hRequest.getParameter(PASS_PARAM);
        // Is there a username/password combination going past?
        if( username != null && password != null ) {
            // Use it:
            log.warning("Found user:"+username+" pass:"+password);
            hRequest.getSession().setAttribute("secret_password", password);
            /*
        FacesContext context = FacesContext.getCurrentInstance();
        LoginBean lb = (LoginBean) context.getApplication().getVariableResolver().resolveVariable(context, "LoginBean");
        lb.setUsername(username);
        lb.setPassword(password);
        lb.login();
             */
        } else {
            password = (String) hRequest.getParameter("secret_password");
            if( password != null ) {
                log.warning("Found secret_pass:"+password);
            }
        }

        // Invoke the next Valve in our pipeline
        getNext().invoke(request, response);

        String stored_password = (String) hRequest.getParameter("secret_password");
        if( stored_password != null ) {
            log.warning("Found secret_pass:"+stored_password);
        } else if( username != null ){
            log.warning("Could not find secret_pass:"+password);
            hRequest.getSession().setAttribute("secret_password", password);
        }
    }



}