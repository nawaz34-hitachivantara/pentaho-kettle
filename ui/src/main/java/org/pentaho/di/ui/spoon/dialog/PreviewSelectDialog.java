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


package org.pentaho.di.ui.spoon.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class PreviewSelectDialog extends Dialog {
  private static Class<?> PKG = PreviewSelectDialog.class; // for i18n purposes, needed by Translator2!!

  private Label wlFields;

  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Button wPreview, wCancel;
  private Listener lsPreview, lsCancel;

  private Shell shell;
  private TransMeta transMeta;

  public String[] previewSteps;
  public int[] previewSizes;

  private PropsUI props;

  public PreviewSelectDialog( Shell parent, int style, TransMeta transMeta ) {
    super( parent, style );

    this.transMeta = transMeta;
    this.props = PropsUI.getInstance();

    previewSteps = null;
    previewSizes = null;
  }

  public void open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "PreviewSelectDialog.Dialog.PreviewSelection.Title" ) ); // Preview
                                                                                                         // selection
                                                                                                         // screen
    shell.setImage( GUIResource.getInstance().getImageLogoSmall() );

    int margin = Const.MARGIN;

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "PreviewSelectDialog.Label.Steps" ) ); // Steps:
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( 0, margin );
    wlFields.setLayoutData( fdlFields );

    List<StepMeta> usedSteps = transMeta.getUsedSteps();
    final int FieldsRows = usedSteps.size();

    ColumnInfo[] colinf =
    {
      new ColumnInfo(
        BaseMessages.getString( PKG, "PreviewSelectDialog.Column.Stepname" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false, true ), // Stepname
      new ColumnInfo(
        BaseMessages.getString( PKG, "PreviewSelectDialog.Column.PreviewSize" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, false ), // Preview size
    };

    wFields =
      new TableView( transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows,
        true, // read-only
        null, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Show" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Close" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wPreview, wCancel }, margin, null );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wPreview.addListener( SWT.Selection, lsPreview );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    BaseStepDialog.setSize( shell );

    getData();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    String[] prSteps = props.getLastPreview();
    int[] prSizes = props.getLastPreviewSize();
    String name;
    List<StepMeta> selectedSteps = transMeta.getSelectedSteps();
    List<StepMeta> usedSteps = transMeta.getUsedSteps();

    if ( selectedSteps.size() == 0 ) {

      int line = 0;
      for ( StepMeta stepMeta : usedSteps ) {

        TableItem item = wFields.table.getItem( line++ );
        name = stepMeta.getName();
        item.setText( 1, stepMeta.getName() );
        item.setText( 2, "0" );

        // Remember the last time...?
        for ( int x = 0; x < prSteps.length; x++ ) {
          if ( prSteps[x].equalsIgnoreCase( name ) ) {
            item.setText( 2, "" + prSizes[x] );
          }
        }
      }
    } else {
      // No previous selection: set the selected steps to the default preview size
      //
      int line = 0;
      for ( StepMeta stepMeta : usedSteps ) {
        TableItem item = wFields.table.getItem( line++ );
        name = stepMeta.getName();
        item.setText( 1, stepMeta.getName() );
        item.setText( 2, "" );

        // Is the step selected?
        if ( stepMeta.isSelected() ) {
          item.setText( 2, "" + props.getDefaultPreviewSize() );
        }
      }
    }

    wFields.optWidth( true );
  }

  private void cancel() {
    dispose();
  }

  private void preview() {
    int sels = 0;
    for ( int i = 0; i < wFields.table.getItemCount(); i++ ) {
      TableItem ti = wFields.table.getItem( i );
      int size = Const.toInt( ti.getText( 2 ), 0 );
      if ( size > 0 ) {
        sels++;
      }
    }

    previewSteps = new String[sels];
    previewSizes = new int[sels];

    sels = 0;
    for ( int i = 0; i < wFields.table.getItemCount(); i++ ) {
      TableItem ti = wFields.table.getItem( i );
      int size = Const.toInt( ti.getText( 2 ), 0 );

      if ( size > 0 ) {
        previewSteps[sels] = ti.getText( 1 );
        previewSizes[sels] = size;

        sels++;
      }
    }

    props.setLastPreview( previewSteps, previewSizes );

    dispose();
  }
}
