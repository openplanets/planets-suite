package eu.planets_project.ifr.core.wee.api.workflow;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.planets_project.ifr.core.common.conf.PlanetsServerConfig;
import eu.planets_project.ifr.core.storage.api.DataRegistry;
import eu.planets_project.ifr.core.storage.api.DataRegistryFactory;
import eu.planets_project.ifr.core.storage.api.DataRegistry.DigitalObjectManagerNotFoundException;
import eu.planets_project.ifr.core.storage.api.DigitalObjectManager.DigitalObjectNotFoundException;
import eu.planets_project.ifr.core.storage.api.DigitalObjectManager.DigitalObjectNotStoredException;
import eu.planets_project.ifr.core.wee.api.ReportingLog;
import eu.planets_project.ifr.core.wee.api.ReportingLog.Message;
import eu.planets_project.services.PlanetsService;
import eu.planets_project.services.datatypes.Agent;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.utils.DigitalObjectUtils;

/**
 * @author <a href="mailto:andrew.lindley@arcs.ac.at">Andrew Lindley</a>
 * @since 18.12.2008 Contains information and operations which are the same for
 *        all objects implementing the workflowTemplate interface
 */
public abstract class WorkflowTemplateHelper implements Serializable {

	private static final long serialVersionUID = -9043319482888030134L;
	private Map<PlanetsService, ServiceCallConfigs> serviceInvocationConfigs = new HashMap<PlanetsService, ServiceCallConfigs>();
    private List<DigitalObject> data = new ArrayList<DigitalObject>();
    private UUID wfInstanceUUID = null;
    private WorkflowContext wfContext = null;
    private DataRegistry dataRegistry = DataRegistryFactory.getDataRegistry();
    private Agent agentWEE = null;
    private ReportingLog wfLogger = null;
    
    /*
     * All services with a Planets interface can be used within a given
     * worklowTemplate
     */
    private static final String[] supportedPlanetsServiceTypes = 
       {"eu.planets_project.services.identify.Identify",
        "eu.planets_project.services.characterise.Characterise",
        "eu.planets_project.services.characterise.DetermineProperties",
        "eu.planets_project.services.compare.BasicCompareFormatProperties",
        "eu.planets_project.services.migrate.Migrate",
        "eu.planets_project.services.modify.Modify",
        "eu.planets_project.services.migrate.MigrateAsync",
        "eu.planets_project.services.compare.Compare",
        "eu.planets_project.services.compare.CompareProperties",
        "eu.planets_project.services.validate.Validate",
        "eu.planets_project.services.view.CreateView"};
    
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getDeclaredWFServices()
     */
    @SuppressWarnings("unchecked")
    public List<Field> getDeclaredWFServices() {
        Class clazz = this.getClass();
        List<Field> ret = new ArrayList<Field>();

        // e.g. look for public and private Fields
        for (Field f : clazz.getDeclaredFields()) {
            // check if the declared Service in the ServiceTemplate is supported
            // e.g. eu.planets_project.services.identify.Identify
            if (this.isServiceTypeSupported(f)) {
                ret.add(f);
            }
        }

        return ret;
    }

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getDeclaredWFServiceNames()
     */
    public List<String> getDeclaredWFServiceNames() {
        List<String> ret = new ArrayList<String>();
        for (Field f : this.getDeclaredWFServices()) {
            ret.add(f.getName());
        }
        return ret;
    }

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getSupportedServiceTypes
     */
    public List<String> getSupportedServiceTypes() {
        return Arrays.asList(supportedPlanetsServiceTypes);
    }

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#isServiceTypeSupported(java.lang.reflect.Field)
     */
    public boolean isServiceTypeSupported(Field f) {
        if (getSupportedServiceTypes().contains(f.getType().getCanonicalName())) {
            return true;
        }
        return false;
    }

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#setServiceCallConfigs(eu.planets_project.services.PlanetsService, eu.planets_project.ifr.core.wee.api.workflow.ServiceCallConfigs)
     */
    public void setServiceCallConfigs(PlanetsService forService, ServiceCallConfigs serCallConfigs) {
        this.serviceInvocationConfigs.put(forService, serCallConfigs);
    }

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getServiceCallConfigs(eu.planets_project.services.PlanetsService)
     */
    public ServiceCallConfigs getServiceCallConfigs(PlanetsService forService) {
        return this.serviceInvocationConfigs.get(forService);
    }

	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#setWorkflowContext(WorkflowContext)
     */
	public void setWorkflowContext(WorkflowContext wfContext){
		this.wfContext = wfContext;
	}
	
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getWorkflowContext()
     */
	public WorkflowContext getWorkflowContext(){
		return this.wfContext;
	}
	
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getData()
     */
    public List<DigitalObject> getData() {
        return this.data;
    }

