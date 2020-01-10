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
import liquibase.precondition.Precondition;
import liquibase.resource.ResourceAccessor;

public class OracleCheckConstraintExistsPrecondition extends OraclePrecondition<Precondition> {

	private String constraintName;
	private String tableName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName( String tableName ) {
		this.tableName = tableName;
	}

	public String getName() {
		return "oracleCheckConstraintExists";
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName( String constraintName ) {
		this.constraintName = constraintName;
	}

	@Override
	protected Precondition fallback( Database database ) {
		throw new AbstractMethodError( getName().concat( " precondition is currently not compatible with migration to other databases." ) );
	}
	
	public Warnings warn( Database database ) {
		Precondition redirect = redirected( database );
		if ( redirect == null ) {
			return new Warnings();
		} else {
			return redirect.warn( database );
		}
	}

	public ValidationErrors validate( Database database ) {
		Precondition redirect = redirected( database );
		if ( redirect == null ) {
			return new ValidationErrors();
		} else {
			return redirect.validate( database );
		}
	}

	public void check( Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener ) throws PreconditionFailedException, PreconditionErrorException {
		Precondition redirect = redirected( database );
		if ( redirect == null ) {
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
				
				final String sql = "select count(*) from all_constraints where upper(constraint_name) = upper(?) and table_name = upper(?) and upper(owner) = upper(?) and constraint_type = 'C'";
				ps = connection.prepareStatement( sql );
				ps.setString( 1, getConstraintName() );
				ps.setString( 2, getTableName() );
				ps.setString( 3, getSchemaName() );
				rs = ps.executeQuery();
				if ( !rs.next() || rs.getInt( 1 ) <= 0 ) {
					throw new PreconditionFailedException( String.format( "The primary key '%s' was not found on the table '%s.%s'.", getConstraintName(), getSchemaName(), getTableName() ), changeLog, this );
				}
			} catch ( SQLException e ) {
				throw new PreconditionErrorException( e, changeLog, this );
			} catch ( DatabaseException e ) {
				throw new PreconditionErrorException( e, changeLog, this );
			} finally {
				closeSilently( rs );
				closeSilently( ps );
			}
		} else {
			redirect.check( database, changeLog, changeSet, changeExecListener );
		}
	}

	@Override
	public void load( ParsedNode parsedNode, ResourceAccessor resourceAccessor ) throws ParsedNodeException {
		super.load( parsedNode, resourceAccessor );
		this.constraintName = parsedNode.getChildValue(null, "constraintName", String.class);
    this.tableName = parsedNode.getChildValue(null, "tableName", String.class);
	}
}