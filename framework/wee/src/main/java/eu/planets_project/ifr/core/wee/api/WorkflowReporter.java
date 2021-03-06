package eu.planets_project.ifr.core.wee.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import eu.planets_project.ifr.core.wee.api.ReportingLog.Level;
import eu.planets_project.ifr.core.wee.api.ReportingLog.Message;
import eu.planets_project.services.datatypes.Parameter;

/**
 * Creates a workflow report. Used by the {@link ReportingLog}
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
final class WorkflowReporter {
    private static final String REPORT_HTML = "wf-report.html";
    private static final String TEMPLATE = "ReportTemplate.html";
    private static final String CONTENT_MARKER = "###CONTENT###";
    private static final String LOCAL = "components/wee/src/main/resources/";
    //private static final String WEE_DATA = "/planets-ftp/gen";
    private static final String WEE_DATA = "/server/default/deploy/jboss-web.deployer/ROOT.war/wee-gen";
    private static final String JBOSS_HOME_DIR_KEY = "jboss.home.dir";
    private static final String JBOSS_HOME = System.getProperty(JBOSS_HOME_DIR_KEY);
    private static final String ENTRY =
    // A template for a workflow report message, used with String.format:
    "<fieldset><legend><b>%s</legend>" // first insert: the title
            + "<table bgcolor=%s width=100%%><tr><td>" // second insert: color
            + "%s" // third insert: the content (a template again, see below)
            + "</td></tr></table></fieldset>";
    private static final String CONTENT = "<b>%s: </b>%s<br/> ";
    private String folderID = "";
    String reportOutputFolder = initOutputFolder();
    private StringBuilder builder = new StringBuilder();

    /**
     * Create a new reporter. This sets a new ID for the report outputs to a
     * folder corresponding to that ID. 
     * If no workflow ID available: generate ID by timestamp
     */
    public WorkflowReporter() {
        this.folderID = getTimeStamp();
        this.reportOutputFolder = initOutputFolder();
    }
    
    /**
     * Uses the workflow's UUID as ID to generate the output directory
     * @see WorkflowReporter#WorkflowReporter()
     * @param workflowUUID
     */
    public WorkflowReporter(String workflowUUID){
    	this.folderID = workflowUUID;
        this.reportOutputFolder = initOutputFolder();
    }

    private String getTimeStamp() {
        return System.currentTimeMillis()+"";
    }

    private String initOutputFolder() {
        return (JBOSS_HOME != null ? JBOSS_HOME + WEE_DATA : LOCAL) + "/id-" + folderID;
    }

    /**
     * @param message The message
     * @param level The level
     * @param t The throwable
     */
    void reportIfStructured(final Object message, final Level level, final Throwable t) {
        if (message instanceof Message) {
            builder.append(message(level, message, t));
        }
    }

    /**
     * @return The report assembled during logging
     */
    String reportAsString() {
        String content = builder.toString();
        InputStream stream = this.getClass().getResourceAsStream(TEMPLATE);
        String template;
        try {
            template = IOUtils.toString(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String result = template.replace(CONTENT_MARKER, content);
        return result;
    }

    /**
     * @return The file the HTML report has been written to
     */
    File reportAsFile() {
        File file = new File(reportOutputFolder, REPORT_HTML);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(reportAsString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return file;
    }

    private String message(final Level level, final Object message, final Throwable t) {
        if (!(message instanceof Message)) {
            throw new IllegalArgumentException("Need a ReportingLog.Message instance!");
        }
        Message m = (Message) message;
        String result = String.format(ENTRY, m.title, level.color, content(m.values));
        /* If we have a throwable, add info about that: */
        if (t != null) {
            result += String.format(ENTRY, "Problems", level.color, t.getLocalizedMessage());
        }
        return result;
    }

    private String content(final Parameter[] values) {
        StringBuilder builder = new StringBuilder();
        for (Parameter parameter : values) {
            builder.append(String.format(CONTENT, parameter.getName(), parameter.getValue()));
        }
        return builder.toString();
    }
    
    /**
     * @see WorkflowReporter#getResultsId() 
     */
    @Deprecated
    public String getTime(){
    	return this.folderID;
    }
    
    /**
     * Identifier for the workflow reporter's output. (formerly used as getTime()
     * @return
     */
    public String getResultsId(){
    	return this.folderID;
    }
    
}
