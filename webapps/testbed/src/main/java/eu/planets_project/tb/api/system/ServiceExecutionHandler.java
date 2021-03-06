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
package eu.planets_project.tb.api.system;

import eu.planets_project.tb.api.model.Experiment;

/**
 * 
 * @author alindley
 *
 */
public interface ServiceExecutionHandler {
	
	/**
	 * runs the migration/characterisation experiment's service
	 * i.e. gather data, transform in proper format, invoke service, extract results, writedata back into the experiment executable, etc.
	 * @param exp
	 */
	public void executeExperiment(Experiment exp);
	
	/**
	 * Runs the provided services for automatically evaluating an experiment
 	 * i.e. gather data, transform in proper format, invoke service, extract results, write data back to BenchmarkGoals, write an EvaluationExecutable for metadata, etc.
	 * @param exp
	 */
	public void executeAutoEvalServices (Experiment exp);
	
	public void executeExperimentAndAutoEvalServices (Experiment exp);
}
