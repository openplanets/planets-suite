package eu.planets_project.ifr.core.services.characterisation.extractor.impl;

import org.junit.BeforeClass;

import eu.planets_project.ifr.core.services.characterisation.extractor.impl.XcdlExtractor;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.datatypes.MigrationPath;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.utils.FileUtils;
import eu.planets_project.services.utils.test.ServiceCreator;

public class XcdlExtractorServerTest extends XcdlExtractorLocalTest {
	
	/**
     * Set up the testing environment: create files and directories for testing.
     */
    @BeforeClass
    public static void setup() {
    	System.out.println("*************************");
    	System.out.println("* Running SERVER tests: *");
    	System.out.println("*************************");
    	System.out.println();
    	System.setProperty("pserv.test.context", "server");
        System.setProperty("pserv.test.host", "localhost");
        System.setProperty("pserv.test.port", "8080");
    	
    	TEST_OUT = XcdlExtractorUnitHelper.XCDL_EXTRACTOR_SERVER_TEST_OUT;
    	
    	testOutFolder = FileUtils.createWorkFolderInSysTemp(TEST_OUT);
        
        extractor = ServiceCreator.createTestService(Migrate.QNAME, XcdlExtractor.class, WSDL);
        
        migrationPaths = extractor.describe().getPaths().toArray(new MigrationPath[]{});
    }

}
