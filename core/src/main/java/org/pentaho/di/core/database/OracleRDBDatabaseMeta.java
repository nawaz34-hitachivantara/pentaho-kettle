/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Oracle RDB specific information through static final members
 *
 * @author Matt
 * @since 27-jul-2006
 */

public class OracleRDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    return false;
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
   */
  @Override
  public String getLimitClause( int nrRows ) {
    return " WHERE ROWNUM <= " + nrRows;
  }

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName
   *          The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName + " WHERE 1=0";
  }

  @Override
  public String getSQLTableExists( String tablename ) {
    return getSQLQueryFields( tablename );
  }

  @Override
  public String getSQLColumnExists( String columnname, String tablename ) {
    return getSQLQueryColumnFields( columnname, tablename );
  }

  public String getSQLQueryColumnFields( String columnname, String tableName ) {
    return "SELECT " + columnname + " FROM " + tableName + " WHERE 1=0";
  }

  @Override
  public String getDriverClass() {
    return "oracle.rdb.jdbc.rdbThin.Driver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    return "jdbc:rdbThin://" + hostname + ":" + port + "/" + databaseName;
  }

  /**
   * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
   */
  @Override
  public boolean supportsOptionsInURL() {
    return false;
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return true;
  }

  @Override
  public String getSQLListOfSequences() {
    return "SELECT SEQUENCE_NAME FROM USER_SEQUENCES";
  }

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    return "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" + sequenceName.toUpperCase() + "'";
  }

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "SELECT " + sequenceName + ".currval FROM DUAL";
  }

  /**
   * Get the SQL to get the next value of a sequence. (Oracle only)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle only)
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "SELECT " + sequenceName + ".nextval FROM dual";
  }

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  @Override
  public boolean useSchemaNameForTableList() {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return true;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE "
      + tablename + " ADD ( " + getFieldDefinition( v, tk, pk, useAutoinc, true, false ) + " ) ";
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " DROP ( " + v.getName() + " ) " + Const.CR;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE "
      + tablename + " MODIFY (" + getFieldDefinition( v, tk, pk, useAutoinc, true, false ) + " )";
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    StringBuilder retval = new StringBuilder( 128 );

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( addFieldName ) {
      retval.append( fieldname ).append( ' ' );
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_DATE:
        retval.append( "DATE" );
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval.append( "CHAR(1)" );
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        retval.append( "NUMBER" );
        if ( length > 0 ) {
          retval.append( '(' ).append( length );
          if ( precision > 0 ) {
            retval.append( ", " ).append( precision );
          }
          retval.append( ')' );
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length >= DatabaseMeta.CLOB_LENGTH ) {
          retval.append( "CLOB" );
        } else {
          if ( length > 0 && length <= 2000 ) {
            retval.append( "VARCHAR2(" ).append( length ).append( ')' );
          } else {
            if ( length <= 0 ) {
              retval.append( "VARCHAR2(2000)" ); // We don't know, so we just use the maximum...
            } else {
              retval.append( "CLOB" );
            }
          }
        }
        break;
      default:
        retval.append( " UNKNOWN" );
        break;
    }

    if ( addCr ) {
      retval.append( Const.CR );
    }

    return retval.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {
      "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN", "BY", "CHAR",
      "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE", "DECIMAL",
      "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT", "FOR",
      "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
      "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
      "MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
      "ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
      "RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
      "SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO",
      "TRIGGER", "UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2",
      "VIEW", "WHENEVER", "WHERE", "WITH" };
  }

  @Override
  public String getSQLLockTables( String[] tableNames ) {
    StringBuilder sql = new StringBuilder( 128 );
    for ( int i = 0; i < tableNames.length; i++ ) {
      sql.append( "LOCK TABLE " ).append( tableNames[i] ).append( " IN EXCLUSIVE MODE;" ).append( Const.CR );
    }
    return sql.toString();
  }

  @Override
  public String getSQLUnlockTables( String[] tableNames ) {
    return null; // commit handles the unlocking!
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "rdbthin.jar" };
  }

}
