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

package eu.planets_project.pp.plato.model.aggregators;

import java.io.Serializable;

import eu.planets_project.pp.plato.model.Alternative;
import eu.planets_project.pp.plato.model.tree.TreeNode;

/**
 * This interface defines the aggregation function 
 * to be applied on a TreeNode
 * 
 * @author Christoph Becker
 */
public interface IAggregator extends Serializable {
    public double getAggregatedValue(TreeNode n, Alternative a);
}
