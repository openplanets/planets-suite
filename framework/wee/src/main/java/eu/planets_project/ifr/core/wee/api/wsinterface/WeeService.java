/**
 * 
 */
package eu.planets_project.ifr.core.wee.api.wsinterface;

import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;

import eu.planets_project.ifr.core.wee.api.workflow.WorkflowResult;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.datatypes.DigitalObject;

/**
 * WebService Interface for the Planets Workflow Execution Engine. This allows 
 *  - job submission
 *  - querying on a job's status and position in queue
 *  - retrieving execution results
 * @author <a href="mailto:andrew.lindley@arcs.ac.at">Andrew Lindley</a>
 * @since 12.11.2008
 *
 */
@WebService(name = WeeService.NAME, targetNamespace = PlanetsServices.NS)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")

public interface WeeService {
	
	public static final String NAME = "WeeService";
	public static final QName QNAME = new QName(PlanetsServices.NS, WeeService.NAME );

    /**
     * 
     * @param digitalObjects A listof Planets Digital Objects - this contains the payload the wf is invoked upon
     * @param QWorkflowTemplateName the fully qualified name of the java workflowTemplate to use for this submission. i.e. specifying the structure. Please note: the template must be registered.
     * @param xmlWorkflowConfig holding the workflowTemplate's configuration i.e. specifying how to populate a static template
     * @return a ticket (UUID) for the submitted job which can be used for polling on the status and its results
     * @throws Exception 
     */
	@WebMethod(
            operationName = WeeService.NAME+ "_submitWorkflow", 
            action = PlanetsServices.NS + "/" + WeeService.NAME +"/submitWorkflow")
    @WebResult(
            name = WeeService.NAME + "UUID", 
            targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
            partName = WeeService.NAME + "UUID")
    public UUID submitWorkflow ( 
    		@WebParam(
                    name = "digitalObjects", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "digitalObjects")
            ArrayList<DigitalObject> digObjs,
            @WebParam(
                    name = "QWorkflowTemplateName", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "xmlWorkflowConfig")
            String qWorkflowTemplateName,
            @WebParam(
                    name = "xmlWorkflowConfig", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "xmlWorkflowConfig")   
            String xmlWorkflowConfig
    ) throws Exception; 
    
    
    /**
     * A successfully executed workflow is marked by the status message 'completed'
     * @see eu.planets_project.ifr.core.wee.api.WorkflowExecutionStatus.java for all possible status messages
     * @param ticket the UUID obtained when submitting a job
     * @return
     * @throws Exception for an unknown ticket request
     */
    @WebMethod(
            operationName = WeeService.NAME +"_getStatus", 
            action = PlanetsServices.NS + "/" + WeeService.NAME+"/getStatus")
    @WebResult(
            name = WeeService.NAME + "status", 
            targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
            partName = WeeService.NAME + "status")
    public String getStatus(
    		@WebParam(
                    name = "ticket", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "ticket")
            UUID ticket
    ) throws Exception;
    
    
    /**
     * Returns the position in the workflow execution enginge's queue.
     * @param ticket the UUID obtained when submitting a job
     * @return -1 when already completed, 0 when currently running
     * @throws Exception for an unknown ticket request
     */
    @WebMethod(
            operationName = WeeService.NAME +"_getPositionInQueue", 
            action = PlanetsServices.NS + "/" + WeeService.NAME +"/getPositionInQueue")
    @WebResult(
            name = WeeService.NAME + "position", 
            targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
            partName = WeeService.NAME + "position")
    public int getPositionInQueue(
    		@WebParam(
                    name = "ticket", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "ticket")
            UUID ticket
    ) throws Exception;
    
    /**
     * Percent of processed (completed) objects
     * @param ticket the UUID obtained when submitting a job
     * @return -1 when scheduled, failed or completed, 0-100 when currently running
     * @throws Exception for an unknown ticket request
     */
    @WebMethod(
            operationName = WeeService.NAME +"_getProgress", 
            action = PlanetsServices.NS + "/" + WeeService.NAME +"/getProgress")
    @WebResult(
            name = WeeService.NAME + "percent", 
            targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
            partName = WeeService.NAME + "percent")
    public int getProgress(
    		@WebParam(
                    name = "ticket", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "ticket")
            UUID ticket
    ) throws Exception;

    
    /**
     * Returns a WorkflowResult containing the workflow execution's results. 
     * (e.g. pointers to the Planets data registry on digital objects, events, etc.)
     * Incremental workflow results as they come in when execution is running
     * @param ticket the UUID obtained when submitting a job
     * @return
     * @throws Exception if the workflow failed to terminate successfully or if the ticket is unknown
     */
    @WebMethod(
            operationName = WeeService.NAME +"_getResult", 
            action = PlanetsServices.NS + "/" + WeeService.NAME +"/getResult")
    @WebResult(
            name = WeeService.NAME + "result", 
            targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
            partName = WeeService.NAME + "result")
    public WorkflowResult getResult(
    		@WebParam(
                    name = "ticket", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "ticket")
            UUID ticket
    ) throws Exception;
    
    /**
     * since 12.03.2010
     * An updated interface taking data registry pointers to digital objects instead of submitting the entire payload. 
     * This approach requires a shared repository between the caller and the WEE in place 
     * @param digitalObjects A list of Planets Digital Object references which contain the payload the wf is invoked upon - object references must be accessible to the WEE
     * @param QWorkflowTemplateName the fully qualified name of the java workflowTemplate to use for this submission. i.e. specifying the structure. Please note: the template must be registered.
     * @param xmlWorkflowConfig holding the workflowTemplate's configuration i.e. specifying how to populate a static template
     * @return a ticket (UUID) for the submitted job which can be used for polling on the status and its results
     * @throws Exception 
     */
	@WebMethod(
            operationName = WeeService.NAME+ "_submitWorkflowByReference", 
            action = PlanetsServices.NS + "/" + WeeService.NAME +"/submitWorkflowByReference")
    @WebResult(
            name = WeeService.NAME + "UUID", 
            targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
            partName = WeeService.NAME + "UUID")
    public UUID submitWorkflowByReference ( 
    		@WebParam(
                    name = "digitalObjectRefs", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "digitalObjectRefs")
            ArrayList<URI> digObjRefs,
            @WebParam(
                    name = "QWorkflowTemplateName", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "xmlWorkflowConfig")
            String qWorkflowTemplateName,
            @WebParam(
                    name = "xmlWorkflowConfig", 
                    targetNamespace = PlanetsServices.NS + "/" + WeeService.NAME, 
                    partName = "xmlWorkflowConfig")   
            String xmlWorkflowConfig
    ) throws Exception; 
}
