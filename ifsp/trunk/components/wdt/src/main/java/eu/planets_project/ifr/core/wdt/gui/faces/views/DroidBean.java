package eu.planets_project.ifr.core.wdt.gui.faces.views;


import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;

//Workflow Logging
import javax.naming.InitialContext;
import javax.naming.Context;
import eu.planets_project.ifr.core.storage.api.WorkflowManager;
import eu.planets_project.ifr.core.storage.api.WorkflowDefinition;
import eu.planets_project.ifr.core.storage.api.WorkflowExecution;
import eu.planets_project.ifr.core.storage.api.InvocationEvent;
import eu.planets_project.ifr.core.storage.api.DataManagerLocal;

import javax.faces.component.*;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.ifr.core.common.api.PlanetsService;
import eu.planets_project.ifr.core.common.api.PlanetsException;
import eu.planets_project.ifr.core.wdt.impl.wf.AbstractWorkflowBean;
import eu.planets_project.ifr.core.wdt.impl.wf.WFTemplate;
import eu.planets_project.ifr.core.wdt.impl.registry.Service;
import eu.planets_project.ifr.core.wdt.impl.registry.ServiceRegistry;
import eu.planets_project.ifr.core.wdt.api.WorkflowBean;

import eu.planets_project.ifr.core.wdt.common.faces.JSFUtil;
import eu.planets_project.ifr.core.wdt.common.services.reportGeneration.*;
import eu.planets_project.ifr.core.common.services.migrate.BasicMigrateOneBinary;
import eu.planets_project.ifr.core.common.services.identify.BasicIdentifyOneBinary;
//--
import eu.planets_project.ifr.core.wdt.common.services.droid.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL; 
	
/**
 *    characterization workflow bean 
 *    demonstrates a workflow comprising a characterization followed by a migration
 * 	  @author Rainer Schmidt, ARC
 */
public class DroidBean extends AbstractWorkflowBean implements PlanetsService, WorkflowBean {
		
	private Log logger = PlanetsLogger.getLogger(this.getClass(), "resources/log/wdt-log4j.xml");	
	
	private String viewId = null;
	
	//backing for drop down box
	private List<SelectItem> charServiceItems = null;
	private List<SelectItem> migServiceItems = null;
	private SelectItem currentCharServiceItem = null;	
	private SelectItem currentMigServiceItem = null;		
	
	//services
	private List<Service> charServices = null;
	private List<Service> migServices = null;

	
	private ServiceRegistry registry = null;	
	private String reportLoc= "";
	
	public DroidBean() {
		super();
		//get a registry url from a .properties file 
		registry = new ServiceRegistry();
		this.resetServices();
	}

	/**
	* loads services from a service registry 
	*/
	public void lookupServices() {
		
		this.resetServices();
		//registry lookup...
		charServices = registry.lookupServices(new Service(null, null, null, null, "uuid:253246f0-ff2f-11dc-95ff-0800200c9a66/characterisation",null));
		migServices = registry.lookupServices(new Service(null, null, null, null, "uuid:253246f0-ff2f-11dc-95ff-0800200c9a66/migration",null));
		
		charServiceItems.addAll( (Collection)toSelectItem(charServices) );
		migServiceItems.addAll( (Collection)toSelectItem(migServices) );
	}
	
	/**
	* removes service urls
	*/
	public void resetServices() {
		charServiceItems = new ArrayList<SelectItem>();
		charServiceItems.add( new SelectItem("please choose a service") );
		currentCharServiceItem = charServiceItems.get(0);
		migServiceItems = new ArrayList<SelectItem>();
		migServiceItems.add( new SelectItem("please choose a service") );
		currentMigServiceItem = migServiceItems.get(0);		
	}
		
	public List<SelectItem> getCharServiceItems() {
		return charServiceItems;
	}
	
	public String getCurrentCharServiceItem() {
		String service = (String) currentCharServiceItem.getValue();
		return service;
	}	
	
