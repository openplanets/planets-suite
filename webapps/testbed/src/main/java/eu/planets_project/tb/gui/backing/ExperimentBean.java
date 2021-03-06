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
package eu.planets_project.tb.gui.backing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.servlet.ServletUtilities;

import eu.planets_project.ifr.core.security.api.model.User;
import eu.planets_project.ifr.core.security.api.services.UserManager;
import eu.planets_project.tb.api.TestbedManager;
import eu.planets_project.tb.api.data.util.DataHandler;
import eu.planets_project.tb.api.data.util.DigitalObjectRefBean;
import eu.planets_project.tb.api.model.BasicProperties;
import eu.planets_project.tb.api.model.Experiment;
import eu.planets_project.tb.api.model.ExperimentEvaluation;
import eu.planets_project.tb.api.model.ExperimentExecutable;
import eu.planets_project.tb.api.model.ExperimentPhase;
import eu.planets_project.tb.api.model.ExperimentReport;
import eu.planets_project.tb.api.model.ExperimentSetup;
import eu.planets_project.tb.api.model.benchmark.BenchmarkGoal;
import eu.planets_project.tb.api.model.ontology.OntologyProperty;
import eu.planets_project.tb.api.services.ServiceTemplateRegistry;
import eu.planets_project.tb.api.services.TestbedServiceTemplate;
import eu.planets_project.tb.api.services.TestbedServiceTemplate.ServiceOperation;
import eu.planets_project.tb.api.services.tags.ServiceTag;
import eu.planets_project.tb.gui.UserBean;
import eu.planets_project.tb.gui.backing.exp.DigitalObjectBean;
import eu.planets_project.tb.gui.backing.exp.EvaluationPropertyResultsBean;
import eu.planets_project.tb.gui.backing.exp.ExpTypeBackingBean;
import eu.planets_project.tb.gui.backing.exp.ExperimentStageBean;
import eu.planets_project.tb.gui.backing.exp.MeasurementPropertyResultsBean;
import eu.planets_project.tb.gui.backing.exp.NewExpWizardController;
import eu.planets_project.tb.gui.backing.exp.ResultsForDigitalObjectBean;
import eu.planets_project.tb.gui.util.JSFUtil;
import eu.planets_project.tb.impl.AdminManagerImpl;
import eu.planets_project.tb.impl.chart.ExperimentChartServlet;
import eu.planets_project.tb.impl.data.util.DataHandlerImpl;
import eu.planets_project.tb.impl.exceptions.InvalidInputException;
import eu.planets_project.tb.impl.model.ExperimentImpl;
import eu.planets_project.tb.impl.model.ExperimentReportImpl;
import eu.planets_project.tb.impl.model.PropertyEvaluationRecordImpl;
import eu.planets_project.tb.impl.model.PropertyRunEvaluationRecordImpl;
import eu.planets_project.tb.impl.model.exec.BatchExecutionRecordImpl;
import eu.planets_project.tb.impl.model.exec.ExecutionRecordImpl;
import eu.planets_project.tb.impl.model.exec.ExecutionStageRecordImpl;
import eu.planets_project.tb.impl.model.finals.DigitalObjectTypesImpl;
import eu.planets_project.tb.impl.model.measure.MeasurementImpl;
import eu.planets_project.tb.impl.model.ontology.OntologyHandlerImpl;
import eu.planets_project.tb.impl.model.ontology.util.OntoPropertyUtil;
import eu.planets_project.tb.impl.services.ServiceTemplateRegistryImpl;
import eu.planets_project.tb.impl.services.mockups.workflow.IdentifyWorkflow;
import eu.planets_project.tb.impl.services.mockups.workflow.MigrateWorkflow;
import eu.planets_project.tb.impl.services.mockups.workflow.ViewerWorkflow;


/**
 * @author <a href="mailto:andrew.lindley@arcs.ac.at">Andrew Lindley</a>
 * @since 03.04.2009
 *
 */
public class ExperimentBean {
	
	public static final int PHASE_EXPERIMENTSETUP_1   = 1;
	public static final int PHASE_EXPERIMENTSETUP_2   = 2;
	public static final int PHASE_EXPERIMENTSETUP_3   = 3;
	public static final int PHASE_EXPERIMENTAPPROVAL   = 4;
	public static final int PHASE_EXPERIMENTEXECUTION  = 5;
    public static final int PHASE_EXPERIMENTEVALUATION = 6;
    public static final int PHASE_EXPERIMENTFINALIZED   = 7;
	
	// To avoid the data held here falling out of date, store the experiment:
	Experiment exp = null;
               
	private static Log log = LogFactory.getLog(ExperimentBean.class);
	private long id;
	private boolean formality = true;
	private String ename = new String();
    private String esummary = new String();
    private String econtactname = new String();
    private String econtactemail = new String();
    private String econtacttel = new String();
    private String econtactaddress = new String();
    private String epurpose = new String();
    private String efocus = new String();
    private String eparticipants = new String();

    private String exid = new String();
    private ArrayList<String> eref = new ArrayList<String>();
    private String erefFinder = new String();
    
    private ArrayList<String> litrefdesc = new ArrayList<String>();
    private ArrayList<String> litrefuri = new ArrayList<String>();
    private ArrayList<String> litreftitle = new ArrayList<String>();
    private ArrayList<String> litrefauthor = new ArrayList<String>();

    private String escope = new String();
    private String eapproach = new String();
    private String econsiderations = new String();
    //the selected TBServiceTemplate's ID
    private TestbedServiceTemplate selSerTemplate;
    private String sSelSerTemplateID="";
    private String sSelSerOperationName="";
    private UIComponent panelAddedSerTags = new UIPanel();
    private UIComponent panelAddedFiles = new UIPanel();
    private boolean bOperationSelectionCompleted = false;
    
    //contains the overall experiment Benchmarks. Map<BMGoal.ID, BenchmarkBean>
    private Map<String,BenchmarkBean> experimentBenchmarks = new HashMap<String,BenchmarkBean>();
    private String intensity="0";
    
    //contains the benchmarks per file basis. Map<InputFileURI+BMGoalID, BenchmarkBean>
    private Map<String, BenchmarkBean> fileBenchmarks = new HashMap<String,BenchmarkBean>();
    boolean bAnyAutoEvalConfigured = false;
    
    //The input file refs with Map<Position+"",localFileRef>
    private Map<String,String> inputData = new HashMap<String,String>();
    //distinguish between migration and characterisation output results
    //output in the form of localInputFile Ref and localFileRef/String
    private Collection<Map.Entry<String,String>> outputData = new Vector<Map.Entry<String,String>>();
    //the external file refs provided for step evaluate experiment
	private List<String> evaluationData = new ArrayList<String>();
	
    private int currStage = ExperimentBean.PHASE_EXPERIMENTSETUP_1;
    private boolean approved = false;
    
    private List<String> dtype = new ArrayList<String>();
    private List<SelectItem> dtypeList = new ArrayList<SelectItem>();
    private DigitalObjectTypesImpl dtypeImpl = new DigitalObjectTypesImpl();
    private List<String[]> fullDtypes = new ArrayList<String[]>();
    
    private ExecutionRecordImpl selectedExecutionRecord = null;
    private BatchExecutionRecordImpl selectedBatchExecutionRecord = null;
    private String selectedDigitalObject = null;
    private ExperimentStageBean selectedStage;
    
    private String ereportTitle;
    private String ereportBody;
	
	private int expRating = 0;
	private int serviceRating = 0;
    
    //a list of added annotationTagNames to restrict the displayed services
    private Map<String,ServiceTag> mapAnnotTagVals = new HashMap<String,ServiceTag>();
    //triggers the XMLResponds in case of an error for stage6
    private boolean bRenderXMLResponds = false;
    
    private long numExecutions;
        
    public ExperimentBean() {
    	/*experimentBenchmarks = new HashMap<String,BenchmarkBean>();
    	Iterator iter = BenchmarkGoalsHandlerImpl.getInstance().getAllBenchmarkGoals().iterator();
    	while (iter.hasNext()) {
    		BenchmarkGoal bm = (BenchmarkGoal)iter.next();
    		experimentBenchmarks.put(bm.getID(), new BenchmarkBean(bm));
    	}*/
        
        fullDtypes = dtypeImpl.getAlLDtypes();
        
        for(int i=0;i<fullDtypes.size();i++) {
            
            String[] tmp = fullDtypes.get(i);
            
            SelectItem option = new SelectItem(tmp[0],tmp[1]);
            dtypeList.add(option);  
        }
        
        // Add in default data:
        UserBean user = (UserBean)JSFUtil.getManagedObject("UserBean");
        this.setUserData(user);
        
        // Default spaces:
        this.litrefdesc.add("");
        this.litrefuri.add("");
        this.litreftitle.add("");
        this.litrefauthor.add("");
        // log
        log.info("New ExperimentBean constructed.");
    }
    
    public void setUserData( UserBean user ) {
        if( user != null ) {
            this.eparticipants = user.getUserid();
            this.econtactname = user.getFullname();
            this.econtactemail = user.getEmail();
            this.econtacttel = user.getTelephone();
            this.econtactaddress = user.getAddress();
        } else {
            log.error("Attempted to re-fill user data with a null user.");
        }
    }
    
