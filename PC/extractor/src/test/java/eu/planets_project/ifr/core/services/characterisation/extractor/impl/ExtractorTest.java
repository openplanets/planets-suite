package eu.planets_project.ifr.core.services.characterisation.extractor.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.services.characterise.BasicCharacteriseOneBinaryXCELtoBinary;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

/**
 * Test of the extractor (local and remote) using binaries. TODO: clean up both
 * local and in the data registry after the tests
 * @author Peter Melms
 * @author Fabian Steeg
 */
public final class ExtractorTest {

    /***/
    private static final String WSDL = "/pserv-pc-extractor/Extractor?wsdl";
    /***/
    private static String xcelString;
    /***/
    private static File outputXcdl;
    /***/
    private static byte[] binary;

    /**
     * Set up the testing environment: create files and directories for testing.
     */
    @BeforeClass
    public static void testCharacterise() {
    	System.setProperty("pserv.test.context", "Standalone");
//        System.setProperty("pserv.test.host", "localhost");
//        System.setProperty("pserv.test.port", "8080");
        
        File inputImage = new File(ExtractorUnitHelper.SAMPLE_FILE);
        File inputXcel = new File(ExtractorUnitHelper.SAMPLE_XCEL);
        File outputFolder = new File(ExtractorUnitHelper.EXTRACTOR_OUTPUT_DIR);
        
        boolean made = outputFolder.mkdir();
        if (!made && !outputFolder.exists()) {
            fail("Could not create directory: " + outputFolder);
        }
        
//        outputXcdl = new File(outputFolder, "client_output.xcdl");
        binary = ByteArrayHelper.read(inputImage);
        
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(inputXcel));
            StringBuffer sb = new StringBuffer();
            String in = "";
            while ((in = br.readLine()) != null) {
                sb.append(in);
            }
            xcelString = sb.toString();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Test using a local instance. 
     * @throws MalformedURLException */
    @Test
    public void testLocal() throws MalformedURLException {
        test(new Extractor());
    }

    /** Test using the web service running on local host. 
     * @throws MalformedURLException */
    @Test
    public void testRemote() throws MalformedURLException {
        Characterise characterise = ServiceCreator.createTestService(Characterise.QNAME, Extractor.class, WSDL);
        test(characterise);
    }

    /**
     * @param extractor The extractor instance to test
     * @throws MalformedURLException 
     */
    private void test(final Characterise extractor) throws MalformedURLException {
    	File testTmpFolder = FileUtils.createWorkFolderInSysTemp(ExtractorUnitHelper.EXTRACTOR_OUTPUT_DIR);
    	String outputFilePath = testTmpFolder.getAbsolutePath() + File.separator + "extractorTestOutput.xcdl";
    	String outputFilePath1 = testTmpFolder.getAbsolutePath() + File.separator + "extractorTestOutput1.xcdl";
    	String outputFilePath2 = testTmpFolder.getAbsolutePath() + File.separator + "extractorTestOutput2.xcdl";
    	String outputFilePath3 = testTmpFolder.getAbsolutePath() + File.separator + "extractorTestOutput3.xcdl";
    	String outputFilePath4 = testTmpFolder.getAbsolutePath() + File.separator + "extractorTestOutput4.xcdl";
    	
    	DigitalObject digitalObject = createDigitalObjectByValue(new URL("http://somePermamentURL"), binary);
    	
        /* find XCEL, no parameters*/
    	System.out.println("find XCEL, no parameters:");
        CharacteriseResult characteriseResult = extractor.characterise(digitalObject, null, null);
        outputXcdl = ByteArrayHelper.writeToDestFile(characteriseResult.getDigitalObject().getContent().getValue(), outputFilePath);
        byte[] resultData = characteriseResult.getDigitalObject().getContent().getValue();
        int fileSize = resultData.length / 1024;
        System.out.println("XCDL file size: " + fileSize + " KB");
        System.out.println("Find the XCDL here: " + outputXcdl.getAbsolutePath());
        System.out.println();
        System.out.println();
        assertTrue("No output file written;", outputXcdl.exists());
        
        /* give XCEL, no parameters*/
        System.out.println("give XCEL, no parameters:");
        characteriseResult = extractor.characterise(digitalObject, xcelString, null);
        outputXcdl = ByteArrayHelper.writeToDestFile(characteriseResult.getDigitalObject().getContent().getValue(), outputFilePath1);
        resultData = characteriseResult.getDigitalObject().getContent().getValue();
        fileSize = resultData.length / 1024;
        System.out.println("XCDL file size: " + fileSize + " KB");
        System.out.println("Find the XCDL here: " + outputXcdl.getAbsolutePath());
        System.out.println();
        System.out.println();
        assertTrue("No output file written;", outputXcdl.exists());
        
        /*give XCEL, give Parameter -r */
        System.out.println("find XCEL, give parameter: -r");
        Parameters parameters = new Parameters();
        parameters.add("enableRawDataInXCDL", "-r");
        characteriseResult = extractor.characterise(digitalObject, xcelString, parameters);
        outputXcdl = ByteArrayHelper.writeToDestFile(characteriseResult.getDigitalObject().getContent().getValue(), outputFilePath2);
        resultData = characteriseResult.getDigitalObject().getContent().getValue();
        fileSize = resultData.length / 1024;
        System.out.println("XCDL file size: " + fileSize + " KB");
        System.out.println("Find the XCDL here: " + outputXcdl.getAbsolutePath());
        System.out.println();
        System.out.println();
        assertTrue("No output file written;", outputXcdl.exists());
        
        
        /*give XCEL, give Parameters */
        System.out.println("give XCEL, parameters: -n, -r");
        parameters = new Parameters();
        parameters.add("disableNormDataInXCDL", "-n");
        parameters.add("enableRawDataInXCDL", "-r");
        characteriseResult = extractor.characterise(digitalObject, xcelString, parameters);
        outputXcdl = ByteArrayHelper.writeToDestFile(characteriseResult.getDigitalObject().getContent().getValue(), outputFilePath3);
        resultData = characteriseResult.getDigitalObject().getContent().getValue();
        fileSize = resultData.length / 1024;
        System.out.println("XCDL file size: " + fileSize + " KB");
        System.out.println("Find the XCDL here: " + outputXcdl.getAbsolutePath());
        System.out.println();
        System.out.println();
        assertTrue("No output file written;", outputXcdl.exists());
    }
    
    private DigitalObject createDigitalObjectByReference(URL permanentURL, URL reference) {
		DigitalObject digObj =  new DigitalObject.Builder(Content.byReference(reference)).build();
		return digObj;
	}
    
    private DigitalObject createDigitalObjectByValue(URL permanentURL, byte[] resultFileBlob) {
		DigitalObject digObj =  new DigitalObject.Builder(Content.byValue(resultFileBlob)).build();
		return digObj;
	}
}
