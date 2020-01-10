package liquibase.ext.oracle.preconditions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Precondition;
import liquibase.resource.ResourceAccessor;

public abstract class OraclePrecondition<R extends Precondition> implements Precondition {

	protected R redirect = null;
	protected String schemaName;
	protected String catalogName;
	
	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName( String schemaName ) {
		this.schemaName = schemaName;
	}
	
	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName( String catalogName ) {
		this.catalogName = catalogName;
	}
		
	void closeSilently( PreparedStatement ps ) {
		if ( ps != null ) {
			try {
				ps.close();
			} catch ( SQLException e ) {
			}
		}
	}

	void closeSilently( ResultSet rs ) {
		if ( rs != null ) {
			try {
				rs.close();
			} catch ( SQLException e ) {
			}
		}
	}

	public void check( Database database, DatabaseChangeLog changeLog, ChangeSet changeSet ) throws PreconditionFailedException, PreconditionErrorException {
		check( database, changeLog, changeSet, null );
	}
	
	public void load( ParsedNode parsedNode, ResourceAccessor resourceAccessor ) throws ParsedNodeException {
		this.schemaName = parsedNode.getChildValue(null, "schemaName", String.class);
		this.catalogName = parsedNode.getChildValue(null, "catalogName", String.class);
		if(this.schemaName==null || this.schemaName==""){
			if(this.catalogName!=null && this.catalogName!=""){
				this.schemaName=this.catalogName;
			}
		}
	}

	public String getSerializedObjectName() {
		return null;
	}

	public Set<String> getSerializableFields() {
		return null;
	}

	public Object getSerializableFieldValue( String field ) {
		return null;
	}

	public SerializationType getSerializableFieldType( String field ) {
		return null;
	}

	public String getSerializableFieldNamespace( String field ) {
		return null;
	}

	public String getSerializedObjectNamespace() {
		return null;
	}

	public ParsedNode serialize() throws ParsedNodeException {
		return null;
	}
	
	protected R redirected( Database database ) {
		if ( ! ( database instanceof OracleDatabase ) ) {
			if (redirect == null) {
				redirect = fallback( database );
			}
		}
		return redirect;
	}
	
	protected abstract R fallback( Database database );
}