    @SuppressWarnings("deprecation")
	public void fill(Experiment exp) {
        log.info("Filling the ExperimentBean with experiment: "+ exp.getExperimentSetup().getBasicProperties().getExperimentName() + " ID:"+exp.getEntityID());
        log.debug("Experiment Phase Name = " + exp.getPhaseName());
        log.debug("Experiment Current Phase " + exp.getCurrentPhase());
        if( exp.getCurrentPhase() != null )
            log.debug("Experiment Current Phase Stage is " + exp.getCurrentPhase().getState());

        this.exp = exp; 
    	ExperimentSetup expsetup = exp.getExperimentSetup();
    	BasicProperties props = expsetup.getBasicProperties();
    	this.id = exp.getEntityID();
    	this.ename =(props.getExperimentName());
    	this.escope=(props.getScope());
    	this.econsiderations=(props.getConsiderations());
    	this.econtactaddress=(props.getContactAddress());
    	this.econtactemail=(props.getContactMail());
    	this.econtacttel=(props.getContactTel());
    	this.econtactname=(props.getContactName());
        
        // references
        this.exid=props.getExternalReferenceID();
        
        List<String[]> lit = props.getAllLiteratureReferences();
        if (lit != null && !lit.isEmpty()) {
            this.litrefdesc = new ArrayList<String>(lit.size());
            this.litrefuri = new ArrayList<String>(lit.size());
            this.litreftitle = new ArrayList<String>(lit.size());
            this.litrefauthor = new ArrayList<String>(lit.size());
            for( int i = 0; i < lit.size(); i++ ) {
        	  this.litrefdesc.add(i, lit.get(i)[0]);
        	  this.litrefuri.add(i,lit.get(i)[1]);
        	  this.litreftitle.add(i,lit.get(i)[2]);
        	  this.litrefauthor.add(i,lit.get(i)[3]);
            }
        }       
        
        List<Long> refs = props.getExperimentReferences();
        if (refs != null && !refs.isEmpty()) {
            this.eref = new ArrayList<String>(refs.size());
            for( int i = 0; i < refs.size(); i++ )
                this.eref.add(i, ""+refs.get(i) );
        }        
        List<String> involvedUsers = props.getInvolvedUserIds();
        String partpnts = " ";
        for(int i=0;i<involvedUsers.size();i++) {
            partpnts +=involvedUsers.get(i);
            if( i < involvedUsers.size()-1 ) partpnts += ", ";
        }
        

        this.eparticipants = partpnts;
        
        String Test = props.getExternalReferenceID();
        
        this.exid=(Test);
        

        this.efocus=(props.getFocus());

    	this.epurpose=(props.getPurpose());
    	this.esummary=(props.getSummary());
    	this.formality = props.isExperimentFormal();    	
    	
        //get already added TestbedServiceTemplate data
        log.debug("fill expBean: executable = "+exp.getExperimentExecutable());
        if(exp.getExperimentExecutable()!=null){
        	ExperimentExecutable executable = exp.getExperimentExecutable();
        	this.selSerTemplate = executable.getServiceTemplate();
        	if( selSerTemplate != null )
        	    this.sSelSerTemplateID = selSerTemplate.getUUID();
        	this.sSelSerOperationName = executable.getSelectedServiceOperationName();
        	this.bOperationSelectionCompleted = true;
        	helperLoadInputData(executable.getInputData());
        	if(executable.isExecutionSuccess()){
        		//uses the executable to get the data
            	this.outputData = exp.getExperimentExecutable().getOutputDataEntries();
        	}
        }
     
        //fill the file benchmarks
        log.info("Looking for BMGs... ");
        try {
        	if (exp.getCurrentPhase() instanceof ExperimentEvaluation) { 
        	    if (this.inputData != null) {

        	        //iterate over all input files
        	        for(String localFileRef : this.inputData.values()) {
        	            // Clean up the localFileRef, so that the TB can cope with it's data store being moved.
        	            //store a set of file BMGoals for every record item
        	            DataHandler dh = new DataHandlerImpl();
        	            DigitalObjectRefBean dorb = dh.get(localFileRef);
        	            if( dorb != null ) {
        	                URI inputFileURI = dorb.getDownloadUri();
        	                Collection<BenchmarkGoal> colFileBMGoals = exp.getExperimentEvaluation().getEvaluatedFileBenchmarkGoals(inputFileURI);
        	                if(colFileBMGoals==null)
        	                    throw new Exception("Exception while setting file benchmarks for record: "+inputFileURI);

        	                for(BenchmarkGoal bmg : colFileBMGoals){
        	                    log.info("Found fileBMG: " + bmg.getName());
        	                    log.info("Found fileBMG.id: " + bmg.getID());
        	                    //now crate the bmb out of the bmg
        	                    BenchmarkBean bmb = new BenchmarkBean(bmg);
        	                    bmb.setSourceValue(bmg.getSourceValue());
        	                    bmb.setTargetValue(bmg.getTargetValue());
        	                    bmb.setEvaluationValue(bmg.getEvaluationValue());
        	                    bmb.setWeight(String.valueOf(bmg.getWeight()));
        	                    bmb.setSelected(true);
        	                    if((bmb.getSourceValue()==null)||(bmb.getSourceValue().equals("")))
        	                        bmb.setSrcError(true);
        	                    if((bmb.getTargetValue()==null)||(bmb.getTargetValue().equals("")))
        	                        bmb.setTarError(true);							

        	                    //now add the file bmbs for this experimentbean
        	                    fileBenchmarks.put(inputFileURI+bmb.getID(), bmb);
        	                }
        	            }
	    			}
	    		}
        	}
        	
        	//fill the experiment overall benchmarks
        	Collection<BenchmarkGoal> lbmbs;
        	if (exp.getCurrentPhase() instanceof ExperimentEvaluation){
        		//get the data from the evaluation phase
        		lbmbs = exp.getExperimentEvaluation().getEvaluatedExperimentBenchmarkGoals();
                log.info("Found eval #BMGs = " + lbmbs.size());
                log.info("Found pre-eval #BMGs = " + exp.getExperimentSetup().getAllAddedBenchmarkGoals().size());
                // if none, get the data from the setup phase
                if( lbmbs.size() == 0 )
                    lbmbs = exp.getExperimentSetup().getAllAddedBenchmarkGoals();
        	}
        	else{
        		//get the data from the setup phase
        		lbmbs = exp.getExperimentSetup().getAllAddedBenchmarkGoals();
                log.info("Found pre-eval #BMGs = " + lbmbs.size());
        	}   	
    		for(BenchmarkGoal bmg : lbmbs){
                log.info("Found BMG: " + bmg.getName());
                log.info("Found BMG.id: " + bmg.getID());
    		    BenchmarkBean bmb = new BenchmarkBean(bmg);
    		    bmb.setSourceValue(bmg.getSourceValue());
    		    bmb.setTargetValue(bmg.getTargetValue());
    		    bmb.setEvaluationValue(bmg.getEvaluationValue());
    		    bmb.setWeight(String.valueOf(bmg.getWeight()));
    		    bmb.setSelected(true);
    		    experimentBenchmarks.put(bmg.getID(), bmb);
    	    }
        }catch (Exception e) {
        	log.error("Exception during attempt to create ExperimentBean for: "+e.toString());
        	if( log.isDebugEnabled() ) e.printStackTrace();
        }
    	
        
    	String intensity = Integer.toString(exp.getExperimentSetup().getExperimentResources().getIntensity());
    	if (intensity != null && intensity != "-1") 
    		this.intensity = intensity;
    	// determine current Stage
    	ExperimentPhase currPhaseObj = exp.getCurrentPhase();
    	if (currPhaseObj != null) {
    		String currPhase = currPhaseObj.getPhaseName();
	    	if (currPhase.equals(ExperimentPhase.PHASENAME_EXPERIMENTSETUP)) {
	    		this.currStage = exp.getExperimentSetup().getSubStage();
	    	} else if (currPhase.equals(ExperimentPhase.PHASENAME_EXPERIMENTAPPROVAL)) {
	    		this.currStage = ExperimentBean.PHASE_EXPERIMENTAPPROVAL;
	    	} else if (currPhase.equals(ExperimentPhase.PHASENAME_EXPERIMENTEXECUTION)) {
	    		this.currStage = ExperimentBean.PHASE_EXPERIMENTEXECUTION;
            } else if (currPhase.equals(ExperimentPhase.PHASENAME_EXPERIMENTEVALUATION)) {
                this.currStage = ExperimentBean.PHASE_EXPERIMENTEVALUATION;
            } else if (currPhase.equals(ExperimentPhase.PHASENAME_EXPERIMENTFINALIZED)) {
                this.currStage = ExperimentBean.PHASE_EXPERIMENTFINALIZED;                
            }
    	}
	    if(currStage>ExperimentBean.PHASE_EXPERIMENTSETUP_3) {
	        approved=true;
	    } else {
	        approved=false;
	    }
        
        this.dtype = props.getDigiTypes();
        
        // Set the report up:
        this.ereportTitle = exp.getExperimentEvaluation().getExperimentReport().getHeader();
        this.ereportBody = exp.getExperimentEvaluation().getExperimentReport().getBodyText();
        this.evaluationData = exp.getExperimentEvaluation().getExternalEvaluationDocuments();
		
		//deal with ratings
		this.expRating = exp.getExperimentEvaluation().getExperimentRating();
		this.serviceRating = exp.getExperimentEvaluation().getServiceRating();
    }
    //END OF FILL METHOD

    /**
     */
    public String redirectToCurrentStage() {
        NewExpWizardController.redirectToCurrentStage(this.exp);
        return "success";
    }
    
    public Map<String,BenchmarkBean> getExperimentBenchmarks() {
		return experimentBenchmarks;    	
    }
    
    /**
     * Please note the String (key) is composed by the Input file's URI+BMGoalID
     * @return
     */
    public Map<String, BenchmarkBean> getFileBenchmarkBeans(){
    	return this.fileBenchmarks;
    }
    
    public List<BenchmarkBean> getExperimentBenchmarkBeans() {
    	return new ArrayList<BenchmarkBean>(experimentBenchmarks.values());
    }
    
    
    /**
     * It's only possible to add/remove experiment overall benchmark beans.
     * File BMBs are identically and are cloned from them
     * @param bmb
     */
    public void addBenchmarkBean(BenchmarkBean bmb) {
    	this.experimentBenchmarks.put(bmb.getID(),bmb);
    }
    
    /**
     * It's only possible to add/remove experiment overall benchmark beans.
     * File BMBs are identically and are cloned from them
     * @param bmb
     */
    public void deleteBenchmarkBean(BenchmarkBean bmb) {
    	this.experimentBenchmarks.remove(bmb.getID());
    }
    
    /**
     * It's only possible to add/remove experiment overall benchmark beans.
     * File BMBs are identically and are cloned from them
     * @param bmb
     */
    public void setBenchmarks(Map<String,BenchmarkBean>bms) {
    	this.experimentBenchmarks = bms;
    }
    
    /**
     * Returns the selected service template UUID
     * @return
     */
    public String getSelectedServiceTemplateID() {
    	if (selSerTemplate !=null)
    		return selSerTemplate.getUUID();
    	else
    		return this.sSelSerTemplateID;
    }
    
    public void setSelServiceTemplateID(String sID){
    	this.sSelSerTemplateID = sID;
    	setSelectedServiceTemplate(sID);
    }
    
    public void setSelectedServiceOperationName(String sName){
    	this.sSelSerOperationName = sName;
    }
    
    public String getSelectedServiceOperationName(){
    	return this.sSelSerOperationName;
    }
    
    /**
     * Sets the selected object's id and also fetches the object from the registry
     * @param sID
     */
    public void setSelectedServiceTemplate(String sID){
    	ServiceTemplateRegistry registry = ServiceTemplateRegistryImpl.getInstance();
    	this.selSerTemplate = registry.getServiceByID(sID);
    }
    
    public TestbedServiceTemplate getSelectedServiceTemplate(){
    	return this.selSerTemplate;
    }
    
    public void removeSelectedServiceTemplate(){
    	this.selSerTemplate = null;
    	this.sSelSerTemplateID = null;
    	this.sSelSerOperationName = null;
    }

    public ServiceOperation getSelectedServiceOperation(){
      if( this.selSerTemplate != null ) {
    	  TestbedServiceTemplate template = this.getSelectedServiceTemplate();
    	  List<String> allOpNames = template.getAllServiceOperationNames();
    	  String selOpName = this.getSelectedServiceOperationName();
    	  //template contains no operations
    	  if(allOpNames.size()==0)
    		  return null;
    	  //template is being changed - select first op as selected
    	  if(!allOpNames.contains(selOpName)){
    		  this.setSelectedServiceOperationName(allOpNames.iterator().next());
    	  }
    	  
    	  return template.getServiceOperation(this.getSelectedServiceOperationName());
      } 
      return null;
    }

    /**
     * Get a copy of the experiment underneath this bean.
     * @return The Experiment this Bean wraps.
     */
    public Experiment getExperiment() {
        return exp;
    }
    public void setExperiment(Experiment exp) {
        this.exp = exp;
    }

    /**
     * Returns a Map of added file Refs
     * Map<position+"",fileRef>
     * @return
     */
    public Map<String,String> getExperimentInputData() {
        log.debug("getting experiment input data: "+this.inputData);
        return this.inputData;
    }
	
    //public Map<String,String> getExperimentEvaluationData() {
    //    log.debug("getting experiment evaluation data: "+this.evaluationData);
    //    return this.evaluationData;
    //}
    
    /**
     * fetches a jsf usable list of the experimentInputData values 
     * @return
     */
    public List<DigitalObjectBean> getExperimentInputDataValues(){
    	List<DigitalObjectBean> ret = new ArrayList<DigitalObjectBean>();
    	for(String val : this.getExperimentInputData().values()){
    		DigitalObjectBean digoBean = new DigitalObjectBean(val);
    		ret.add(digoBean);
    	}
    	return ret;
    }
	
