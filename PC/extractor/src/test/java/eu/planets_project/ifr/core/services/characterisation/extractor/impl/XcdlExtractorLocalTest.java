package eu.planets_project.ifr.core.services.characterisation.extractor.impl;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.services.characterisation.extractor.impl.XcdlExtractor;
import eu.planets_project.ifr.core.techreg.api.formats.Format;
import eu.planets_project.ifr.core.techreg.api.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.api.formats.FormatRegistryFactory;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.FileFormatProperty;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Parameters;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.migration.soxservices.SoXTestsHelper;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.FileUtils;

/**
 * Test of the extractor (local and remote) using binaries. TODO: clean up both
 * local and in the data registry after the tests
 * @author Peter Melms
 * @author Fabian Steeg
 */
public class XcdlExtractorLocalTest {

    /***/
    static final String WSDL = "/pserv-pc-extractor/XcdlExtractor?wsdl";
    /***/
    static String xcelString;
    /***/
    static File outputXcdl;
    /***/
    static byte[] binary;
    
    /**
     * the service
     */
    static Migrate extractor;
    
    static String TEST_OUT  = null;
    
    static MigrationPath[] migrationPaths;
    
    static File testOutFolder = null;

    /**
     * Set up the testing environment: create files and directories for testing.
     */
    @BeforeClass
    public static void setup() {
    	System.out.println("************************");
    	System.out.println("* Running LOCAL tests: *");
    	System.out.println("************************");
    	System.out.println();
    	System.setProperty("pserv.test.context", "local");
    	
    	TEST_OUT = XcdlExtractorUnitHelper.XCDL_EXTRACTOR_LOCAL_TEST_OUT;
    	
    	testOutFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUT);
        
        extractor = new XcdlExtractor();
        
        migrationPaths = extractor.describe().getPaths().toArray(new MigrationPath[]{});
    }
    
    /**
     * test describe method
     */
    @Test
    public void testDescribe() {
    	ServiceDescription sd = extractor.describe();
    	System.out.println("test: describe()");
    	System.out.println("--------------------------------------------------------------------");
    	System.out.println();
    	System.out.println("Received ServiceDescription from: " + extractor.getClass().getName());
    	System.out.println(sd.toXmlFormatted());
    	System.out.println("--------------------------------------------------------------------");
    	assertTrue("The ServiceDescription should not be NULL.", sd != null );
    }
    
    @Test
		public void testAllPossibleMigrationPathways() {
    		System.out.println("Testing all possible pathways...START");
			for(int i=0;i<migrationPaths.length;i++) {
				MigrationPath path = migrationPaths[i];
				URI inputFormat = path.getInputFormat();
				URI outputFormat = path.getOutputFormat();
				
				System.out.println();
				System.out.println("Testing migrationPath: [" + inputFormat.toASCIIString() + " --> " + outputFormat.toASCIIString() + "]");
				System.out.println();
				
				System.out.println("PARAMS: disableNormData = FALSE, enableRawData = FALSE, XCEL = YES");
				Parameters parameters = createParameters(false, false, getTestXCEL(getFormatExtension(inputFormat)));
				testMigrate(inputFormat, outputFormat, parameters);
				System.out.println("*******************");
				System.out.println();
				
				System.out.println("PARAMS: disableNormData = FALSE, enableRawData = FALSE, XCEL = NO");
				parameters = createParameters(false, false, null);
				testMigrate(inputFormat, outputFormat, parameters);
				System.out.println("*******************");
				System.out.println();
				
				System.out.println("PARAMS: disableNormData = TRUE, enableRawData = FALSE, XCEL = YES");
				parameters = createParameters(true, false, getTestXCEL(getFormatExtension(inputFormat)));
				testMigrate(inputFormat, outputFormat, parameters);
				System.out.println("*******************");
				System.out.println();
				
				System.out.println("PARAMS: disableNormData = TRUE, enableRawData = FALSE, XCEL = NO");
				parameters = createParameters(true, false, null);
				testMigrate(inputFormat, outputFormat, parameters);
				System.out.println("*******************");
				System.out.println();
			}
			System.out.println("Testing all possible pathways...END");
		}

	private Parameters createParameters(boolean disableNormDataFlag, boolean enableRawDataFlag, String optionalXCELString) {
    	List<Parameter> parameterList = new ArrayList<Parameter>();
        
    	if(disableNormDataFlag) {
    		Parameter normDataFlag = new Parameter("disableNormDataInXCDL", "-n");
            normDataFlag.setDescription("Disables NormData output in result XCDL. Reduces file size. Allowed value: '-n'");
            parameterList.add(normDataFlag);
    	}
    	
        if(enableRawDataFlag) {
        	Parameter enableRawData = new Parameter("enableRawDataInXCDL", "-r");
            enableRawData.setDescription("Enables the output of RAW Data in XCDL file. Allowed value: '-r'");
            parameterList.add(enableRawData);
        }
        
        if(optionalXCELString!=null) {
        	Parameter xcelStringParam = new Parameter("optionalXCELString", optionalXCELString);
            xcelStringParam.setDescription("Could contain an optional XCEL String which is passed to the Extractor tool.\n\r" +
            		"If no XCEL String is passed, the Extractor tool will try to  find the corresponding XCEL himself.");
            parameterList.add(xcelStringParam);
        }
        
        Parameters parameters = new Parameters();
        parameters.setParameters(parameterList);
        
        return parameters;
    }
	
