package eu.planets_project.tb.gui.backing.exp;

import java.io.FileNotFoundException;

import eu.planets_project.services.utils.PlanetsLogger;
import eu.planets_project.tb.api.data.util.DataHandler;
import eu.planets_project.tb.api.data.util.DigitalObjectRefBean;
import eu.planets_project.tb.impl.data.util.DataHandlerImpl;

public class DigitalObjectBean {
	
	private PlanetsLogger log = PlanetsLogger.getLogger(ResultsForDigitalObjectBean.class, "testbed-log4j.xml");
	
	private String digitalObject;
	    
	private String downloadURL;
	    
	private String digitalObjectName;
	
	public DigitalObjectBean(String input){
		this.init(input);
	}
	
    /**
     * @return the digitalObject
     * e.g. 54f8cff2-2f27-46f3-add1-541a00937653.tif
     */
    public String getDigitalObject() {
        return digitalObject;
    }
	
	/**
     * @param digitalObject the digitalObject to set
     */
    public void setDigitalObject(String digitalObject) {
        this.digitalObject = digitalObject;
    }

    /**
     * @return the downloadURL
     */
    public String getDownloadURL() {
        return downloadURL;
    }

    /**
     * @param downloadURL the downloadURL to set
     */
    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    /**
     * @return the digitalObjectName
     */
    public String getDigitalObjectName() {
        return digitalObjectName;
    }

    /**
     * @param digitalObjectName the digitalObjectName to set
     */
    public void setDigitalObjectName(String digitalObjectName) {
        this.digitalObjectName = digitalObjectName;
    }
	
	/**
     * 
     */
    private void init( String file ) {
        // Populate using the results:
        DataHandler dh = DataHandlerImpl.findDataHandler();
        // Set up the DO name, etc
        setDigitalObject(file);
        DigitalObjectRefBean bean;
        try {
            bean = dh.get(file);
            setDownloadURL(bean.getDownloadUri().toString());
            setDigitalObjectName(DataHandlerImpl.createShortDOName(bean.getName()));
        } catch (FileNotFoundException e) {
            setDownloadURL("");
            setDigitalObjectName(DataHandlerImpl.createShortDOName(file));
        }
        log.info("DigitalObjectBean initialised.");
    }

}