    //public List<DigitalObjectBean> getExperimentEvaluationDataValues(){
    //	List<DigitalObjectBean> ret = new ArrayList<DigitalObjectBean>();
    //	for(String val : this.getExperimentEvaluationData().values()){
    //		DigitalObjectBean digoBean = new DigitalObjectBean(val);
    //		ret.add(digoBean);
    //	}
    //	return ret;
    //}
    
    /**
     * Get the list of files as a simple String collection.
     * @return
     */
    public Collection<String> getExperimentInputDataFiles() {
        return this.inputData.values();
    }
	
    //public Collection<String> getExperimentEvaluationDataFiles() {
    //    return this.evaluationData.values();
    //}
    
    /**
     * Returns a map containing the input data's uri as key and its corresponding
     * original logical file name as value
     * e.g. Collection<Map<"http://../planets-testbed/inputdata/fdsljfsdierw.doc,"data1.doc">>
     * This is used to render as dataTable within the GUI
     * @return
     */
    public Collection<Map<String,String>> getExperimentInputDataNamesAndURIs(){
    	return helperGetDataNamesAndURIS("design experiment");
    }
    
    /**
     * Returns a map containing the evaluation data's uri as key and its corresponding
     * original logical file name as value
     * e.g. Collection<Map<"http://../planets-testbed/inputdata/fdsljfsdierw.doc,"data1.doc">>
     * This is used to render as dataTable within the GUI
     * @return
     */
    public Collection<Map<String,String>> getExperimentEvaluationDataNamesAndURIs(){
    	return helperGetDataNamesAndURIS("evaluate expeirment");
    }
    
    
    private Collection<Map<String,String>> helperGetDataNamesAndURIS(String expStage){
    	Collection<Map<String,String>> ret = new Vector<Map<String,String>>();
    	DataHandler dh = new DataHandlerImpl();
    	
    	//when we're calling this in design experiment -> fetch the input data refs
    	if(expStage.equals("design experiment")){
    		Map<String, String> localFileRefs = this.getExperimentInputData();
        	for( String key : localFileRefs.keySet() ) {
        	    boolean found = false;
        		try {
        			Map<String,String> map = new HashMap<String,String>();
        			//retrieve URI
        		    String fInput = localFileRefs.get(key);
        		    DigitalObjectRefBean dobr = dh.get(fInput);
        		    if( dobr != null ) {
        		        URI uri = dobr.getDownloadUri();
        		        map.put("uri", uri.toString()) ;
        		        map.put("name", this.createShortDoName(dobr) );
        		        map.put("inputID", key);
        		        ret.add(map);
        		        found = true;
        		    } else {
        		        log.error("Digital Object "+key+" could not be found!");
        		    }
    			} catch (FileNotFoundException e) {
    				log.error(e.toString());
    			}
    			// Catch lost items...
    			if( !found ) {
                    Map<String,String> map = new HashMap<String,String>();
                    String fInput = localFileRefs.get(key);
                    map.put("uri", fInput);
                    map.put("name", "ERROR: Digital Object Not Found: " + getLeafnameFromPath(fInput));
                    map.put("inputID", key);
                    ret.add(map);
    			}
        	}
    	}
    	//when we're calling this in evaluate experiment -> fetch the external eval data refs
    	if(expStage.equals("evaluate expeirment") && this.getEvaluationExternalDigoRefs() != null ){
    		for(String digoRef : this.getEvaluationExternalDigoRefs()){
    			try {
    			Map<String,String> map = new HashMap<String,String>();
    			DigitalObjectRefBean dobr = dh.get(digoRef);
    			if(dobr != null ) {
    				URI uri = dobr.getDownloadUri();
    				map.put("uri", uri.toString()) ;
    				map.put("name", this.createShortDoName(dobr) );
    				map.put("inputID", dobr.getDomUri()+"");
    				ret.add(map);
        		    } else {
        		    	log.error("Digital Object "+digoRef+" could not be found!");
        		    }
    			} catch (FileNotFoundException e) {
    				log.error(e.toString());
    			}
    		}
    	}
    	return ret;
    }
    
    /**
     * returns the digital objects that are uploaded in step: evaluate experiment
     * @return
     */
    public List<String> getEvaluationExternalDigoRefs(){
    	return this.evaluationData;
    }
    
    public void removeEvaluationExternalDigoRef(String digoRef){
    	this.evaluationData.remove(digoRef);
    	//this.getExperiment().getExperimentEvaluation().removeExternalEvaluationDocument(digoRef);
    	this.updateExperiment();
    }
    
    public void addEvaluationExternalDigoRef(String digoRef){
    	this.evaluationData.add(digoRef);
    	//this.getExperiment().getExperimentEvaluation().addExternalEvaluationDocument(digoRef);
    	this.updateExperiment();
    }
	
    //public Collection<Map<String,String>> getExperimentEvaluationDataNamesAndURIs(){
    //	Collection<Map<String,String>> ret = new Vector<Map<String,String>>();
    //	DataHandler dh = new DataHandlerImpl();
    //	Map<String, String> localFileRefs = this.getExperimentEvaluationData();
    //	for( String key : localFileRefs.keySet() ) {
    //		try {
    //			Map<String,String> map = new HashMap<String,String>();
    //			//retrieve URI
    //		    String fInput = localFileRefs.get(key);
    //		    DigitalObjectRefBean dobr = dh.get(fInput);
    //		    if(dobr != null ) {
	//			URI uri = dobr.getDownloadUri();
	//			map.put("uri", uri.toString()) ;
	//			map.put("name", this.createShortDoName(dobr) );
	//			map.put("inputID", key);
	//			ret.add(map);
    //		    } else {
    //		    	log.error("Digital Object "+key+" could not be found!");
    //		    }
	//		} catch (FileNotFoundException e) {
	//			log.error(e.toString());
	//		}
    //	}
    //	return ret;
    //}

    /**
     * @param name
     * @return
     */
    private String createShortDoName( DigitalObjectRefBean dobr ) {
        String name = dobr.getName();
        if( name == null ) return dobr.getDomUri().getPath();
        if( name == null ) return "no-name";
        return this.getLeafnameFromPath(name);
    }
    
    private String getLeafnameFromPath(String name) {
        int lastSlash = name.lastIndexOf("/");
        if( lastSlash != -1 ) {
            return name.substring( lastSlash + 1, name.length() );
        }
        return name;
    }
    
    /**
     * Used to package execution results by digital object.
     * @return
     */
    public List<ResultsForDigitalObjectBean> getExperimentDigitalObjectResults() {
        log.info("Looking for results...");
        List<ResultsForDigitalObjectBean> results = new Vector<ResultsForDigitalObjectBean>();
        Collection<String> runOnes = new HashSet<String>();
        // Populate using the results:
        Set<BatchExecutionRecordImpl> records = getExperiment().getExperimentExecutable().getBatchExecutionRecords();
        log.info("Found batch list: "+records);
        if( records != null && records.size() > 0 ) {
            log.info("Found batches: "+records.size());
            BatchExecutionRecordImpl batch = records.iterator().next();
            for( ExecutionRecordImpl exr : batch.getRuns() ) {
                log.info("Found result: "+exr.getResultType());
                ResultsForDigitalObjectBean res = new ResultsForDigitalObjectBean(exr.getDigitalObjectReferenceCopy());
                results.add(res);
                // Collate successes:
                runOnes.add( normaliseDataReference(exr.getDigitalObjectReferenceCopy()) );
                log.info("Recorded result for: "+exr.getDigitalObjectReferenceCopy());
            }
        }
        // Patch in any input files which are not represented. 
        for( String file : getExperimentInputData().values() ) {
            file = normaliseDataReference(file);
            log.info("Checking for results for "+file);
            if( ! runOnes.contains(file) ) {
                ResultsForDigitalObjectBean res = new ResultsForDigitalObjectBean(file);
                log.info("Adding missing result for: "+file);
                results.add(res);
            }
        }

        // Now return the results:
        return results;
    }
    
    private static String normaliseDataReference( String inUri ) {
        URI uri;
        try {
            uri = new URI(inUri);
            return uri.normalize().toASCIIString();
        } catch (Exception e) {
            log.error("Could not normalise: "+inUri);
        }
        return inUri;
    }
        
    
    /**
     * Returns the position where this item has been added
     * @param localFileRef
     * @return
     */
    public String addExperimentInputData(String localFileRef) {
        log.debug("Adding input file: "+localFileRef);
    	String key = getNextInputDataKey();
    	if(!this.inputData.values().contains(localFileRef)){
    		this.inputData.put(key, localFileRef);
    		this.exp.getExperimentExecutable().addInputData(localFileRef);
    		this.updateExperiment();
    		// Also add to UI component:
    		UIComponent panel = this.getPanelAddedFiles();
    		this.helperCreateRemoveFileElement(panel, localFileRef, key);
            return key;
    	} else {
    	    log.info("InputData contains "+localFileRef);
    	    return null;
        }
    }
    
    public void removeExperimentInputData(String key){
    	if(this.inputData.containsKey(key)){
            this.exp.getExperimentExecutable().removeInputData(this.inputData.get(key));
            log.info("Removed input data matching key: "+key+" : "+this.inputData.get(key));
            this.inputData.remove(key);
    	} else {
    	    log.warn("Could not remove input data, no matching key for "+key);
    	}
    }
	
    //public String addExperimentEvaluationData(String localFileRef) {
    //    log.debug("Adding evaluation file: "+localFileRef);
    //	String key = getNextEvaluationDataKey();
    //	if(!this.evaluationData.values().contains(localFileRef)){
    //		this.evaluationData.put(key, localFileRef);
    //		this.exp.getExperimentExecutable().addEvaluationData(localFileRef);
    //		this.updateExperiment();
    		// Also add to UI component:
    //		UIComponent panel = this.getPanelAddedFiles();
    //		this.helperCreateRemoveFileElement(panel, localFileRef, key);
    //        return key;
    //	} else {
    //	    log.info("EvaluationData contains "+localFileRef);
    //	    return null;
    //    }
    //}
    
    //public void removeExperimentEvaluationData(String key){
    //	if(this.inputData.containsKey(key)){
    //       this.exp.getExperimentExecutable().removeInputData(this.inputData.get(key));
    //       log.info("Removed evaluation data matching key: "+key+" : "+this.inputData.get(key));
    //        this.inputData.remove(key);
    //	} else {
    //	    log.warn("Could not remove input data, no matching key for "+key);
    //	}
    //}
    
    public void removeAllExperimentInputData(){
    	this.inputData = new HashMap<String,String>();
    	//remove all added delete-link GUI elements
    	this.getPanelAddedFiles().getChildren().clear();
    }
    
    /**
     * As the InputData HashMap should be filled up with IDs without any 
     * gap, this method is used to find the next possible key
     * @return
     */
    private String getNextInputDataKey(){
    	boolean bFound = true;
    	int count = 0;
    	while(bFound){
    		// Check if the current key is already in use: 
    		if(this.inputData.containsKey(count+"")){
                count++;
    		}
    		else{
                bFound = false;
    		}
    	}
    	log.debug("Returning input data key: "+count+" / "+inputData.size());
    	return count+"";
    }

    //private String getNextEvaluationDataKey(){
    //	boolean bFound = true;
    //	int count = 0;
    //	while(bFound){
    //		// Check if the current key is already in use: 
    //		if(this.evaluationData.containsKey(count+"")){
    //           count++;
    //		}
    //		else{
    //            bFound = false;
    //		}
    //	}
    //	log.debug("Returning input data key: "+count+" / "+evaluationData.size());
    //	return count+"";
    //}	
    