private String getTestXCEL (String srcExtension) {
    	if (srcExtension.equalsIgnoreCase("TIFF")) {
    		return FileUtils.readTxtFileIntoString(XcdlExtractorUnitHelper.TIFF_XCEL);
		}
    	
    	if (srcExtension.equalsIgnoreCase("BMP")) {
    		return FileUtils.readTxtFileIntoString(XcdlExtractorUnitHelper.BMP_XCEL);
		}
    	
    	if (srcExtension.equalsIgnoreCase("GIF")) {
    		return FileUtils.readTxtFileIntoString(XcdlExtractorUnitHelper.GIF_XCEL);
		}
    	
    	if (srcExtension.equalsIgnoreCase("PDF")) {
    		return FileUtils.readTxtFileIntoString(XcdlExtractorUnitHelper.PDF_XCEL);
		}
    	
    	if (srcExtension.equalsIgnoreCase("JPEG") || srcExtension.equalsIgnoreCase("JPG")) {
    		return FileUtils.readTxtFileIntoString(XcdlExtractorUnitHelper.JPEG_XCEL);
		}
    	
    	if (srcExtension.equalsIgnoreCase("PNG")) {
    		return FileUtils.readTxtFileIntoString(XcdlExtractorUnitHelper.PNG_XCEL);
		}
    	
//    	if (srcExtension.equalsIgnoreCase("DOC")) {
//    		return XcdlExtractorUnitHelper.DOC_INPUT;
//		}
    	return null;
    }
	
	private File getTestFile(String srcExtension) {
    	
    	if (srcExtension.equalsIgnoreCase("TIFF")) {
    		return XcdlExtractorUnitHelper.TIFF_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("BMP")) {
    		return XcdlExtractorUnitHelper.BMP_INPUT;
		}
    	
    	
    	if (srcExtension.equalsIgnoreCase("GIF")) {
    		return XcdlExtractorUnitHelper.GIF_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("PDF")) {
    		return XcdlExtractorUnitHelper.PDF_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("JPEG") || srcExtension.equalsIgnoreCase("JPG")) {
    		return XcdlExtractorUnitHelper.JPEG_INPUT;
		}
    	
    	if (srcExtension.equalsIgnoreCase("PNG")) {
    		return XcdlExtractorUnitHelper.PNG_INPUT;
		}
    	
//    	if (srcExtension.equalsIgnoreCase("DOC")) {
//    		return XcdlExtractorUnitHelper.DOC_INPUT;
//		}
    	return null;
    }
    
    private DigitalObject createDigitalObject(String srcExtension) {
		
		File inputFile = getTestFile(srcExtension);
    	
        DigitalObject input = null;
        
		try {
			input = new DigitalObject.Builder(Content.byValue(inputFile)).permanentUrl(new URL("http://xcdlExtractorMigrationTest.eu")).build();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return input;
	}
    
    private void testMigrate(URI inputFormat, URI outputFormat, Parameters parameters) {
    	String extension = getFormatExtension(inputFormat);
		DigitalObject digObj = createDigitalObject(extension);
		
		MigrateResult mr = extractor.migrate(digObj, inputFormat, outputFormat, parameters);

		ServiceReport sr = mr.getReport();
		
		if(sr.getErrorState()==1) {
			System.err.println("FAILED: " + sr);
		}
		else {
			System.out.println("Got Report: " + sr);
        
			DigitalObject doOut = mr.getDigitalObject();

			assertTrue("Resulting digital object is null.", doOut != null);
			
			File formatFolder = FileUtils.createFolderInWorkFolder(testOutFolder, extension);
			
			File result = FileUtils.writeInputStreamToFile(doOut.getContent().read(), formatFolder, "xcdlExtractorTest_" + extension + ".xcdl");
			
			System.out.println("Resulting file size: " + result.length() + " KB.");
			System.out.println("Resulting file path: " + result.getAbsolutePath());

			System.out.println("Result: " + doOut);
			System.out.println("Result.content: " + doOut.getContent());
			System.out.println("Result.content.isByValue: " + doOut.getContent().isByValue());
			System.out.println("Result.content.getReference: " + doOut.getContent().getReference());
		}
	}
    
    private String getFormatExtension (URI formatURI) {
		Format f = new Format(formatURI);
		String extension = null;
		if(Format.isThisAnExtensionURI(formatURI)) {
			extension = f.getExtensions().iterator().next(); 
		}
		else {
			FormatRegistry formatRegistry = FormatRegistryFactory.getFormatRegistry();
			Format fileFormat = formatRegistry.getFormatForURI(formatURI);
			Set <String> extensions = fileFormat.getExtensions();
			if(extensions != null){
				Iterator <String> iterator = extensions.iterator();
				extension = iterator.next();
			}
		}
		return extension;
	}
    
    
    
    private URI getUriForFile (File testFile) {
    	String fileName = testFile.getAbsolutePath();
    	String testFileExtension = null;
    	if(fileName.contains(".")) {
    		testFileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
    	}
    	else {
    		System.err.println("Could not find file extension!!!");
    		return null;
    	}
    	return Format.extensionToURI(testFileExtension);
    }

    @SuppressWarnings("unused")
	private DigitalObject createDigitalObjectByReference(URL permanentURL, URL reference) {
		DigitalObject digObj =  new DigitalObject.Builder(Content.byReference(reference)).build();
		return digObj;
	}
}
