package liquibase.ext.oracle.preconditions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class OracleUniqueConstraintExistsPrecondition extends OraclePrecondition {

	private String constraintName;
	private String tableName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName( String tableName ) {
		this.tableName = tableName;
	}

	public String getName() {
		return "oracleUniqueConstraintExists";
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName( String constraintName ) {
		this.constraintName = constraintName;
	}

	public Warnings warn( Database database ) {
		return new Warnings();
	}

	public ValidationErrors validate( Database database ) {
		return new ValidationErrors();
	}

	public void check( Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener ) throws PreconditionFailedException, PreconditionErrorException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			/*
				THE CONSTRAINT_TYPE will tell you what type of constraint it is
				
				R - Referential key ( foreign key)
				U - Unique key
				P - Primary key
				C - Check constraint
			 */
			
			final String sql = "select count(*) from all_constraints where upper(constraint_name) = upper(?) and table_name = upper(?) and upper(owner) = upper(?) and constraint_type = 'U'";
			ps = connection.prepareStatement( sql );
			ps.setString( 1, getConstraintName() );
			ps.setString( 2, getTableName() );
			ps.setString( 3, database.getLiquibaseSchemaName() );
			rs = ps.executeQuery();
			if ( !rs.next() || rs.getInt( 1 ) <= 0 ) {
				throw new PreconditionFailedException( String.format( "The primary key '%s' was not found on the table '%s.%s'.", getConstraintName(), database.getLiquibaseSchemaName(), getTableName() ), changeLog, this );
			}
		} catch ( SQLException e ) {
			throw new PreconditionErrorException( e, changeLog, this );
		} catch ( DatabaseException e ) {
			throw new PreconditionErrorException( e, changeLog, this );
		} finally {
			closeSilently( rs );
			closeSilently( ps );
		}
	}

	@Override
	public void load( ParsedNode parsedNode, ResourceAccessor resourceAccessor ) throws ParsedNodeException {
		super.load( parsedNode, resourceAccessor );
    this.tableName = parsedNode.getChildValue(null, "tableName", String.class);
    this.constraintName = parsedNode.getChildValue(null, "constraintName", String.class);
	}
}