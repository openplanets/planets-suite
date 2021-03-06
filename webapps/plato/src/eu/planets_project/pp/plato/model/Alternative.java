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

package eu.planets_project.pp.plato.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * Entity describing a preservation alternative. A preservation action can also be a preservation
 * action service, then {@link #action} is set.
 *
 * @author Hannes Kulovits
 */
@Entity
public class Alternative implements Serializable, ITouchable {

    private static final long serialVersionUID = -2995207877066383992L;

    @OneToOne(cascade = CascadeType.ALL)
    private Experiment experiment;

    @OneToOne(cascade = CascadeType.ALL)
    private ResourceDescription resourceDescription;

    @ManyToOne
    private AlternativesDefinition alternativesDefinition;

    private boolean discarded = false;

    public boolean isExecutable() {
        return action != null && action.isExecutable();
    }
    
    /**
     * this is our index when he store the alternative in a list.
     * due to a bug of hibernate http://opensource.atlassian.com/projects/hibernate/browse/HHH-3160
     * we have to maintain the index ourselves
     */
    private long alternativeIndex;

    @NotNull
    @Length(min = 1, max = 30)
    private String name;

    /**
     * standard length for a string column is 255
     * validation is broken because we use facelet templates (issue resolved in  Seam 2.0)
     * therefore allow "long" entries
     */
    @NotNull
    @Length(min = 1, max = 32672)
    @Column(length = 32672)
    private String description;

    @Id
    @GeneratedValue
    private int id;

    @OneToOne(cascade=CascadeType.ALL)
    private ChangeLog changeLog = new ChangeLog();

    /**
     * An alternative might correspond to a preservation action service.
     */
    @OneToOne(cascade = CascadeType.ALL)
    private PreservationActionDefinition action;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Alternative() {

    }

    public Alternative(String name, String description) {
        setName(name);
        setDescription(description);
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public ResourceDescription getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(ResourceDescription resourceDescription) {
        this.resourceDescription = resourceDescription;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Creates an empty alternative.
     * @return {@link Alternative} object
     */
    public static Alternative createAlternative() {
        Alternative alt = new Alternative();
        alt.setResourceDescription(new ResourceDescription());
        alt.setExperiment(new Experiment());
        return alt;
    }

    public AlternativesDefinition getAlternativesDefinition() {
        return alternativesDefinition;
    }

    public void setAlternativesDefinition(
            AlternativesDefinition alternativesDefinition) {
        this.alternativesDefinition = alternativesDefinition;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void setDiscarded(boolean discarded) {
        this.discarded = discarded;
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(ChangeLog value) {
        changeLog = value;
    }

    public boolean isChanged(){
        return changeLog.isAltered();
    }

    public void touch() {
        changeLog.touch();
    }

    /**
     * @see ITouchable#handleChanges(IChangesHandler)
     */
    public void handleChanges(IChangesHandler h) {
        h.visit(this);
        // call handleChanges of all child elementss
        experiment.handleChanges(h);
        resourceDescription.handleChanges(h);
        // manually created alternatives obviously don't have related preservation actions
        if (action != null)
           action.handleChanges(h);
    }

    public PreservationActionDefinition getAction() {
        return action;
    }

    public void setAction(PreservationActionDefinition action) {
        this.action = action;
    }

    public long getAlternativeIndex() {
        return alternativeIndex;
    }

    public void setAlternativeIndex(long alternativeIndex) {
        this.alternativeIndex = alternativeIndex;
    }
    
    public static Alternative createAlternative (String uniqueName, PreservationActionDefinition action) {
        Alternative a = Alternative.createAlternative();
        
        ResourceDescription rd = new ResourceDescription();
        //rd.setConfigSettings(configSettings);
        
        a.setResourceDescription(rd);
        
        a.setName(uniqueName);
        // generate service description
        String descr = action.getInfo()+ " using service at: " + action.getUrl();
        
        if (action.getParameterInfo() != null && !"".equals(action.getParameterInfo())) {                    
            descr += "\n\n" + "Additional information about parameters: \n" + action.getParameterInfo();
        }
        
        a.setDescription(descr);
        a.setAction(action);
        
        return a;
    }    
}
