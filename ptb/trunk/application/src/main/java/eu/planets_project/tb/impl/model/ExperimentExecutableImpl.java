/**
 * 
 */
package eu.planets_project.tb.impl.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.tb.api.data.util.DataHandler;
import eu.planets_project.tb.api.model.ExperimentExecutable;
import eu.planets_project.tb.api.services.TestbedServiceTemplate;
import eu.planets_project.tb.impl.data.util.DataHandlerImpl;
import eu.planets_project.tb.impl.services.TestbedServiceTemplateImpl;

/**
 * @author alindley
 * 
 * This class contains all information that is required for the experiment's service execution. 
 * This object is handed over to the service execution and results are written 
 * back to it. i.e. corresponds to the idea of an executable part of a preservation plan
 * 
 * Please note: As service currently aren't able to take http file references as input, this
 * class holds and takes only local file refs. 
 * 
 * Beware: OutputFileRef must not always be the pointer to a file. e.g. for characterisation services
 * this will correspond to a String. 
 */
@Entity
@XmlAccessorType(XmlAccessType.FIELD) 
public class ExperimentExecutableImpl extends ExecutableImpl implements ExperimentExecutable, java.io.Serializable{
	
	//hashmap of local file refs for input and output data of service execution
	//note: C:/DATA/ rather than http://localhost:8080/testbed/
	private HashMap<String,String> hmInputOutputData;
	//no one-to-one annotation, as we want to persist this data by value and not per reference
	private TestbedServiceTemplateImpl tbServiceTemplate;
	private String sSelectedServiceOperationName="";

	//A logger for this - transient: it's not persisted with this entity
    @Transient
    @XmlTransient
	private static Log log;
	
	
	public ExperimentExecutableImpl(TestbedServiceTemplate template) {
		log = PlanetsLogger.getLogger(this.getClass(),"testbed-log4j.xml");
		//decouple this object
		tbServiceTemplate = ((TestbedServiceTemplateImpl)template).clone();
		//sets the object's discriminator value to "experiment" and not "template"
		tbServiceTemplate.setDiscriminator(tbServiceTemplate.DISCR_EXPERIMENT);
		
		//Info: HashMap<InputFileRef,OutputFileRef>
		hmInputOutputData = new HashMap<String,String>();
	}
	