	public void toggleCharServiceItems(ValueChangeEvent vce) {
		String selectedService = (String) vce.getNewValue();
		//point currentCharService to new selection
		for( SelectItem indexService : charServiceItems) {
			if( indexService.getValue().toString().equals(selectedService)) currentCharServiceItem = indexService;
		}
    logger.debug("currentCharServiceItem: " + currentCharServiceItem.getValue().toString() );
	}
	
	//public void setMigServies(List services) {
	//	this.migServices = services;
	//}
		
	public List<SelectItem> getMigServiceItems() {
		return migServiceItems;
	}
	
	public String getCurrentMigServiceItem() {
		String service = (String) currentMigServiceItem.getValue();
		return service;
	}	
	
	public void toggleMigServiceItems(ValueChangeEvent vce) {
		String selectedService = (String) vce.getNewValue();
		//point currentCharService to new selection
		for( SelectItem indexService : migServiceItems) {
			if( indexService.getValue().toString().equals(selectedService)) currentMigServiceItem = indexService;
		}
		logger.debug("currentMigServiceItem: " + currentMigServiceItem.getValue().toString() );
	}

	

	public String invokeService() {
				
		ReportGenerationService report = null;
		int reportID = -1;
		
		try {
			
			Properties env = new java.util.Properties(); 
			InitialContext ctx = new InitialContext(env);
			Service charService = this.getService(currentCharServiceItem, charServices);
			Service migService = this.getService(currentMigServiceItem, migServices);			
			
			//lookup workflow manager
			WorkflowManager wfManager = (WorkflowManager)ctx.lookup("planets-project.eu/WorkflowManager");
			logger.debug("wfManager: "+wfManager);
			
			//lookup data manager
			DataManagerLocal dataManager = (DataManagerLocal)ctx.lookup("planets-project.eu/DataManager/local");
			logger.debug("dataManager: "+dataManager);
			
			
			//add the definition to data registry
			WorkflowDefinition wfDef = new WorkflowDefinition("/wfDefs/Level1ConvertBean.def", "Rainer", /*doc*/null);

	  	//already exists wfManager.createWorkflow(wfDef);
	  	WorkflowExecution wfExec = new WorkflowExecution(wfDef.getId(), "Rainer"); 
	  	String workflowId = wfManager.createWorkflowInstance(wfExec);
			logger.debug("workflowId: "+workflowId);	  	
						
			//start report			
			ReportGenerationService_Service report_locator = new ReportGenerationService_Service();
			report = report_locator.getReportGenerationServicePort();			
			reportID = report.startReport();
			
			//create a droid service
			Droid_Service locator = 
				new Droid_Service(new URL(charService.getEndpoint()),charService.getQName() );
			Droid droid = locator.getDroidPort();
				
			//create a migration service
			//String serviceEndpoint = "http://localhost:8080/ifr-jmagickconverter-ejb/JpgToTiffConverter?wsdl";
			//service = javax.xml.ws.Service.create(new URL(migService.getEndpoint()), BasicMigrateOneBinary.QNAME);
			//BasicMigrateOneBinary converter = service.getPort(BasicMigrateOneBinary.class);
			
												
			for (int i=0; i<inputData.size();i++) {
				
				String pdURI = inputData.get(i);
								
				//File srcFile = new File(fileLoc);				
				//fileLoc = fileLoc.substring("file:/".length(), fileLoc.length());
								
				//logger.debug("isFile: "+srcFile.isFile());
		    //byte[] imageData = getByteArrayFromFile(srcFile);
		    byte[] imageData = dataManager.retrieveBinary(new URI(pdURI));
		    
		    //create an event for that		    
		    List<String> rets = droid.identifyBytes(imageData).getTypes();
		    for( String ret : rets) {
		    	logger.debug("Droid reported: "+ret);
		    }
		    
		    /*
		    Date d1 = new Date();
				byte[] out = converter.basicMigrateOneBinary(imageData);
		    Date d2 = new Date();
		    			
	      URI[] list = dataManager.list(null);
	      URI resultFile = new URI(pdURI+workflowId+"/outfile.tiff");
	      dataManager.storeBinary(resultFile, out);
	
				//link uri with gui
				InvocationEvent event = new InvocationEvent(null, new URI(charService.getEndpoint()), "basicMigrateBinary", new URI(pdURI), resultFile, d1, d2);      
				
				String ret = wfManager.createInvocationEvent(event, workflowId);
				logger.debug("wfMan.createIEvent: "+ret);
				
	
							
				report.appendCDATA(reportID, "<fieldset><legend><b>File:</b><i>"+pdURI+"</i></legend><table><tr><td>"+
				"<b>File Format Information:</b>"+resultType+"<br>" +
				"<b>Conversion Status: </b><font color=#00CC00>File successfuly converted</font> <br>" +
				"<b>Converted File URI:</b><a href="+"\""+resultFile+"\""+" target=_blank>"+resultFile+"</a>" +
				"</td></tr></table></fieldset>");
				*/
			}
		
		} catch (Exception e) {
			logger.error("workflow bean error: "+e);
		} finally {
			reportLoc = report.finalizeReport(reportID);
			logger.debug("report location: "+reportLoc);
		}
		return reportLoc ;
	}
	
	
	public String getReportLoc() {
		return reportLoc;
	}
	
