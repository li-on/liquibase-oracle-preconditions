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
import liquibase.precondition.core.ColumnExistsPrecondition;

public class OracleColumnExistsPrecondition extends OraclePrecondition<ColumnExistsPrecondition> {

	private String columnName;
	private String tableName;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName( String columnName ) {
		this.columnName = columnName;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName( String tableName ) {
		this.tableName = tableName;
	}
	
	@Override
	public String getName() {
		return "oracleColumnExists";
	}
	
	@Override
	protected ColumnExistsPrecondition fallback( Database database ) {
		ColumnExistsPrecondition redirect = new ColumnExistsPrecondition();
		redirect.setCatalogName( getCatalogName() );
		redirect.setSchemaName( getSchemaName() );
		redirect.setColumnName( getColumnName() );
		redirect.setTableName( getTableName() );
		return redirect;
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
				final String sql = "select count(*) from all_tab_cols where upper(column_name) = upper(?) and upper(table_name) = upper(?) and upper(owner) = upper(?)";
				ps = connection.prepareStatement( sql );
				ps.setString( 1, getColumnName() );
				ps.setString( 2, getTableName() );
				ps.setString( 3, getSchemaName() );
				rs = ps.executeQuery();
				if ( !rs.next() || rs.getInt( 1 ) <= 0 ) {
					throw new PreconditionFailedException( String.format( "The column '%s.%s.%s' was not found.", getSchemaName(), getTableName(), getColumnName()), changeLog, this );
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
    	this.tableName = parsedNode.getChildValue(null, "tableName", String.class);
		this.columnName = parsedNode.getChildValue(null, "columnName", String.class);
	}
	
}
