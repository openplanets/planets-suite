package eu.planets_project.ifr.core.storage.impl.jcr;

/**
 * Helper class that contains DOJCR type and name constants
 * 
 * @author GrafR
 *
 */
public class DOJCRConstants 
{
	
	/**
	 * Function results 
	 */
	public static final int RESULT_ERROR       = -1;
	public static final int RESULT_OK          = 1;	
	public static final int RESULT_LIST_EMPTY  = 2;
	
	/**
	 * Registry name for digital objects
	 */
	public static final String REGISTRY_NAME = "planets-jcr";
	
	/**
	 * Title of the digital object 
	 */
	public static final String DOJCR = "DigitalObject";
	
	/**
	 * Title of the digital object 
	 */
	public static final String DOJCR_TITLE = "title";
	
	/**
	 * Permanent URI of the digital object 
	 */
	public static final String DOJCR_PERMANENT_URI = "permanent_uri";
	
	/**
	 * Manifestation of the digital object 
	 */
	public static final String DOJCR_MANIFESTATION_OF = "manifistation_of";
	
	/**
	 * Metadata of the digital object 
	 */
	public static final String DOJCR_METADATA = "metadata";
	
	/**
	 * Metadata type of the digital object 
	 */
	public static final String DOJCR_METADATA_TYPE = "metadata_type";
	
	/**
	 * Metadata content of the digital object 
	 */
	public static final String DOJCR_METADATA_CONTENT = "metadata_content";
	
	/**
	 * Metadata name of the digital object 
	 */
	public static final String DOJCR_METADATA_NAME = "metadata_name";
	
	/**
	 * Events of the digital object 
	 */
	public static final String DOJCR_EVENTS = "events";
	
	/**
	 * Events summary of the digital object 
	 */
	public static final String DOJCR_EVENTS_SUMMARY = "events_summary";
	
	/**
	 * Events timestamp of the digital object 
	 */
	public static final String DOJCR_EVENTS_DATETIME = "events_datetime";
	
	/**
	 * Events duration of the digital object 
	 */
	public static final String DOJCR_EVENTS_DURATION = "events_duration";
	
	/**
	 * Events agent of the digital object 
	 */
	public static final String DOJCR_EVENTS_AGENT = "events_agent";
	
	/**
	 * Events agent id of the digital object 
	 */
	public static final String DOJCR_EVENTS_AGENT_ID = "events_agent_id";
	
	/**
	 * Events agent name of the digital object 
	 */
	public static final String DOJCR_EVENTS_AGENT_NAME = "events_agent_name";
	
	/**
	 * Events agent type of the digital object 
	 */
	public static final String DOJCR_EVENTS_AGENT_TYPE = "events_agent_type";
	
	/**
	 * Events properties of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES = "events_agent_properties";
	
	/**
	 * Events properties URI of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES_URI = "events_agent_properties_uri";
	
	/**
	 * Events properties name of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES_NAME = "events_agent_properties_name";
	
	/**
	 * Events properties value of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES_VALUE = "events_agent_properties_value";
	
	/**
	 * Events properties description of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES_DESCRIPTION = "events_agent_properties_description";
	
	/**
	 * Events properties unit of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES_UNIT = "events_agent_properties_unit";
	
	/**
	 * Events properties type of the digital object 
	 */
	public static final String DOJCR_EVENTS_PROPERTIES_TYPE = "events_agent_properties_type";
	
	/**
	 * Format of the digital object 
	 */
	public static final String DOJCR_FORMAT = "format";
	
	/**
	 * Content of the digital object 
	 */
	public static final String DOJCR_CONTENT = "content";
	
	/**
	 * Content folder of the digital object 
	 */
	public static final String DOJCR_CONTENT_FOLDER = "content_folder";
	
	/**
	 * Content of the digital object 
	 */
	public static final String DOJCR_CONTENT_URI = "content_uri";

	/**
	 * Default value for the jcr:mimetype property
	 */
	public static final String DOJCR_PROPERTY_DEFAULT_MIMETYPE = "application/octet-stream";

	/**
	 * JCR path separator character
	 */
	public static final String JCR_PATH_SEPARATOR = "/";
	
	/**
	 * JCR path node index braces
	 */
	public static final String NODE_INDEX_BEGIN = "[";
	public static final String NODE_INDEX_END = "]";
	
	/**
	 * Defaults for the storing of binary data in JCR
	 */
	public static final String NT_FILE = "nt:file";
	public static final String JCR_CONTENT = "jcr:content";
	public static final String NT_RESOURCE = "nt:resource";
	public static final String JCR_LASTMODIFIED = "jcr:lastModified";
	public static final String JCR_MIMETYPE = "jcr:mimeType";
	public static final String NT_FOLDER = "nt:folder";
	public static final String JCR_DATA = "jcr:data";
	

	/**
	 * Defines for PREMIS standard
	 */
	public static final String PREMIS_TITLE = "PREMIS_TITLE";
	public static final String PREMIS_PERMANENT_URI = "PREMIS_PERMANENT_URI";
	public static final String PREMIS_FORMAT_URI = "PREMIS_FORMAT_URI";
	public static final String PREMIS_METADATA_TYPE = "PREMIS_METADATA_TYPE";
	public static final String PREMIS_METADATA_CONTENT = "PREMIS_METADATA_CONTENT";
	public static final String PREMIS_METADATA_NAME = "PREMIS_METADATA_NAME";
	public static final String PREMIS_EVENT_SUMMARY = "PREMIS_EVENT_SUMMARY";
	public static final String PREMIS_EVENT_DATETIME = "PREMIS_EVENT_DATETIME";
	public static final String PREMIS_EVENT_DURATION = "PREMIS_EVENT_DURATION";
	public static final String PREMIS_EVENT_AGENT_ID = "PREMIS_EVENT_AGENT_ID";
	public static final String PREMIS_EVENT_AGENT_NAME = "PREMIS_EVENT_AGENT_NAME";
	public static final String PREMIS_EVENT_AGENT_TYPE = "PREMIS_EVENT_AGENT_TYPE";
	public static final String PREMIS_EVENT_PROPERTY_URI = "PREMIS_EVENT_PROPERTY_URI";
	public static final String PREMIS_EVENT_PROPERTY_NAME = "PREMIS_EVENT_PROPERTY_NAME";
	public static final String PREMIS_EVENT_PROPERTY_VALUE = "PREMIS_EVENT_PROPERTY_VALUE";
	public static final String PREMIS_EVENT_PROPERTY_DESCRIPTION = "PREMIS_EVENT_PROPERTY_DESCRIPTION";
	public static final String PREMIS_EVENT_PROPERTY_UNIT = "PREMIS_EVENT_PROPERTY_UNIT";
	public static final String PREMIS_EVENT_PROPERTY_TYPE = "PREMIS_EVENT_PROPERTY_TYPE";
}
