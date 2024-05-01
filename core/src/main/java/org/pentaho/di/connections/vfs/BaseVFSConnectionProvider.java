/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2022 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.connections.vfs;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.utils.VFSConnectionTestOptions;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class BaseVFSConnectionProvider<T extends VFSConnectionDetails> implements VFSConnectionProvider<T> {

  private final Supplier<ConnectionManager> connectionManagerSupplier;

  private static final Log LOGGER = LogFactory.getLog( BaseVFSConnectionProvider.class );

  public static final String DELIMITER = "/";

  protected BaseVFSConnectionProvider() {
    this( ConnectionManager::getInstance );
  }

  protected BaseVFSConnectionProvider( Supplier<ConnectionManager> connectionManagerSupplier ) {
    Objects.requireNonNull( connectionManagerSupplier );
    this.connectionManagerSupplier = connectionManagerSupplier;
  }

  @Override public List<String> getNames() {
    return connectionManagerSupplier.get().getNamesByType( getClass() );
  }

  @SuppressWarnings( "unchecked" )
  @Override public List<T> getConnectionDetails() {
    return (List<T>) connectionManagerSupplier.get().getConnectionDetailsByScheme( getKey() );
  }

  @Override public T prepare( T connectionDetails ) throws KettleException {
    return connectionDetails;
  }

  @Override public String sanitizeName( String string ) {
    return string;
  }

  // Utility method to perform variable substitution on values
  protected String getVar( String value, VariableSpace variableSpace ) {
    if ( variableSpace != null ) {
      return variableSpace.environmentSubstitute( value );
    }
    return value;
  }

  // Utility method to derive a boolean checkbox setting that may use variables instead
  protected static boolean getBooleanValueOfVariable( VariableSpace space, String variableName, String defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = space.environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        Boolean b = ValueMetaBase.convertStringToBoolean( value );
        return b != null && b;
      }
    }
    return Objects.equals( Boolean.TRUE, ValueMetaBase.convertStringToBoolean( defaultValue ) );
  }

  protected VariableSpace getSpace( ConnectionDetails connectionDetails ) {
    return connectionDetails.getSpace() == null ? Variables.getADefaultVariableSpace() : connectionDetails.getSpace();
  }
  // Check exists... pvfs://connection name ->  exists ?
  // 1. External path / Generic file path: pvfs://connection name/foo // ConnectionFileProvider  END USER
  // 2. Internal path / Apache VFS path / Connection path: s3-avfs://foo + FSOptions (Connection Name)
  // 3. Physical path: S3 API actually knows of: s3://root/path/here/foo  ADMIN USER will config the root physical path

  // Local
  // 2. Internal path: local://foo + FSOptions (Connection Name, Root Path...)
  // 3. Physical path: c:/root/path/here/foo
  // KettleVFS.getFileObject() -->> FileObject -->> FileName -->> FileSystem
  @Override
  public boolean test( T connectionDetails, VFSConnectionTestOptions vfsConnectionTestOptions ) throws KettleException {
    boolean valid = test( connectionDetails );
    if ( !valid ) {
      return false;
    }

    if ( !connectionDetails.isSupportsRootPath()  || vfsConnectionTestOptions.isIgnoreRootPath() ) {
      return true;
    }

    String resolvedRootPath = getResolvedRootPath( connectionDetails );
    if ( resolvedRootPath == null ) {
      return !connectionDetails.isRootPathRequired();
    }

    String internalUrl = buildUrl( connectionDetails, resolvedRootPath );
    FileObject fileObject = KettleVFS.getFileObject( internalUrl, new Variables(), getOpts( connectionDetails ) );

    try {
      return fileObject.exists();
    } catch ( FileSystemException fileSystemException ) {
      LOGGER.error( fileSystemException.getMessage() );
      return false;
    }
  }

  @Override
  public String getResolvedRootPath( VFSConnectionDetails connectionDetails ) {
    if ( StringUtils.isNotEmpty( connectionDetails.getRootPath() ) ) {
      VariableSpace space = getSpace( connectionDetails );
      String resolvedRootPath = getVar( connectionDetails.getRootPath(), space );
      if ( StringUtils.isNotBlank( resolvedRootPath ) ) {
        return normalizeRootPath( resolvedRootPath );
      }
    }

    return null;
  }

  private String normalizeRootPath( String rootPath ) {
    if ( StringUtils.isNotEmpty( rootPath ) ) {
      if ( !rootPath.startsWith( DELIMITER ) ) {
        rootPath = DELIMITER + rootPath;
      }
      if (rootPath.endsWith( DELIMITER ) ) {
        rootPath = rootPath.substring( 0, rootPath.length() - 1 );
      }
    }
    return rootPath;
  }

  private String buildUrl( VFSConnectionDetails connectionDetails, String rootPath ) {
    String domain = connectionDetails.getDomain();
    if ( !domain.isEmpty() ) {
      domain = DELIMITER + domain;
    }
    return connectionDetails.getType() + ":/" + domain + rootPath;
  }
}
