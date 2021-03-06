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
/**
 * 
 */
package eu.planets_project.tb.gui.backing.exp;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.techreg.formats.Format;
import eu.planets_project.ifr.core.techreg.formats.Format.UriType;
import eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate;
import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf;
import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf.Services;
import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf.Template;
import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf.Services.Service;
import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf.Services.Service.Parameters;
import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf.Services.Service.Parameters.Param;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.tb.api.data.util.DataHandler;
import eu.planets_project.tb.api.data.util.DigitalObjectRefBean;
import eu.planets_project.tb.api.model.ExperimentExecutable;
import eu.planets_project.tb.api.system.batch.BatchProcessor;
import eu.planets_project.tb.gui.backing.ExperimentBean;
import eu.planets_project.tb.gui.backing.ServiceBrowser;
import eu.planets_project.tb.gui.backing.data.DigitalObjectCompare;
import eu.planets_project.tb.gui.backing.data.DigitalObjectTreeNode;
import eu.planets_project.tb.gui.backing.exp.utils.ExpTypeWeeBean;
import eu.planets_project.tb.gui.backing.exp.utils.ExpTypeWeeUtils;
import eu.planets_project.tb.gui.util.JSFUtil;
import eu.planets_project.tb.impl.AdminManagerImpl;
import eu.planets_project.tb.impl.data.util.DataHandlerImpl;
import eu.planets_project.tb.impl.model.exec.ExecutionRecordImpl;
import eu.planets_project.tb.impl.model.measure.MeasurementImpl;
import eu.planets_project.tb.impl.services.mockups.workflow.MigrateWorkflow;
import eu.planets_project.tb.impl.system.BackendProperties;

/**
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class ExpTypeMigrate extends ExpTypeBackingBean implements ExpTypeWeeBean{
    private Log log = LogFactory.getLog(ExpTypeMigrate.class);
    private BackendProperties bp = new BackendProperties();
    private ExpTypeWeeUtils expTypeWeeUtils;
    //contains the specified parameters for a given serviceEndpoint
    private HashMap<String, Parameters> serviceParams = new HashMap<String,Parameters>();
    
    public ExpTypeMigrate(){
    	this.initBean();
    }
    
    private void initBean(){
    	bp  = new BackendProperties();
    	expTypeWeeUtils = new ExpTypeWeeUtils(this);
    }
    
    private void initWorkflowParams(){
    	//worfklow parameters are modified by this bean and therefore aren't directly loaded from the exp bean
    	//every time. the init fetches parameters from the WorkflowConf object where they are stored
    	WorkflowConf wfConf = this.getWeeWorkflowConf();
    	if((wfConf!=null)&&(wfConf.getServices()!=null)){
    		this.serviceParams = new HashMap<String,Parameters>();
    		for(Service service : wfConf.getServices().getService()){
    			//add the params for this endpoint
    			this.serviceParams.put(service.getEndpoint(), service.getParameters());
    		}
    	}
    }
    
    /**
	 * This method is used to initialize this bean from a given experiment
	 * i.e. initializes all parts that aren't persisted within the TB db model
	 * but are required within that gui
	 * @param wfConf
	 */
	@Override
	public void initExpTypeBeanForExistingExperiment(){
		this.initBean();
		//init the workflow configuration
		if(this.getWeeWorkflowConf()==null){
			//in this case we might not have stored the config yet
			this.setWeeWorkflowConf(buildWorkflowConfFromCurrentConfiguration());
		}
		else{
			//fetches the workflow config either from the experiment or from this backing bean
			this.setWeeWorkflowConf(this.getWeeWorkflowConf());
		}
		//init the parameters from the workflow configuration
		initWorkflowParams();
	}
    
    /**
     * @return the identifyService
     */
    public String getMigrationService() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
