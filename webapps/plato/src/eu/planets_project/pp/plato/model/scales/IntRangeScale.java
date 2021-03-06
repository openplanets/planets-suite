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
import java.util.regex.Pattern;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import eu.planets_project.pp.plato.model.values.IntRangeValue;
import eu.planets_project.pp.plato.model.values.Value;
import eu.planets_project.pp.plato.util.PlatoLogger;

/**
 * Integer value within a specified range, to be used in the leaves of the objective tree
 * @author Christoph Becker
 *
 */
@Entity
@DiscriminatorValue("R")
// We don't use the annotation NotNullField anymore, as the error message doesn't allow
// to specify the name of the leaf. So the error message is not very accurate. As we already
// have all methods available in the base class Scale we use them to check for restriction/unit.
// @NotNullField(fieldname="restriction", message="Please enter a restriction for the scale of type 'Int Range'.")
public class IntRangeScale extends RestrictedScale {

    private static final long serialVersionUID = -857528692185469821L;

    public  String getDisplayName() {
        return "Restricted integer";
    }

    public IntRangeValue createValue() {
        IntRangeValue v = new IntRangeValue();
        v.setScale(this);
        return v;
    }
    
    @Override
    public boolean isInteger() {
        return true;
    }

    /**
     * This checks the provided restriction, and iff it is valid,
     * this.restriction is set to the provided value.
     * @param restriction new restriction to set
     * @return true if restriction conforms to the {@link #pattern}, in which case the restriction is set;
     * false otherwise, in which case the restriction is not set
     */
    public boolean validateAndSetRestriction(String restriction)
    {
        if (p.matcher(restriction).matches()) {
            setRestriction(restriction);
            return true;
        } else {
            return false;
        }

    }

    public static final String intPattern = "[-+]?(\\d)+";
    /**
     * regular expression pattern for matching input format strings
     * @see Pattern#compile(String)
     * @see #p
     */
    public static final String pattern = intPattern + Scale.SEPARATOR + intPattern;

    /**
     * Regexp pattern compiled from {@link #pattern}
     */
    private static Pattern p = Pattern.compile(pattern);

    private int lowerBound = 0;
    private int upperBound = 5;

    public void setLowerBound(int lower) {
        this.lowerBound = lower;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public void setUpperBound(int upper) {
        this.upperBound = upper;
    }

    public int getUpperBound() {
        return upperBound;
    }

    @Override
    public String getRestriction() {
        if ((lowerBound == Integer.MIN_VALUE)&&(upperBound == Integer.MAX_VALUE))
            return null;
        else
            return Integer.toString(lowerBound)+Scale.SEPARATOR+Integer.toString(upperBound);
    }

    @Override
    public String getReadableRestriction() {
        return "between " + this.lowerBound + " and " + this.upperBound;
    }

    @Override
    public void setRestriction(String restriction) {
        if (restriction == null) {
            return;
        }
        String[] s =  restriction.split(Scale.SEPARATOR);
        try {
            if (s.length == 2) {
                // keep old values if restriction is not valid
                int lower = Integer.parseInt(s[0]);
                int upper = Integer.parseInt(s[1]);
                setLowerBound(lower);
                setUpperBound(upper);
            }
        } catch (NumberFormatException e) {
            PlatoLogger.getLogger(getClass()).warn("Ignoring invalid numberformat in setRestriction:"+restriction);
        }
    }

    @Override
    protected boolean restrictionIsValid(String leafName, List<String> errorMessages) {
        if (getRestriction() == null || "".equals(getRestriction())) {
            errorMessages.add("Please enter a restriction for the scale of type 'Int Range' at leaf '" + leafName + "'");
            return false;
        }

        if (this.lowerBound >= this.upperBound) {
            errorMessages.add("The lower bound specified for leaf \"" + leafName + "\" is greater or equal its upper bound!");
            return false;
        }
        return true;
    }

    @Override
    public boolean isEvaluated(Value value) {
        boolean evaluated = false;
        if ((value != null) && (value instanceof IntRangeValue)) {
            IntRangeValue v = (IntRangeValue)value;

            evaluated = value.isChanged() &&
            (v.getValue() >= getLowerBound() && v.getValue() <= getUpperBound());
        }
        return evaluated;
    }
}
