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

package org.pentaho.di.shared;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;

public class ChangeTrackingClusterSchemaManager extends ChangeTrackingSharedObjectManager<ClusterSchema> implements ClusterSchemaManagementInterface {

  public ChangeTrackingClusterSchemaManager( SharedObjectsManagementInterface<ClusterSchema> parent ) {
    super( parent );
  }
}
