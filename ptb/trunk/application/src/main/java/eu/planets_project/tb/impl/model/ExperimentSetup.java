/**
 * 
 */
package eu.planets_project.tb.impl.model;

import java.util.GregorianCalendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import eu.planets_project.tb.impl.model.BasicProperties;
import eu.planets_project.tb.impl.model.ExperimentObjectives;
import eu.planets_project.tb.impl.model.ExperimentResources;
import eu.planets_project.tb.impl.model.finals.ExperimentTypes;
import eu.planets_project.tb.impl.services.mockups.ComplexWorkflow;

/**
 * @author alindley
 *
 */
@Entity
public class ExperimentSetup extends ExperimentPhase implements
		eu.planets_project.tb.api.model.ExperimentSetup,
		java.io.Serializable{
	
	//private eu.planets_project.tb.api.model.BasicProperties basicProperties;
	@OneToOne(cascade={CascadeType.ALL})
	private eu.planets_project.tb.impl.model.BasicProperties basicProperties;
	@OneToOne(cascade={CascadeType.ALL})
	private eu.planets_project.tb.impl.model.ExperimentResources experimentResources;
	private eu.planets_project.tb.api.model.ExperimentObjectives experimentObjectives;
	private eu.planets_project.tb.api.services.mockups.ComplexWorkflow complexWorkflow;
	private int iExperimentTypeID;
	private String sExperimentTypeName;

	
	public ExperimentSetup(){
		basicProperties = new BasicProperties();
		experimentResources = new ExperimentResources();
		experimentObjectives = new ExperimentObjectives();
		complexWorkflow = new ComplexWorkflow();
	}
	

	public eu.planets_project.tb.api.model.BasicProperties getBasicProperties() {
		return this.basicProperties;
	}

	public eu.planets_project.tb.api.model.ExperimentResources getExperimentResources() {
		return this.experimentResources;
	}

	public int getExperimentTypeID() {
		return this.iExperimentTypeID;
	}

	public String getExperimentTypeName() {
		return this.sExperimentTypeName;
	}

	public eu.planets_project.tb.api.services.mockups.ComplexWorkflow getExperimentWorkflow() {
		return this.complexWorkflow;
	}

	public eu.planets_project.tb.api.model.ExperimentObjectives getExperimentedObjectives() {
		return this.experimentObjectives;
	}

	public void setExperimentResources(eu.planets_project.tb.api.model.ExperimentResources experimentResources) {
		this.experimentResources = (ExperimentResources)experimentResources;
	}
	
	public void setBasicProperties(eu.planets_project.tb.api.model.BasicProperties props) {
		this.basicProperties = (BasicProperties) props;
	}

	public void setExperimentType(int typeID) {
		ExperimentTypes finalsExperimentTypes = new ExperimentTypes();
		boolean bOK= finalsExperimentTypes.checkExperimentTypeIDisValid(typeID);
		if  (bOK){
			this.iExperimentTypeID = typeID;
			this.sExperimentTypeName = finalsExperimentTypes.getExperimentTypeName(typeID);
		}
	}


	public void setExperimentedObjectives(
			eu.planets_project.tb.api.model.ExperimentObjectives exploredObjectives) {
		this.experimentObjectives = exploredObjectives;
	}

	public void setWorkflow(eu.planets_project.tb.api.services.mockups.ComplexWorkflow workflow) {
		this.complexWorkflow = workflow;
	}
	
}