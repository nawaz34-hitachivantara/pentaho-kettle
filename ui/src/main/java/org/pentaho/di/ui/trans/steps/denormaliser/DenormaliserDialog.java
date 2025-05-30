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


package org.pentaho.di.ui.trans.steps.denormaliser;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.denormaliser.DenormaliserMeta;
import org.pentaho.di.trans.steps.denormaliser.DenormaliserTargetField;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class DenormaliserDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = DenormaliserMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  public static final String STRING_SORT_WARNING_PARAMETER = "PivotSortWarning";

  private Label wlGroup;
  private TableView wGroup;
  private FormData fdlGroup, fdGroup;

  private Label wlTarget;
  private TableView wTarget;
  private FormData fdlTarget, fdTarget;

  private Label wlKeyField;
  private CCombo wKeyField;
  private FormData fdlKeyField, fdKeyField;

  private Button wGet, wGetAgg;
  private FormData fdGet, fdGetAgg;
  private Listener lsGet, lsGetAgg;

  private DenormaliserMeta input;

  private boolean gotPreviousFields = false;

  public DenormaliserDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (DenormaliserMeta) in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    backupChanged = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "DenormaliserDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "DenormaliserDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
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

    // Key field...
    wlKeyField = new Label( shell, SWT.RIGHT );
    wlKeyField.setText( BaseMessages.getString( PKG, "DenormaliserDialog.KeyField.Label" ) );
    props.setLook( wlKeyField );
    fdlKeyField = new FormData();
    fdlKeyField.left = new FormAttachment( 0, 0 );
    fdlKeyField.right = new FormAttachment( middle, -margin );
    fdlKeyField.top = new FormAttachment( wStepname, margin );
    wlKeyField.setLayoutData( fdlKeyField );

    wKeyField = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wKeyField );
    wKeyField.addModifyListener( lsMod );
    fdKeyField = new FormData();
    fdKeyField.left = new FormAttachment( middle, 0 );
    fdKeyField.top = new FormAttachment( wStepname, margin );
    fdKeyField.right = new FormAttachment( 100, 0 );
    wKeyField.setLayoutData( fdKeyField );
