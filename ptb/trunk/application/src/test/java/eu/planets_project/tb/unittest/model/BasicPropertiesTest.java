package eu.planets_project.tb.unittest.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import eu.planets_project.tb.api.TestbedManager;
import eu.planets_project.tb.api.model.BasicProperties;
import eu.planets_project.tb.api.model.Experiment;
import eu.planets_project.tb.api.model.ExperimentSetup;
import eu.planets_project.tb.api.model.finals.TestbedRoles;
import eu.planets_project.tb.impl.TestbedManagerImpl;
import eu.planets_project.tb.impl.model.BasicPropertiesImpl;
import eu.planets_project.tb.impl.model.ExperimentImpl;
import eu.planets_project.tb.impl.model.ExperimentSetupImpl;
import eu.planets_project.tb.test.model.SetupBasicPropertiesRemote;
import eu.planets_project.tb.api.model.finals.ExperimentTypes;

import junit.framework.TestCase;

/**
 * @author alindley
 * This class represents a JUnittest for the BasicProperties Object, testing all of its
 * bean's setter's and getters as well as producing required input parameters for the tests.
 * 
 * The method getBasicPropertiesSample() can be used from outside to retrieve a sample BasicProperties object
 *
 */
public class BasicPropertiesTest extends TestCase{
	
	private long propID1, propID2;
	
	protected void setUp(){

	}
	
	public void testConsiderations(){
		BasicProperties props = new BasicPropertiesImpl();
		String sConsiderations = "Consideration1";
		props.setConsiderations(sConsiderations);
		
		assertEquals(sConsiderations, props.getConsiderations());
	}
	
	
	public void testContact(){
		//Test: setContact
		BasicProperties props = new BasicPropertiesImpl();
		props.setContact("Name", "Mail@yahoo.com", "+431585", "Thurngasse 8, 1090 Wien");
		
		assertEquals("Thurngasse 8, 1090 Wien", props.getContactAddress());
		assertEquals("Mail@yahoo.com", props.getContactMail());
		assertEquals("Name", props.getContactName());
		assertEquals("+431585", props.getContactTel());
	}
	
	
	public void testExperimentApproach(){
		BasicProperties props = new BasicPropertiesImpl();
		props.setExperimentApproach(ExperimentTypes.EXPERIMENT_TYPE_SIMPLEMIGRATION);
		assertEquals(ExperimentTypes.EXPERIMENT_TYPE_SIMPLEMIGRATION, props.getExperimentApproach());
	
		int iType = ExperimentTypes.EXPERIMENT_TYPE_MIGRATIONWORKFLOW;
		props.setExperimentApproach(iType);
		assertEquals("EXPERIMENT_TYPE_MIGRATIONWORKFLOW", props.getExperimentApproachName(iType));
	}
	
	
	public void testExperimenter(){
		BasicProperties props = new BasicPropertiesImpl();
		props.setExperimenter("TestUser1");
		
		assertEquals("TestUser1",props.getExperimenter());
		
	}
	
	
	public void testExperimentedObjectType(){
		BasicProperties props = new BasicPropertiesImpl();
	//Test1:
		props.setExperimentedObjectType("text/plain");
		List<String> vTypes = new Vector<String>();
		vTypes = props.getExperimentedObjectTypes();
		assertEquals(1,vTypes.size());
		assertTrue(vTypes.contains("text/plain"));
	
	//Test2:
		vTypes = new Vector<String>();
		vTypes.add("text/plain");
		vTypes.add("text/plain");
		vTypes.add("text/html");
		vTypes.add("image/gif");
		props.setExperimentedObjectTypes(vTypes);
		vTypes = props.getExperimentedObjectTypes();
		assertEquals(3,vTypes.size());
		assertTrue(vTypes.contains("text/html"));
		assertTrue(vTypes.contains("image/gif"));
		
	//Test3:
		vTypes = new Vector<String>();
		vTypes.add("text/html");
		vTypes.add("image\\gif");
		props.setExperimentedObjectTypes(vTypes);
		vTypes = props.getExperimentedObjectTypes();
		assertEquals(1,vTypes.size());
		assertTrue(vTypes.contains("text/html"));
		assertTrue(!vTypes.contains("image\\gif"));
		
	}
	
