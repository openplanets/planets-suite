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

package eu.planets_project.pp.plato.model.tree;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.hibernate.validator.Valid;

import eu.planets_project.pp.plato.evaluation.MeasurementsDescriptor;
import eu.planets_project.pp.plato.model.Alternative;
import eu.planets_project.pp.plato.model.Values;
import eu.planets_project.pp.plato.model.scales.Scale;
import eu.planets_project.pp.plato.model.values.Value;
import eu.planets_project.pp.plato.util.MeasurementInfoUri;
import eu.planets_project.pp.plato.util.PlatoLogger;

/**
 * This class is the container of the ObjectiveTree
 * @author Christoph Becker
 * @author Florian Motlik
 * @author Kevin Stadler
 * @see TreeNode
 * @see Node
 * @see Leaf
 */
@Entity
public class ObjectiveTree implements Serializable {

    private static Log log = PlatoLogger.getLogger(ObjectiveTree.class);

    private static final long serialVersionUID = -4894131307261590502L;

    /**
     * indicates where the initial balancing of weights in the objective tree
     * has been performed yet. 
     * @see #initWeights()
     */
    private boolean weightsInitialized = false;

    /**
     * Checks if weights have been initialised ({@link #weightsInitialized}), 
     * and if not, performs initialisation and sets {@link #weightsInitialized} to true.
     * The initialisation sets the weights of all leaves in the tree, i.e.
     * distributes all weights equally.
     * @see TreeNode#initWeights()
     * @see #weightsInitialized
     */
    public void initWeights() {
        if (this.isWeightsInitialized()) {
            log.debug("Weights already initialized");
        } else {
            log.debug("Initializing weights for the first time");
            root.setWeight(1.0);
            root.initWeights();
            this.setWeightsInitialized(true);
        }
    }
 
    /**
     * inits the value objects throughout the tree.
     * @see #initValues(List, int, boolean)
     * @param list of Alternatives
     * @param records number of samples
     */
    public void initValues(List<Alternative> list, int records) {
        initValues(list, records, false);
    }

    /**
     * inits the value objects throughout the tree.
     * @see Leaf#initValues(List, int, boolean)
     * @param list
     * @param records
     * @param initLinkage
     */
    public void initValues(List<Alternative> list, int records, boolean initLinkage) {
        root.initValues(list, records, initLinkage);
    }

    /**
     * reference to the root node of the objective tree.
     */
    @Valid
    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    private TreeNode root;

    @Id @GeneratedValue
    private int id;
    
    
    @Transient
    private boolean mappingExistent;    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * empty default constructor
     *
     */
    public ObjectiveTree() {
    }

    /**
     * Checks the whole tree whether evaluation values for the given records and alternative exist.
     * This function is used then asking the user before removing
     * a record or alternative and the values associated with it from the objective tree.
     * @param records indices of sample records in the records list that should be checked
     * @param a the Alternative, or null if all alternatives should be checked
     * @return true if any leaf of the tree contains a value for the given records and alternative
     */
    public boolean hasValues(int records[], Set<String> checkAlternatives) {

        List<Leaf> list = root.getAllLeaves();

        if (list.size() > 0 && records.length > 0) {

            for (Leaf l : list) {
                for (String alt : checkAlternatives) {
                    if (hasValuesForRecords(l.getValues(alt), records)) {
                        return true;
                    }
                }
            }

        }

        return false;
    }