    public void setOutputData(Collection<Entry<String,String>> data){
    	this.outputData = data;
    }
    
    
    /**
     * Returns the data used in the experiment's input-output data table
     * @return
     */
    public Collection<Map<String,Object>> getIOTableDataForGUI(){
    	//Entry of inputComponent, outputComponent
    	Collection<Map<String,Object>> ret = new Vector<Map<String,Object>>();
    	Iterator<Entry<String,String>> itData = this.outputData.iterator();
    	
    	DataHandler dh = new DataHandlerImpl();
    	while(itData.hasNext()){
    		Entry<String,String> entry = itData.next();
    		String input = entry.getKey();
    		String output = entry.getValue();
    		
    		//mixing different objects within this map
    		HashMap<String,Object> hm = new HashMap<String,Object>();
    	 //For the Input:
    		try{
    		//test: convert input to URI
                DigitalObjectRefBean dobr = dh.get(input);
    			URI uriInput = dobr.getDownloadUri();
    			//add "inputURI" and "inputName" into ret hashmap
    			hm.put("input", uriInput.toString());
    			hm.put("inputName", dobr.getName());
    			hm.put("inputTypeURI", "URI");
    			
    		}
    		catch(Exception e){
    			//this input was not a file or fileRef not readable  - display as text
    			hm.put("input", input);
    			hm.put("inputName", null);
    			hm.put("inputTypeURI", null);
    		}
    		
    	 //For the Output:
    		try{
        		//test: convert output to URI
                DigitalObjectRefBean dobr = dh.get(output);
        		URI uriOutput = dobr.getDownloadUri();
        		//add "outputURI" and "outputName" "outputType" into ret hashmap
    			hm.put("output", uriOutput.toString());
    			hm.put("outputName", dobr.getName());
    			hm.put("outputTypeURI", "URI");
        	}
        	catch(Exception e){
        		//this input was not a file or fileRef not readable  - display as text
        		hm.put("output", output);
    			hm.put("outputName", null);
    			hm.put("outputType", null);
        	}  
        	
        	//panel control, only first item is displayed as opened
        	if(ret.size()<1){
        		hm.put("panelOpened", "true");
        	}else{
        		hm.put("panelOpened", "false");
        	}
        	
        	//add the fileBenchmarkBeans (as List) for evaluation
        	List<BenchmarkBean> lFileBMGs = new ArrayList<BenchmarkBean>();
        	for(String key : this.fileBenchmarks.keySet()){
        		lFileBMGs.add(this.fileBenchmarks.get(key));
        	}
        	hm.put("fileBMGoals", lFileBMGs);
        	
        	ret.add(hm);
    	}
    	return ret;
    }
    
    public void processFileBMGoalTargetValueChange(ValueChangeEvent vce){	
    	processFileBMGoalValueChange(vce,"tar");
    }
    
    public void processFileBMGoalSrcValueChange(ValueChangeEvent vce){	
    	processFileBMGoalValueChange(vce,"src");
    }
    
    public void processFileBMGoalEvalValueChange(ValueChangeEvent vce){
    	processFileBMGoalValueChange(vce,"eval");
    }
    
    /**
     * Sets a new src,target or evaluation value for a given File-BenchmarkGoal 
     * @param vce
     */
    private void processFileBMGoalValueChange(ValueChangeEvent vce, String sSrcOrTarget){	
    	String sInputFile =null,sBMGoalID = null;
    	for(UIComponent comp:((List<UIComponent>)vce.getComponent().getChildren())){
    		try{
    			UIParameter param = (UIParameter)comp;
    			if(param.getName().equals("forInputFile")){
    				sInputFile = param.getValue().toString();
    			}
    			if(param.getName().equals("forBMgoalID")){
    				sBMGoalID = param.getValue().toString();
    			}
    		}
    		catch(Exception ex){}
    	}
    	
    	//now set the new values
		try{
	    	if((sInputFile!=null)&&(sBMGoalID!=null)){
				BenchmarkBean fbmb = this.fileBenchmarks.get(sInputFile+sBMGoalID);
				
				if(sSrcOrTarget.equals("src")){
					if(fbmb.checkInputValueValid(vce.getNewValue().toString())){
						fbmb.setSourceValue(vce.getNewValue().toString());
						fbmb.setSrcError(false);
					}
					else{
						fbmb.setSrcError(true);
						throw new Exception(vce.getNewValue().toString());
					}
				}
				if(sSrcOrTarget.equals("tar")){
					if(fbmb.checkInputValueValid(vce.getNewValue().toString())){
						fbmb.setTargetValue(vce.getNewValue().toString());
						fbmb.setTarError(false);
					}
					else{
						fbmb.setTarError(true);
						throw new Exception(vce.getNewValue().toString());
					}
				}
				if(sSrcOrTarget.equals("eval"))
					fbmb.setEvaluationValue(vce.getNewValue().toString());
			}
			else{
				throw new Exception();
			}
		}
	    catch(Exception e){
	    	//can't use the standard renderResponse life-cycle with Ajax within a JSF Table
	    	/*FacesMessage fmsg = new FacesMessage();
	        fmsg.setDetail("value:"+e.toString()+" not a valid!");
	        fmsg.setSummary("value:"+e.toString()+" not a valid!");
	        fmsg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext ctx = FacesContext.getCurrentInstance();
	        ctx.addMessage("fileSrcBmGoal_"+sBMGoalID,fmsg);
			log.error("entered data: "+e.toString()+" not validated properly");*/
	    }
    }
    

	public String getNumberOfOutput() {
		return this.outputData.size()+"";
	}
	
	public int getNumberOfInputFiles(){
		return this.inputData.values().size();
	}
	
	//public int getNumberOfEvaluationFiles(){
	//	return this.evaluationData.values().size();
	//}

	public void setIntensity(String intensity) {
		this.intensity = intensity;
	}
	
	public String getIntensity(){
		return this.intensity;
	}
	
    public void setID(long id) {
    	this.id = id;
    }
    
    public long getID(){
    	return this.id;
    }
    
    public void setFormality(boolean formality) {
        this.formality = formality;
    }
    
    public boolean getFormality() {
        return formality;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }
    
    public String getEname() {
        return ename;
    }
    
    public void setEsummary(String esummary) {
        this.esummary = esummary;
    }
    
    public String getEsummary() {
        return esummary;
    }
    
    public String getEcontactname() {
        return econtactname;
    }
    
    public void setEcontactname(String econtactname) {
        this.econtactname = econtactname;
    }
    
    public String getEcontactemail() {
        return econtactemail;
    }
    
    public void setEcontactemail(String econtactemail) {
        this.econtactemail = econtactemail;
    }
    
    public String getEcontacttel() {
        return econtacttel;
    }
    
    public void setEcontacttel(String econtacttel) {
        this.econtacttel = econtacttel;
    }
    
    public String getEcontactaddress() {
        return econtactaddress;
    }
    
    public void setEcontactaddress(String econtactaddress) {
        this.econtactaddress = econtactaddress;
    }
    
    public String getEpurpose() {
        return epurpose;
    }
    
    public void setEpurpose(String epurpose) {
        this.epurpose = epurpose;
    }
    
    public String getEfocus() {
        return efocus;
    }
    
    public void setEfocus(String efocus) {
        this.efocus = efocus;
    }
    
    public String getEscope() {
        return escope;
    }
    
    public void setEscope(String escope) {
        this.escope = escope;
    }
    
    public String getEapproach() {
        return eapproach;
    }
    
    public void setEapproach(String eapproach) {
        this.eapproach = eapproach;
    }
    
    public String getEconsiderations() {
        return econsiderations;
    }
    
    public void setEconsiderations(String econsiderations) {
        this.econsiderations = econsiderations;
    }
  
    public void setEtype(String type) {
        log.info("Setting experiment type to: "+type);
        try {
            this.exp.getExperimentSetup().setExperimentType(type);
            this.updateExperiment();
        } catch (InvalidInputException e) {
            e.printStackTrace();
        }
    }
	
    public void setMigrationType() {
		setEtype("migrate");
    }
	
    public void setPlanType() {
		setEtype("executablepp");
    }
	
    public void setEmulationType() {
		setEtype("emulate");
    }
    
    public String getEtype() {
        if( this.exp != null && this.exp.getExperimentSetup() != null ) {
            log.info("Getting EtypeID:"+ this.exp.getExperimentSetup().getExperimentTypeID() );
            log.info("Also EtypeName:"+ this.exp.getExperimentSetup().getExperimentTypeName());
            return this.exp.getExperimentSetup().getExperimentTypeID();
        } 
        return "";
    }
    
    public String getEtypeName() {
        return this.exp.getExperimentSetup().getExperimentTypeName();
    }
    
    public int getCurrentStage() {
    	return this.currStage;
    }
    
    public void setCurrentStage(int cs) {
    	this.currStage = cs;
    }
    
    public boolean getApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public void setExid(String exid) {
    	this.exid = exid;
    }
    
    public String getExid() {
    	return this.exid;
    }
    
    public void setLitRefDesc(ArrayList<String> litref) {
    	this.litrefdesc = litref;
    }
    
    public ArrayList<String> getLitRefDesc() {
    	return this.litrefdesc;
    }

    public void setLitRefURI(ArrayList<String> uri) {
    	this.litrefuri = uri;
    }
    
    public ArrayList<String> getLitRefURI() {
    	return this.litrefuri;
    }
    
    public void setLitRefAuthor(ArrayList<String> author) {
    	this.litrefauthor = author;
    }
    
    public ArrayList<String> getLitRefAuthor() {
    	return this.litrefauthor;
    }
    
    public void setLitRefTitle(ArrayList<String> title) {
    	this.litreftitle = title;
    }
    
    public ArrayList<String> getLitRefTitle() {
    	return this.litreftitle;
    }
    
    public void addLitRefSpot() {
        // Add space for another lit ref:
        this.litrefdesc.add("");
        this.litrefuri.add("");
        this.litrefauthor.add("");
        this.litreftitle.add("");
    }

    public void setEparticipants(String eparticipants) {
    	this.eparticipants = eparticipants;
    }
    
    public String getEparticipants() {
    	return this.eparticipants;
    }

    public ArrayList<String> getEref() {
        return eref;
    }

    public ArrayList<Experiment> getErefBeans() {
        ArrayList<Experiment> ert = new ArrayList<Experiment>();
        TestbedManager testbedMan = (TestbedManager)JSFUtil.getManagedObject("TestbedManager");  
        for( int i=0; i < this.eref.size(); i++ ) {
            Experiment erp = testbedMan.getExperiment((Long.parseLong(this.eref.get(i))));
            ert.add(i,erp);
        }
        return ert;
    }

    
    public void setEref(ArrayList<String> eref) {
    	this.eref = eref;
    }
    
    public Collection<SelectItem> getAllExperimentsSelectItems() {
        TestbedManager testbedMan = (TestbedManager)JSFUtil.getManagedObject("TestbedManager");  
        
        ArrayList<SelectItem> expList = new ArrayList<SelectItem>();
        Collection<Experiment> allExps = testbedMan.getAllExperiments();
        for( Experiment exp : allExps ) {
            SelectItem item = new SelectItem();
            item.setValue( ""+exp.getEntityID() );
            item.setLabel(exp.getExperimentSetup().getBasicProperties().getExperimentName());
            if( exp.getEntityID() != this.getID() )
                expList.add(item);
        }
        return expList;
    }    
    
    public List<String> getDtype() {
        return dtype;
    }
    
    public void setDtype(List<String> dtype) {
    	this.dtype = dtype;
    }
    
    public List<SelectItem> getDtypeList() {
        if( dtypeList == null ) return new ArrayList<SelectItem>();
        return dtypeList;
    }
   