	public void testExperimentReference(){
		BasicProperties props = new BasicPropertiesImpl();
		
		//Test1:
		long l1 = 123;
		props.addExperimentReference(l1);

		assertTrue(props.getExperimentReferences().contains(l1));
		assertEquals(1,props.getExperimentReferences().size());

		//Test2:
		long l2 = 234;
		props.addExperimentReference(l2);
		assertTrue(props.getExperimentReferences().contains(l1));
		assertTrue(props.getExperimentReferences().contains(l2));
		assertEquals(2,props.getExperimentReferences().size());

		//Test3: 1/4
		TestbedManager manager = TestbedManagerImpl.getInstance();
		Experiment exp = manager.createNewExperiment();
		props.setExperimentReference(exp);

		assertTrue(manager.containsExperiment(exp.getEntityID()));
		assertEquals(1, props.getExperimentReferences().size());
		assertTrue(props.getExperimentReferences().contains(exp.getEntityID()));
		manager.removeExperiment(exp.getEntityID());

		//Test3: 2/4
		Experiment exp2 = manager.createNewExperiment();
		props.setExperimentReference(exp2.getEntityID());

		assertTrue(manager.containsExperiment(exp2.getEntityID()));
		assertEquals(1, props.getExperimentReferences().size());
		assertTrue(props.getExperimentReferences().contains(exp2.getEntityID()));
		manager.removeExperiment(exp2.getEntityID());

		//Test3: 3/4
		Experiment exp3 = manager.createNewExperiment();
		Experiment exp4 = manager.createNewExperiment();
		Experiment[] exparray = {exp3, exp4};
		props.setExperimentReferences(exparray);

		assertTrue(manager.containsExperiment(exp3.getEntityID()));
		assertTrue(manager.containsExperiment(exp4.getEntityID()));
		assertEquals(2, props.getExperimentReferences().size());
		assertTrue(props.getExperimentReferences().contains(exp3.getEntityID()));
		assertTrue(props.getExperimentReferences().contains(exp4.getEntityID()));
		manager.removeExperiment(exp3.getEntityID());
		manager.removeExperiment(exp4.getEntityID());

		//Test: 4/4
		List<Long> refIDs = new Vector<Long>();
		Experiment exp5 = manager.createNewExperiment();
		Experiment exp6 = manager.createNewExperiment();
		refIDs.add(exp5.getEntityID());
		refIDs.add(exp6.getEntityID());
		props.setExperimentReferences(refIDs);

		assertTrue(manager.containsExperiment(exp5.getEntityID()));
		assertTrue(manager.containsExperiment(exp6.getEntityID()));
		assertEquals(2, props.getExperimentReferences().size());
		assertTrue(props.getExperimentReferences().contains(exp5.getEntityID()));
		assertTrue(props.getExperimentReferences().contains(exp6.getEntityID()));
		
		//Test: 5
		HashMap<Long,Experiment> refs = (HashMap<Long,Experiment>)props.getReferencedExperiments();
		assertEquals(2, props.getReferencedExperimentIDs().size());
		assertTrue(props.getReferencedExperimentIDs().contains(exp5.getEntityID()));
		assertTrue(refs.get(exp5.getEntityID()).equals(exp5));
		manager.removeExperiment(exp5.getEntityID());
		manager.removeExperiment(exp6.getEntityID());
		
		
	}
	
	
	public void testInvolvedUsers(){
		BasicProperties props = new BasicPropertiesImpl();
		//Test1:
		props.addInvolvedUser("TestUser1");
		props.addInvolvedUser("TestUser2");
		
		assertEquals(2, props.getInvolvedUserIds().size());
		assertTrue(props.getInvolvedUserIds().contains("TestUser1"));
		
		//Test2:
		List<String> userIDs = new Vector<String>();
		userIDs.add("TestUser3");
		userIDs.add("TestUser4");
		userIDs.add("TestUser4");
		props.addInvolvedUsers(userIDs);

		assertEquals(4, props.getInvolvedUserIds().size());
		assertTrue(props.getInvolvedUserIds().contains("TestUser1"));
		assertTrue(props.getInvolvedUserIds().contains("TestUser3"));
		
		//Test3:
		props.removeInvolvedUser("TestUser1");
		
		assertEquals(3, props.getInvolvedUserIds().size());
		assertTrue(!props.getInvolvedUserIds().contains("TestUser1"));
		assertTrue(props.getInvolvedUserIds().contains("TestUser3"));

		//Test4:
		List<String> userIDs2 = new Vector<String>();
		userIDs2.add("TestUser3");
		userIDs2.add("TestUser3");
		props.removeInvolvedUsers(userIDs2);

		assertEquals(2, props.getInvolvedUserIds().size());
		assertTrue(props.getInvolvedUserIds().contains("TestUser2"));
		assertTrue(props.getInvolvedUserIds().contains("TestUser4"));
		assertTrue(!props.getInvolvedUserIds().contains("TestUser3"));
	}
	
	
	public void testExperimentNameUnique(){
		TestbedManager manager = TestbedManagerImpl.getInstance();
		
		BasicProperties props = new BasicPropertiesImpl();
		ExperimentSetup expSetup = new ExperimentSetupImpl();
		Experiment exp1 = new ExperimentImpl();
		
		long expID = manager.registerExperiment(exp1);
		exp1 = manager.getExperiment(expID);

		String sTestname= "TestName12334234445";
		boolean bUnique = manager.isExperimentNameUnique(sTestname);
		//check if the two methods deliver the same results
		assertEquals(bUnique, props.checkExperimentNameUnique(sTestname));
		
		props.setExperimentName(sTestname);
		assertEquals(sTestname,props.getExperimentName());
		expSetup.setBasicProperties(props);
		exp1.setExperimentSetup(expSetup);
		manager.updateExperiment(exp1);
		
		assertEquals(false, props.checkExperimentNameUnique(sTestname));
		assertEquals(false, manager.isExperimentNameUnique(sTestname));
		
		//clean up the mess
		manager.removeExperiment(exp1.getEntityID());
	}
	

