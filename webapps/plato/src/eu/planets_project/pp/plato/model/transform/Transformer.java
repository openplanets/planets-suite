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
package eu.planets_project.pp.plato.model.transform;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;

import eu.planets_project.pp.plato.model.ChangeLog;
import eu.planets_project.pp.plato.model.IChangesHandler;
import eu.planets_project.pp.plato.model.ITouchable;
import eu.planets_project.pp.plato.model.Values;
import eu.planets_project.pp.plato.model.values.INumericValue;
import eu.planets_project.pp.plato.model.values.IOrdinalValue;
import eu.planets_project.pp.plato.model.values.TargetValues;
import eu.planets_project.pp.plato.model.values.Value;

/**
 * Implements basic transformation functionality, i.e. aggregation over {@link Values} and
 * common properties of transformers.
 * @author Hannes Kulovits
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "type")
public abstract class Transformer implements ITransformer, Serializable, ITouchable
{
    private static final long serialVersionUID = -3708795251848706848L;

    @Id
    @GeneratedValue
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(cascade=CascadeType.ALL)
    private ChangeLog changeLog = new ChangeLog();

    /**
     * Transforms all the values in the list of the provided {@link Values}.
     * According to the type of each {@link Value}, either
     * {@link ITransformer#transform(INumericValue)} or {@link ITransformer#transform(IOrdinalValue)}
     * is called.
     * @param values List of values to be transformed
     * @return {@link TargetValues}, which contains a list of all transformed values corresponding to the provided input
     */
    public TargetValues transformValues(Values values) {
        TargetValues result = new TargetValues();
        for (Value v : values.getList()) {
            if (v instanceof INumericValue) {
                result.add(transform((INumericValue) v));
            } else {
                result.add(transform((IOrdinalValue) v));
            }
        }
        return result;
    }

    public ChangeLog getChangeLog() {
        return this.changeLog;
    }

    public void setChangeLog(ChangeLog value) {
        changeLog = value;
    }

    public boolean isChanged() {
        return changeLog.isAltered();
    }
    
    public void touch(String username) {
        getChangeLog().touch(username);
    }
    
    public void touch() {
        getChangeLog().touch();
    }

    /**
     * @see ITouchable#handleChanges(IChangesHandler)
     */
    public void handleChanges(IChangesHandler h){
        h.visit(this);
    }
    /**
     * If this Transformer is not correctly configured, this method adds
     * an appropriate error-message to the given list and returns false.
     *
     * @return true if this transformer is correctly configured
     */
    public abstract boolean isTransformable(List<String> errorMessages);

    public abstract Transformer clone(); 
}