	//Default Constructor required for Entity Annotation
	public ExperimentExecutableImpl(){
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#addInputData(java.lang.String)
	 */
	public void addInputData(String localFileRef) {
		if(!this.hmInputOutputData.containsKey(localFileRef)){
			//add new InputFileRef and set OutputFileRef null
			this.hmInputOutputData.put(localFileRef, null);
			//add Mapping
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#addInputData(java.util.List)
	 */
	public void addInputData(Collection<String> localFileRefs) {
		Iterator<String> itFileRefs = localFileRefs.iterator();
		while(itFileRefs.hasNext()){
			this.addInputData(itFileRefs.next());
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#getInputData()
	 */
	public Collection<String> getInputData() {
		return this.hmInputOutputData.keySet();
	}
	
	/* (non-Javadoc)
     * @see eu.planets_project.tb.api.model.ExperimentExecutable#getNumberOfInputs()
     */
    public int getNumberOfInputs() {
        if( this.getInputData() == null ) return 0;
        return this.getInputData().size();
    }

    /* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#removeInputData(java.lang.String)
	 */
	public void removeInputData(String localFileRef) {
		//InputData represented by keys
		if(this.hmInputOutputData.keySet().contains(localFileRef)){
			this.hmInputOutputData.remove(localFileRef);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#removeAllInputData()
	 */
	public void removeAllInputData(){
		this.hmInputOutputData = new HashMap<String,String>();
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#removeInputData(java.util.List)
	 */
	public void removeInputData(Collection<String> localFileRefs) {
		Iterator<String> itFileRefs = localFileRefs.iterator();
		while(itFileRefs.hasNext()){
			this.removeInputData(itFileRefs.next());
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#setInputData(java.util.List)
	 */
	public void setInputData(Collection<String> localFileRefs) {
		this.hmInputOutputData = new HashMap<String,String>();
		Iterator<String> itFileRefs = localFileRefs.iterator();
		while(itFileRefs.hasNext()){
			this.addInputData(itFileRefs.next());
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#getOutputData()
	 */
	public Collection<String> getOutputData() {
		Vector<String> vRet = new Vector<String>();
		Iterator<String> itOutput = this.hmInputOutputData.values().iterator();
		while(itOutput.hasNext()){
			String output = itOutput.next();
			if(output!=null){
				vRet.add(output);
			}
		}
		return vRet;
	}
	


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#setOutputData(java.util.Collection)
	 */
	public void setOutputData(Collection<Entry<String, String>> ioLocalFileRefs) {
		Iterator<Entry<String,String>> itIOFiles = ioLocalFileRefs.iterator();
		while(itIOFiles.hasNext()){
			this.setOutputData(itIOFiles.next());
		}
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#setOutputData(java.lang.String, java.lang.String)
	 */
	public void setOutputData(String inputFileRef, String outputFileRef) {
		if(this.hmInputOutputData.containsKey(inputFileRef)){
			this.hmInputOutputData.put(inputFileRef, outputFileRef);
		}
	}

	
	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.mockups.ExperimentExecutable#setOutputData(java.util.Map.Entry)
	 */
	public void setOutputData(Entry<String, String> ioFileRef) {
		//check if the inputURI is known - don't care about what's the output data (e.g. null allowed)
		if(this.hmInputOutputData.keySet().contains(ioFileRef.getKey())){
			this.hmInputOutputData.put(ioFileRef.getKey(), ioFileRef.getValue());
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getInputHttpFileRef(java.lang.String)
	 */
	public URI getInputHttpFileRef(String localFileRef) {
		DataHandler dh = new DataHandlerImpl();
		if(localFileRef!=null){
			try {
				return dh.getHttpFileRef(new File(localFileRef), true);
				
			} catch (FileNotFoundException e) {
			} catch (URISyntaxException e) {
				log.debug("Exception while building URI for InputFile");
			}
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getAllInputHttpDataEntries()
	 */
	public Collection<URI> getAllInputHttpDataEntries(){
		List<URI> ret = new Vector<URI>();
		DataHandler dh = new DataHandlerImpl();
		Iterator<String> itInputData =  this.getInputData().iterator();
		while(itInputData.hasNext()){
			try {
				URI uri = dh.getHttpFileRef(new File(itInputData.next()), true);
				if(uri!=null){
					ret.add(uri);
				}
			} catch (FileNotFoundException e) {
			} catch (URISyntaxException e) {
				log.debug("Exception while building URI for all Input data");
			}
			
		}
		return ret;
	}


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getOutputHttpFileRef(java.lang.String)
	 */
	public URI getOutputHttpFileRef(String localFileRef) {
		DataHandler dh = new DataHandlerImpl();
		if(localFileRef!=null){
			try {
				return dh.getHttpFileRef(new File(localFileRef), false);
				
			} catch (FileNotFoundException e) {
			} catch (URISyntaxException e) {
				log.debug("Exception while building URI for OutputFile");
			}
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getAllMigrationOutputHttpData()
	 */
	public Collection<URI> getAllMigrationOutputHttpData(){
		List<URI> ret = new Vector<URI>();
		DataHandler dh = new DataHandlerImpl();
		Iterator<String> itOutputData =  this.getOutputData().iterator();
		while(itOutputData.hasNext()){
			try {
				URI uri = dh.getHttpFileRef(new File(itOutputData.next()), false);
				if(uri!=null){
					ret.add(uri);
				}
			} catch (FileNotFoundException e) {
			} catch (URISyntaxException e) {
				log.debug("Exception while building URI for all output data");
			}
			
		}
		return ret;
	}
	


	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getAllCharacterisationOutputHttpData()
	 */
	public Collection<String> getAllCharacterisationOutputHttpData(){

		return this.getOutputData();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getCharacterisationDataEntries()
	 */
	public Collection<Entry<String, String>> getCharacterisationDataEntries() {
		return this.hmInputOutputData.entrySet();
	}
	
	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getOutputDataEntries()
	 */
	public Collection<Entry<String, String>> getOutputDataEntries() {
		return this.hmInputOutputData.entrySet();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getCharacterisationHttpDataEntries()
	 */
	public Collection<Entry<URI, String>> getCharacterisationHttpDataEntries() {
		HashMap<URI,String> hmRet = new HashMap<URI,String>();
		Iterator<Entry<String,String>> itEntries = this.hmInputOutputData.entrySet().iterator();
		
		while(itEntries.hasNext()){
			//Entry<localFileRef,characterisation result>
			Entry<String,String> entry = itEntries.next();
			URI uri = this.getInputHttpFileRef(entry.getKey());
			if(uri!=null){
				hmRet.put(uri, entry.getValue());
			}
		}
		return hmRet.entrySet();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getMigrationDataEntries()
	 */
	public Collection<Entry<String, String>> getMigrationDataEntries() {
		return this.hmInputOutputData.entrySet();
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getMigrationHttpDataEntries()
	 */
	public Collection<Entry<URI, URI>> getMigrationHttpDataEntries() {
		HashMap<URI,URI> hmRet = new HashMap<URI,URI>();
		Iterator<Entry<String,String>> itEntries = this.hmInputOutputData.entrySet().iterator();
		
		while(itEntries.hasNext()){
			//Entry<localFileRef,characterisation result>
			Entry<String,String> entry = itEntries.next();
			URI uriInput = this.getInputHttpFileRef(entry.getKey());
			URI uriOutput = this.getOutputHttpFileRef(entry.getValue());
			
			if((uriInput!=null)&&(uriOutput!=null)){
				hmRet.put(uriInput,uriOutput);
			}
		}
		return hmRet.entrySet();
	}



	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getMigrationHttpDataEntry(java.lang.String)
	 */
	public Map.Entry<URI, URI> getMigrationHttpDataEntry(String localFileInputRef){
		if(this.hmInputOutputData.containsKey(localFileInputRef)){
			String outputFileRef = this.hmInputOutputData.get(localFileInputRef);
			HashMap<URI,URI> hmRet = new HashMap<URI,URI>();
			
			//get the URI return values for the local file ref
			URI inputFile = this.getOutputHttpFileRef(outputFileRef);
			URI outputFile = this.getInputHttpFileRef(localFileInputRef);
			if((inputFile!=null)&&(outputFile!=null)){
				hmRet.put(inputFile, outputFile);
				
				Iterator<Entry<URI,URI>> itRet = hmRet.entrySet().iterator();
				while(itRet.hasNext()){
					//return the Entry
					return itRet.next();
				}
			}
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getCharacterisationHttpDataEntry(java.lang.String)
	 */
	public Map.Entry<URI, String> getCharacterisationHttpDataEntry(String localFileInputRef){
		if(this.hmInputOutputData.containsKey(localFileInputRef)){
			String outputFileRef = this.hmInputOutputData.get(localFileInputRef);
			HashMap<URI,String> hmRet = new HashMap<URI,String>();
			
			//get the URI return values for the local file ref
			URI inputFile = this.getOutputHttpFileRef(outputFileRef);

			if(inputFile!=null){
				hmRet.put(inputFile, outputFileRef);
				
				Iterator<Entry<URI,String>> itRet = hmRet.entrySet().iterator();
				while(itRet.hasNext()){
					//return the Entry
					return itRet.next();
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#getSelectedServiceOperationName()
	 */
	public String getSelectedServiceOperationName() {
		return this.sSelectedServiceOperationName;
	}

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.ExperimentExecutable#setSelectedServiceOperationName(java.lang.String)
	 */
	public void setSelectedServiceOperationName(String sOperationName) {
		if(sOperationName!=null){
			//need to check if the registered ServiceTemplate contains this operation name
			if(this.tbServiceTemplate.getAllServiceOperationNames().contains(sOperationName)){
				this.sSelectedServiceOperationName = sOperationName;
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.Executable#getServiceTemplate()
	 */
	public TestbedServiceTemplate getServiceTemplate() {
		return this.tbServiceTemplate;
	}

	
	/* (non-Javadoc)
	 * @see eu.planets_project.tb.api.model.Executable#setServiceTemplate(eu.planets_project.tb.api.services.TestbedServiceTemplate)
	 */
	public void setServiceTemplate(TestbedServiceTemplate template) {
		this.tbServiceTemplate = (TestbedServiceTemplateImpl)template;
	}

}
