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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import eu.planets_project.pp.plato.model.values.IOrdinalValue;
import eu.planets_project.pp.plato.model.values.OrdinalValue;
import eu.planets_project.pp.plato.model.values.Value;

/**
 * @author Christoph Becker
 * This class represents ordinal scale. Derived from @link {@link RestrictedScale} which it uses
 * for the possible values, this is also the base class for @link {@link BooleanScale} and @link {@link YanScale}.
 */
@Entity
@DiscriminatorValue("O")
public class OrdinalScale extends RestrictedScale {

    private static final long serialVersionUID = 6810621867093652338L;

    private String restriction;

    public String getDisplayName() {
        return "Ordinal";
    }

    public OrdinalValue createValue() {
        OrdinalValue v = new OrdinalValue();
        v.setScale(this);
        return v;
    }

    @Override
    public void setRestriction(String restriction) {
        // make sure the transient restriction-list is updated on demand
        this.list = null;

        if (restriction == null) {
            // No text-input.
            this.restriction = null;
        } else {

            // Remove leading and trailing whitespace
            String validatedRestriction = "";
            for (String value : restriction.split(SEPARATOR)) {
                String trimmedValue = value.trim();
                if (trimmedValue.length() > 0) {
                    validatedRestriction += trimmedValue + SEPARATOR;
                }
            }

            if (validatedRestriction.length() == 0) {
                this.restriction = null;
            } else {
                // Remove last trailing SEPARATOR and store.
                this.restriction = validatedRestriction.substring(0,
                        validatedRestriction.length() - 1);
            }
        }
    }

    @Override
    protected boolean restrictionIsValid(String leafName,
            List<String> errorMessages) {
        if (getRestriction() == null || "".equals(getRestriction())) {
            errorMessages.add("Please enter a restriction for the scale of type 'Ordinal' at leaf '" + leafName + "'");
            return false;
        }

        List<String> values = this.getList();
        if (values.size() < 2) {
            errorMessages
                    .add("At least 2 possible ordinal values have to be defined for leaf \""
                            + leafName + "\"");
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).length() == 0) {
                errorMessages
                        .add("The empty String is not a valid value for an Ordinal Scale (used in leaf \""
                                + leafName + "\")");
                return false;
            } else if (values.lastIndexOf(values.get(i)) > i) {
                errorMessages
                        .add("\""
                                + values.get(i)
                                + "\" is specified as a possible Ordinal Value of leaf \""
                                + leafName + "\" more than once!");
                return false;
            }
        }
        return true;
    }

    @Override
    public String getRestriction() {
        return restriction;
    }

    @Override
    public String getReadableRestriction() {
        String niceFormat = this.restriction.replaceAll(Scale.SEPARATOR, ", ");
        int lastIndex = niceFormat.lastIndexOf(", ");
        return niceFormat.substring(0, lastIndex) + " or " + niceFormat.substring(lastIndex+2);
    }

    /**
     * @return a separated list of all possible values as specified in the {@link #restriction}
     */
    @Override
    public List<String> getList() {
        // Because the list is transient, we might have to recreate or refill
        // it!
        if (list == null || list.size()<=0) {
            list = new ArrayList<String>();
            if (restriction != null) {
                for (String s : restriction.split(SEPARATOR)) {
                    list.add(s);
                }
            }
        }
        return list;
    }

   
    @Override
    public Scale clone() {
        OrdinalScale clone = (OrdinalScale)super.clone();
        // possible restriction values are kept in a list, recreate it the next time it is used
        clone.list = null;
        return clone;
    }

    @Override
    public ScaleType getType() {
        return ScaleType.ordinal;
    }

    /**
     * checks if the provided Value is not null and conforms to this scale, i.e. is
     * an @link {@link OrdinalValue}, <b>and</b> that its actual value is one of the values
     * specified (as in {@link #getList()}) 
     */
    @Override
    public boolean isEvaluated(Value value) {
        boolean evaluated = false;
        if ((value != null) && (value.getScale().getType() == ScaleType.ordinal)) {
            evaluated = value.isChanged() && true && 
               getList().contains(((IOrdinalValue)value).getValue());
        }
        return evaluated;
    }

}
