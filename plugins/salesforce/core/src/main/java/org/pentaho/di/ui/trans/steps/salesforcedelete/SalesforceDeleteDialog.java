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


package org.pentaho.di.ui.trans.steps.salesforcedelete;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforce.SalesforceStep;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.di.trans.steps.salesforcedelete.SalesforceDeleteMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.ComponentSelectionListener;
import org.pentaho.di.ui.trans.steps.salesforce.SalesforceStepDialog;

import java.util.ArrayList;

public class SalesforceDeleteDialog extends SalesforceStepDialog {

  private static Class<?> PKG = SalesforceDeleteMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wGeneralTab;

  private Composite wGeneralComp;

  private FormData fdGeneralComp;

  private FormData fdlModule, fdModule;

  private FormData fdlDeleteField, fdDeleteField;

  private FormData fdlBatchSize, fdBatchSize;

  private FormData fdUserName, fdURL, fdPassword;

  private Label wlModule, wlBatchSize;

  private Label wlDeleteField;

  private SalesforceDeleteMeta input;

  private LabelTextVar wUserName, wURL, wPassword;

  private TextVar wBatchSize;

  private ComboVar wModule;

  private ComboVar wDeleteField;

  private Button wTest;

  private FormData fdTest;
  private Listener lsTest;

  private Group wConnectionGroup;
  private FormData fdConnectionGroup;

  private Group wSettingsGroup;
  private FormData fdSettingsGroup;

  private boolean gotPrevious = false;

  private boolean gotModule = false;

  private boolean getModulesListError = false; /* True if error getting modules list */

  private Label wlUseCompression;
  private FormData fdlUseCompression;
  private Button wUseCompression;
  private FormData fdUseCompression;

  private Label wlTimeOut;
  private FormData fdlTimeOut;
  private TextVar wTimeOut;
  private FormData fdTimeOut;

  private Label wlRollbackAllChangesOnError;
  private FormData fdlRollbackAllChangesOnError;
  private Button wRollbackAllChangesOnError;
  private FormData fdRollbackAllChangesOnError;

