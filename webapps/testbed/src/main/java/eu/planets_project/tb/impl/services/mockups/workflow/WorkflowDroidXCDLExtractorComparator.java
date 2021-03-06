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
package eu.planets_project.tb.impl.services.mockups.workflow;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import eu.planets_project.services.PlanetsException;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.compare.Compare;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.tb.api.model.eval.EvaluationExecutable;
import eu.planets_project.tb.api.services.mockups.workflow.Workflow;
import eu.planets_project.tb.impl.model.eval.EvaluationExecutableImpl;


/**
 * @author lindleyA
 * This mockup implements the following scenario:
 * In-->Takes two files as input
 * a) identify using Droid: to analyze a given file and to extract it's pronom Id(s)
 * b) extract using XCDL Extractor: to extract it's XCDL representation (if it is supported)
 * c) compare using XCDL Comparator: compares the two XCDL descriptions.
 * Out<--returns the comparator's responds 
 * 
 * It is fully functional in terms of service execution
 * Due to the abstinence of a technical registry, the logic of pronom ID mapping to XCEL selection 
 * is currently provided within this workflow.
 * At the moment TIFF and PNG are supported
 * 
 * It however does not follow the Planets (IF) patterns of designing workflows and
 * therefore is marked as mockup
 *
 */
@Deprecated
public class WorkflowDroidXCDLExtractorComparator implements Workflow{
	
	private static final String URL_DROID = "http://localhost:8080/pserv-pc-droid/Droid?wsdl";
	//private static final String URL_XCDLEXTRACTOR = "http://localhost:8080/pserv-pc-extractor/Extractor2Binary?wsdl";
	private static final String URL_XCDLEXTRACTOR = "http://planetarium.hki.uni-koeln.de:8080/pserv-pc-extractor/Extractor2Binary?wsdl";
	private static final String URL_XCDLCOMPARATOR = "http://localhost:8080/pserv-pp-comparator/ComparatorBasicCompareTwoXcdlValues?wsdl";
	
	//the new extractor interface does not require to hand over the xcel anymore
	//private static final String PATH_TO_xcel_tiff = "Planets_XCEL_Exctractor/res/xcl/xcel/xcel_docs/xcel_tiff.xml";
	//private static final String PATH_TO_xcel_png = "Planets_XCEL_Exctractor/res/xcl/xcel/xcel_docs/xcel_png.xml";
	
	enum supportedTypes{
		TIFF(new String[]{"info:pronom/fmt/7","info:pronom/fmt/8","info:pronom/fmt/9","info:pronom/fmt/10","info:pronom/fmt/152","info:pronom/fmt/153","info:pronom/fmt/154","info:pronom/fmt/155","info:pronom/fmt/156","info:pronom/x-fmt/387","info:pronom/x-fmt/388","info:pronom/x-fmt/399"}/*,PATH_TO_xcel_tiff*/),
		PNG(new String[]{"info:pronom/fmt/11","info:pronom/fmt/12","info:pronom/fmt/13"}/*,PATH_TO_xcel_png*/),
		JPEG(new String[]{"info:pronom/fmt/41","info:pronom/fmt/42","info:pronom/fmt/43","info:pronom/fmt/44","info:pronom/fmt/112","info:pronom/fmt/149","info:pronom/x-fmt/390","info:pronom/x-fmt/391","info:pronom/x-fmt/398"}/*,PATH_TO_xcel_jpeg*/),
		GIF(new String[]{"info:pronom/fmt/3","info:pronom/fmt/4"}/*,PATH_TO_xcel_gif*/),
		BMP(new String[]{"info:pronom/fmt/114","info:pronom/fmt/115","info:pronom/fmt/116","info:pronom/fmt/117","info:pronom/fmt/118","info:pronom/fmt/119","info:pronom/x-fmt/25","info:pronom/x-fmt/270"}/*,PATH_TO_xcel_bmp*/);
		private String[] idsForType;
		//private String xcelPath;
		
		supportedTypes(String[] ids /*, String xcelPath*/){
			this.idsForType = ids;
			//this.xcelPath = xcelPath;
		}
		
		public String[] pronomIDs(){
			return this.idsForType;
		}
		
