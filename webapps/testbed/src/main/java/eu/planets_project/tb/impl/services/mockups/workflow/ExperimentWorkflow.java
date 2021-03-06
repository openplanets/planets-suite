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
package eu.planets_project.tb.impl.services.mockups.workflow;

import java.util.HashMap;
import java.util.List;

import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.tb.gui.backing.exp.ExperimentStageBean;
import eu.planets_project.tb.impl.model.measure.MeasurementImpl;

/**
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 * @author <a href="mailto:Andrew.Lindley@ait.ac.at">Andrew Lindley</a>
 *
 */

public interface ExperimentWorkflow {
    
    /**
     * @return A list of all of the properties that can be automatically measured during each stage of this experiment workflow.
     */
    public abstract HashMap<String,List<MeasurementImpl>> getObservables();
    
    /**
     * @return A list of all of the properties that can be manually measured during each stage of this experiment workflow.
     */
    public abstract HashMap<String,List<MeasurementImpl>> getManualObservables();
    
    /**
     * @return A list of the stages involved in this experiment.
     */
    public abstract List<ExperimentStageBean> getStages();
    

    /**
     * @param parameters The set of parameters to use when invoking this workflow.
     * @throws Exception Throws an exception if there are any problems with the parameters.  FIXME Is using an Exception sane?
     */
    @Deprecated
    public void setParameters( HashMap<String,String> parameters ) throws Exception;
    @Deprecated
    public HashMap<String, String> getParameters();

    /**
     * Executes the workflow.
     * 
     * @param dob The Digital Object the workflow should be executed upon.
     * @return The WorkflowResult containing results, measurements, logs etc.
     */
    @Deprecated
    public abstract WorkflowResult execute( DigitalObject dob );
    
}