package simpledb.systemtest;

import org.junit.Before;

import simpledb.Database;

/**
 * Base class for all simpledb.SimpleDb simpledb.test classes.
 * @author nizam
 *
 */
public class SimpleDbTestBase {
	/**
	 * Reset the database before each simpledb.test is run.
	 */
	@Before	public void setUp() throws Exception {					
		Database.reset();
	}
	
}