		/*public String xcelPath(){
			return xcelPath;
		}*/
		/**
		 * Returns the XCEL - parsed from the definition files
		 * @return
		 * @throws UnsupportedEncodingException
		 */
		/*private String xcel() throws UnsupportedEncodingException{
			return new String(ByteArrayHelper.read(new File(xcelPath())),"UTF-8");
		}*/
	
	}
	
	
	/**
	 * Returns an EvaluationExecutable containing the XCDLs, etc.
	 * @param f1
	 * @param f2
	 * @return
	 * @throws Exception
	 */
	public EvaluationExecutable execute(File f1, File f2){
	
		//check if the results can be returned from the cache else - perform the workflow
		if(this.isResultCached(f1, f2)){
			//results already cached
			return(this.getResultFromCache(f1, f2));
		}
		
		//the execution results are packed within this executable
		//please note this mockup is not using the concept of the TestbedServiceTemplate
		EvaluationExecutable executable = new EvaluationExecutableImpl();
		executable.setExecutionStartDate(new GregorianCalendar().getTimeInMillis());
		executable.setExecutableInvoked(true);
		executable.setExecutionCompleted(false);
		
		try{
	  //step1: identify using Pronom;
		String[] sPronomIDs1 = runDroid(f1);
		String[] sPronomIDs2 = runDroid(f2);
		
	  //if-else logic - linking pronom results as input for xcdl extraction	
		@SuppressWarnings("unused")
		supportedTypes typeF1,typeF2;
		typeF1 = getType(sPronomIDs1);
		typeF2 = getType(sPronomIDs2);
		
	  //step2: extract the xcdl descriptions
		String XCDL1 = runXCDLExtractor(f1 /*, typeF1.xcel()*/);
		String XCDL2 = runXCDLExtractor(f2 /*, typeF2.xcel()*/);
		
	  //step3: run the XCDL comparison
		String result = runXCDLComparator(XCDL1, XCDL2);
		
		executable.setXCDLForSource(XCDL1);
		executable.setXCDLForTarget(XCDL2);
		executable.setXCDLsComparisonResult(result);
		executable.setExecutionSuccess(true);
		
		}catch(Exception e){
			executable.setExecutionSuccess(false);
		}
		//End Workflow; write back the results into the executable
		executable.setExecutionEndDate(new GregorianCalendar().getTimeInMillis());
		executable.setExecutionCompleted(true);

		//add the result to the cache
		this.addResultToCache(executable, f1, f2);
		
		return executable;
	}
	
	/**
	 * Takes an Array of PronomIDs and returns the supported type e.g. TIFF, PNG
	 * If no type is known, supportedTypes = null;
	 * @param pronomIDs
	 * @return
	 */
	private supportedTypes getType(String[] pronomIDs) throws Exception{
		supportedTypes typeFound = null;
		//iterate over all supportedTypes and check if we match one
		for(supportedTypes type : supportedTypes.values()){
			List<String> lIDs = Arrays.asList(type.idsForType);
			for(int j=0;j<pronomIDs.length;j++){
				if(lIDs.contains(pronomIDs[j])){
					//we've found a supported type as e.g. TIFF, PNG, etc.
					typeFound = type;
				}
			}
		}
		if(typeFound ==null){
	       throw new Exception("The specified file type is currently not supported by this workflow");
		}
		return typeFound;
	}
	
	
	/**
	 * Runs droid on a given File and returns an Array of PronomIDs
	 * @param f1
	 * @return
	 * @throws Exception
	 */
	private String[] runDroid(File f1) throws Exception{
		//Step1: identify using droid - returns a status and a list of IDs
	 	URL url = null;
 		try {
 		url = new URL(URL_DROID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        }
        Service service = Service.create(url, new QName(PlanetsServices.NS,
                Identify.NAME));
        Identify droid = service.getPort(Identify.class);
        byte[] array = FileUtils.readFileToByteArray(f1);
        
        //invoke the service and extract results
        IdentifyResult identify = droid.identify(new DigitalObject.Builder(Content.byValue(array)).build(), null);
        List<URI> result = identify.getTypes();
        String status = identify.getReport().getMessage();
        
        if(!status.equals("Positive")){
        	throw new Exception("Service execution failed");
        }
        if(result.size()<1){
        	throw new Exception("The specified file type is currently not supported by this workflow");
        }
        
        String[] strings = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            String string = result.get(i).toASCIIString();
            //received 1..n Pronom IDs
            strings[i] = string;
        }
        return strings;
	}
	
	
	/**
	 * Runs the XCDL extractor on a given file and for a given xcel descriptor
	 * Returns the XCDL description (UTF-8 encoded) for the provided file
	 * @param f1
	 * @param xcel
	 * @return
	 * @throws Exception
	 */
	private String runXCDLExtractor(File f1 /*, String xcel*/) throws Exception{
		URL url = null;
	    try {
	    	url = new URL(URL_XCDLEXTRACTOR);
	    	
	    	Service service = Service.create(url, new QName(PlanetsServices.NS,
		        		Migrate.NAME));
	        Migrate extractor = service.getPort(Migrate.class);
	        
	        //the service's input
	        byte[] array = FileUtils.readFileToByteArray(f1);
	        
	        //the service call and it's result
            DigitalObject digitalObject = extractor.migrate(
                    new DigitalObject.Builder(Content.byValue(array))
                            .build(), null, null, null).getDigitalObject();
            byte[] results = IOUtils.toByteArray(digitalObject.getContent().getInputStream());
			String xcdl = new String(results,"UTF-8");
			
			if(xcdl==null){
	        	throw new Exception("XCDL extraction failed - please check service logs for details");
	        }
			
			return xcdl;
	
	    } catch (MalformedURLException e) {
	    	e.printStackTrace();
	    	throw e;
		} catch (UnsupportedEncodingException e) {
			//xcel file was not UTF-8 encodable
			e.printStackTrace();
			throw e;
		} catch (PlanetsException e) {
			//error calling the web-service
			e.printStackTrace();
			throw e;
		}	
	}
	
