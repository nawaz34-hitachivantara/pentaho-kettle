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


package org.pentaho.di.trans.steps.monetdbbulkloader;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;


import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * User: dgriffen Date: 12/04/2018
 */
public class MonetDBBulkLoaderTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private StepMockHelper<MonetDBBulkLoaderMeta, MonetDBBulkLoaderData> stepMockHelper;
  private MonetDBBulkLoader monetDbBulkLoader;

  @BeforeClass
  public static void initEnvironment() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<>( "MonetDB Bulk Loader", MonetDBBulkLoaderMeta.class,
      MonetDBBulkLoaderData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    monetDbBulkLoader = new MonetDBBulkLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface,
      0, stepMockHelper.transMeta, stepMockHelper.trans );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  /**
   * [PDI-17481] Testing the ability that if no connection is specified, we will mark it as a fail and log the
   * appropriate reason to the user by throwing a KettleException.
   */
  @Test
  public void testNoDatabaseConnection() {
    try {
      doReturn( null ).when( stepMockHelper.initStepMetaInterface ).getDatabaseMeta();
      assertFalse( monetDbBulkLoader.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface ) );
      // Verify that the database connection being set to null throws a KettleException with the following message.
      monetDbBulkLoader.verifyDatabaseConnection();
      // If the method does not throw a Kettle Exception, then the DB was set and not null for this test. Fail it.
      fail( "Database Connection is not null, this fails the test." );
    } catch ( KettleException aKettleException ) {
      assertThat( aKettleException.getMessage(), containsString( "There is no connection defined in this step." ) );
    }
  }
}
