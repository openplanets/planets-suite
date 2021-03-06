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
package eu.planets_project.tb.impl.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author alindley
 *
 */
@Entity
@XmlAccessorType(XmlAccessType.FIELD) 
public class ExperimentResourcesImpl implements
		eu.planets_project.tb.api.model.ExperimentResources,
		java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7590148290731269041L;
	@Id
	@GeneratedValue
	@XmlTransient
	private long lExpRessourceID;
	private int iNumberOfOutputFiles;
	private int iIntensity;
	
	public ExperimentResourcesImpl(){
		lExpRessourceID = -1;
		iNumberOfOutputFiles = -1;
		iIntensity = -1;
	}
	
	public long getExperimentResourcesID(){
		return this.lExpRessourceID;
	}
	
	@SuppressWarnings("unused")
	private void setExperimentResourcesID(long lID){
		this.lExpRessourceID = lID;
	}
	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentResources#getNumberOfFiles()
	 */
	public int getNumberOfOutputFiles() {
		return this.iNumberOfOutputFiles;
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentResources#setNumberOfFiles(int)
	 */
	public void setNumberOfOutputFiles(int nr) {
		this.iNumberOfOutputFiles = nr;
	}
	
	public void setIntensity(int iIntensity) {
		this.iIntensity = iIntensity;
	}
	public int getIntensity() {
		return this.iIntensity;
	}
	

}
