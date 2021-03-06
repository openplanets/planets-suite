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
package eu.planets_project.tb.api.model;

import java.util.List;
import java.util.Map;

import eu.planets_project.tb.impl.exceptions.ExperimentNotFoundException;
import eu.planets_project.tb.impl.exceptions.InvalidInputException;

/**
 * @author alindley
 *
 */
public interface BasicProperties{
	
	/**
	 * It's only allowed to set a unique experimentName
	 * @param sName
	 */
	public void setExperimentName(String sName) throws InvalidInputException;
	public String getExperimentName();
	public boolean checkExperimentNameUnique(String sExpName);
	
	/**
	 * Allows to specify experiments that were an influence, starting point, etc. for this current one.
	 * @param sRefIDs experimentID
	 */
	public void setExperimentReferences(List<Long> sRefIDs);
	public void setExperimentReference(long sRefID);
	public void addExperimentReference(long sRefID);
	public void removeExperimentReference(long sRefID);
	public void setExperimentReference(Experiment refExp);
	public void setExperimentReferences(Experiment[] refExps);
	public List<Long> getExperimentReferences();
	public Map<Long,Experiment> getReferencedExperiments();
	public List<Long> getReferencedExperimentIDs();
	
	public void setExperimentStructureReference(Experiment expStructure) throws InvalidInputException;
	public void setExperimentStructureReference(long expID) throws ExperimentNotFoundException;
	public void removeExperimentStructureReference();
	public Experiment getExperimentStructureReference() throws ExperimentNotFoundException;
	
	public void setSummary(String sSummary);
	public String getSummary();
	
	/**
	 * Should default to the current user's information
	 * @param sName
	 * @param sMail
	 * @param sAddress
	 */
	public void setContact(String sName, String sMail, String sTel, String sAddress);
	public String getContactName();
	public String getContactMail();
	public String getContactTel();
	public String getContactAddress();
	
	public void setPurpose(String sPurpose);
	public String getPurpose();
	
	public void setIndication(String sDescription);
	public String getIndication();
	
	/**
	 * The Object Type  will e.g. specify a experiment on "jpeg" images � but does not contain
	 * any reference to the actual data which is part of the Design Experiment stage.
	 * 
	 * @param sMimeType: formating string/string is checked
	 */
	public void setExperimentedObjectType(String sMimeType) throws InvalidInputException;
	public void setExperimentedObjectTypes(List<String> sMimeTypes) throws InvalidInputException;
	public void addExperimentedObjectType(String mimeType) throws InvalidInputException;
	public void removeExperimentedObjectType(String mimeType) throws InvalidInputException;
	public List<String> getExperimentedObjectTypes();
	
	public void setFocus(String sFocus);
	public String getFocus();

	public void setScope(String sScope);
	public String getScope();

	public void setExperimenter(String sUserID);
	public String getExperimenter();
	
	public void addInvolvedUser(String sUserID);
	public void removeInvolvedUser(String sUserID);
	
	public void addInvolvedUsers(List<String> usersIDs);
	public void removeInvolvedUsers(List<String> userIDs);
	
	public List<String> getInvolvedUserIds();
	
	/**
	 * A user may take a seperate role (besides his overall Testbed role) for an
	 * experiment. E.g. he/she could beReader, but within the given context of a certain
	 * Experiment he/she may act as Experimenter.
	 * @param hUserIDsAndExperimentRoles Hashtable<userID,roleID>
	 * @see eu.planets_project.TB.data.model.finals.TestbedRoles
	 */
//	public void addInvolvedUsersWithSpecialExperimentRole(HashMap<Long,Vector<Integer>> hmUserIDsAndExperimentRoles);
//	public void removeInvolvedUsersAndSpecialExperimentRole(HashMap<Long,Vector<Integer>> hmUserIDsAndExperimentRoles);
	/**
	 * Sets the approache's ID with an experimentType
	 * @param iID
	 */
	public void setExperimentApproach(String sExperimentTypeID) throws InvalidInputException;
	/**
	 * Returns the approache's ID.
	 * @return
	 */
	public String getExperimentApproach();
	/**
	 * Returns the corresponding name for a given ExperimentTypeID.
	 * e.g. "simple migration" for "experimentTypes.simpleMigration" 
	 * @param iID ExperimentApproach ID.
	 * @return "migration", "emulation" or null
	 */
	public String getExperimentApproachName(String experimentTypeID);
	
	public void setConsiderations(String sConsid);
	public String getConsiderations();
	
	/**
	 * An experiment may either be formal or informal.  
	 * A formal experiment is available for other users 
	 * whereas an informal experiment will only be visible to the owner.
	 * @param bFormal
	 */
	public void setExperimentFormal(boolean bFormal);
	public boolean isExperimentFormal();
	public boolean isExperimentInformal();
	
	/**
	 * This method is used to specify an external ID for the experiments.  
	 * Note that the Testbed will automatically generate a Testbed specific ID for each experiment 
	 * so this method should only be used if one wishes to tie an experiment to an external reference/system. 
	 * @param sRefName
	 */
	public void setExternalReferenceID(String sRefName);
	public String getExternalReferenceID();
	
	/**
	 * If the experiment references any papers, books or web pages you can add references to them here.
	 * @param sDesc
	 * @param URI
	 * @param sTitle
	 * @param sAuthor
	 */
    public void addLiteratureReference(String sDesc, String URI, String sTitle, String sAuthor);
    public void addLiteratureReference(String sDesc, String URI);
	public void removeLiteratureReference(String sDesc, String URI);

	/**
	 * @param references String[0]=desc, String[1]=URI, String[2]=title, String[3]=author
	 */
	public void setLiteratureReferences(List<String[]> references) throws InvalidInputException;;
	/**
	 * @return String[0]=desc, String[1]=URI, String[2]=title, String[3]=author
	 */
	public List<String[]> getAllLiteratureReferences();

	
	/**The Tool Type will specify for example a "jpeg2pdfMigration" experiment � but does not contain
	 * any reference to actual tools instances, which is part of the Design Experiment stage.
	 * @param toolTypes: requires to be in the format which is accepted and known by the service registry
	 **/
	public void setToolTypes(List<String> toolTypes);
	public void addToolType(String toolType);
	public void removeToolType(String toolType);
	public List<String> getToolTypes();
        
	/**The Digi Type will hold the digital object types selected for an experiment.
	 * These will be used to limit the types of Benchmark Goals that are avaialble for selection
         * in Stage 3 of the experiment process.
	 * @param digiTypes: this stores the unique ID for all select digital object types.
	 **/
	public void setDigiTypes(List<String> digiTypes);
	public void addDigiType(String digiType);
	public void removeDigiType(String digiType);
	public List<String> getDigiTypes();
	
}
