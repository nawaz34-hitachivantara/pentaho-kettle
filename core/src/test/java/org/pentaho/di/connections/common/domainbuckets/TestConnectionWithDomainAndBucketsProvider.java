/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.common.domainbuckets;

import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;

import java.util.List;

public class TestConnectionWithDomainAndBucketsProvider
  extends BaseVFSConnectionProvider<TestConnectionWithDomainAndBucketsDetails> {

  public static final String NAME = "Test3";
  public static final String SCHEME = "test3";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public List<VFSRoot> getLocations( TestConnectionWithDomainAndBucketsDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public String getProtocol( TestConnectionWithDomainAndBucketsDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public Class<TestConnectionWithDomainAndBucketsDetails> getClassType() {
    return TestConnectionWithDomainAndBucketsDetails.class;
  }

  @Override public boolean test( TestConnectionWithDomainAndBucketsDetails connectionDetails ) {
    return true;
  }
}