  public SalesforceDeleteDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, in, transMeta, sname );
    input = (SalesforceDeleteMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    ModifyListener lsTableMod = new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        input.setChanged();
      }
    };

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.DialogTitle" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF FILE TAB ///
    // ////////////////////////
    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.General.Tab" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ///////////////////////////////
    // START OF Connection GROUP //
    // ///////////////////////////////

    wConnectionGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wConnectionGroup );
    wConnectionGroup.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.ConnectionGroup.Label" ) );

    FormLayout connectionGroupLayout = new FormLayout();
    connectionGroupLayout.marginWidth = 10;
    connectionGroupLayout.marginHeight = 10;
    wConnectionGroup.setLayout( connectionGroupLayout );

    // Webservice URL
    wURL = new LabelTextVar( transMeta, wConnectionGroup,
      BaseMessages.getString( PKG, "SalesforceDeleteDialog.URL.Label" ),
      BaseMessages.getString( PKG, "SalesforceDeleteDialog.URL.Tooltip" ) );
    props.setLook( wURL );
    wURL.addModifyListener( lsMod );
    fdURL = new FormData();
    fdURL.left = new FormAttachment( 0, 0 );
    fdURL.top = new FormAttachment( wStepname, margin );
    fdURL.right = new FormAttachment( 100, 0 );
    wURL.setLayoutData( fdURL );

    // UserName line
    wUserName = new LabelTextVar( transMeta, wConnectionGroup,
      BaseMessages.getString( PKG, "SalesforceDeleteDialog.User.Label" ),
      BaseMessages.getString( PKG, "SalesforceDeleteDialog.User.Tooltip" ) );
    props.setLook( wUserName );
    wUserName.addModifyListener( lsMod );
    fdUserName = new FormData();
    fdUserName.left = new FormAttachment( 0, 0 );
    fdUserName.top = new FormAttachment( wURL, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wPassword = new LabelTextVar( transMeta, wConnectionGroup,
      BaseMessages.getString( PKG, "SalesforceDeleteDialog.Password.Label" ),
      BaseMessages.getString( PKG, "SalesforceDeleteDialog.Password.Tooltip" ), true );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // Test Salesforce connection button
    wTest = new Button( wConnectionGroup, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.TestConnection.Label" ) );
    props.setLook( wTest );
    fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.TestConnection.Tooltip" ) );
    fdTest.top = new FormAttachment( wPassword, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    fdConnectionGroup = new FormData();
    fdConnectionGroup.left = new FormAttachment( 0, margin );
    fdConnectionGroup.top = new FormAttachment( wStepname, margin );
    fdConnectionGroup.right = new FormAttachment( 100, -margin );
    wConnectionGroup.setLayoutData( fdConnectionGroup );

    // ///////////////////////////////
    // END OF Connection GROUP //
    // ///////////////////////////////

    // ///////////////////////////////
    // START OF Settings GROUP //
    // ///////////////////////////////

    wSettingsGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wSettingsGroup );
    wSettingsGroup.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.SettingsGroup.Label" ) );

    FormLayout settingGroupLayout = new FormLayout();
    settingGroupLayout.marginWidth = 10;
    settingGroupLayout.marginHeight = 10;
    wSettingsGroup.setLayout( settingGroupLayout );

    // Timeout
    wlTimeOut = new Label( wSettingsGroup, SWT.RIGHT );
    wlTimeOut.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.TimeOut.Label" ) );
    props.setLook( wlTimeOut );
    fdlTimeOut = new FormData();
    fdlTimeOut.left = new FormAttachment( 0, 0 );
    fdlTimeOut.top = new FormAttachment( wSettingsGroup, margin );
    fdlTimeOut.right = new FormAttachment( middle, -margin );
    wlTimeOut.setLayoutData( fdlTimeOut );
    wTimeOut = new TextVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTimeOut );
    wTimeOut.addModifyListener( lsMod );
    fdTimeOut = new FormData();
    fdTimeOut.left = new FormAttachment( middle, 0 );
    fdTimeOut.top = new FormAttachment( wSettingsGroup, margin );
    fdTimeOut.right = new FormAttachment( 100, 0 );
    wTimeOut.setLayoutData( fdTimeOut );

    // Use compression?
    wlUseCompression = new Label( wSettingsGroup, SWT.RIGHT );
    wlUseCompression.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.UseCompression.Label" ) );
    props.setLook( wlUseCompression );
    fdlUseCompression = new FormData();
    fdlUseCompression.left = new FormAttachment( 0, 0 );
    fdlUseCompression.top = new FormAttachment( wTimeOut, margin );
    fdlUseCompression.right = new FormAttachment( middle, -margin );
    wlUseCompression.setLayoutData( fdlUseCompression );
    wUseCompression = new Button( wSettingsGroup, SWT.CHECK );
    props.setLook( wUseCompression );
    wUseCompression
      .setToolTipText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.UseCompression.Tooltip" ) );
    fdUseCompression = new FormData();
    fdUseCompression.left = new FormAttachment( middle, 0 );
    fdUseCompression.top = new FormAttachment( wTimeOut, margin );
    wUseCompression.setLayoutData( fdUseCompression );
    wUseCompression.addSelectionListener( new ComponentSelectionListener( input ) );

    // Rollback all changes on error?
    wlRollbackAllChangesOnError = new Label( wSettingsGroup, SWT.RIGHT );
    wlRollbackAllChangesOnError.setText( BaseMessages.getString(
      PKG, "SalesforceDeleteDialog.RollbackAllChangesOnError.Label" ) );
    props.setLook( wlRollbackAllChangesOnError );
    fdlRollbackAllChangesOnError = new FormData();
    fdlRollbackAllChangesOnError.left = new FormAttachment( 0, 0 );
    fdlRollbackAllChangesOnError.top = new FormAttachment( wUseCompression, margin );
    fdlRollbackAllChangesOnError.right = new FormAttachment( middle, -margin );
    wlRollbackAllChangesOnError.setLayoutData( fdlRollbackAllChangesOnError );
    wRollbackAllChangesOnError = new Button( wSettingsGroup, SWT.CHECK );
    props.setLook( wRollbackAllChangesOnError );
    wRollbackAllChangesOnError.setToolTipText( BaseMessages.getString(
      PKG, "SalesforceDeleteDialog.RollbackAllChangesOnError.Tooltip" ) );
    fdRollbackAllChangesOnError = new FormData();
    fdRollbackAllChangesOnError.left = new FormAttachment( middle, 0 );
    fdRollbackAllChangesOnError.top = new FormAttachment( wUseCompression, margin );
    wRollbackAllChangesOnError.setLayoutData( fdRollbackAllChangesOnError );
    wRollbackAllChangesOnError.addSelectionListener( new ComponentSelectionListener( input ) );

    // BatchSize value
    wlBatchSize = new Label( wSettingsGroup, SWT.RIGHT );
    wlBatchSize.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.Limit.Label" ) );
    props.setLook( wlBatchSize );
    fdlBatchSize = new FormData();
    fdlBatchSize.left = new FormAttachment( 0, 0 );
    fdlBatchSize.top = new FormAttachment( wRollbackAllChangesOnError, margin );
    fdlBatchSize.right = new FormAttachment( middle, -margin );
    wlBatchSize.setLayoutData( fdlBatchSize );
    wBatchSize = new TextVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBatchSize );
    wBatchSize.addModifyListener( lsMod );
    fdBatchSize = new FormData();
    fdBatchSize.left = new FormAttachment( middle, 0 );
    fdBatchSize.top = new FormAttachment( wRollbackAllChangesOnError, margin );
    fdBatchSize.right = new FormAttachment( 100, 0 );
    wBatchSize.setLayoutData( fdBatchSize );

    // Module
    wlModule = new Label( wSettingsGroup, SWT.RIGHT );
    wlModule.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.Module.Label" ) );
    props.setLook( wlModule );
    fdlModule = new FormData();
    fdlModule.left = new FormAttachment( 0, 0 );
    fdlModule.top = new FormAttachment( wBatchSize, margin );
    fdlModule.right = new FormAttachment( middle, -margin );
    wlModule.setLayoutData( fdlModule );
    wModule = new ComboVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wModule.setEditable( true );
    props.setLook( wModule );
    wModule.addModifyListener( lsTableMod );
    fdModule = new FormData();
    fdModule.left = new FormAttachment( middle, 0 );
    fdModule.top = new FormAttachment( wBatchSize, margin );
    fdModule.right = new FormAttachment( 100, -margin );
    wModule.setLayoutData( fdModule );
    wModule.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
        getModulesListError = false;
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        // check if the URL and login credentials passed and not just had error
        if ( Utils.isEmpty( wURL.getText() )
          || Utils.isEmpty( wUserName.getText() ) || Utils.isEmpty( wPassword.getText() )
          || ( getModulesListError ) ) {
          return;
        }

        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getModulesList();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Salesforce Id Field
    wlDeleteField = new Label( wSettingsGroup, SWT.RIGHT );
    wlDeleteField.setText( BaseMessages.getString( PKG, "SalesforceDeleteDialog.KeyField.Label" ) );
    props.setLook( wlDeleteField );
    fdlDeleteField = new FormData();
    fdlDeleteField.left = new FormAttachment( 0, 0 );
    fdlDeleteField.top = new FormAttachment( wModule, margin );
    fdlDeleteField.right = new FormAttachment( middle, -margin );
    wlDeleteField.setLayoutData( fdlDeleteField );
    wDeleteField = new ComboVar( transMeta, wSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wDeleteField.setEditable( true );
    props.setLook( wDeleteField );
    wDeleteField.addModifyListener( lsMod );
    fdDeleteField = new FormData();
    fdDeleteField.left = new FormAttachment( middle, 0 );
    fdDeleteField.top = new FormAttachment( wModule, margin );
    fdDeleteField.right = new FormAttachment( 100, -margin );
    wDeleteField.setLayoutData( fdDeleteField );
    wDeleteField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        getPreviousFields();
      }
    } );

    fdSettingsGroup = new FormData();
    fdSettingsGroup.left = new FormAttachment( 0, margin );
    fdSettingsGroup.top = new FormAttachment( wConnectionGroup, margin );
    fdSettingsGroup.right = new FormAttachment( 100, -margin );
    wSettingsGroup.setLayoutData( fdSettingsGroup );

    // ///////////////////////////////
    // END OF Settings GROUP //
    // ///////////////////////////////

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( wStepname, margin );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, null );

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( wOK, -margin );
    wTabFolder.setLayoutData( fdTabFolder );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsTest = new Listener() {
      public void handleEvent( Event e ) {
        test();
      }
    };

    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wTest.addListener( SWT.Selection, lsTest );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    setSize();
    getData( input );
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void getPreviousFields() {
    if ( !gotPrevious ) {
      try {
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          String value = wDeleteField.getText();
          wDeleteField.removeAll();
          wDeleteField.setItems( r.getFieldNames() );
          if ( value != null ) {
            wDeleteField.setText( value );
          }
        }
      } catch ( KettleException ke ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "SalesforceDeleteDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "SalesforceDeleteDialog.FailedToGetFields.DialogMessage" ), ke );
      }
      gotPrevious = true;
    }
  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param in
   *          The SalesforceDeleteMeta object to obtain the data from.
   */
  public void getData( SalesforceDeleteMeta in ) {
    wURL.setText( Const.NVL( in.getTargetURL(), "" ) );
    wUserName.setText( Const.NVL( in.getUsername(), "" ) );
    wPassword.setText( Const.NVL( in.getPassword(), "" ) );
    wBatchSize.setText( in.getBatchSize() );
    wModule.setText( Const.NVL( in.getModule(), "Account" ) );
    if ( in.getDeleteField() != null ) {
      wDeleteField.setText( in.getDeleteField() );
    }
    wBatchSize.setText( "" + in.getBatchSize() );
    wTimeOut.setText( Const.NVL( in.getTimeout(), SalesforceConnectionUtils.DEFAULT_TIMEOUT ) );
    wUseCompression.setSelection( in.isCompression() );
    wRollbackAllChangesOnError.setSelection( in.isRollbackAllChangesOnError() );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    try {
      getInfo( input );
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "SalesforceDeleteDialog.ErrorValidateData.DialogTitle" ),
        BaseMessages.getString( PKG, "SalesforceDeleteDialog.ErrorValidateData.DialogMessage" ), e );
    }
    dispose();
  }

  @Override
  protected void getInfo( SalesforceStepMeta in ) throws KettleException {
    SalesforceDeleteMeta meta = (SalesforceDeleteMeta) in;
    stepname = wStepname.getText(); // return value

    // copy info to SalesforceDeleteMeta class (input)
    meta.setTargetURL( Const.NVL( wURL.getText(), SalesforceConnectionUtils.TARGET_DEFAULT_URL ) );
    meta.setUsername( wUserName.getText() );
    meta.setPassword( wPassword.getText() );
    meta.setModule( wModule.getText() );
    meta.setDeleteField( wDeleteField.getText() );
    meta.setBatchSize( wBatchSize.getText() );
    meta.setCompression( wUseCompression.getSelection() );
    meta.setTimeout( Const.NVL( wTimeOut.getText(), "0" ) );
    meta.setRollbackAllChangesOnError( wRollbackAllChangesOnError.getSelection() );
  }

  private void getModulesList() {
    if ( !gotModule ) {
      try {
        Trans trans = new Trans( transMeta, null );
        trans.rowsets = new ArrayList<>();

        getInfo( meta );
        SalesforceStep step = (SalesforceStep) meta.getStep( stepMeta, meta.getStepData(), 0, transMeta, trans );
        step.setStepMetaInterface( meta );
        String selectedField = wModule.getText();
        wModule.removeAll();
        wModule.setItems( step.getModules( "false" ) );

        if ( !Utils.isEmpty( selectedField ) ) {
          wModule.setText( selectedField );
        }

        gotModule = true;
        getModulesListError = false;

      } catch ( Exception e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "SalesforceDeleteDialog.ErrorRetrieveModules.DialogTitle" ),
          BaseMessages.getString( PKG, "SalesforceDeleteDialog.ErrorRetrieveData.ErrorRetrieveModules" ), e );
        getModulesListError = true;
      }
    }
  }
}
