package eu.planets_project.ifr.core.sample;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.planets_project.ifr.core.sample.impl.PassThruMigrationServiceTest;
import eu.planets_project.ifr.core.sample.impl.SimpleIdentifyServiceTest;

/**
 * Suite to run all tests in the simple component.
 * 
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( { PassThruMigrationServiceTest.class,
        SimpleIdentifyServiceTest.class })
public class AllSimpleSuite {}