	/**
	 * Runs the XCDLComparator on two given xcdls and returns the comparison result.
	 * @param xcdl1
	 * @param xcdl2
	 * @return
	 * @throws Exception
	 */
	private String runXCDLComparator(String xcdl1, String xcdl2) throws Exception{

		URL url = null;
	    try {
	    	url = new URL(URL_XCDLCOMPARATOR);
	    	
	    	Service service = Service.create(url, new QName(PlanetsServices.NS,
	    			Compare.NAME));
	    	Compare comparator = service.getPort(Compare.class);

	        //the service call and it's result
	        DigitalObject x1 = new DigitalObject.Builder(Content.byValue(xcdl1.getBytes())).build();
	        DigitalObject x2 = new DigitalObject.Builder(Content.byValue(xcdl2.getBytes())).build();
	        List<Property> result = comparator.compare(x1, x2, null).getProperties();
	        
	        if(result==null){
	        	throw new Exception("XCDL comparison failed - please check service logs for details");
	        }
			
			return result.toString();
	
	    } catch (MalformedURLException e) {
	    	e.printStackTrace();
	    	throw e;
		}
	}
	
	//Info: Map<f1.getAbsolutePath()+f2.getAbsolutePath(), EvaluationExecutable>
	private Map<String, EvaluationExecutableImpl> cache = new HashMap<String, EvaluationExecutableImpl>();
	/**
	 *@see isStaticAutoEvaluationWorkflowResultCached
	 */
	private EvaluationExecutable getResultFromCache(File f1, File f2){
		if(cache.containsKey(f1.getAbsolutePath()+f2.getAbsolutePath())){
			return (cache.get(f1.getAbsolutePath()+f2.getAbsolutePath())).clone();
		}
		else{
			return null;
		}
	}
	
	/**
	 * Check if for a given data pair input and output file if the service
	 * request has already been issued. If yes the results will be the same and therefore may
	 * be returned from this cache
	 * @param f1
	 * @param f2
	 * @return
	 */
	private boolean isResultCached(File f1, File f2){
		return cache.containsKey(f1.getAbsolutePath()+f2.getAbsolutePath());
	}
	
	/**
	 *@see isStaticAutoEvaluationWorkflowResultCached
	 */
	private void addResultToCache(EvaluationExecutable executable, File f1, File f2){
		cache.put((f1.getAbsolutePath()+f2.getAbsolutePath()), (EvaluationExecutableImpl)executable);
	}

}