    public void setDtypeList(List<SelectItem> dtypeList) {
    	this.dtypeList = dtypeList;
    }

    /**
     * Gets a list of all the phases of this experiment.
     * @return List of ExperimentPhaseBean, one for each possible Phase.
     */
    public List<ExperimentPhaseBean> getPhaseBeans() {
        return java.util.Arrays.asList(getPhaseBeanArray());
    }
    
    private ExperimentPhaseBean[] getPhaseBeanArray() {
        // TODO ANJ Surely there is a better way of organising this:
        log.debug("Building array of ExperimentPhaseBeans");
        ExperimentPhaseBean[] phaseBeans = new ExperimentPhaseBean[7]; 
        phaseBeans[ExperimentBean.PHASE_EXPERIMENTSETUP_1] =  
                new ExperimentPhaseBean(this, ExperimentPhase.PHASENAME_EXPERIMENTSETUP);
        phaseBeans[ExperimentBean.PHASE_EXPERIMENTSETUP_2] =  
                new ExperimentPhaseBean(this, ExperimentPhase.PHASENAME_EXPERIMENTSETUP);
        phaseBeans[ExperimentBean.PHASE_EXPERIMENTSETUP_3] = 
                new ExperimentPhaseBean(this, ExperimentPhase.PHASENAME_EXPERIMENTSETUP);
        phaseBeans[ExperimentBean.PHASE_EXPERIMENTAPPROVAL] =
                new ExperimentPhaseBean(this, ExperimentPhase.PHASENAME_EXPERIMENTAPPROVAL);
        phaseBeans[ExperimentBean.PHASE_EXPERIMENTEXECUTION] =
                new ExperimentPhaseBean(this, ExperimentPhase.PHASENAME_EXPERIMENTEXECUTION);
        phaseBeans[ExperimentBean.PHASE_EXPERIMENTEVALUATION] =
                new ExperimentPhaseBean(this, ExperimentPhase.PHASENAME_EXPERIMENTEVALUATION);
        return phaseBeans;
    }
    
    public String getCurrentPhaseName() {
        return exp.getCurrentPhase().getPhaseName();
    }
    
    public boolean isFinished() {
        if( this.currStage == ExperimentBean.PHASE_EXPERIMENTFINALIZED ) {
            return true;
        } else {
            return false;
        }
    }
    
    public String getReportHeader() {
        log.debug("get Report Header: "+this.ereportTitle);
        return this.ereportTitle;
    }
    
    public void setReportHeader(String text) {
    	initExpReport();
        this.ereportTitle = text;
        ExperimentReport exReport = this.getExperiment().getExperimentEvaluation().getExperimentReport();
        exReport.setHeader(ereportTitle);
    }
    
    public String getReportBody() {
        return this.ereportBody;
    }
    
    public void setReportBody(String text) {
    	initExpReport();
    	ExperimentReport exReport = this.getExperiment().getExperimentEvaluation().getExperimentReport();
        this.ereportBody = text;
        exReport.setBodyText(ereportBody);
    }
    
    private void initExpReport(){
    	if(this.getExperiment().getExperimentEvaluation().getExperimentReport()==null){
    		ExperimentReport exReport = new ExperimentReportImpl();
    		this.getExperiment().getExperimentEvaluation().setExperimentReport(exReport);
    	}
    	
    }
	
    public int getExpRating() {
        return this.expRating;
    }
    
    public void setExpRating(int rating) {
	
		this.expRating = rating;
		//set the rating for this experiment
		this.getExperiment().getExperimentEvaluation().setExperimentRating(this.expRating);
		//also treat the experimenter's rating as first user vote
        UserBean currentUser = (UserBean) JSFUtil.getManagedObject("UserBean");
		this.getExperiment().setUserRatingForExperiment(currentUser.getUserid(), (double)this.expRating);
    }
    
    public int getServiceRating() {
        return this.serviceRating;
    }
    
    public void setServiceRating(int rating) {
	
		this.serviceRating = rating;
		this.getExperiment().getExperimentEvaluation().setServiceRating(this.serviceRating);
    }
	
    /**
     * Helper to load already entered input data from the experiment's executable
     * into this backing bean
     * @param fileRefs
     */
    private void helperLoadInputData(Collection<String> fileRefs){
        // Use a new, empty panel:
        this.panelAddedFiles = new UIPanel();
    	Iterator<String> itFileRefs = fileRefs.iterator();
    	int i =0;
    	while(itFileRefs.hasNext()){
    	    log.debug("helperLoadInputData: adding file: "+i);
    	    String fileRef = itFileRefs.next();
    		this.inputData.put(i+"", fileRef);
    		// Also add an item to the panel:
    	    helperCreateRemoveFileElement( this.panelAddedFiles, fileRef, i+"" );
    		i++;
    	}
    }
    
    public boolean isOperationSelectionCompleted(){
    	if(this.currStage <= PHASE_EXPERIMENTSETUP_2)
    		return this.bOperationSelectionCompleted;
    	return true;
    }
    
    /**
     * Marks the process of selecting service + operation as completed
     * Note: selecting an other service or operation after this step leads to
     * loosing all added input data
     * @param b
     */
    public void setOpartionSelectionCompleted(boolean b){
    	this.bOperationSelectionCompleted = b;
    }
    
    /**
     * The panel to render all added file inputs
     * @return
     */
    public UIComponent getPanelAddedFiles(){
    	return this.panelAddedFiles;
    }
    
    public void setPanelAddedFiles(UIComponent panel){
    	this.panelAddedFiles = panel;
    }
    
    /**
     * The panel to restrict service selection by available tags
     * @param panel
     */
    public void setAddedSerTags(UIComponent panel){
    	this.panelAddedSerTags = panel;
    }
    