    /**
     * Checks whether the given Values-Object contains values for the specified records
     * @param values the Values-Object to be checked
     * @param records an array of record-indices
     * @return true if the Values-Object contains values at any of the indices
     */
    private boolean hasValuesForRecords(Values values, int records[]) {
        if (values != null) {
            List<Value> list = values.getList();
            for (int record : records) {
                if (record < list.size() && list.get(record) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRoot() {
        return root;
    }

    /**
     * prints debug information for the whole tree 
     */
    public void debug() {
        debug(root);
    }

    /**
     * recursively prints debug information on the provided node and its children
     */
    private void debug(TreeNode node) {
        if (node instanceof Leaf) {
            Leaf l = (Leaf)node;
            log.debug( "leaf: " + node.getName() + " has " + l.getValueMap().size() + " values.");
            for (String a : l.getValueMap().keySet()) {
                log.debug( "---" + a);
            }
        }
        for ( TreeNode n : node.getChildren() ) {
            debug(n);
        }
    }

    /**
     * removes associated evaluation {@link Values} for a given list of alternatives
     * and a give record index from all leaves in this tree.
     * @param list list of Alternatives for which values shall be removed
     * @param record index of the record for which  values shall be removed
     * @see Leaf#removeValues(List, int)
     */
    public void removeValues(List<Alternative> list, int record) {
        for (Leaf l : root.getAllLeaves())  {
           l.removeValues(list, record);
        }
    }
    
    
    /**
     * Removes all evaluation-values associated with the given alternative
     * from all leaves of the tree
     */
    public void removeValues(Alternative a) {
        for (Leaf l : root.getAllLeaves()) {
           l.getValueMap().remove(a.getName());
        }
    }
    

    /**
     * checks if this tree is specifed completely (i.e. all nodes have some leaves as 
     * children and all leaves have a scale)
     * @param errorMessages the list to which error messages are appended
     * @see TreeNode#isCompletelySpecified(List)
     * @see Leaf#isCompletelySpecified(List)
     */
    public boolean isCompletelySpecified(List<String> errorMessages){
        return root.isCompletelySpecified(errorMessages);
    }

    /**
     * checks if this tree is evaluated completely (i.e. all leaves have
     * evaluation values for all alternatives and records)
     * @param inputList the list to which error messages are appended
     * @see TreeNode#isCompletelyEvaluated(List, List, List)
     * @see Leaf#isCompletelyEvaluated(List, List, List)
     */
    boolean isCompletelyEvaluated(List<Alternative> inputList){
        return false;
    }

    public boolean isWeightsInitialized() {
        return weightsInitialized;
    }

    public void setWeightsInitialized(boolean weightsInitialized) {
        this.weightsInitialized = weightsInitialized;
    }

    public boolean isMappingExistent() {
        for (Leaf leaf : getRoot().getAllLeaves()) {
            if (leaf.isMapped())
                return true;
        }
        return false;
    }

    /**
     * this method iterates through all leaves and updates the value maps,
     * changing the name of the alternative to the new one.
     * @param oldName old name to be updated
     * @param newName new name to be used instead of oldName
     * @see Leaf#updateAlternativeName(String, String)
     */
    public void updateAlternativeName(String oldName, String newName) {
        for (Leaf leaf: root.getAllLeaves()) {
            leaf.updateAlternativeName(oldName,newName);
        }
    }

    /**
     * <ul>
     * <li>
     * removes all {@link Values} from all leaves which are not mapped by one of the 
     * names provided in the list
     * </li>
     * <li>
     * removes all {@link Value} objects in the {@link Values} which are out of the index of 
     * the sample records (which should not happen, but apparently we have some projects where this
     * is the case), or where a leaf is single and there is more than one {@link Value}
     * </li>
     * </ul>
     * @param alternatives list of names of alternatives
     * @param records number of records in the project
     * @return number of {@link Values} and {@link Value} objects removed (sum of instances)
     * @see Leaf#removeLooseValues(List)
     */
    public int removeLooseValues(List<String> alternatives, int records) {
        int number = 0;
        for (Leaf l: getRoot().getAllLeaves()) {
            number += l.removeLooseValues(alternatives, records);
        }
        return number;
    }

    /**
     * This makes sure that the scales of all leaves match the scales implied by the measurement info uri 
     * Can e.g. be used after freemind import, to complete the measurement info
     */
    public void adjustScalesToMeasurements(MeasurementsDescriptor descriptor) {
        for (Leaf l : getRoot().getAllLeaves()) {
            MeasurementInfoUri mInfo = l.getMeasurementInfo().toMeasurementInfoUri();
            if (mInfo.getAsURI() != null) {
                Scale s = descriptor.getMeasurementScale(mInfo);
                if (s != null) {
                    l.adjustScale(s);
                }
            }
        }
    }    
    
}