	public void setReportLoc(String reportLoc) {
		this.reportLoc = reportLoc;
	}

}



//DOM STUFF

/*
			//lookup stateful ejb
			//Context context = new InitialContext();
			//WorkflowManager wfHome = (WorkflowManager)PortableRemoteObject.
			//	narrow(context.lookup("/planets-project.eu/WorkflowManager"), WorkflowManager.class);
			//WorkflowManager wfManager = wfHome.create();
			//....
			
			//creata a workflow document
			Document doc = null;
			//try {
  		//	DOMParser p = new DOMParser();
  		//	p.setFeature("http://xml.org/sax/features/validation",true);
  		//	p.parse("c:\\data\\wfDef.xml");              
  		//	doc = p.getDocument();
			//} catch (IOException io) { logger.error(""+io); 
			//} catch (SAXException s) { logger.error(""+s); 
			
			//try {
  		//	SAXBuilder b = new SAXBuilder(false);  // validierenden Parser nutzen
  		//	doc = b.build(new File("c:\\data\\wfDef.xml"));
			//}
			//catch (Exception e) {
  		//	logger.error(""+e);
			//}
			
			//InputSource source = new InputSource(new FileInputStream("c:\\data\\wfDef.xml"));
      //DOMParser parser = new DOMParser();
      //parser.parse(source);
      
      try {
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	      DocumentBuilder builder = factory.newDocumentBuilder();
	  		doc = builder.parse( new File("c:\\data\\wfDef.xml") );
  		} catch (SAXParseException spe) {
    		// Error generated by the parser
    		logger.error("\n** Parsing error"
      	+ ", line " + spe.getLineNumber()
      	+ ", uri " + spe.getSystemId());
    		logger.error("   " + spe.getMessage() );
  
    		// Use the contained exception, if any
    		Exception  x = spe;
    		if (spe.getException() != null)
      		x = spe.getException();
    		logger.error(""+x);

	  	} catch (SAXException sxe) {
	    	// Error generated during parsing
	    	Exception  x = sxe;
	    	if (sxe.getException() != null)
	      	x = sxe.getException();
	    	logger.error(""+x);
	
	  	} catch (ParserConfigurationException pce) {
	    	// Parser with specified options can't be built
	    	logger.error(""+pce);
	  	} catch (IOException ioe) {
	    	// I/O error
	    	logger.error(""+ioe);
	  	}

      
			logger.debug("wf doc: "+doc);

*/