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

package org.pentaho.di.trans.steps.pgbulkloader;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.postgresql.copy.PGCopyOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BOOLEAN;

public class PGBulkLoaderTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private StepMockHelper<PGBulkLoaderMeta, PGBulkLoaderData> stepMockHelper;
  private PGBulkLoader pgBulkLoader;

  private static final String CONNECTION_NAME = "PSQLConnect";
  private static final String CONNECTION_DB_NAME = "test1181";
  private static final String CONNECTION_DB_HOST = "localhost";
  private static final String CONNECTION_DB_PORT = "5093";
  private static final String CONNECTION_DB_USERNAME = "postgres";
  private static final String CONNECTION_DB_PASSWORD = "password";
  private static final String DB_NAME_OVVERRIDE = "test1181_2";
  private static final String DB_NAME_EMPTY = "";

  private static final String PG_TEST_CONNECTION =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<connection> <name>" + CONNECTION_NAME + "</name><server>" + CONNECTION_DB_HOST + "</server><type>POSTGRESQL</type><access>Native</access><database>" + CONNECTION_DB_NAME + "</database>"
          + "  <port>" + CONNECTION_DB_PORT + "</port><username>" + CONNECTION_DB_USERNAME + "</username><password>Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde</password></connection>";

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<>( "PostgreSQL Bulk Loader", PGBulkLoaderMeta.class, PGBulkLoaderData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    pgBulkLoader = new PGBulkLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta, stepMockHelper.trans );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testCreateCommandLine() throws Exception {
    PGBulkLoaderMeta meta = mock( PGBulkLoaderMeta.class );
    doReturn( new DatabaseMeta() ).when( meta ).getDatabaseMeta();
    doReturn( new String[0] ).when( meta ).getFieldStream();
    PGBulkLoaderData data = mock( PGBulkLoaderData.class );

    PGBulkLoader spy = spy( pgBulkLoader );
    doReturn( new Object[0] ).when( spy ).getRow();
    doReturn( "" ).when( spy ).getCopyCommand();
    doNothing().when( spy ).connect();
    doNothing().when( spy ).checkClientEncoding();
    doNothing().when( spy ).processTruncate();
    spy.processRow( meta, data );
    verify( spy ).processTruncate();
  }

  @Test
  public void testDBNameOverridden_IfDbNameOverrideSetUp() throws Exception {
    // Db Name Override is set up
    PGBulkLoaderMeta pgBulkLoaderMock = getPgBulkLoaderMock( DB_NAME_OVVERRIDE );
    Database database = pgBulkLoader.getDatabase( pgBulkLoader, pgBulkLoaderMock );
    assertNotNull( database );
    // Verify DB name is overridden
    assertEquals( DB_NAME_OVVERRIDE, database.getDatabaseMeta().getDatabaseName() );
    // Check additionally other connection information
    assertEquals( CONNECTION_NAME, database.getDatabaseMeta().getName() );
    assertEquals( CONNECTION_DB_HOST, database.getDatabaseMeta().getHostname() );
    assertEquals( CONNECTION_DB_PORT, database.getDatabaseMeta().getDatabasePortNumberString() );
    assertEquals( CONNECTION_DB_USERNAME, database.getDatabaseMeta().getUsername() );
    assertEquals( CONNECTION_DB_PASSWORD, database.getDatabaseMeta().getPassword() );
  }

  @Test
  public void testDBNameNOTOverridden_IfDbNameOverrideEmpty() throws Exception {
    // Db Name Override is empty
    PGBulkLoaderMeta pgBulkLoaderMock = getPgBulkLoaderMock( DB_NAME_EMPTY );
    Database database = pgBulkLoader.getDatabase( pgBulkLoader, pgBulkLoaderMock );
    assertNotNull( database );
    // Verify DB name is NOT overridden
    assertEquals( CONNECTION_DB_NAME, database.getDatabaseMeta().getDatabaseName() );
    // Check additionally other connection information
    assertEquals( CONNECTION_NAME, database.getDatabaseMeta().getName() );
    assertEquals( CONNECTION_DB_HOST, database.getDatabaseMeta().getHostname() );
    assertEquals( CONNECTION_DB_PORT, database.getDatabaseMeta().getDatabasePortNumberString() );
    assertEquals( CONNECTION_DB_USERNAME, database.getDatabaseMeta().getUsername() );
    assertEquals( CONNECTION_DB_PASSWORD, database.getDatabaseMeta().getPassword() );
  }

  @Test
  public void testDBNameNOTOverridden_IfDbNameOverrideNull() throws Exception {
    // Db Name Override is null
    PGBulkLoaderMeta pgBulkLoaderMock = getPgBulkLoaderMock( null );
    Database database = pgBulkLoader.getDatabase( pgBulkLoader, pgBulkLoaderMock );
    assertNotNull( database );
    // Verify DB name is NOT overridden
    assertEquals( CONNECTION_DB_NAME, database.getDatabaseMeta().getDatabaseName() );
    // Check additionally other connection information
    assertEquals( CONNECTION_NAME, database.getDatabaseMeta().getName() );
    assertEquals( CONNECTION_DB_HOST, database.getDatabaseMeta().getHostname() );
    assertEquals( CONNECTION_DB_PORT, database.getDatabaseMeta().getDatabasePortNumberString() );
    assertEquals( CONNECTION_DB_USERNAME, database.getDatabaseMeta().getUsername() );
    assertEquals( CONNECTION_DB_PASSWORD, database.getDatabaseMeta().getPassword() );
  }

  @Test
  public void testProcessRow_StreamIsNull() throws Exception {
    PGBulkLoader pgBulkLoaderStreamIsNull = mock( PGBulkLoader.class );
    doReturn( null ).when( pgBulkLoaderStreamIsNull ).getRow();
    PGBulkLoaderMeta meta = mock( PGBulkLoaderMeta.class );
    PGBulkLoaderData data = mock( PGBulkLoaderData.class );
    assertEquals( false, pgBulkLoaderStreamIsNull.processRow( meta, data ) );
  }

  /**
   * [PDI-17481] Testing the ability that if no connection is specified, we will mark it as a fail and log the
   * appropriate reason to the user by throwing a KettleException.
   */
  @Test
  public void testNoDatabaseConnection() {
    try {
      doReturn( null ).when( stepMockHelper.initStepMetaInterface ).getDatabaseMeta();
      assertFalse( pgBulkLoader.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface ) );
      // Verify that the database connection being set to null throws a KettleException with the following message.
      pgBulkLoader.verifyDatabaseConnection();
      // If the method does not throw a Kettle Exception, then the DB was set and not null for this test. Fail it.
      fail( "Database Connection is not null, this fails the test." );
    } catch ( KettleException aKettleException ) {
      assertThat( aKettleException.getMessage(), containsString( "There is no connection defined in this step." ) );
    }
  }

  @Test
  public void writeBooleanToPgOutput() throws Exception {
    final ByteArrayOutputStream out = initPGCopyOutputStream();

    pgBulkLoader.init( initMeta( "tested value" ), initData() );
    final RowMeta rowMeta = initRowMeta( "tested value", TYPE_BOOLEAN );

    pgBulkLoader.writeRowToPostgres( rowMeta, new Object[] {true} );
    assertEquals( "true", "1" + Const.CR, out.toString() );

    out.reset();
    pgBulkLoader.writeRowToPostgres( rowMeta, new Object[] {false} );
    assertEquals( "false", "0" + Const.CR, out.toString() );
  }

  private ByteArrayOutputStream initPGCopyOutputStream() throws IOException, NoSuchFieldException, IllegalAccessException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PGCopyOutputStream pgCopy = mock( PGCopyOutputStream.class );
    doAnswer( invocation -> {
      out.write( (byte[]) invocation.getArguments()[0] );
      return null;
    } ).when( pgCopy ).write( any() );
    final Field pgCopyOut = pgBulkLoader.getClass().getDeclaredField( "pgCopyOut" );
    pgCopyOut.setAccessible( true );
    pgCopyOut.set( pgBulkLoader, pgCopy );
    return out;
  }

  private RowMeta initRowMeta( String valueName, int type ) throws KettlePluginException {
    final RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( ValueMetaFactory.createValueMeta( valueName, type ) );
    return rowMeta;
  }

  private PGBulkLoaderMeta initMeta( String valueName ) throws KettleXMLException {
    final PGBulkLoaderMeta meta = getPgBulkLoaderMock( null );
    when( meta.getFieldStream() ).thenReturn( new String[] {valueName} );
    when( meta.getDateMask() ).thenReturn( new String[] {""} );
    return meta;
  }

  private PGBulkLoaderData initData() {
    final PGBulkLoaderData data = new PGBulkLoaderData();
    data.quote = "\"".getBytes();
    data.separator = ";".getBytes();
    data.newline = "\n".getBytes();
    data.keynrs = new int[] {0};
    return data;
  }

  private static PGBulkLoaderMeta getPgBulkLoaderMock( String DbNameOverride ) throws KettleXMLException {
    PGBulkLoaderMeta pgBulkLoaderMetaMock = mock( PGBulkLoaderMeta.class );
    when( pgBulkLoaderMetaMock.getDbNameOverride() ).thenReturn( DbNameOverride );
    DatabaseMeta databaseMeta = getDatabaseMetaSpy();
    when( pgBulkLoaderMetaMock.getDatabaseMeta() ).thenReturn( databaseMeta );
    return pgBulkLoaderMetaMock;
  }

  private static DatabaseMeta getDatabaseMetaSpy() throws KettleXMLException {
    DatabaseMeta databaseMeta = spy( new DatabaseMeta( PG_TEST_CONNECTION ) );
    return databaseMeta;
  }
}