	public void testExternalReferenceID(){
		BasicProperties props = new BasicPropertiesImpl();
		props.setExternalReferenceID("LocalSystem://refID1");
		assertEquals("LocalSystem://refID1", props.getExternalReferenceID());
	}
	
	public void testFocus(){
		BasicProperties props = new BasicPropertiesImpl();
		String sFocus = "Focus is on testing speed of jpeg->tiff migration tool";
		props.setFocus(sFocus);
		assertEquals(sFocus, props.getFocus());
	}
	
	public void testScope(){
		BasicProperties props = new BasicPropertiesImpl();
		String sScope = "Not tested is the outputquality";
		props.setScope(sScope);
		assertEquals(sScope, props.getScope());
	}
	
	public void testPurpose(){
		BasicProperties props = new BasicPropertiesImpl();
		String sPurpose ="Find the best migration tool supporting PDF/A";
		props.setScope(sPurpose);
		assertEquals(sPurpose, props.getScope());
	}
	
	public void testSpecificFocus(){
		BasicProperties props = new BasicPropertiesImpl();
		String sSpecificFocus = "A more detailed description";
		props.setSpecificFocus(sSpecificFocus);
		assertEquals(sSpecificFocus, props.getSpecificFocus());
	}
	
	public void testIndication(){
		BasicProperties props = new BasicPropertiesImpl();
		String sIndication = "A overall description";
		props.setIndication(sIndication);
		assertEquals(sIndication, props.getIndication());
	}
	
	public void testSummary(){
		BasicProperties props = new BasicPropertiesImpl();
		String sSummary = "Summary of the experiment";
		props.setSummary(sSummary);
		assertEquals(sSummary, props.getSummary());
	}
	
	public void testExternalReferences(){
		
	}
	

	
	public BasicProperties getBasicPropertiesSample(){
		//TODO impl
		return null;
	}
	
	
	protected void tearDown(){

	}

}