//    wKeyField.addFocusListener( new FocusListener() {
//      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
//      }
//
//      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
//        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
//        shell.setCursor( busy );
//        getPreviousFieldNames();
//        shell.setCursor( null );
//        busy.dispose();
//      }
//    } );

    wKeyField.addMouseListener( new MouseListener() {
      @Override public void mouseDoubleClick( MouseEvent e ) {
      }

      @Override public void mouseDown( MouseEvent e ) {
      }

      @Override public void mouseUp( MouseEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFieldNames();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    wlGroup = new Label( shell, SWT.NONE );
    wlGroup.setText( BaseMessages.getString( PKG, "DenormaliserDialog.Group.Label" ) );
    props.setLook( wlGroup );
    fdlGroup = new FormData();
    fdlGroup.left = new FormAttachment( 0, 0 );
    fdlGroup.top = new FormAttachment( wKeyField, margin );
    wlGroup.setLayoutData( fdlGroup );

    int nrKeyCols = 1;
    int nrKeyRows = ( input.getGroupField() != null ? input.getGroupField().length : 1 );

    ColumnInfo[] ciKey = new ColumnInfo[nrKeyCols];
    ciKey[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.GroupField" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );

    wGroup =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey,
        nrKeyRows, lsMod, props );

    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "DenormaliserDialog.GetFields.Button" ) );
    fdGet = new FormData();
    fdGet.top = new FormAttachment( wlGroup, margin );
    fdGet.right = new FormAttachment( 100, 0 );
    wGet.setLayoutData( fdGet );

    fdGroup = new FormData();
    fdGroup.left = new FormAttachment( 0, 0 );
    fdGroup.top = new FormAttachment( wlGroup, margin );
    fdGroup.right = new FormAttachment( wGet, -margin );
    fdGroup.bottom = new FormAttachment( 30, 0 );
    wGroup.setLayoutData( fdGroup );

    // THE unpivot target field fields
    wlTarget = new Label( shell, SWT.NONE );
    wlTarget.setText( BaseMessages.getString( PKG, "DenormaliserDialog.Target.Label" ) );
    props.setLook( wlTarget );
    fdlTarget = new FormData();
    fdlTarget.left = new FormAttachment( 0, 0 );
    fdlTarget.top = new FormAttachment( wGroup, margin );
    wlTarget.setLayoutData( fdlTarget );

    int UpInsRows = ( input.getDenormaliserTargetField() != null ? input.getDenormaliserTargetField().length : 1 );

    ColumnInfo[] ciTarget =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.TargetFieldname" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.ValueFieldname" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Keyvalue" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Type" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getAllValueMetaNames(), false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Format" ),
          ColumnInfo.COLUMN_TYPE_FORMAT, 4 ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Length" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Precision" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Currency" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Decimal" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Group" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.NullIf" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "DenormaliserDialog.ColumnInfo.Aggregation" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, DenormaliserTargetField.typeAggrLongDesc, false ), };

    ciTarget[ciTarget.length - 1].setToolTip( BaseMessages.getString( PKG, "DenormaliserDialog.CiTarget.Title" ) );
    ciTarget[2].setUsingVariables( true );

    wTarget =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciTarget,
        UpInsRows, lsMod, props );

    wGetAgg = new Button( shell, SWT.PUSH );
    wGetAgg.setText( BaseMessages.getString( PKG, "DenormaliserDialog.GetLookupFields.Button" ) );
    fdGetAgg = new FormData();
    fdGetAgg.top = new FormAttachment( wlTarget, margin );
    fdGetAgg.right = new FormAttachment( 100, 0 );
    wGetAgg.setLayoutData( fdGetAgg );

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, null );

    fdTarget = new FormData();
    fdTarget.left = new FormAttachment( 0, 0 );
    fdTarget.top = new FormAttachment( wlTarget, margin );
    fdTarget.right = new FormAttachment( wGetAgg, -margin );
    fdTarget.bottom = new FormAttachment( wOK, -margin );
    wTarget.setLayoutData( fdTarget );

    // Add listeners
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGet = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsGetAgg = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        getAgg();
      }
    };
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wGetAgg.addListener( SWT.Selection, lsGetAgg );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( backupChanged );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    logDebug( BaseMessages.getString( PKG, "DenormaliserDialog.Log.Getting.KeyInfo" ) );

    if ( input.getKeyField() != null ) {
      wKeyField.setText( input.getKeyField() );
    }

    if ( input.getGroupField() != null ) {
      for ( int i = 0; i < input.getGroupField().length; i++ ) {
        TableItem item = wGroup.table.getItem( i );
        if ( input.getGroupField()[i] != null ) {
          item.setText( 1, input.getGroupField()[i] );
        }
      }
    }

    if ( input.getDenormaliserTargetField() != null ) {
      for ( int i = 0; i < input.getDenormaliserTargetField().length; i++ ) {
        DenormaliserTargetField field = input.getDenormaliserTargetField()[i];

        TableItem item = wTarget.table.getItem( i );

        if ( field.getTargetName() != null ) {
          item.setText( 1, field.getTargetName() );
        }
        if ( field.getFieldName() != null ) {
          item.setText( 2, field.getFieldName() );
        }
        if ( field.getKeyValue() != null ) {
          item.setText( 3, field.getKeyValue() );
        }
        if ( field.getTargetTypeDesc() != null ) {
          item.setText( 4, field.getTargetTypeDesc() );
        }
        if ( field.getTargetFormat() != null ) {
          item.setText( 5, field.getTargetFormat() );
        }
        if ( field.getTargetLength() >= 0 ) {
          item.setText( 6, "" + field.getTargetLength() );
        }
        if ( field.getTargetPrecision() >= 0 ) {
          item.setText( 7, "" + field.getTargetPrecision() );
        }
        if ( field.getTargetCurrencySymbol() != null ) {
          item.setText( 8, field.getTargetCurrencySymbol() );
        }
        if ( field.getTargetDecimalSymbol() != null ) {
          item.setText( 9, field.getTargetDecimalSymbol() );
        }
        if ( field.getTargetGroupingSymbol() != null ) {
          item.setText( 10, field.getTargetGroupingSymbol() );
        }
        if ( field.getTargetNullString() != null ) {
          item.setText( 11, field.getTargetNullString() );
        }
        if ( field.getTargetAggregationType() >= 0 ) {
          item.setText( 12, field.getTargetAggregationTypeDescLong() );
        }
      }
    }

    wGroup.setRowNums();
    wGroup.optWidth( true );
    wTarget.setRowNums();
    wTarget.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    int sizegroup = wGroup.nrNonEmpty();
    int nrfields = wTarget.nrNonEmpty();

    input.setKeyField( wKeyField.getText() );

    input.allocate( sizegroup, nrfields );

    for ( int i = 0; i < sizegroup; i++ ) {
      TableItem item = wGroup.getNonEmpty( i );
      //CHECKSTYLE:Indentation:OFF
      input.getGroupField()[i] = item.getText( 1 );
    }

    for ( int i = 0; i < nrfields; i++ ) {
      DenormaliserTargetField field = new DenormaliserTargetField();

      TableItem item = wTarget.getNonEmpty( i );
      field.setTargetName( item.getText( 1 ) );
      field.setFieldName( item.getText( 2 ) );
      field.setKeyValue( item.getText( 3 ) );
      field.setTargetType( item.getText( 4 ) );
      field.setTargetFormat( item.getText( 5 ) );
      field.setTargetLength( Const.toInt( item.getText( 6 ), -1 ) );
      field.setTargetPrecision( Const.toInt( item.getText( 7 ), -1 ) );
      field.setTargetCurrencySymbol( item.getText( 8 ) );
      field.setTargetDecimalSymbol( item.getText( 9 ) );
      field.setTargetGroupingSymbol( item.getText( 10 ) );
      field.setTargetNullString( item.getText( 11 ) );
      field.setTargetAggregationType( item.getText( 12 ) );

      //CHECKSTYLE:Indentation:OFF
      input.getDenormaliserTargetField()[i] = field;
    }

    stepname = wStepname.getText();

    if ( "Y".equalsIgnoreCase( props.getCustomParameter( STRING_SORT_WARNING_PARAMETER, "Y" ) ) ) {
      MessageDialogWithToggle md =
        new MessageDialogWithToggle(
          shell,
          BaseMessages.getString( PKG, "DenormaliserDialog.Unpivot.DialogTitle" ),
          null,
          BaseMessages.getString( PKG, "DenormaliserDialog.Unpivot.DialogMessage", Const.CR, Const.CR ),
          MessageDialog.WARNING,
          new String[] { BaseMessages.getString( PKG, "DenormaliserDialog.WarningMessage.Option.1" ) },
          0,
          BaseMessages.getString( PKG, "DenormaliserDialog.WarningMessage.Option.2" ),
          "N".equalsIgnoreCase( props.getCustomParameter( STRING_SORT_WARNING_PARAMETER, "Y" ) ) );
      MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      md.open();
      props.setCustomParameter( STRING_SORT_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y" );
      props.saveProps();
    }

    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious( r, wGroup, 1, new int[] { 1 }, new int[] {}, -1, -1, null );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "DenormaliserDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "DenormaliserDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  private void getAgg() {
    // The grouping fields: ignore those.
    wGroup.removeEmptyRows();
    final String[] groupingFields = wGroup.getItems( 0 );
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious(
          r, wTarget, 2, new int[] {}, new int[] {}, -1, -1, new TableItemInsertListener() {
            @Override
            public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
              if ( Const.indexOfString( v.getName(), groupingFields ) < 0 ) { // Not a grouping field
                if ( !wKeyField.getText().equalsIgnoreCase( v.getName() ) ) { // Not the key field
                  int nr = tableItem.getParent().indexOf( tableItem ) + 1;
                  tableItem.setText( 1, BaseMessages.getString( PKG, "DenormaliserDialog.TargetFieldname.Label" )
                    + nr ); // the target fieldname
                  tableItem.setText( 2, v.getName() );
                  tableItem.setText( 4, v.getTypeDesc() );
                  if ( v.getLength() >= 0 ) {
                    tableItem.setText( 6, "" + v.getLength() );
                  }
                  if ( v.getPrecision() >= 0 ) {
                    tableItem.setText( 7, "" + v.getPrecision() );
                  }
                }
              }
              return true;
            }
          } );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "DenormaliserDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "DenormaliserDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  private void getPreviousFieldNames() {
    if ( !gotPreviousFields ) {
      String keyValue = wKeyField.getText();
      try {
        wKeyField.removeAll();
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );

        if ( r != null ) {
          r.getFieldNames();

          for ( int i = 0; i < r.getFieldNames().length; i++ ) {
            wKeyField.add( r.getFieldNames()[i] );
          }
        }
        if ( keyValue != null ) {
          wKeyField.setText( keyValue );
        }
        gotPreviousFields = true;
      } catch ( KettleException ke ) {
        if ( keyValue != null ) {
          wKeyField.setText( keyValue );
        }
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "DenormaliserDialog.FailedToGetFields.DialogTitle" ), BaseMessages
            .getString( PKG, "DenormaliserDialog.FailedToGetFields.DialogMessage" ), ke );
      }
    }
  }
}
