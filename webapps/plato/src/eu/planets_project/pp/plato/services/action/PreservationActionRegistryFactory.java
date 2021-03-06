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

package eu.planets_project.pp.plato.services.action;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;

import eu.planets_project.pp.plato.util.PlatoLogger;
import eu.planets_project.pp.plato.xml.StrictErrorHandler;
/**
 * Provides a list of defined preservation service registries.
 * Creates instances of preservation service registries. 
 * 
 * @author Michael Kraxner
 *
 */
public class PreservationActionRegistryFactory {
    private static final Log log = PlatoLogger.getLogger(PreservationActionRegistryFactory.class);
    
    /**
     * Returns an instance of a IPreservationActionRegistry, 
     * which is already connected to the remote endpoint.
     * 
     * @param registry defines type and loaction of the registry     
     * @return {@link IPreservationActionRegistry}
     */
    static public IPreservationActionRegistry getInstance(PreservationActionRegistryDefinition registry) throws IllegalArgumentException {
        try {
            Class serviceLocatorClass = Class.forName(registry.getType());
            
            Object serviceLocator = serviceLocatorClass.newInstance();
            if (serviceLocator instanceof IPreservationActionRegistry) {
                IPreservationActionRegistry locator = null;
                locator = (IPreservationActionRegistry)serviceLocator;
                
                locator.connect(registry.getUrl());
                return locator;
            }
            else
                throw new IllegalArgumentException("schema  "+ registry.getType() + " is not a IPreservationActionRegistry.");
        } catch (IllegalArgumentException e) {
            throw e;  
        } catch (Exception e) {
            throw new IllegalArgumentException("schema '" + registry.getType() + "' is not a IPreservationActionRegistry", e);
        }
    }
    /**
     *   
     * @return a list of all in PreservationActionRegistries.xml defined registries.
     * @throws IllegalArgumentException
     */
    static public List<PreservationActionRegistryDefinition> getAvailableRegistries() throws IllegalArgumentException{
        ArrayList<PreservationActionRegistryDefinition> allRegistries = 
            new ArrayList<PreservationActionRegistryDefinition>();

        String configFile = "data/services/PreservationActionRegistries.xml";
        InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
        
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setErrorHandler(new StrictErrorHandler());
        
        digester.push(allRegistries);
        digester.addObjectCreate("*/registry", PreservationActionRegistryDefinition.class);
        digester.addBeanPropertySetter("*/registry/shortname", "shortname");
        digester.addBeanPropertySetter("*/registry/logo", "logo");
        digester.addBeanPropertySetter("*/registry/url", "url");
        digester.addBeanPropertySetter("*/registry/type", "type");
        digester.addBeanPropertySetter("*/registry/active", "active");
        digester.addSetNext("*/registry", "add");

        try {
            digester.setUseContextClassLoader(true);            
            digester.parse(config);
        } catch (Exception e) {
            log.error("Error in config file: " + configFile, e);
        }
        return allRegistries;
        
    }

}
