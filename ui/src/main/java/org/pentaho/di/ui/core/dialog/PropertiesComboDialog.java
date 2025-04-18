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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertiesComboDialog extends PropertiesDialog {
  private static final Class<?> PKG = PropertiesComboDialog.class;

  private List<String> comboOptions = new ArrayList<>();

  public PropertiesComboDialog( Shell shell, TransMeta transMeta, Map<String, String> properties, String title ) {
    this( shell, transMeta, properties, title, null, null, null );
  }

  public PropertiesComboDialog( Shell shell, TransMeta transMeta, Map<String, String> properties, String title,
                                String helpUrl, String helpTitle, String helpHeader ) {
    super( shell, transMeta, properties, title, helpUrl, helpTitle, helpHeader );
  }

  @Override
  protected ColumnInfo[] createColumns() {

    ColumnInfo nameColumn = new ColumnInfo( BaseMessages.getString( PKG, "PropertiesDialog.Name" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
            comboOptions.toArray( new String[ 0 ] ) );
    ColumnInfo valueColumn = new ColumnInfo( BaseMessages.getString( PKG, "PropertiesDialog.Value" ), ColumnInfo.COLUMN_TYPE_TEXT );

    // allow the use of parameters on the value column (BACKLOG-31476)
    valueColumn.setUsingVariables( true );

    return new ColumnInfo[] { nameColumn, valueColumn };
  }

  public List<String> getComboOptions() {
    return comboOptions;
  }

  public void setComboOptions( List<String> comboOptions ) {
    this.comboOptions = comboOptions;
  }

}