	/* (non-Javadoc)
     *  @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#setData(java.util.List)
     */
    public void setData(List<DigitalObject> data) {
        this.data = data;
    }

    /**
     * @param objects The digital objects
     * @param folder The folder to store the files in
     * @return References to the given digital object, stored in the given
     *         folder
     */
    @SuppressWarnings("deprecation")
	public static List<URL> reference(List<DigitalObject> objects, File folder) {
        List<URL> urls = new ArrayList<URL>();
        List<File> files = DigitalObjectUtils.getDigitalObjectsAsFiles(objects, folder);
        for (File f : files) {
            try {
                urls.add(f.toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
    
    /**
     * @param results URLs to files (output of {@link #reference(List, File)})
     * @return The message linking to the given files, to be passed to a
     *         reporting log
     */
    public static Message link(List<URL> results) {
        List<Parameter> links = new ArrayList<Parameter>();
        for (int i = 0; i < results.size(); i++) {
            URL url = results.get(i);
            String[] strings = url.getFile().split("/");
            //hack to change from file uri to relative path
            links.add(new Parameter("File " + (i + 1), String.format("<a href='%s'>%s</a>", /*url*/strings[strings.length - 1],
                    strings[strings.length - 1])));
        }
        Parameter[] array = links.toArray(new Parameter[] {});
        return new Message("Results", array);
    }

    /**
     * @param template The template
     * @return A overview message for the template
     */
    public static Message overview(WorkflowTemplate template) {
        return new Message("Overview", new Parameter("Description", template.describe()), new Parameter("Started",
                new Date(System.currentTimeMillis()).toString()));
    }
    
    /**
     * Returns host and port of the system the wee is running on as configured through AppServer installation
     * @return the host authority as a java.lang.String
     * @throws URISyntaxException
     */
    public static String getHostAuthority() throws URISyntaxException{
    	String authority = PlanetsServerConfig.getHostname() + ":" + PlanetsServerConfig.getPort();
        return authority;
    }

    private WorkflowResult wfResult = null;
    @SuppressWarnings("unused")
	private void setWFResult(WorkflowResult wfResult){
    	wfResult = new WorkflowResult();
    }
    
	/* (non-Javadoc)
     * @see WorkflowTemplate#getWFResult()
     */
    public WorkflowResult getWFResult(){
    	if(this.wfResult==null)
    		this.wfResult = new WorkflowResult(this.getWorkflowReportingLogger());
    	return this.wfResult;
    }
    
	/* (non-Javadoc)
     * @see WorkflowTemplate#addWFResultItem(WorkflowResultItem)
     */
    public void addWFResultItem(WorkflowResultItem wfResultItem){
    	this.getWFResult().addWorkflowResultItem(wfResultItem);
    }
    
    /**
     * @return The ReportingLog
     */
    public ReportingLog getWorkflowReportingLogger(){
    	if(this.wfLogger==null)
    		this.wfLogger = new ReportingLog(Logger.getLogger(WorkflowResult.class),this.getWorklowInstanceID()+"");
    	return this.wfLogger;
    }
    
    /**
     * A reference to the WOrkflowInstance's UUID
     * @param id
     */
    public void setWorkflowInstanceID(UUID id){
    	this.wfInstanceUUID = id;
    }
    
    /**
     * @return the UUID identifying the WorkflowInstance
     */
    public UUID getWorklowInstanceID(){
    	return this.wfInstanceUUID;
    }
    
    /*
     * @see WorkflowTemplate#storeDigitalObjectInJCR(DigitalObject)
     * @return
     */
    /*public DigitalObject storeDigitalObjectInJCR(DigitalObject digoToStore) {
    	//decode upon the location where to store the data
    	URI PERMANENT_URI_PATH;
		if(getWorklowInstanceID()==null){
			PERMANENT_URI_PATH = URI.create("/weedata/");
		}
		else{
			//group all information for a workflow execution by it's UUID
			PERMANENT_URI_PATH = URI.create("/weedata/"+getWorklowInstanceID());
		}
		
		DigitalObject resDo;
		try {
			// store and retrieve content
			resDo = dom.store(PERMANENT_URI_PATH, digoToStore, true);
			this.getWorkflowReportingLogger().debug("Stored Digital Object "+digoToStore.getTitle()+" for node: "+PERMANENT_URI_PATH+" in JCR repository. Received permanent URI: "+resDo.getPermanentUri()+" content reference: "+resDo.getContent().toString());
		} catch (DigitalObjectNotStoredException e) {
			this.getWorkflowReportingLogger().debug("Error storing Digital Object "+digoToStore.getPermanentUri()+" for node: "+PERMANENT_URI_PATH+" in JCR repository",e);
			//return the original digo
			resDo = digoToStore;
		}
		return resDo;
    }*/
    
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#storeDigitalObject
     */
    public URI storeDigitalObject(DigitalObject digoToStore) 
    	throws DigitalObjectManagerNotFoundException, DigitalObjectNotStoredException{
    	
    	return this.getDataRegistry().getDefaultDigitalObjectManager().storeAsNew(digoToStore);
    }
       
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#storeDigitalObject
     */
    public URI storeDigitalObjectInRepository(DigitalObject digoToStore, URI repositoryID) 
    	throws DigitalObjectManagerNotFoundException, DigitalObjectNotStoredException{
		
    	return this.getDataRegistry().getDigitalObjectManager(repositoryID).storeAsNew(digoToStore);
    }
    
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#storeDigitalObject
     */
    public URI storeDigitalObjectInRepository(URI objectLocation, DigitalObject digoToStore, URI repositoryID) 
    	throws DigitalObjectManagerNotFoundException, DigitalObjectNotStoredException{
		
    	return this.getDataRegistry().getDigitalObjectManager(repositoryID).storeAsNew(objectLocation,digoToStore);
    }
    
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#getDataRegistry
     */
    public DataRegistry getDataRegistry(){
    	return this.dataRegistry;
    }
    
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#retrieveDigitalObjectDataRegistryRef
     */
    public DigitalObject retrieveDigitalObjectDataRegistryRef(URI digitalObjectRef) throws DigitalObjectNotFoundException{
    	return this.getDataRegistry().retrieve(digitalObjectRef);
    }
    
    /**
     * Returns the Agent of the batch processor the template is currently executed by.
     * @return the Agent
     */
    public Agent getWEEAgent(){
    	if(this.agentWEE==null)
    		this.agentWEE = new Agent("Planets-WEE-v1.0", "The Planets Workflow Execution Engine", "planets://workflow/processor");
    	return this.agentWEE;
    }
    
	/* (non-Javadoc)
     * @see eu.planets_project.ifr.core.wee.api.workflow.WorkflowTemplate#setWEEAgent
     */
    public void setWEEAgent(Agent agent){
    	//FIXME: AL: this should actually be provided by the WEE at runtime
    	this.agentWEE = agent;
    }
}
