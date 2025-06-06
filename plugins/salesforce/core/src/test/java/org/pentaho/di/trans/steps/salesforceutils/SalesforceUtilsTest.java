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


package org.pentaho.di.trans.steps.salesforceutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class SalesforceUtilsTest {

  private static LogChannelInterface logMock;

  private String inputFieldName;

  private String expectedFieldName;

  @Before
  public void setUp() {
    logMock = mock( LogChannelInterface.class );
  }

  @Test
  public void testNoInstances() {
    Constructor<?>[] methods = SalesforceUtils.class.getConstructors();
    for ( int i = 0; i < methods.length; i++ ) {
      assertFalse( methods[i].isAccessible() );
    }
  }

  @Test
  public void testFieldWithExtIdYes_StandartObject() {
    inputFieldName = "Account:ExtID_AccountId__c/Account";
    expectedFieldName = "AccountId";
    String fieldToNullName = SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    assertEquals( expectedFieldName, fieldToNullName );
  }

  @Test
  public void testFieldWithExtIdNo_StandartObject() {
    inputFieldName = "AccountId";
    expectedFieldName = "AccountId";
    String fieldToNullName = SalesforceUtils.getFieldToNullName( logMock, inputFieldName, false );
    assertEquals( expectedFieldName, fieldToNullName );
  }

  @Test
  public void testFieldWithExtIdYes_CustomObject() {
    inputFieldName = "ParentObject__c:Name/ParentObjectId__r";
    expectedFieldName = "ParentObjectId__c";
    String fieldToNullName = SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    assertEquals( expectedFieldName, fieldToNullName );
  }

  @Test
  public void testFieldWithExtIdNo_CustomObject() {
    inputFieldName = "ParentObjectId__c";
    expectedFieldName = "ParentObjectId__c";
    String fieldToNullName = SalesforceUtils.getFieldToNullName( logMock, inputFieldName, false );
    assertEquals( expectedFieldName, fieldToNullName );
  }

  @Test
  public void testFieldWithExtIdYesButNameInIncorrectSyntax_StandartObject() {
    when( logMock.isDebug() ).thenReturn( true );
    inputFieldName = "Account";
    expectedFieldName = inputFieldName;
    String fieldToNullName = SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    assertEquals( expectedFieldName, fieldToNullName );
  }

  @Test
  public void testIncorrectExternalKeySyntaxWarnIsLoggedInDebugMode() {
    when( logMock.isDebug() ).thenReturn( true );
    inputFieldName = "AccountId";
    verify( logMock, never() ).logDebug( anyString() );
    SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    verify( logMock ).logDebug(
        "The field has incorrect external key syntax: AccountId. Syntax for external key should be : object:externalId/lookupField. Trying to use fieldToNullName=AccountId." );
  }

  @Test
  public void testIncorrectExternalKeySyntaxWarnIsNotLoggedInNotDebugMode() {
    when( logMock.isDebug() ).thenReturn( false );
    inputFieldName = "AccountId";
    verify( logMock, never() ).logDebug( anyString() );
    SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    verify( logMock, never() ).logDebug(
        "The field has incorrect external key syntax: AccountId. Syntax for external key should be : object:externalId/lookupField. Trying to use fieldToNullName=AccountId." );
  }

  @Test
  public void testFinalNullFieldNameIsLoggedInDebugMode_StandartObject() {
    when( logMock.isDebug() ).thenReturn( true );
    inputFieldName = "Account:ExtID_AccountId__c/Account";
    verify( logMock, never() ).logDebug( anyString() );
    SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    verify( logMock ).logDebug( "fieldToNullName=AccountId" );
  }

  @Test
  public void testFinalNullFieldNameIsLoggedInDebugMode_CustomObject() {
    when( logMock.isDebug() ).thenReturn( true );
    inputFieldName = "ParentObject__c:Name/ParentObjectId__r";
    verify( logMock, never() ).logDebug( anyString() );
    SalesforceUtils.getFieldToNullName( logMock, inputFieldName, true );
    verify( logMock ).logDebug( "fieldToNullName=ParentObjectId__c" );
  }

}