    public UIComponent getAddedSerTags(){  
    	return this.panelAddedSerTags;
    }
    
    
    /**
     * Creates the JSF Elements to render a given fileRef as CommandLink within the given UIComponent
     * @param panel
     * @param fileRef
     * @param key
     */
    public void helperCreateRemoveFileElement(UIComponent panel, String fileRef, String key){
        log.info("Looking for fileRef: "+fileRef+" w/ key: "+key);
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            DataHandler dh = new DataHandlerImpl();
            //file ref
            HtmlOutputText outputText = (HtmlOutputText) facesContext
                    .getApplication().createComponent(
                            HtmlOutputText.COMPONENT_TYPE);
            DigitalObjectRefBean dobr = dh.get(fileRef);
            outputText.setValue(" "+dobr.getName());
            outputText.setId("fileName" + key);
            //file name
            HtmlOutputLink link_src = (HtmlOutputLink) facesContext
                    .getApplication().createComponent(
                            HtmlOutputLink.COMPONENT_TYPE);
            link_src.setId("fileRef" + key);
            URI URIFileRef = dobr.getDownloadUri();
            link_src.setValue(URIFileRef);
            link_src.setTarget("_new");

            //CommandLink+Icon allowing to delete this entry
            HtmlCommandLink link_remove = (HtmlCommandLink) facesContext
                    .getApplication().createComponent(
                            HtmlCommandLink.COMPONENT_TYPE);
            //set the ActionMethod to the method: "commandRemoveAddedFileRef(ActionEvent e)"
            Class<?>[] parms = new Class<?>[] { ActionEvent.class };
            ExpressionFactory ef = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
            MethodExpression mb = ef.createMethodExpression(FacesContext.getCurrentInstance().getELContext(), 
                    "#{NewExp_Controller.commandRemoveAddedFileRef}", null, parms);
            link_remove.setActionExpression(mb);
            link_remove.setId("removeLink" + key);
            link_remove.setTitle("Remove this file."); // FIXME Localise this!
            //send along an helper attribute to identify which component triggered the event
            link_remove.getAttributes().put("IDint", key);
            HtmlGraphicImage image = (HtmlGraphicImage) facesContext
                    .getApplication().createComponent(
                            HtmlGraphicImage.COMPONENT_TYPE);
            image.setUrl("../graphics/user_trash.png");
            image.setAlt("delete-image");
            image.setId("graphicRemove" + key);
            link_remove.getChildren().add(image);

            //add all three components
            panel.getChildren().add(link_remove);
            link_src.getChildren().add(outputText);
            panel.getChildren().add(link_src);
            
        } catch (Exception e) {
            log.error("error building components for file removal "+e.toString());
        }
    }
        
    
    /*---------for step2 popup overlay-----------*/
    
    /**
     * Method adds a given service annotation tag and its value. Tags are
     * used to restrict the list of displayed services
     */
    public void addAnnotationTag(ServiceTag tag){
    	if(tag!=null){
    		this.mapAnnotTagVals.put(tag.getName(), tag);
    	}
    }
    
    /**
     * Method removes a given service annotation tag and its value. Tags are
     * used to restrict the list of displayed services
     * @param name
     */
    public void removeAnnotationTag(String name){
    	if(name!=null){
    		if(this.mapAnnotTagVals.containsKey(name)){
    			//remove tag
    			this.mapAnnotTagVals.remove(name);
    		}
    	}
    }
    
    /**
     * Method removes all service annotation tags and their values, which are
     * used to restrict the list of displayed services
     */
    public void removeAllAnnotationTags(){
    	this.mapAnnotTagVals = new HashMap<String,ServiceTag>();
    }
    
    /**
     * Returns a list of maps containing the selected annotation tag names and values
     * e.g. list<map<"name","author">,<"value","val1 or ""><"description","description1">..
     * @return
     */
    public Collection<ServiceTag> getSelectedAnnotationTags(){
    	return this.mapAnnotTagVals.values();
    }
    
    public String getServiceXMLResponds(){
    	return this.exp.getExperimentExecutable().getServiceXMLResponds();
    }
    
   
    /**
     * Triggers: display xml responds in case of an error on page 6
     * @param b
     */
    public void setViewXMLRespondsTrue(){
    	this.bRenderXMLResponds = true;
    }
    
    public void setViewXMLRespondsFalse(){
    	this.bRenderXMLResponds = false;
    }
    
    public boolean isViewXMLResponds(){
    	return this.bRenderXMLResponds;
    }
    
    private String sSelBMGoalCategoryValueToFilter = "";
    /**
     * The filter on stage3 for selecting which BMGoals to display
     * Please note the selection is already predetermined by the
     * experiment digital object type from page 1
     * @param catname
     */
    public void setBMGoalCategoryFilterValue(String catname){
    	sSelBMGoalCategoryValueToFilter = catname;
    }
    
    public String getBMGoalCategoryFilterValue(){
    	return this.sSelBMGoalCategoryValueToFilter;
    }
   
    /**
     * Please note the selection is already predetermined by the
     * experiment digital object type from page 1
     * @return
     */
    public List<SelectItem> getAllBMGoalCategoriesForFilter(){
    	List<SelectItem> ret = new ArrayList<SelectItem>();
    	Iterator<String> selDigObjTypes = this.getDtype().iterator();
    	int count=0;
    	while(selDigObjTypes.hasNext()){
    		String SDTypeID = selDigObjTypes.next();
    		ret.add(new SelectItem(dtypeImpl.getDtypeName(SDTypeID)));
    		if(count==0)
    			this.setBMGoalCategoryFilterValue(dtypeImpl.getDtypeName(SDTypeID));
    		count++;
    	}
    	ret.add(new SelectItem("disable highlighting"));
    	return ret;
    }
    
    public boolean isOldExperiment() {
        if( AdminManagerImpl.getInstance().isDeprecated( this.getEtype() )) {
            return true;
        } else {
           return false;
       }
    }

    public String getBGExperimentText() {
        if( this.getEtypeName() == null ) return "";
        
        //would prefer to read the returned text from the backed resource file
        if (this.getEtypeName().equalsIgnoreCase("simple characterisation"))
            return "Correct Characterisation Of... ";
        if (this.getEtypeName().equalsIgnoreCase("simple migration"))
            return "Preservation Of... ";
        else return "";
    }

    
    /* Auto complete and ajax hooks */
    
    /**
     * 
     * @param query
     * @return
     */
    public List<User> autocompleteUsers( Object query ) {
        if( query == null) return null;
        // look for matching users:
        String qs = (String) query;
        log.debug("Looking for users that match " + qs );
        // Get all users:
        UserManager um = UserBean.getUserManager();
        // Filter this into a list of matching users:
        ArrayList<User> matches = new ArrayList<User>();
        for( User u : um.getUsers() ) {
            if( u.getUsername().startsWith(qs) ||
                u.getFirstName().startsWith(qs) ||
                u.getLastName().startsWith(qs) ) {
                  matches.add(u);
            }
        }
        return matches;
    }
    
    /**
     * 
     * @param query
     * @return
     */
    public List<Experiment> autocompleteExperiments(Object query ) {
        if( query == null) return null;
        // look for matching users:
        String qs = (String) query;
        log.debug("Looking for experiments that match " + qs );
        TestbedManager testbedMan = (TestbedManager)JSFUtil.getManagedObject("TestbedManager");  
        List<Experiment> allExps = testbedMan.searchAllExperiments(qs);
        log.debug("Found "+allExps.size()+" matching experiment(s).");
        return allExps;
    }
    
     /**
     * @return the erefFinder
     */
    public String getErefFinder() {
        return erefFinder;
    }

    /**
     * @param erefFinder the erefFinder to set
     */
    public void setErefFinder(String erefFinder) {
        this.erefFinder = erefFinder;
    }
    
    /**
     * 
     * @return
     */
    public String addAnotherExpRefAction() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        log.info("Looking for matches.");
        List<Experiment> matches = this.autocompleteExperiments(expBean.erefFinder);
        log.info("Found "+matches.size()+" matches.");
        if( matches != null && matches.size() == 1 ) {
            if( ! expBean.eref.contains(""+matches.get(0).getEntityID())) {
                expBean.eref.add(""+matches.get(0).getEntityID());
                log.info("Added ID:"+matches.get(0).getEntityID());
            }
            this.erefFinder = "";
        }
        return "success";
    }
    
    /**
     * 
     * @param exp
     */
    public String removeExpRef() {
        log.info("Attempting to remove: "+expToRemove);
        if( expToRemove == null ) return "success";
        log.info("Attempting to remove: "+expToRemove.getExperimentSetup().getBasicProperties().getExperimentName());
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        if( expBean.eref.contains(""+expToRemove.getEntityID()))
            expBean.eref.remove(""+expToRemove.getEntityID());
        return "success";
    }
    
    /**
     * 
     */
    private Experiment expToRemove;

    /**
     * @return the expToRemove
     */
    public Experiment getExpToRemove() {
        return expToRemove;
    }

    /**
     * @param expToRemove the expToRemove to set
     */
    public void setExpToRemove(Experiment expToRemove) {
        this.expToRemove = expToRemove;
    }
    
    
    /**
     * Indicates if an FacesMessage (e.g. error or info message) was added for the current context
     * Used for displaying source/target validation errors
     * @return
     */
    public boolean isHasErrMessages() {
    	return FacesContext.getCurrentInstance().getMessages().hasNext();
    }
    
    
    public void setHasErrMessages(boolean b){
    	//
    }
    
    
	private boolean bEvalWFRunning = false;
	private Calendar cEvalWFRunningStart = new GregorianCalendar();
	public void setExecuteAutoEvalWfRunning(boolean b){
		this.bEvalWFRunning = b;
		if(b)
			cEvalWFRunningStart = new GregorianCalendar();
		if(!b){
			 FacesMessage fmsg = new FacesMessage();
			 fmsg.setDetail("auto evaluation completed");
			 fmsg.setSummary("auto evaluation completed");
			 fmsg.setSeverity(FacesMessage.SEVERITY_INFO);
			 FacesContext ctx = FacesContext.getCurrentInstance();
			 ctx.addMessage("evalWFProgress:autoEvalButton",fmsg); 
		}
	}
	
	/**
	 * Indicates if/not an autoEvaluation workflow is within the process of execution
	 * @return
	 */
	public boolean isExecuteAutoEvalWfRunning(){
		 return this.bEvalWFRunning;
	}
	
	/**
	 * Indicates how long the autoEvalWF is already running
	 * @return
	 */
	public String getAutoEvalWFRunningSeconds(){
		Calendar cEvalWFRunningSec = new GregorianCalendar();
		return ((cEvalWFRunningSec.getTimeInMillis() - cEvalWFRunningStart.getTimeInMillis())/1000)+"";
	}

    /**
     * @return the selectedExecutionRecord
     */
    public ExecutionRecordImpl getSelectedExecutionRecord() {
        return selectedExecutionRecord;
    }

    /**
     * @param selectedExecutionRecord the selectedExecutionRecord to set
     */
    public void setSelectedExecutionRecord(
            ExecutionRecordImpl selectedExecutionRecord) {
        if( selectedExecutionRecord != null )
            log.info("Setting exec record: "+selectedExecutionRecord.getDigitalObjectSource());
        this.selectedExecutionRecord = selectedExecutionRecord;
    }
    
    /**
     * @return the selectedBatchExecutionRecord
     */
    public BatchExecutionRecordImpl getSelectedBatchExecutionRecord() {
        if( selectedExecutionRecord != null ) {
            log.info("Getting exec record: "+selectedBatchExecutionRecord.getRuns().size());
        } else {
            this.selectedBatchExecutionRecord = this.exp.getExperimentExecutable().getBatchExecutionRecords().iterator().next();
        }
        log.info("Getting exec record: "+selectedBatchExecutionRecord);
        return selectedBatchExecutionRecord;
    }

    /**
     * @param selectedBatchExecutionRecord the selectedBatchExecutionRecord to set
     */
    public void setSelectedBatchExecutionRecord(
            BatchExecutionRecordImpl selectedBatchExecutionRecord) {
        log.info("Setting batch record: "+selectedBatchExecutionRecord);
        if( selectedBatchExecutionRecord != null  ) {
            log.info("Setting batch record runs: "+selectedBatchExecutionRecord.getRuns().size());
        }
        log.info("Setting batch record: "+selectedBatchExecutionRecord);
        this.selectedBatchExecutionRecord = selectedBatchExecutionRecord;
    }
    
    /**
     * @return the selectedDigitalObject
     */
    public String getSelectedDigitalObject() {
        return selectedDigitalObject;
    }

    /**
     * @param selectedDigitalObject the selectedDigitalObject to set
     */
    public void setSelectedDigitalObject(String selectedDigitalObject) {
        this.selectedDigitalObject = selectedDigitalObject;
    }

    /**
     * @return
     */
    public List<ExperimentStageBean> getStages() {
        ExpTypeBackingBean exptype = ExpTypeBackingBean.getExpTypeBean(getEtype());
        if( exptype != null ) {
            return exptype.getStageBeans();
        } else {
            log.warn("Got experiment type NULL: "+this.getEtype());
            return null;
        }
    }

    /**
     * @param stage
     */
    public void setSelectedStage(ExperimentStageBean stage) {
        this.selectedStage = stage;
    }
    
    /**
     * @return the selectedStage
     */
    public ExperimentStageBean getSelectedStage() {
        if( selectedStage == null ) {
            if( getStages() != null && getStages().size() > 0 ) {
                selectedStage = getStages().get(0);
            }
        }
        return selectedStage;
    }
    
    public void setNumExecutions(long numExecutions){
    	this.numExecutions = numExecutions;
    }
    
    /**
     * @return the number of executions
     */
    public long getNumExecutions(){
    	return this.numExecutions;
    }

    /**
     * @param expBean
     */
    public static boolean saveExperimentFromSession(ExperimentBean expBean) {
        // create message for name error message
        FacesMessage fmsg = new FacesMessage();
        fmsg.setDetail("Experiment name was not valid and could not be stored!");
        fmsg.setSummary("Experiment name could not be stored!");
        fmsg.setSeverity(FacesMessage.SEVERITY_ERROR);
        // Flag to catch new/existing state:
        log.debug("Checking if this is a new experiment.");
        Experiment exp = expBean.getExperiment();
        // if not yet created, create new Experiment object and new Bean
        if ((expBean.getID() <= 0)) { 
            // Create new Experiment if necessary
            if( exp == null )
                exp = new ExperimentImpl();
            // Get userid info from managed bean
            UserBean currentUser = (UserBean) JSFUtil.getManagedObject("UserBean");
            // set current User as experimenter
            exp.getExperimentSetup().getBasicProperties().setExperimenter(currentUser.getUserid());
            try {
                log.debug("New experiment, setting name: " + expBean.getEname() );
                exp.getExperimentSetup().getBasicProperties().setExperimentName(expBean.getEname());        
            } catch (InvalidInputException e) {
                // add message-tag for duplicate name
                FacesContext ctx = FacesContext.getCurrentInstance();
                ctx.addMessage("ename",fmsg);
                return false;
            }
            log.info("Creating a new experiment.");
            TestbedManager testbedMan = (TestbedManager) JSFUtil.getManagedObject("TestbedManager");
            long expId = testbedMan.registerExperiment(exp);
            expBean.setID(expId);
            expBean.setExperiment(testbedMan.getExperiment(expId));
        }
        log.debug("Created experiment if necessary, now passing it back.");    
        return true;
    }
    
    public long persistExperiment() {
        TestbedManager testbedMan = (TestbedManager) JSFUtil.getManagedObject("TestbedManager");
        long eid = testbedMan.registerExperiment(this.getExperiment());
        log.info("Created experiment eid = "+eid);
        return eid;
    }
    
    public void updateExperiment() {
        TestbedManager testbedMan = (TestbedManager) JSFUtil.getManagedObject("TestbedManager");
        testbedMan.updateExperiment(this.getExperiment());
    }
    
    private String selDigORefStep5OverviewTable = null;
    public void setSelDigitalObjectRefInStep5OverviewTable(String inputDigObjRef){
    	this.selDigORefStep5OverviewTable = inputDigObjRef;
    }
    
    public String getSelDigitalObjectRefPageInStep5OverviewTable(){
    	if(this.selDigORefStep5OverviewTable==null){
    		if(!this.getExperimentInputDataValues().isEmpty()){
    			selDigORefStep5OverviewTable = this.getExperimentInputDataValues().iterator().next().getDigitalObject();
    		}
    	}
    	return this.selDigORefStep5OverviewTable;
    }
    
    public void processDigitalObjectRefInStep5OverviewTable(ActionEvent e){
    	for(UIComponent c : e.getComponent().getChildren()){
    		if(c instanceof UIParameter){
    			UIParameter p = (UIParameter)c;
    			if(p.getName().equals("selInputDataRef")){
    				this.setSelDigitalObjectRefInStep5OverviewTable((String)p.getValue());
    			}
    		}
    	}
    }
    
    //TODO continue when time, fetch digital object and use ImageThumbnail class to generate a thumbnail
    /*public String getDigitalObjectRefInStep5OverviewTableThumbnail(){
    	try {
			DigitalObject digo = new DataHandlerImpl().getDigitalObject(this.getSelDigitalObjectRefPageInStep5OverviewTable());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }�*/
    
    /**
     * Gathers all manual experiment results over all experiment runs for a given measurement property and a selected inputDigitalObject (selDigORefStep5OverviewTable)
     * and groups this data by the stage name  
     * @param
     * @return
     */
    public HashMap<String, List<MeasurementPropertyResultsBean>> getAllManualExecutionRecords(){
    	HashMap<String, List<MeasurementPropertyResultsBean>> ret = new HashMap<String, List<MeasurementPropertyResultsBean>>();
    	for(ExperimentStageBean stage : this.getStages()){
    		ret.put(stage.getName(), this.getAllManualExecutionRecordsHelper(this.getSelDigitalObjectRefPageInStep5OverviewTable(), stage.getName(),true));
    	}
    	return ret;
    }
    
    
    private List<MeasurementPropertyResultsBean> getAllManualExecutionRecordsHelper(String inputDigoRef, String stageName, boolean manualProps){
    	List<MeasurementPropertyResultsBean> ret = new ArrayList<MeasurementPropertyResultsBean>();
    	String etype = this.getEtype();
		ExpTypeBackingBean.getExpTypeBean(etype);
		
		//1. get all measurement property IDs
		Vector<String> propertyIDs = null;
		if(manualProps){
			//fetch the manual properties
			propertyIDs = this.getExperiment().getExperimentExecutable().getManualProperties(stageName);
		}
		else{
			//fetch the automatically measured properties
			//FIXME: not correct: still need to find out stage information for automatically measured properties
			propertyIDs = this.getExperiment().getExperimentExecutable().getProperties();
		}
		
		//2. build the results on a per property basis
		for(String propertyID : propertyIDs){
			
			MeasurementPropertyResultsBean resBean = new MeasurementPropertyResultsBean(inputDigoRef, propertyID,this.getAllRunDates());
			
			//2a. now iterate over the results and filter out the relevant ones for this property
			for(BatchExecutionRecordImpl batchr : this.getExperiment().getExperimentExecutable().getBatchExecutionRecords()){
				Calendar runDate = batchr.getEndDate();
				for(ExecutionRecordImpl execRec : batchr.getRuns()){
					//filter out by the selected inputDigitalObject
					if(execRec.getDigitalObjectReferenceCopy().equals(inputDigoRef)){
						
						for(ExecutionStageRecordImpl execStageRec : execRec.getStages()){
							//filter out the selected stage
							if(execStageRec.getStage().equals(stageName)){
								List<MeasurementImpl> mRecords=null;
								if(manualProps){
									//fetch the manual properties
									mRecords = execStageRec.getManualMeasurements();
								}
								else{
									//fetch the automatically measured properties
									mRecords = execStageRec.getMeasurements();
								}
								for(MeasurementImpl mr : mRecords){
									if(mr.getIdentifier().equals(propertyID)){
										//log.info("adding "+inputDigoRef+ " "+runDate.getTimeInMillis()+" "+execStageRec.getStage()+" "+mr.getIdentifier() +" "+ mr.getValue());
										//found the measurementRecord for this property in this run
										resBean.addResult(runDate, mr);
									}
								}
							}
						}
					}
				}
			}
			
			//finally add the MeasurementInfo data (name, description, for the propertyID etc.
			if(manualProps){
				OntologyProperty ontop = OntologyHandlerImpl.getInstance().getProperty(propertyID);
				//create a MeasurementImpl from the OntologyProperty
				try {
					MeasurementImpl measurementinfo = OntoPropertyUtil.createMeasurementFromOntologyProperty(ontop);
					resBean.setMeasurementInfo(measurementinfo);
				} catch (Exception e) {
					log.debug("Could not retrieve Ontology Property information for propertyID: "+propertyID);
				}
			}
			else{
				//TODO: still need to request this information from the workflow authority
			}
			ret.add(resBean);
		}
		return ret;
    }
    
    /**
     * Returns a list of all experiment run dates
     * @return
     */
    public List<Calendar> getAllRunDates(){
    	List<Calendar> ret = new ArrayList<Calendar>();
		for(BatchExecutionRecordImpl batchr : this.getExperiment().getExperimentExecutable().getBatchExecutionRecords()){		
			ret.add(batchr.getEndDate());
		}
		return ret;
    }
    
    public int getAllRunDatesSize(){
    	return this.getAllRunDates().size();
    }
    
    public boolean isRunDateBatchRunSucceeded(Calendar runDate){
    	boolean b = false;
    	for(BatchExecutionRecordImpl batchr : this.getExperiment().getExperimentExecutable().getBatchExecutionRecords()){		
			if(batchr.getEndDate().getTime().equals(runDate.getTime())){
				b = batchr.isBatchRunSucceeded();
			}
		}
    	return b;
    }
    
    /**
     * Returns a single list with manual and automatically measured properties for an in the evaluation table usable form
     * Fetches information for a pre-selected inputDigitalObject Ref
     * @return
     */
    public List<EvaluationPropertyResultsBean> getEvaluationPropertyResultsBeans(){
    	return getEvaluationPropertyResultsBeans(this.selDigORefStep6DigoEvalTable);
    }
    
    public List<EvaluationPropertyResultsBean> getEvaluationPropertyResultsBeans(String inputDigoRef){
    	String etype = this.getEtype();
    	String[] stagesToCompare = null;
    	
    	/*
    	 * Decides which stages are connected for evaluation and determines a common set of properties
    	 * . e.g. within an migration experiment we're evaluating a common set of properties pre and post migration process.
    	 */
    	if( etype.equals( AdminManagerImpl.IDENTIFY ) ) {
            stagesToCompare = new String[]{IdentifyWorkflow.STAGE_IDENTIFY};
        } else if( etype.equals( AdminManagerImpl.MIGRATE ) ) {
        	stagesToCompare = new String[]{MigrateWorkflow.STAGE_PRE_MIGRATE,MigrateWorkflow.STAGE_POST_MIGRATE};
        } else if(etype.equals( AdminManagerImpl.EMULATE)){
        	stagesToCompare = new String[]{ViewerWorkflow.STAGE_CREATEVIEW};
        }
    	//get the manual property results
    	List<EvaluationPropertyResultsBean> manualProps = this.getEvaluationPropertyResultsBeansHelper(inputDigoRef, stagesToCompare, true);
    	
    	//get the service extracted property results
    	List<EvaluationPropertyResultsBean> autoMProps = this.getEvaluationPropertyResultsBeansHelper(inputDigoRef, stagesToCompare,false);
    	
    	//merge the two - we're doing joint evaluation
    	manualProps.addAll(autoMProps);
    
    	return manualProps;
    }
    
    /**
     * Gathers all records that are used for evaluation for a given measurement property and a selected inputDigitalObject
     * and groups them according to the experiment type's evaluation rules in a EvaluationPropertyResultsBean which contains the table line's information
     * TODO AL: split this method up into sub-methodds and share common parts with getAllManualExecutionRecordsHelper
     * TODO AL: Implementation for manualProps = false : automatically measured properties still missing
     * @param
     * @return
     */
    @SuppressWarnings("unchecked")
	private List<EvaluationPropertyResultsBean> getEvaluationPropertyResultsBeansHelper(String inputDigoRef, String[] comparedStageNames, boolean manualProps){
    	
    	List<EvaluationPropertyResultsBean> ret = new ArrayList<EvaluationPropertyResultsBean>();
    	
		//1. get all measurement property IDs
		Vector<String>[] lpropertyIDs = new Vector[comparedStageNames.length];
		if(manualProps){
			//fetch the manual properties
			int count = 0;
			for(String stageName : comparedStageNames){
				lpropertyIDs[count] = this.getExperiment().getExperimentExecutable().getManualProperties(stageName);
				count++;
			}
		}
		else{
			//fetch the automatically measured properties
			//FIXME: not correct: still need to find out stage information for automatically measured properties
			//propertyIDs = expBean.getExperiment().getExperimentExecutable().getProperties();
			int count = 0;
			for(String stageName : comparedStageNames){
				lpropertyIDs[count] = helperBuildAutoPropertyPerStage().get(stageName);
				count++;
			}
		}
		
		//1b) determine a common set of property IDs that are available in all requested stages
		List<String> commonPropIDs = new ArrayList<String>();
		for(int i=0; i<lpropertyIDs.length;i++){
			Vector<String> propertyIDs = lpropertyIDs[i];
			if(i==0){
				if(propertyIDs!=null){
					commonPropIDs = propertyIDs;
				}
			}else{
				List<String> propsForRemoval = new ArrayList<String>();
				for(String propID : commonPropIDs){
					if(propertyIDs != null && !propertyIDs.contains(propID)){
						//property not contained in all requested stages - remove
						propsForRemoval.add(propID);
					}
				}
				commonPropIDs.removeAll(propsForRemoval);
			}
		}
		
			
		//2. build the results on a per property basis
		if( commonPropIDs != null ) {
		for(String propertyID : commonPropIDs){
			
			EvaluationPropertyResultsBean evalPropResBean = new EvaluationPropertyResultsBean(inputDigoRef, propertyID,this.getAllRunDates(),comparedStageNames);
			List<String> lStageNames = Arrays.asList(comparedStageNames);
			
			//2a. now iterate over the results and filter out the relevant ones for this property
			for(BatchExecutionRecordImpl batchr : this.getExperiment().getExperimentExecutable().getBatchExecutionRecords()){
				Calendar runDate = batchr.getEndDate();
				for(ExecutionRecordImpl execRec : batchr.getRuns()){
					//filter out by the selected inputDigitalObject
					if(execRec.getDigitalObjectReferenceCopy().equals(inputDigoRef)){
						
						for(ExecutionStageRecordImpl execStageRec : execRec.getStages()){
							//filter out the selected stage
							if(lStageNames.contains(execStageRec.getStage())){
							
								/*
								 * FIXME: change this code - as a MeasurementImpl m 
								 * from execStageRec.getMeasuredObservables() 
								 * already has the value contained - accessible through m.getValue
								 */
								List<MeasurementImpl> mRecords=null;
								if(manualProps){
									//fetch the manual properties
									mRecords = execStageRec.getManualMeasurements();
								}
								else{
									//fetch the automatically measured properties
									mRecords = execStageRec.getMeasurements();
								}
								for(MeasurementImpl mr : mRecords){
									if(mr.getIdentifier().equals(propertyID)){
										//found the measurementRecord for this property in this run
										evalPropResBean.addMeasurementResult(runDate, execStageRec.getStage(), mr);
									}
								}
							}
						}
					}
				}
			}

			//2b. now iterate over the evaluation results and filter out the relevant ones
			List<PropertyEvaluationRecordImpl> propEvalRecordords = this.getExperiment().getExperimentEvaluation().getPropertyEvaluation(inputDigoRef);
			if(propEvalRecordords!=null){
				for(PropertyEvaluationRecordImpl propEvalRecordImpl : propEvalRecordords){
					//filter by the propertyID we're looking for
					if(propEvalRecordImpl.getPropertyID().equals(evalPropResBean.getMeasurementPropertyID())){
						//set the line evaluation value
						evalPropResBean.setPropertyEvalValue(propEvalRecordImpl.getPropertyEvalValue());
						for(Calendar runDate : this.getAllRunDates()){
							PropertyRunEvaluationRecordImpl propRunEvalRecImpl = propEvalRecordImpl.getPropertyRunEvalRecord(runDate);
							if(propRunEvalRecImpl!=null){
								//set the per run evaluation value
								evalPropResBean.setEvalResultValue(runDate, comparedStageNames, propRunEvalRecImpl.getRunEvalValue());
							}
						}
					}
				}
			}
			
			//3.finally add the MeasurementInfo data (name, description, for the propertyID etc.
			if(manualProps){
				OntologyProperty ontop = OntologyHandlerImpl.getInstance().getProperty(propertyID);
				//create a MeasurementImpl from the OntologyProperty
				try {
					MeasurementImpl measurementinfo = OntoPropertyUtil.createMeasurementFromOntologyProperty(ontop);
					evalPropResBean.setMeasurementInfo(measurementinfo);
				} catch (Exception e) {
					log.debug("Could not retrieve Ontology Property information for propertyID: "+propertyID);
				}
			}
			else{
				//TODO: still need to request this information from the workflow authority
				MeasurementImpl measurementinfo = helperQueryAutoMeasurementAuthority(propertyID);
				evalPropResBean.setMeasurementInfo(measurementinfo);
			}
			
			//Finally: check requirements if to add this evalProp item
			//if(checkIfToAddEvaluationPropertyBean(evalPropResBean,comparedStageNames)){
				ret.add(evalPropResBean);
			//}
		}
		}
		return ret;
    }
    
    
    /**
     * Builds a map of all measured property IDs per stage, which isn't explicitly stored.
     * Map<StageName,List<PropertyID>
     * @return
     */
    private HashMap<String,Vector<String>> helperBuildAutoPropertyPerStage(){
    	HashMap<String,Vector<String>> ret = new HashMap<String,Vector<String>>();
    	Iterator<String> it = helperBuildAutoPropertyAuthority().keySet().iterator();
    	while(it.hasNext()){
    		String stageName = it.next();
    		Vector<String> propIDs = new Vector<String>();
    		for(MeasurementImpl m : helperBuildAutoPropertyAuthority().get(stageName)){
    			propIDs.add(m.getIdentifier()+"");
    		}
    		ret.put(stageName, propIDs);
    	}
    	return ret;
    }
    
    /**
     * A dirty workaround 
     * @param propertyID
     * @return
     */
    private MeasurementImpl helperQueryAutoMeasurementAuthority(String propertyID){
    	Iterator<String> it = helperBuildAutoPropertyAuthority().keySet().iterator();
    	while(it.hasNext()){
    		String stageName = it.next();
    		for(MeasurementImpl m : helperBuildAutoPropertyAuthority().get(stageName)){
    			if((m.getIdentifier()+"").equals(propertyID)){
    				return m.clone();
    			}
    		}
    	}
    	return null;
    }
    
    HashMap<String,Vector<MeasurementImpl>> tempAutoPropAuthority = null;
    private HashMap<String,Vector<MeasurementImpl>> helperBuildAutoPropertyAuthority(){
    	if(tempAutoPropAuthority==null){
	    	HashMap<String,Vector<MeasurementImpl>> ret = new HashMap<String,Vector<MeasurementImpl>>();
	    	for(BatchExecutionRecordImpl batchr : this.getExperiment().getExperimentExecutable().getBatchExecutionRecords()){
	    		//filter out the inputDigo's we're looking for
	    		for(ExecutionRecordImpl execRec : batchr.getRuns()){
		    		for(ExecutionStageRecordImpl stage : execRec.getStages()){
		    			Vector<MeasurementImpl> propIDs = new Vector<MeasurementImpl>();
		    			for(MeasurementImpl measurement : stage.getMeasuredObservables()){
		    				//copy to avaid any cross refs and add copy for authority
		    				MeasurementImpl m = measurement.clone();
		    				propIDs.add(m);
		    			}
		    			ret.put(stage.getStage(), propIDs);
		    		}
	    		}
	    	}
	    	tempAutoPropAuthority = ret;
    	}
    	return tempAutoPropAuthority;
    }
    
    /**
     * Checks if the requirements for this bean have been met through the population process
     * 1.checks if at least one value for all measured stages has been submitted
     * 2.if the property is contained in all requested stages
     * @return
     */
    /*private boolean checkIfToAddEvaluationPropertyBean(EvaluationPropertyResultsBean evalPropResBean, String[] comparedStageNames){
    	boolean atLeastOneFound = false;
    	for(Calendar runDate : this.getAllRunDates()){
    		HashMap<String,EvalRecordBean> evalResults = evalPropResBean.getAllEvalResults().get(runDate.getTimeInMillis());
    		if(evalResults != null){
    			//c2. check property is contained in all requested stages
    			if(evalResults.size()!=comparedStageNames.length){
					//property not measured in all requested stages
					return false;
				}
    			//c1. check if a record value for a certain run date was submitted or extracted
    			boolean bOK = true;
    			for(String compStageName : comparedStageNames){
    				
    				String recordValue = evalResults.get(compStageName).getRecordValue();
    				if((recordValue!=null)&&(!recordValue.equals(""))){
    					//ok - this record is fine - check the other stages for this property
    				}
    				else{
    					bOK = false;
    				}
    			}
    			if(bOK){
    				atLeastOneFound = true;
    			}	
    		}		
    	}
    	return atLeastOneFound;
    }*/
    
    private String selDigORefStep6DigoEvalTable = null;
    public void setSelDigitalObjectRefInStep6DigoEvalTable(String inputDigObjRef){
    	this.selDigORefStep6DigoEvalTable = inputDigObjRef;
    }
    
    public String getSelDigitalObjectRefInStep6DigoEvalTable(){
    	if(this.selDigORefStep6DigoEvalTable==null){
    		if(!this.getExperimentInputDataValues().isEmpty()){
    			selDigORefStep6DigoEvalTable = this.getExperimentInputDataValues().iterator().next().getDigitalObject();
    		}
    	}
    	return this.selDigORefStep6DigoEvalTable;
    }
    
    public void processDigitalObjectRefInStep6DigoEvalTable(ActionEvent e){
    	for(UIComponent c : e.getComponent().getChildren()){
    		if(c instanceof UIParameter){
    			UIParameter p = (UIParameter)c;
    			if(p.getName().equals("selInputDataRef")){
    				this.setSelDigitalObjectRefInStep6DigoEvalTable((String)p.getValue());
    			}
    		}
    	}
    }
    
    List<MeasurementImpl> propertyIDsForExperimentEvaluation = new ArrayList<MeasurementImpl>();
    /**
     * Returns a list of all propertyIDs (in form of MeasurementImpl with Name, etc) we're evaluating in step6 of an experiment
     * @return
     */
    public List<MeasurementImpl> getPropertyIDsForOverallExperimentEvaluation(){
    	if(propertyIDsForExperimentEvaluation.size()<1)
    		for(EvaluationPropertyResultsBean b : this.getEvaluationPropertyResultsBeans()){
    			propertyIDsForExperimentEvaluation.add(b.getMeasurementInfo());
    		}
    	return propertyIDsForExperimentEvaluation;
    }

    
    PropertyDnDTreeBean simpleTreeDndBean = null;
    /**
     * Get the DnD Ontology Bean, creating it if required.
     * @return
     */
    public PropertyDnDTreeBean getOntologyDnDBean() {
        if( simpleTreeDndBean == null ) simpleTreeDndBean = new PropertyDnDTreeBean();
        return simpleTreeDndBean;
    }
   

    /* ----------------- Chart stuff ------------------- */

   JFreeChart chart = null;
   String graphId = null;
   String graphImageMap = null;
   String graphUrl = null ;
   
   public String getResultChartUrl() {
       try {
           ExperimentChartServlet ec = new ExperimentChartServlet();
           chart = ec.createWallclockChart( ""+this.getExperiment().getEntityID() );
           // Pick into the session...
           HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
           HttpSession session = request.getSession();
           //  Write the chart image to the temporary directory
           ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
           this.graphId = ServletUtilities.saveChartAsPNG(chart, 600, 500, info, session);
           this.graphImageMap = ChartUtilities.getImageMap(graphId, info);
           this.graphUrl = request.getContextPath() + "/servlet/DisplayChart?filename=" + graphId;
           return this.graphUrl;
       } catch ( Exception e ) {
           log.error("Failure while generating graph: "+e);
           e.printStackTrace();
           return null;
       }
   }
   public String getResultChartImageMap() {
       return this.graphImageMap;
   }
   public String getResultChartIdentifier() {
       return this.graphId;
   }
   /*---------------------end Chart stuff ------------------*/
   
   
   private List<String> lTempFileDownloadLinkForWEEWFResults = new ArrayList<String>();;
   /**
    * Get download links for all BatchExecutionRecordImpl - in future we're only
    * gonna have one batchRecord anyway.
    * @return
    */
   public List<String> getTempFileDownloadLinkForWEEWFResults(){
   	ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
   	if(expBean.getExperiment()==null){
			//this is the case when the 'new experiment' hasn't been persisted
			return new ArrayList<String>();
		}
   	//check if we need to update the cache
   	if(expBean.getExperiment().getExperimentExecutable().getBatchExecutionRecords().size()!=lTempFileDownloadLinkForWEEWFResults.size()){
   		lTempFileDownloadLinkForWEEWFResults = new ArrayList<String>();
   		for(BatchExecutionRecordImpl batchRec : expBean.getExperiment().getExperimentExecutable().getBatchExecutionRecords()){
				if((batchRec.getWorkflowExecutionLog()!=null)&&(batchRec.getWorkflowExecutionLog().getSerializedWorkflowResult()!=null)){
					//create a temp file for this.
					DataHandler dh = new DataHandlerImpl();
					try {
						//get a temporary file
						File f = dh.createTempFileInExternallyAccessableDir();
						Writer out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(f), "UTF-8" ) );
						out.write(batchRec.getWorkflowExecutionLog().getSerializedWorkflowResult());
						out.close();
						lTempFileDownloadLinkForWEEWFResults.add(""+dh.getHttpFileRef(f));
					} catch (Exception e) {
						log.debug("Error getting getTempFileDownloadLinkForWEEWFResults "+e);
						return new ArrayList<String>();
					}
				}
				else{
					return new ArrayList<String>();
				}
			}
   	}else{
   		//just return the cached object
   		return lTempFileDownloadLinkForWEEWFResults;
   	}
		return lTempFileDownloadLinkForWEEWFResults;
   }
   
   /**
    * @return
    */
   public boolean isCurrentUserAnExperimenter() {
       UserBean user = (UserBean)JSFUtil.getManagedObject("UserBean");
       if( user == null || user.getUserid() == null ) return false;
       // Admins can always edit:
       if( user.isAdmin() ) return true;
       // Check if this user is authorised:
       if( this.getExperiment() == null ) return false;
       log.info("Exp not null");
       if( this.getExperiment().getExperimentSetup() == null ) return false;
       log.info("Setup not null");
       if( this.getExperiment().getExperimentSetup().getBasicProperties() == null ) return false;
       log.info("BasicProp not null");
       if( this.getExperiment().getExperimentSetup().getBasicProperties().getInvolvedUserIds() == null ) return false;
       log.info("InvolvedUsers not null");
       for( String authUser : this.getExperiment().getExperimentSetup().getBasicProperties().getInvolvedUserIds()) {
           if( user.getUserid().equals(authUser)) return true;
       }
       return false;
   }
   
   /**
    * @return
    */
   public boolean isStage1ReadOnly() {
       if( ! this.isCurrentUserAnExperimenter() ) return true;
       if( this.getApproved() == true ) return true;
       return false;
   }
   public boolean isStage2ReadOnly() {
       // As stage 1
       return this.isStage1ReadOnly();
   }
   public boolean isStage3ReadOnly() {
       if( ! this.isCurrentUserAnExperimenter() ) return true;
       return false;
   }
   public boolean isStage4ReadOnly() {
       if( ! this.isCurrentUserAnExperimenter() ) return true;
       if( this.isFinished() ) return true;
       return false;
   }
   public boolean isStage5ReadOnly() {
       // As stage 4
       return this.isStage4ReadOnly();
   }
   public boolean isStage6ReadOnly() {
       // As stage 4
       return this.isStage4ReadOnly();
   }

}
