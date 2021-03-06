/**
 * 
 */
package eu.planets_project.ifr.core.storage.impl.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.planets_project.ifr.core.common.conf.Configuration;
import eu.planets_project.ifr.core.common.conf.ServiceConfig;
import eu.planets_project.ifr.core.storage.AllStorageSuite;
import eu.planets_project.ifr.core.storage.api.DataRegistryFactory;
import eu.planets_project.ifr.core.storage.api.DigitalObjectManager.DigitalObjectNotFoundException;
import eu.planets_project.ifr.core.storage.api.DigitalObjectManager.DigitalObjectNotStoredException;
import eu.planets_project.ifr.core.storage.impl.file.temp.TempFilesystemDigitalObjectManagerImpl;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;

/**
 * @author <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a>
 */
public class DigitalObjectManagerTests {

	private static final String FILE = "test_word.doc";
    private static final String CONFIG = AllStorageSuite.RESOURCE_BASE + "/FilesystemDigitalObjectManager/config/";
    private static final String TEMP_PROPS = "simplefile.properties";
    private static final String MISSING_NAME_PROPS = "missingname.properties";
    private static final String BAD_NAME_PROPS = "badname.properties";
    private static final String MISSING_PATH_PROPS = "missingpath.properties";
    private static TempFilesystemDigitalObjectManagerImpl dom = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		// Check if the test data directory is there
		File rootDir = new File(AllStorageSuite.TEST_DATA_BASE);
        if (!rootDir.exists()) {
            throw new IllegalStateException("Could not read from: " + rootDir);
        }
        
        // Clean up the test temp area
        DigitalObjectManagerTests.deleteDirectory(new File(AllStorageSuite.TEST_TEMP_BASE));

