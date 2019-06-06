package liquibase.ext.oracle.preconditions;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.PreconditionFailedException;
import liquibase.ext.oracle.preconditions.core.BaseTestCase;

public class OracleMaterializedViewExistsTest extends BaseTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPreconditionFailedForOtherDatabases() throws Exception {
		OracleMaterializedViewExistsPrecondition precondition = new OracleMaterializedViewExistsPrecondition();
		DatabaseChangeLog dbChangeLog = new DatabaseChangeLog();
		try {
			precondition.check( new MSSQLDatabase(), dbChangeLog, new ChangeSet( dbChangeLog ) );
			fail( "When running against MSSQL Server the concept of materialized views is false. Therefore a PreconditionFailedException should have been raised." );
		} catch ( PreconditionFailedException e ) {
		}
	}
}
