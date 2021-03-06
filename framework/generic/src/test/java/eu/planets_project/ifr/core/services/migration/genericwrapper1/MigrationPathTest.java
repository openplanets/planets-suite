package eu.planets_project.ifr.core.services.migration.genericwrapper1;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import eu.planets_project.services.datatypes.Parameter;

/**
 * @author Thomas Skou Hansen &lt;tsh@statsbiblioteket.dk&gt;
 * 
 */
public class MigrationPathTest {


    // final URI sourceFormat = new URI("info:test/lowercase");
    // final URI destinationFormat = new URI("info:test/uppercase");

    /**
     * @throws Exception 
     */
    @SuppressWarnings("deprecation")
	@Test
    public void testSetGetCommandLine() throws Exception {
        final String originalCommandLine = "cat #param1 #tempSource > "
                + "#myInterimFile && tr #param2 #myInterimFile > "
                + "#tempDestination";

        // The first trivial test.
        final MigrationPath migrationPath = new MigrationPath();
        migrationPath.setCommandLine(Arrays.asList("/bin/sh","-c",originalCommandLine));
        Assert.assertEquals(
                "getCommandLine() did not return the value just set.",
                originalCommandLine, migrationPath.getCommandLine());

        final ArrayList<Parameter> toolParameters = new ArrayList<Parameter>();
        // Options for the 'cat' command
        toolParameters.add(new Parameter("param1", "-n"));
        // Options for the 'tr' command
        toolParameters.add(new Parameter("param2", "'[:lower:]' '[:upper:]'"));

        TempFile input = new TempFile("tempSource");
        input.setFile(new File("/random-source-name"));
        migrationPath.setTempInputFile(input);

        TempFile output = new TempFile("tempDestination");
        output.setFile(new File("/random-destination-name"));
        migrationPath.setTempOutputFile(output);

        TempFile tempfile = new TempFile("myInterimFile");
        tempfile.setFile(new File("/random-temp-file-name"));
        migrationPath.addTempFilesDeclaration(tempfile);

        final String executableCommandLine = migrationPath
                .getCommandLine(toolParameters).get(2);

        final String expectedCommandLine = "cat -n /random-source-name > "
                + "/random-temp-file-name && tr '[:lower:]' '[:upper:]' "
                + "/random-temp-file-name > " + "/random-destination-name";

        Assert.assertEquals("Un-expected output from getCommandLine().",
                expectedCommandLine, executableCommandLine);
    }
}
