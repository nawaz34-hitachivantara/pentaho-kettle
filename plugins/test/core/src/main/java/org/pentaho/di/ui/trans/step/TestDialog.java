package org.pentaho.di.ui.trans.step;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.TestMeta;
import org.pentaho.di.ui.core.dialog.EnterValueDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class TestDialog extends BaseStepDialog implements StepDialogInterface {

    private static Class<?> PKG = TestMeta.class;

    private TestMeta inputMeta;

    private TextVar noOfRows;

    /*private ValueMetaAndData value;*/


    public TestDialog(Shell parent, BaseStepMeta baseStepMeta, TransMeta transMeta, String stepname) {
        super(parent, baseStepMeta, transMeta, stepname);
    }

    public TestDialog( Shell parent, Object in, TransMeta tr, String sname ) {
        super( parent, (BaseStepMeta) in, tr, sname );
        inputMeta = (TestMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent,SWT.DIALOG_TRIM|SWT.RESIZE|SWT.MIN|SWT.MAX);
        props.setLook(shell);
        setShellImage(shell,inputMeta);

        ModifyListener lsMod = new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent e ) {
                inputMeta.setChanged();
            }
        };
        changed = inputMeta.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout( formLayout );
        shell.setText( "Rows Count" );

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wlStepname = new Label( shell, SWT.RIGHT );
        wlStepname.setText( " Name " );
        props.setLook( wlStepname );
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment( 0, 0 );
        fdlStepname.right = new FormAttachment( middle, -margin );
        fdlStepname.top = new FormAttachment( 0, margin );
        wlStepname.setLayoutData( fdlStepname );
        wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wStepname );
        wStepname.addModifyListener( lsMod );
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment( middle, 0 );
        fdStepname.top = new FormAttachment( 0, margin );
        fdStepname.right = new FormAttachment( 100, 0 );
        wStepname.setLayoutData( fdStepname );
        Control lastControl = wStepname;

        Label wlRowsToSkip = new Label( shell, SWT.RIGHT );
        wlRowsToSkip.setText( "No of Rows" );
        props.setLook( wlRowsToSkip );
        FormData fdlRowsToSkip = new FormData();
        fdlRowsToSkip = new FormData();
        fdlRowsToSkip.left = new FormAttachment( 0, 0 );
        fdlRowsToSkip.top = new FormAttachment( middle, margin );
        fdlRowsToSkip.right = new FormAttachment( middle, -margin );
        wlRowsToSkip.setLayoutData( fdlRowsToSkip );
        noOfRows = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( noOfRows );
        noOfRows.addModifyListener( lsMod );
        FormData fdRowsToSkip = new FormData();
        fdRowsToSkip = new FormData();
        fdRowsToSkip.left = new FormAttachment( middle, 0 );
        fdRowsToSkip.top = new FormAttachment( lastControl, margin );
        fdRowsToSkip.right = new FormAttachment( 100, 0 );
        noOfRows.setLayoutData( fdRowsToSkip );
        lastControl = noOfRows;


        wOK = new Button( shell, SWT.PUSH );
        wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        wCancel = new Button( shell, SWT.PUSH );
        wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

        setButtonPositions( new Button[] { wOK, wCancel }, margin, lastControl );

        wStepname.setText("Data received");
        noOfRows.setText(Const.NVL( String.valueOf(inputMeta.getNoOfRows()), "" ));

        lsCancel = new Listener() {
            @Override
            public void handleEvent( Event e ) {
                cancel();
            }
        };

        lsOK = new Listener() {
            @Override
            public void handleEvent(Event event) {
                ok();
            }
        };

        wCancel.addListener( SWT.Selection, lsCancel );
        wOK.addListener(SWT.Selection,lsOK);
        setSize();
        inputMeta.setChanged( changed );

        shell.open();

        return stepname;

    }


    private void cancel() {
        stepname = null;
        inputMeta.setChanged( backupChanged );
        inputMeta.setNoOfRows(0);
        dispose();
    }



    private void ok() {
        /*stepname = wStepname.getText(); // return value
        value.getValueMeta().setName( wValName.getText() );
        input.setValue( value );*/
        dispose();
    }





}
