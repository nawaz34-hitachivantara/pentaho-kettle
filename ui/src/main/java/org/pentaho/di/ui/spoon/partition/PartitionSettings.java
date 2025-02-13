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


package org.pentaho.di.ui.spoon.partition;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.ui.spoon.PartitionSchemasProvider;

import java.util.Collections;
import java.util.List;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class PartitionSettings {

  private final StepMeta stepMeta;
  private final TransMeta transMeta;
  private final PartitionSchemasProvider schemasProvider;
  private final String[] options;
  private final String[] codes;
  private final StepMeta before;

  public PartitionSettings( int exactSize, TransMeta transMeta, StepMeta stepMeta,
                            PartitionSchemasProvider schemasProvider ) {
    this.transMeta = transMeta;
    this.stepMeta = stepMeta;
    this.schemasProvider = schemasProvider;
    this.options = new String[ exactSize ];
    this.codes = new String[ exactSize ];
    this.before = (StepMeta) stepMeta.clone();
    System.arraycopy(
      StepPartitioningMeta.methodDescriptions, 0, options, 0, StepPartitioningMeta.methodDescriptions.length );
    System.arraycopy( StepPartitioningMeta.methodCodes, 0, codes, 0, StepPartitioningMeta.methodCodes.length );
  }

  public void fillOptionsAndCodesByPlugins( List<PluginInterface> plugins ) {
    int pluginIndex = 0;
    for ( PluginInterface plugin : plugins ) {
      options[ StepPartitioningMeta.methodDescriptions.length + pluginIndex ] = plugin.getDescription();
      codes[ StepPartitioningMeta.methodCodes.length + pluginIndex ] = plugin.getIds()[ 0 ];
      pluginIndex++;
    }
  }

  public int getDefaultSelectedMethodIndex() {
    for ( int i = 0; i < codes.length; i++ ) {
      if ( codes[ i ].equals( stepMeta.getStepPartitioningMeta().getMethod() ) ) {
        return i;
      }
    }
    return 0;
  }

  public int getDefaultSelectedSchemaIndex() {
    List<String> schemaNames;
    try {
      schemaNames = schemasProvider.getPartitionSchemasNames( transMeta );
    } catch ( KettleException e ) {
      schemaNames = Collections.emptyList();
    }

    PartitionSchema partitioningSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
    int defaultSelectedSchemaIndex = 0;
    if ( partitioningSchema != null && partitioningSchema.getName() != null
        && !schemaNames.isEmpty() ) {
      defaultSelectedSchemaIndex =
        Const.indexOfString( partitioningSchema.getName(), schemaNames );
    }
    return defaultSelectedSchemaIndex != -1 ? defaultSelectedSchemaIndex : 0;
  }

  public String getMethodByMethodDescription( String methodDescription ) {
    String method = StepPartitioningMeta.methodCodes[ StepPartitioningMeta.PARTITIONING_METHOD_NONE ];
    for ( int i = 0; i < options.length; i++ ) {
      if ( options[ i ].equals( methodDescription ) ) {
        method = codes[ i ];
      }
    }
    return method;
  }

  public String[] getOptions() {
    return options;
  }

  public String[] getCodes() {
    return codes;
  }

  public List<String> getSchemaNames() {
    try {
      return schemasProvider.getPartitionSchemasNames( transMeta );
    } catch ( KettleException e ) {
      return Collections.emptyList();
    }
  }

  public String[] getSchemaNamesArray() {
    List<String> schemas = getSchemaNames();
    return schemas.toArray( new String[ schemas.size() ] );
  }

  public List<PartitionSchema> getSchemas() {
    try {
      return schemasProvider.getPartitionSchemas( transMeta );
    } catch ( KettleException e ) {
      return Collections.emptyList();
    }
  }

  public StepMeta getStepMeta() {
    return stepMeta;
  }

  public void updateMethodType( int methodType ) {
    stepMeta.getStepPartitioningMeta().setMethodType( methodType );
  }

  public void updateMethod( String method ) throws KettlePluginException {
    stepMeta.getStepPartitioningMeta().setMethod( method );
  }

  public void updateSchema( PartitionSchema schema ) {
    if ( schema != null && schema.getName() != null ) {
      stepMeta.getStepPartitioningMeta().setPartitionSchema( schema );
    }
  }

  public void rollback( StepMeta before ) throws KettlePluginException {
    updateMethod( before.getStepPartitioningMeta().getMethod() );
    updateMethodType( before.getStepPartitioningMeta().getMethodType() );
    updateSchema( before.getStepPartitioningMeta().getPartitionSchema() );
  }

  public StepMeta getBefore() {
    return before;
  }

  public StepMeta getAfter() {
    return (StepMeta) stepMeta.clone();
  }

  public TransMeta getTransMeta() {
    return transMeta;
  }
}