//        log.info("Got params: "+expBean.getExperiment().getExperimentExecutable().getParameters() );
//        log.info("Got param: "+expBean.getExperiment().getExperimentExecutable().getParameters().get(MigrateWorkflow.PARAM_SERVICE) );
        return expBean.getExperiment().getExperimentExecutable().getParameters().get(MigrateWorkflow.PARAM_SERVICE);
    }

    /**
     * @param identifyService the identifyService to set
     */
    public void setMigrationService(String identifyService) {
        // FIXME Refresh the service list at this moment?
        log.info("Setting the Migrate service to: "+identifyService);
    	//update the experimentBean
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        expBean.getExperiment().getExperimentExecutable().getParameters().put(MigrateWorkflow.PARAM_SERVICE, identifyService);
        expBean.updateExperiment();
        //update the wfConfiguration with the new values
    	this.buildWorkflowConfFromCurrentConfiguration();
    }
    
    private boolean isServiceSet() {
        return this.isValueSet(this.getMigrationService());
    }

    private boolean isValueSet( String value ) {
        if( value == null ) return false;
        if( "".equals(value) ) return false;
        return true;
    }

    /**
     * @return Return available Services consistent with the current Input and Output.
     */
    public List<String> getMigrationServiceParamList() {
    	List<String> migrSerParams = new ArrayList<String>();
    	Parameters params = this.serviceParams.get(this.getMigrationService());
    	if((params!=null)&&params.getParam()!=null){
    		for(Param p : params.getParam()){
    			String s =  p.getName()+": "+p.getValue();
    			migrSerParams.add(s);
    		}
    	}
    	return migrSerParams;
    }
    

    public List<SelectItem> getMigrationServiceList() {
        log.info("IN: getMigrationServiceList");
        ServiceBrowser sb = (ServiceBrowser)JSFUtil.getManagedObject("ServiceBrowser");
        
        String input = this.getInputFormat();
        if( ! this.isInputSet() ) input = null;
        String output = this.getOutputFormat();
        if( ! this.isOutputSet() ) output = null;
        
        List<ServiceDescription> sdl = sb.getMigrationServices( input, output );
        
        log.info("OUT: getMigrationServiceList");
        return ServiceBrowser.mapServicesToSelectList( sdl );
    }
    

    /**
     * @return
     */
    public List<SelectItem> getPreMigrationServiceList() {
        List<SelectItem> slist = new ArrayList<SelectItem>();
        for( ServiceDescription sd : this.listAllCharacteriseServices() ) {
            if( ! this.isInputSet() || this.inputsMatchFormat(sd, this.getInputFormat() )) {
                slist.add( createCharServiceSelectItem(sd) );
            }
        }
        for( ServiceDescription sd : this.listAllIdentificationServices() ) {
            if( ! this.isInputSet() || this.inputsMatchFormat(sd, this.getInputFormat() )) {
                slist.add( createIdentifyServiceSelectItem(sd) );
            }
        }
        return slist;
    }
    
    private boolean inputsMatchFormat( ServiceDescription sd, String format ) {
        URI formatUri;
        try {
            formatUri = new URI( format );
        } catch (URISyntaxException e) {
            return false;
        }

        // Accepts any input?
        if( sd.getInputFormats() == null 
                || sd.getInputFormats().size() == 0 
                || sd.getInputFormats().contains( UriType.ANY ) ) return true;
        
        // Examine accepted inputs:
        for( URI sinf : sd.getInputFormats() ) {
            // Examine aliases for that format:
            for( Format alias : ServiceBrowser.fr.getFormatAliases(sinf) ) {
                if( alias.getUri().equals( formatUri )) return true;
            }
        }
        return false;
    }
    
    /**
     * @param sd
     * @return
     */
    private static SelectItem createCharServiceSelectItem( ServiceDescription sd ) {
        String serviceName = "Characterise via " + sd.getName();
        serviceName += " (@"+sd.getEndpoint().getHost()+")";
        return new SelectItem( 
                encodeCharParFromOp(
                        MigrateWorkflow.SERVICE_TYPE_CHARACTERISE, 
                        sd.getEndpoint().toString() 
                        ), 
                serviceName );
    }

    /**
     * @param sd
     * @return
     */
    private static SelectItem createIdentifyServiceSelectItem( ServiceDescription sd ) {
        String serviceName = "Identify via " + sd.getName();
        serviceName += " (@"+sd.getEndpoint().getHost()+")";
        return new SelectItem( 
                encodeCharParFromOp(
                        MigrateWorkflow.SERVICE_TYPE_IDENTIFY, 
                        sd.getEndpoint().toString() 
                        ), 
                serviceName );
    }
    
    /**
     * @return
     */
    public List<SelectItem> getPostMigrationServiceList() {
        List<SelectItem> slist = new ArrayList<SelectItem>();
        for( ServiceDescription sd : this.listAllCharacteriseServices() ) {
            if( ! this.isOutputSet() || this.inputsMatchFormat(sd, this.getOutputFormat() )) {
                slist.add( createCharServiceSelectItem(sd) );
            }
        }
        for( ServiceDescription sd : this.listAllIdentificationServices() ) {
            if( ! this.isOutputSet() || this.inputsMatchFormat(sd, this.getOutputFormat() )) {
                slist.add( createIdentifyServiceSelectItem(sd) );
            }
        }
        return slist;
    }
 
    /** Name to store the look-up tables under. */
    private final static String CHAR_SD_CACHE_NAME = "CacheCharacterisationServicesCache";
    
    /**
     * @return A list of all the characterisation services (cached in request-scope).
     */
    @SuppressWarnings("unchecked")
    private List<ServiceDescription> listAllCharacteriseServices() {
        Map<String,Object> reqmap =
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        
        // Lookup or re-build:
        List<ServiceDescription> sds = (List<ServiceDescription>) reqmap.get(CHAR_SD_CACHE_NAME);
        if( sds == null ) {
            log.info("Refreshing list of characterisation services...");
            sds = lookupServicesByType(Characterise.class.getCanonicalName());
            reqmap.put(CHAR_SD_CACHE_NAME, sds);
            log.info("Refreshed.");
        }
        return sds;
    }
    
    /** Name to store the look-up tables under. */
    private final static String IDENTIFY_SD_CACHE_NAME = "CacheIdentifcationServicesCache";
    
    /**
     * @return A list of all the identification services (cached in request-scope).
     */
    @SuppressWarnings("unchecked")
    private List<ServiceDescription> listAllIdentificationServices() {
        Map<String,Object> reqmap =
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        
        // Lookup or re-build:
        List<ServiceDescription> sds = (List<ServiceDescription>) reqmap.get(IDENTIFY_SD_CACHE_NAME);
        if( sds == null ) {
            log.info("Refreshing list of identification services...");
            sds = lookupServicesByType(Identify.class.getCanonicalName());
            reqmap.put(IDENTIFY_SD_CACHE_NAME, sds);
            log.info("Refreshed.");
        }
        return sds;
    }

    /**
     * @param service
     */
    public void setPreMigrationService(String service) {
        log.info("Setting the Pre-Migrate service to: "+service);
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        HashMap<String, String> p = expBean.getExperiment().getExperimentExecutable().getParameters();
        p.put(MigrateWorkflow.PARAM_PRE_SERVICE_TYPE, decodeOpFromCharPar(service) );
        p.put(MigrateWorkflow.PARAM_PRE_SERVICE, decodeEndpointFromCharPar(service) );
        expBean.updateExperiment();
    }

    /**
     * @return
     */
    public String getPreMigrationService() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        HashMap<String, String> p = expBean.getExperiment().getExperimentExecutable().getParameters();
        String config = encodeCharParFromOp(
                p.get(MigrateWorkflow.PARAM_PRE_SERVICE_TYPE), p.get(MigrateWorkflow.PARAM_PRE_SERVICE) );
        log.info("Getting the Pre-Migrate service: "+config);
        return config;

    }
    
    /**
     * @param service
     */
    public void setPostMigrationService(String service) {
        log.info("Setting the Post-Migrate service to: "+service);
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        HashMap<String, String> p = expBean.getExperiment().getExperimentExecutable().getParameters();
        p.put(MigrateWorkflow.PARAM_POST_SERVICE_TYPE, decodeOpFromCharPar(service) );
        p.put(MigrateWorkflow.PARAM_POST_SERVICE, decodeEndpointFromCharPar(service) );
        expBean.updateExperiment();
    }

    /**
     * @return
     */
    public String getPostMigrationService() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        HashMap<String, String> p = expBean.getExperiment().getExperimentExecutable().getParameters();
        String config = encodeCharParFromOp(
                p.get( MigrateWorkflow.PARAM_POST_SERVICE_TYPE), p.get(MigrateWorkflow.PARAM_POST_SERVICE) );
        log.info("Getting the Post-Migrate service: "+config);
        return config;
    }

    private static String encodeCharParFromOp( String operation, String endpoint ) {
        return operation+":"+endpoint;
    }
    
    private static String decodeOpFromCharPar( String charparameter ) {
        if( charparameter == null || charparameter.indexOf(":") == -1 ) return "";
        return charparameter.substring(0, charparameter.indexOf(":"));
    }
    
    private static String decodeEndpointFromCharPar( String charparameter ) {
        if( charparameter == null || charparameter.indexOf(":") == -1 ) return "";
        return charparameter.substring(charparameter.indexOf(":")+1);
    }
    

    /**
     * @return
     */
    public String getInputFormat() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        return expBean.getExperiment().getExperimentExecutable().getParameters().get( MigrateWorkflow.PARAM_FROM );
    }
    
    /**
     * @param inputFormat
     */
    public void setInputFormat( String inputFormat) {
    	//update the experimentBean
    	ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        expBean.getExperiment().getExperimentExecutable().getParameters().put(MigrateWorkflow.PARAM_FROM, inputFormat );
        expBean.updateExperiment();
        //update the wfConfiguration with the new values
    	this.buildWorkflowConfFromCurrentConfiguration();
    }
    
    private boolean isInputSet() {
        return isValueSet( this.getInputFormat() );
    }

    /**
     * @return Return available Input Formats consistent with the current Service and Output.
     */
    public List<SelectItem> getInputFormatList() {
        log.info("IN: getInputFormatList");
        ServiceBrowser sb = (ServiceBrowser)JSFUtil.getManagedObject("ServiceBrowser");
        
        String endpoint = this.getMigrationService();
        if( ! this.isServiceSet() ) endpoint = null;
        String output = this.getOutputFormat();
        if( ! this.isOutputSet() ) output = null;
        
        Set<Format> inputFormats = sb.getMigrationInputFormats(endpoint, output);

        log.info("OUT: getInputFormatList");
        return ServiceBrowser.mapFormatsToSelectList(inputFormats);
    }
    
    /**
     * @return
     */
    public String getOutputFormat() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        return expBean.getExperiment().getExperimentExecutable().getParameters().get( MigrateWorkflow.PARAM_TO );
    }
    
    /**
     * @param format
     */
    public void setOutputFormat( String format) {
    	//update the experimentBean
    	ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        expBean.getExperiment().getExperimentExecutable().getParameters().put(MigrateWorkflow.PARAM_TO, format );
        expBean.updateExperiment();
        //update the wfConfiguration with the new values
    	this.buildWorkflowConfFromCurrentConfiguration();
    }

    private boolean isOutputSet() {
        return isValueSet( this.getOutputFormat() );
    }
    /**
     * @return Return available Output Formats consistent with the current Service and Input.
     */
    public List<SelectItem> getOutputFormatList() {
        log.info("IN: getOutputFormatList");
        ServiceBrowser sb = (ServiceBrowser)JSFUtil.getManagedObject("ServiceBrowser");
        
        String endpoint = this.getMigrationService();
        if( ! this.isServiceSet() ) endpoint = null;
        String input = this.getInputFormat();
        if( ! this.isInputSet() ) input = null;
        
        Set<Format> outputFormats = sb.getMigrationOutputFormats(endpoint, input);

        log.info("OUT: getOutputFormatList");
        return ServiceBrowser.mapFormatsToSelectList(outputFormats);
    }
    
    
    
    /* (non-Javadoc)
     * @see eu.planets_project.tb.gui.backing.exp.ExpTypeBackingBean#getStageBeans()
     */
    @Override
    public List<ExperimentStageBean> getStageBeans() {
        return getWorkflow(AdminManagerImpl.MIGRATE).getStages();
    }

    /* (non-Javadoc)
     * @see eu.planets_project.tb.gui.backing.exp.ExpTypeBackingBean#getObservables()
     */
    @Override
    public HashMap<String,List<MeasurementImpl>> getObservables() {
        return getWorkflow(AdminManagerImpl.MIGRATE).getObservables();
    }
    
    HashMap<String,List<MeasurementImpl>> manualObsCache;
    long cacheExperimentID;
    /* (non-Javadoc)
     * @see eu.planets_project.tb.gui.backing.exp.ExpTypeBackingBean#getManualObservables()
     */
    @Override
    public HashMap<String,List<MeasurementImpl>> getManualObservables() {
    	ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
    	if(manualObsCache==null||(cacheExperimentID != expBean.getExperiment().getEntityID())){
    		cacheExperimentID = expBean.getExperiment().getEntityID();

        	//query for properties that have been added from the Ontology
        	HashMap<String,Vector<String>> ontoPropIDs = new HashMap<String, Vector<String>>();
        	for(ExperimentStageBean stage : expBean.getStages()){
        		ontoPropIDs.put(stage.getName(),expBean.getExperiment().getExperimentExecutable().getManualProperties(stage.getName()));
        	}
        	//this is the static list of manual properties - normally empty
        	HashMap<String,List<MeasurementImpl>> staticWFobs = 
        		getWorkflow(AdminManagerImpl.MIGRATE).getManualObservables();
        	
        	//FIXME AL: staticWFobs returns wrong items - where are they added - exclude staticWFobs for now
        	//manualObsCache = mergeManualObservables(staticWFobs, ontoPropIDs);
        	manualObsCache = mergeManualObservables(null, ontoPropIDs);

    	}
    	return manualObsCache;
    }

    /**
     * 
     * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
     */
    public class MigrateResultForDO extends ResultsForDigitalObjectBean {

        /**
         * @param input
         */
        public MigrateResultForDO(String input) {
            super(input);
        }
        
        /**
         * @return
         */
        public MigrationResultBean getMigrationResult() {
            for( ExecutionRecordImpl exerec : this.getExecutionRecords() ) {
                return new MigrationResultBean( exerec );
            }
            return null;
        }
        
        public DigitalObjectTreeNode getInputDob() {
            return DigitalObjectCompare.lookupDob(this.getDigitalObject());
        }
        
        public DigitalObjectTreeNode getOutputDob() {
            if( this.getMigrationResult() == null ) return null;
            return DigitalObjectCompare.lookupDob(this.getMigrationResult().getDobResultReference());
        }
        
        
    }
    
    
    /**
     * @return
     */
    public List<MigrateResultForDO> getMigrateResults() {
        ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        List<MigrateResultForDO> results = new Vector<MigrateResultForDO>();
        // Populate using the results:
        for( String file : expBean.getExperimentInputData().values() ) {
            MigrateResultForDO res = new MigrateResultForDO(file);
            results.add(res);
        }

        // Now return the results:
        return results;
        
    }

    /**
     * 
     * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
     *
     */
    public class MigrationResultBean {

        private ExecutionRecordImpl exerec;
        private Properties props;

        /**
         * @param exerec
         */
        public MigrationResultBean(ExecutionRecordImpl exerec) {
            this.exerec = exerec;
            try {
				props =  exerec.getPropertiesListResult();
			} catch (IOException e) {
				log.debug("No Properties recorded.");
				props = new Properties();
			}
        }
            
        
        /**
         * get the DigoResult object if there's one
         * @return
         */
        public String getDigitalObjectResult() {
            String summary = "";
            String tbDigoURI = this.getDobResultReference();
            if( tbDigoURI != null ) {
                DataHandler dh = new DataHandlerImpl();
                DigitalObjectRefBean dobr;
                try {
                    dobr = dh.get(tbDigoURI);
                } catch (FileNotFoundException e) {
                    log.error("Could not find file. "+e);
                    return "";
                }
                if( dobr == null ) return "";
                summary += dobr.getName();
                long size = dobr.getSize();
                if( size >= 0 ) {
                    summary += " ("+size+" bytes)";
                }
                return summary;
            }
            summary += "No Result.";
            return summary;
        }
        
        /**
         * @return
         */
        public String getDobResultReference() {
            Object tbDigoURI = props.get(ExecutionRecordImpl.RESULT_PROPERTY_URI);
            return (String)tbDigoURI;
        }

        
        public String getDigitalObjectDownloadURL() {
        	Object tbDigoURI = props.get(ExecutionRecordImpl.RESULT_PROPERTY_URI);
        	return getDigitalObjectDownloadURL(tbDigoURI);
        }
        
        /**
         * Creates an external http:// object ref for any TB datamanager ref.
         * e.g. https://localhost:8443/testbed/reader/download.jsp?fid=file%253A%252FD%253A%252FImplementation%252Fifr_server%252Fplanets-ftp%252Fusa_bundesstaaten_png.png
         * @return
         */
        private String getDigitalObjectDownloadURL(Object tbDigoURI) {
            if( tbDigoURI != null ) {
                DataHandler dh = new DataHandlerImpl();
                try {
                    DigitalObjectRefBean dobr = dh.get((String)tbDigoURI);
                    return dobr.getDownloadUri().toString();
                } catch ( Exception e ) {
                    log.error("Failed to generate download URL. " + e);
                    return "";
                }
            }
            return "";
        }

    }
    
    //Embed system properties in ServiceDescription property chunk?;
    //Drag-and-drop in RF moves the pane? NO.
    // FIXME Remove this.
    public static void main(String args[]) {
               
        Properties p = System.getProperties();
        
        ByteArrayOutputStream byos = new ByteArrayOutputStream();
        try {
            p.storeToXML(byos, "Automatically generated for PLANETS Service ", "UTF-8");
            String res = byos.toString("UTF-8");
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // This.
        List<String> pl = new ArrayList<String>();
        for( Object key : p.keySet() ) {
            pl.add( (String)key );
        }
        Collections.sort(pl);
        
        //
        for( String key : pl ) {
            System.out.println(key + " = "+p.getProperty(key));
        }
        
        /*
         * http://java.sun.com/j2se/1.5.0/docs/api/java/lang/management/ThreadMXBean.html#getCurrentThreadCpuTime()
         * 
         * http://www.java-forums.org/new-java/5303-how-determine-cpu-usage-using-java.html
         * 
         */
        
        ThreadMXBean TMB = ManagementFactory.getThreadMXBean();
        int mscale = 1000000;
        long time = 0, time2 = 0;
        long cput = 0, cput2 = 0;
        double cpuperc = -1;

        //Begin loop.
        for( int i=0; i< 10; i++ ) {

            if( TMB.isThreadCpuTimeSupported() )
            {
                if(!TMB.isThreadCpuTimeEnabled())
                {
                    TMB.setThreadCpuTimeEnabled(true);
                }
                
//                if(new Date().getTime() * mscale - time > 1000000000) //Reset once per second
//                {
                System.out.println("Resetting...");
                time = System.currentTimeMillis() * mscale;
                cput = TMB.getCurrentThreadCpuTime();
//                cput = TMB.getCurrentThreadUserTime();
//                }

            }

            //Do cpu intensive stuff
            for( int k = 0; k < 10; k++ ) {
                for( int j = 0; j < 100000; j++ ) {
                    double a = Math.pow(i, j);
                    double b = a/j + Math.random();
                    a = b * Math.random();
                    b = a * Math.random();
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            if( TMB.isThreadCpuTimeSupported() )
            {
//                if(new Date().getTime() * mscale - time != 0) {
                cput2 = TMB.getCurrentThreadCpuTime();
                System.out.println("cpu: " + (cput2 - cput)/(1000.0*mscale) );
//                cput2 = TMB.getCurrentThreadUserTime();
                
                time2 = System.currentTimeMillis() * mscale;
                System.out.println("time: " + (time2 - time)/(1000.0*mscale) );
                
                cpuperc = 100.0 * (cput2 - cput) / (double)(time2 - time);
                System.out.println("cpu perc = " + cpuperc);
//                }
            }
            //End Loop
        }
        System.out.println("Done.");
    }

    /* 
	 * TODO AL: version 1.0 uses this structure to check for a valid workflow (exp-type specific) configuration.
	 * (non-Javadoc)
	 * @see eu.planets_project.tb.gui.backing.exp.ExpTypeBackingBean#checkExpTypeBean_Step2_WorkflowConfigurationOK()
	 */
	@Override
	public void checkExpTypeBean_Step2_WorkflowConfigurationOK() throws Exception{
		
		//steps 3 is not required by the workflow logic for this expType. Step3 is done implicitly
		//1. check valid configuration file provided
		if(!this.isValidCurrentConfiguration()){
			throw new Exception("The provided workflow configuration is not valid");
		}
		//2 check workflow available on system
		if(!isTemplateAvailableInWftRegistry()){
			throw new Exception("The selected workflow is not available on the execution engine - please contact Testbed helpdesk");
		}
		//3 check all selected services available
		/*if(!helperCheckAllSelectedServicesAvailable()){
			throw new Exception("One or more selected services are not available within the Testbed - please contact Testbed helpdesk");
		}*/
	}
	
	/**
	 * Takes the bean's information and persist it into the testbed's db model
	 * i.e. the experiment's executable.setWorkflowConfig method
	 */
	@Override
	public void saveExpTypeBean_Step2_WorkflowConfiguration_ToDBModel(){
		ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
        //store information in the db entities
		ExperimentExecutable expExecutable = expBean.getExperiment().getExperimentExecutable();
        expExecutable.setWEEWorkflowConfig(this.buildWorkflowConfFromCurrentConfiguration());
        //specify which batch processing system WEE or TB/Local we want to use for this experiment
        expExecutable.setBatchSystemIdentifier(BatchProcessor.BATCH_QUEUE_TESTBED_WEE_LOCAL);
        expBean.updateExperiment();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.gui.backing.exp.ExpTypeBackingBean#isExperimentBeanType()
	 */
	@Override
	public boolean isExperimentBeanType() {
		ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
		if( expBean != null && AdminManagerImpl.MIGRATE.equals(expBean.getEtype()) ) return true;
		return false;
	}
	
	// --------------------------- START CHANGES SWITCHING TO WEE BACKEND ----------------------

	/**
	 * Add a parameter in the editParam screen
	 * @param event
	 */
	public void addParameter(ActionEvent event) {
		//TODO create logic for adding and editing parameters in GUI
		//then call the addParam method from this bean 
	}

	/**
	 * adds the parameter to the bean's list of params for a given service id.
	 * @param serviceId
	 * @param name
	 * @param value
	 */
	private void addParam(String serviceId, String name, String value){
		if(this.serviceParams.get(serviceId)==null){
			//Params object not available
			this.serviceParams.put(serviceId, new Parameters());
		}
		
		//check if we're updating a param?
		Param spUpdate = this.getServiceParamContained(serviceId, name);
		if(spUpdate!=null){
			//delete
			this.removeParameter(serviceId, spUpdate);
		}
		//finally add
		Param parameter = new Param();
		parameter.setName(name);
		parameter.setValue(value);
		this.serviceParams.get(serviceId).getParam().add(parameter);
	}
	
	public void removeParameter(String serviceId, Param par) {
		this.serviceParams.get(serviceId).getParam().remove(par);
	}
	
	/**
	 * Checks if a given ServiceParameterName is already contained in the list
	 * off added ServiceParameters
	 * @param paramName
	 * @return
	 */
	private Param getServiceParamContained(String serId, String paramName){
		if((this.serviceParams!=null)&&(this.serviceParams.get(serId)!=null)){
			for(Param p : this.serviceParams.get(serId).getParam()){
				if(p.getName().equals(paramName)){
					return p;
				}
			}
		}
		return null;
	}
	
	/**
	 * Removes all service parameters for a given serviceID
	 * @param serId
	 */
	private void removeAllServiceParams(String serId){
		for(Param p : this.serviceParams.get(serId).getParam()){
			this.serviceParams.get(serId).getParam().remove(p);
		}
	}
	
	
	public WorkflowConf getWeeWorkflowConf() {
		ExperimentBean expBean = (ExperimentBean)JSFUtil.getManagedObject("ExperimentBean");
		
		if(!expBean.getApproved()){
			//that's the one used when building this in 'design experiment'
			return this.expTypeWeeUtils.getWeeWorkflowConf();
        } 
		if(expBean.getApproved()){
			//that's the one after 'design experiment' has been saved
			ExperimentExecutable expExecutable = expBean.getExperiment().getExperimentExecutable();
			return expExecutable.getWEEWorkflowConfig();
        }
		return null;
	}

	public void setWeeWorkflowConf(WorkflowConf wfConfig) {
		this.expTypeWeeUtils.setWeeWorkflowConf(wfConfig);
	}
	
	/**
	 *  Takes the current bean's configuration (e.g. service, input, output format) and
	 *  creates a WorkflowConf object.
	 */
	public WorkflowConf buildWorkflowConfFromCurrentConfiguration(){
		
		//create the template name - it's predefined
		Template serTempl = new Template();
		serTempl.setClazz(bp.getProperty(BackendProperties.TB_EXPTYPE_MIGRATION_WEE_WFTEMPLATENAME));
    	
    	//create the Service
    	Services services = new Services();
    	Service sMigrate = new Service();
    	sMigrate.setId("migrate1");
    	sMigrate.setEndpoint(this.getMigrationService());
    	
    	//add the general parameters for current input and output format
		Param pMigFrom = this.getServiceParamContained(sMigrate.getEndpoint(), WorkflowTemplate.SER_PARAM_MIGRATE_FROM);
		if(pMigFrom!=null){
			//delete old value - it's not updated until know
			this.removeParameter(sMigrate.getEndpoint(), pMigFrom);
		}
		this.addParam(sMigrate.getEndpoint(), WorkflowTemplate.SER_PARAM_MIGRATE_FROM, this.getInputFormat());
		
		Param pMigTo = this.getServiceParamContained(sMigrate.getEndpoint(), WorkflowTemplate.SER_PARAM_MIGRATE_TO);
		if(pMigTo!=null){
			//delete old value - it's not updated until know
			this.removeParameter(sMigrate.getEndpoint(), pMigTo);
		}
		this.addParam(sMigrate.getEndpoint(), WorkflowTemplate.SER_PARAM_MIGRATE_TO, this.getOutputFormat());
    	
		//now add all parameters - also including the one's we've added above
    	Parameters params = this.serviceParams.get(this.getMigrationService());
    	if((params!=null)&&(params.getParam().size()>0)){
			//there needs to be a Parameter element only if there's a param for being xsd compliant
    		sMigrate.setParameters(params);
		}else{
			sMigrate.setParameters(new Parameters());
		}
    	services.getService().add(sMigrate);
    	
    	WorkflowConf wfConf = null;
	    try {
			if((this.getMigrationService()!=null)&&(this.getInputFormat()!=null)&&(this.getOutputFormat()!=null)){
				//build the WorkflowConf object from the bean's content
				wfConf = this.expTypeWeeUtils.buildWorkflowConf(serTempl, services);
			}
		} catch (Exception e) {
			log.debug("Unable to retrieve the WorkflowConfiguration"+e);
		}
		this.setWeeWorkflowConf(wfConf);
		return this.getWeeWorkflowConf();
	}

	/** {@inheritDoc} */
	public String getTempFileDownloadLinkForCurrentXMLConfig() {
		return this.expTypeWeeUtils.getTempFileDownloadLinkForCurrentXMLConfig();
	}

	/** {@inheritDoc} */
	public boolean isValidCurrentConfiguration() {
		return this.expTypeWeeUtils.isValidCurrentConfiguration();
	}

	/** {@inheritDoc} */
	public boolean isTemplateAvailableInWftRegistry() {
		return this.expTypeWeeUtils.isTemplateAvailableInWftRegistry(bp.getProperty(BackendProperties.TB_EXPTYPE_MIGRATION_WEE_WFTEMPLATENAME));
	}

	@Override
	public Map<String, List<Parameter>> getWorkflowParameters() {
		Map<String,List<Parameter>> ret = new HashMap<String,List<Parameter>>();
		
		for(String serID: this.serviceParams.keySet()){
			List<Parameter> retParamList = new ArrayList<Parameter>();
			for(Param param : this.serviceParams.get(serID).getParam()){
				Parameter p = new Parameter.Builder(
					    param.getName(), param.getValue()).build();
				retParamList.add(p);
			}
			ret.put(serID, retParamList);
		}
		return ret;
	}

	@Override
	public void setWorkflowParameters(Map<String,List<Parameter>> params) {
		if((params!=null)&&(!params.isEmpty())){
			//iterate over all services
			for(String serviceID : params.keySet()){
				//remove all old parameters for this service
				this.removeAllServiceParams(serviceID);
				
				//now get and store the new values
				if(params.get(serviceID)!=null){
					for(Parameter p : params.get(serviceID)){
						this.addParam(serviceID, p.getName(), p.getValue());
					}
				}
			}
		}
	}
}