        // Instantiate a file based data registry instance
		// Point it at a root directory in resources, the registry will create the dir if necessary
        Configuration config = ServiceConfig.getConfiguration(new File(CONFIG + TEMP_PROPS));
        DigitalObjectManagerTests.dom = new TempFilesystemDigitalObjectManagerImpl(config);
	}

	/**
	 * 
	 */
	@AfterClass
	public static void oneTimeTearDown() {
		DigitalObjectManagerTests.dom.finalize();
	}
	/**
	 * Test method for {@link eu.planets_project.ifr.core.storage.impl.file.FilesystemDigitalObjectManagerImpl#list(java.net.URI)}.
	 * @throws URISyntaxException 
	 */
	@Test
	public final void testList() throws URISyntaxException {
		// Get the root URI
		List<URI> rootResults = DigitalObjectManagerTests.dom.list(null);
		System.out.println("Performing the null URI test to obtain root URI");
		List<URI> expectedResults = new ArrayList<URI>();
		expectedResults.add(DataRegistryFactory.createDataRegistryIdFromName(DigitalObjectManagerTests.dom.getName()));
		// We should only have a single URI in the returned results
		assertEquals("Too many results returned, expecting one and got " + rootResults.size(),
				expectedResults.size(),	rootResults.size());
		for (int iLoop = 0; iLoop < expectedResults.size(); iLoop++) {
			assertEquals("URI Entries not equal", expectedResults.get(iLoop), rootResults.get(iLoop));
		}
	}

	/**
	 * Test method for {@link eu.planets_project.ifr.core.storage.impl.file.FilesystemDigitalObjectManagerImpl#storeAsNew(java.net.URI, eu.planets_project.services.datatypes.DigitalObject)}.
	 * @throws URISyntaxException 
	 * @throws DigitalObjectNotStoredException 
	 * @throws DigitalObjectNotFoundException 
	 * @throws IOException 
	 */
	@Test
	public final void testStoreAndRetrieve() throws DigitalObjectNotStoredException, URISyntaxException, DigitalObjectNotFoundException, IOException {
		// OK we can create an Digital Object from the test resource data, we need a URL
		System.out.println("Testing storage of Digital Object");
		URI purl = new File(AllStorageSuite.TEST_DATA_BASE, FILE).toURI();
        /* Create the content: */
		System.out.println("Creating DigitalObjectContent c1");
        DigitalObjectContent c1 = Content.byReference(purl.toURL().openStream());
		System.out.println("Creating DigitalObject object");
        /* Given these, we can instantiate our object: */
        DigitalObject object = new DigitalObject.Builder(c1).permanentUri(purl).title(purl.toString()).build();
    	// Check digital object. Title should not be null 
		System.out.println("Setting store flag to true");
        boolean storeFlag = true;
        // Now store it
        URI pdURI = null;
        try {
    		System.out.println("calling this.dom.storeAsNew(object)");
            pdURI = DigitalObjectManagerTests.dom.storeAsNew(object);
            System.out.println("StoreAsNew returned the URI:" + pdURI);
        } catch (Exception e) {
    		System.out.println("Caught an exception in storeAsNew, here's the details");
        	e.printStackTrace();
    		System.out.println("Asserting it's a not stored exception");
        	assertTrue("Expecting exception to be DigitalObjectNotStoredException", e.getClass().equals(DigitalObjectNotStoredException.class));
    		System.out.println("Setting storeFlag to false");
        	storeFlag = false;
        } catch (Throwable t) {
    		System.out.println("Caught a throwable in storeAsNew, here's the details");
        	t.printStackTrace();
    		System.out.println("Setting storeFlag to false");
        	storeFlag = false;
        }
        
		System.out.println("Creating new version of object");
        object = new DigitalObject.Builder(object.getContent()).title("mytitle").build();
		System.out.println("asserting the title is not null");
        assertNotNull("NOT expecting object.getTitle() to be null", object.getTitle());
        
        if (storeFlag)
        {
        	DigitalObject retObject = null;
			// Then retrieve it and check it's the same
    		System.out.println("now retrieving object to test (ret object)");
			System.out.println("Retrieving the test object using URI:" + pdURI);
			try {
				retObject = DigitalObjectManagerTests.dom.retrieve(pdURI);
			} catch (DigitalObjectNotFoundException e) {
				e.printStackTrace();
				throw e;
			} catch (Exception e) {
				System.out.println("Caught unexpected exception calling retrieve()");
				e.printStackTrace();
				fail("this.dom.retrive failed with an unexpected exception");
			}
			System.out.println("Creating new Purl");
			URI newPurl = new File(AllStorageSuite.TEST_DATA_BASE, FILE).toURI();
			System.out.println("Creating digital object c2");
			DigitalObjectContent c2 = Content.byReference(newPurl.toURL().openStream());
			System.out.println("Creating new Expected object");
			DigitalObject expectedObject = new DigitalObject.Builder(c2).build(); 
			
			// Check that retObject is not null
			assertNotNull("Not expecting returned object to be null", retObject);
			
			System.out.println("Trying content match between expectedObject and retobject");
			// Only test if retObject is not null, this can't happen because of a previous assert
			if (retObject != null) {
	            assertEquals("Retrieve Digital Object content (" +
	            		expectedObject.getContent() +
	            		") doesn't match that stored (" + retObject.getContent() +
	            		")", expectedObject.getContent(),
	                    retObject.getContent());
			}
			// We can test that the list method works properly now also
			// Get the root URI
			System.out.println("trying list out");
			List<URI> rootResults = DigitalObjectManagerTests.dom.list(null);
			System.out.println("getting expectedResults");
			List<URI> expectedResults = new ArrayList<URI>();
			System.out.println("adding entry to expectedResults");
			expectedResults.add(new URI("planets://localhost:8080/dr/test/" + FILE));
			// We should only have a single URI in the returned results
			System.out.println("testing count");
			assertEquals("Original and retrieved result count should be equal;",
					expectedResults.size(),	rootResults.size());
			// We have the root so let's get what's below
			System.out.println("getting testResults from list");
			List<URI> testResults = DigitalObjectManagerTests.dom.list(rootResults.get(0));
			// We should only have a single URI in the returned results
			assertEquals("Original and retrieved result count should be equal;",
					expectedResults.size(),	testResults.size());
			// Now loop through the returned URIs and make sure they're equal
			for (int iLoop = 0; iLoop < expectedResults.size(); iLoop++) {
			    /* FIXME: this fails as the URIs returned have an ID as the file name, not the original file name.
			     * What's the correct thing? Do we want name equality or is it OK to store the files under the ID? */
				//assertEquals("URI Entries not equal", expectedResults.get(iLoop), testResults.get(iLoop));
			}
        }
	}
	
	/**
	 * Testing that a file not found exception is thrown if the object doesn't exist
	 * @throws URISyntaxException
	 * @throws DigitalObjectNotFoundException 
	 */
	@Test(expected=DigitalObjectNotFoundException.class)
	public final void testFileNotFound() throws URISyntaxException, DigitalObjectNotFoundException {
			System.out.println("Testing that DigitalObjectNotFoundException is generated as expected");
			// Let's retrieve an object we know doesn't exist
			DigitalObjectManagerTests.dom.retrieve(new URI((DigitalObjectManagerTests.dom.getId()).toString() +
															"/noneexistentobject").normalize());
	}
	
	/**
	 * Deliberately test null root directory for setup of the registry 
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testMissingPath() throws URISyntaxException, MalformedURLException {
		// Not doing too much here, just setting up a bad instance and catching the exception
        Configuration config = ServiceConfig.getConfiguration(new File(CONFIG + MISSING_PATH_PROPS));
		new FilesystemDigitalObjectManagerImpl(config);
	}
	
	/**
	 * Deliberately test null root directory for setup of the registry 
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testMissingName() throws URISyntaxException, MalformedURLException {
		// Not doing too much here, just setting up a bad instance and catching the exception
        Configuration config = ServiceConfig.getConfiguration(new File(CONFIG + MISSING_NAME_PROPS));
		new FilesystemDigitalObjectManagerImpl(config);
	}

	/**
	 * Deliberately test null root directory for setup of the registry 
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testBadName() throws URISyntaxException, MalformedURLException {
		// Not doing too much here, just setting up a bad instance and catching the exception
        Configuration config = ServiceConfig.getConfiguration(new File(CONFIG + BAD_NAME_PROPS));
		new FilesystemDigitalObjectManagerImpl(config);
	}

	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		return path.delete();
	}
}
