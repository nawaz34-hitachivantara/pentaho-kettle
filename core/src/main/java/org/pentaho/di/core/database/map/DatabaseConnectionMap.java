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


package org.pentaho.di.core.database.map;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseTransactionListener;
import org.pentaho.di.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class contains a map between on the one hand
 * <p/>
 * the transformation name/thread the partition ID the connection group
 * <p/>
 * And on the other hand
 * <p/>
 * The database connection The number of times it was opened
 *
 * @author Matt
 */
public class DatabaseConnectionMap {
  private final ConcurrentMap<String, Database> map;
  private final AtomicInteger transactionId;
  private final Map<String, List<DatabaseTransactionListener>> transactionListenersMap;

  private static final DatabaseConnectionMap connectionMap = new DatabaseConnectionMap();

  public static DatabaseConnectionMap getInstance() {
    return connectionMap;
  }

  private DatabaseConnectionMap() {
    map = new ConcurrentHashMap<String, Database>();
    transactionId = new AtomicInteger( 0 );
    transactionListenersMap = new HashMap<String, List<DatabaseTransactionListener>>();
  }

  /**
   * Tries to obtain an existing <tt>Database</tt> instance for specified parameters. If none is found, then maps the
   * key's value to <tt>database</tt>. Similarly to {@linkplain ConcurrentHashMap#putIfAbsent(Object, Object)} returns
   * <tt>null</tt> if there was no value for the specified key and they mapped value otherwise.
   *
   * @param connectionGroup connection group
   * @param partitionID     partition's id
   * @param database        database
   * @return <tt>null</tt> or previous value
   */
  public Database getOrStoreIfAbsent( String connectionGroup, String partitionID, Database database ) {
    String key = createEntryKey( connectionGroup, partitionID, database );
    return map.putIfAbsent( key, database );
  }

  public void removeConnection( String connectionGroup, String partitionID, Database database ) {
    String key = createEntryKey( connectionGroup, partitionID, database );
    map.remove( key );
  }

  /**
   * @deprecated use {@linkplain #getOrStoreIfAbsent(String, String, Database)} instead
   */
  @Deprecated
  public synchronized void storeDatabase( String connectionGroup, String partitionID, Database database ) {
    String key = createEntryKey( connectionGroup, partitionID, database );
    map.put( key, database );
  }

  /**
   * @deprecated use {@linkplain #getOrStoreIfAbsent(String, String, Database)} instead
   */
  @Deprecated
  public synchronized Database getDatabase( String connectionGroup, String partitionID, Database database ) {
    String key = createEntryKey( connectionGroup, partitionID, database );
    return map.get( key );
  }

  public static String createEntryKey( String connectionGroup, String partitionID, Database database ) {
    StringBuilder key = new StringBuilder( connectionGroup );

    key.append( ':' ).append( database.getDatabaseMeta().getName() );
    if ( !Utils.isEmpty( partitionID ) ) {
      key.append( ':' ).append( partitionID );
    }

    return key.toString();
  }

  public Map<String, Database> getMap() {
    return map;
  }

  public String getNextTransactionId() {
    return Integer.toString( transactionId.incrementAndGet() );
  }

  public void addTransactionListener( String transactionId, DatabaseTransactionListener listener ) {
    List<DatabaseTransactionListener> transactionListeners = getTransactionListeners( transactionId );
    transactionListeners.add( listener );
  }

  public void removeTransactionListener( String transactionId, DatabaseTransactionListener listener ) {
    List<DatabaseTransactionListener> transactionListeners = getTransactionListeners( transactionId );
    transactionListeners.remove( listener );
  }

  public List<DatabaseTransactionListener> getTransactionListeners( String transactionId ) {
    List<DatabaseTransactionListener> transactionListeners = transactionListenersMap.get( transactionId );
    if ( transactionListeners == null ) {
      transactionListeners = new ArrayList<DatabaseTransactionListener>();
      transactionListenersMap.put( transactionId, transactionListeners );
    }
    return transactionListeners;
  }

  public void removeTransactionListeners( String transactionId ) {
    transactionListenersMap.remove( transactionId );
  }
}
