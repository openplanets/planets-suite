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
package eu.planets_project.pp.plato.model.scales;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import eu.planets_project.pp.plato.model.values.IntegerValue;
import eu.planets_project.pp.plato.model.values.Value;

/**
 * An integer value to be used in the leaves of the objective tree
 * @author Michael Kraxner
 *
 */
@Entity
@DiscriminatorValue("I")
// We don't use the annotation NotNullField anymore, as the error message doesn't allow
// to specify the name of the leaf. So the error message is not very accurate. As we already
// have all methods available in the base class Scale we use them to check for restriction/unit.
// @NotNullField(fieldname="unit", message="Please enter a unit for the scale of type 'Integer'")
public class IntegerScale extends Scale {

    private static final long serialVersionUID = 8594390834250087870L;

    /**
     * @see eu.planets_project.pp.plato.model.scales.Scale#createValue()
     */
    @Override
    public Value createValue() {
        Value v = new IntegerValue();
        v.setScale(this);
        return v;
    }

    /**
     * @see eu.planets_project.pp.plato.model.scales.Scale#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return "Integer";
    }

    /**
     * @see eu.planets_project.pp.plato.model.scales.Scale#getType()
     */
    @Override
    public ScaleType getType() {
        return ScaleType.value;
    }

    @Override
    public boolean isInteger() {
        return true;
    }
    
    /**
     * @see eu.planets_project.pp.plato.model.scales.Scale#isCorrectlySpecified(java.lang.String, java.util.List)
     */
    @Override
    public boolean isCorrectlySpecified(String leafName,
            List<String> errorMessages) {

        if (getUnit() == null || "".equals(getUnit())) {
            errorMessages.add("Please enter a unit for the scale of type 'Integer' at leaf '" + leafName + "'");
            return false;
        }

        return true;
    }

    /**
     * @see eu.planets_project.pp.plato.model.scales.Scale#isEvaluated(eu.planets_project.pp.plato.model.values.Value)
     */
    @Override
    public boolean isEvaluated(Value value) {
        boolean evaluated = false;
        if ((value != null) && (value instanceof IntegerValue)) {
            IntegerValue v = (IntegerValue)value;

            evaluated = v.isChanged();
        }
        return evaluated;
    }

    /**
     * An {@link IntegerScale} is not restricted.
     * @see eu.planets_project.pp.plato.model.scales.Scale#isRestricted()
     */
    @Override
    public boolean isRestricted() {
        return false;
    }

}
