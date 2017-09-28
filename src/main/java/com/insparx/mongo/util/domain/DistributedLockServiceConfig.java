package com.insparx.mongo.util.domain;


import com.mongodb.MongoClient;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * The global distributed lock service options/config.
 */
public class DistributedLockServiceConfig {

  private final MongoClient mongoClient;
  private final String mongoUri;
  private final String databaseName;
  private final String collectionName;

  private String hostname;
  private String hostAddress;

  private final String appName;


  private long _heartbeatFrequency = 5000;
  private long _timeoutFrequency = 60000;
  private long _lockUnlockedFrequency = 1000;

  /**
   * The basic constructor. This uses the following:<br />
   * <ul>
   * <li>database name: mongo-distributed-locking
   * <li>collection name: locks
   * </ul>
   */
  public DistributedLockServiceConfig(final String pMongoUri) {
    this(pMongoUri, "mongo-distributed-locking", "locks", null);
  }

  /**
   * Constructor that allows the user to specify database and colleciton name.
   */
  public DistributedLockServiceConfig(final String pMongoUri,
                                      final String pDbName,
                                      final String pCollectionName) {
    this(pMongoUri, pDbName, pCollectionName, null);
  }

  /**
   * Constructor that allows the user to specify database, colleciton and app name.
   * The app name should definetly be used if the db/collection names are shared by multiple
   * apps/systems (e.g., SomeCoolDataProcessor).
   */
  public DistributedLockServiceConfig(final String pMongoUri,
                                      final String pDbName,
                                      final String pCollectionName,
                                      final String pAppName) {
    mongoUri = pMongoUri;
    databaseName = pDbName;
    collectionName = pCollectionName;
    appName = pAppName;

    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (final UnknownHostException e) {
      hostAddress = null;
    }

    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (final UnknownHostException e) {
      hostname = null;
    }
    mongoClient = null;
  }

  /**
   * The basic constructor. This uses the following:<br />
   * <ul>
   * <li>database name: mongo-distributed-locking
   * <li>collection name: locks
   * </ul>
   */
  public DistributedLockServiceConfig(final MongoClient pMongoClient) {
    this(pMongoClient, "mongo-distributed-locking", "locks", null);
  }

  /**
   * Constructor that allows the user to specify database and colleciton name.
   */
  public DistributedLockServiceConfig(final MongoClient pMongoClient,
                                      final String pDbName,
                                      final String pCollectionName) {
    this(pMongoClient, pDbName, pCollectionName, null);
  }

  /**
   * Constructor that allows the user to specify database, colleciton and app name.
   * The app name should definetly be used if the db/collection names are shared by multiple
   * apps/systems (e.g., SomeCoolDataProcessor).
   */
  public DistributedLockServiceConfig(final MongoClient pMongoClient,
                                      final String pDbName,
                                      final String pCollectionName,
                                      final String pAppName) {
    mongoClient = pMongoClient;
    databaseName = pDbName;
    collectionName = pCollectionName;
    appName = pAppName;

    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (final UnknownHostException e) {
      hostAddress = null;
    }

    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (final UnknownHostException e) {
      hostname = null;
    }
    mongoUri = null;
  }

  public MongoClient getMongoClient() {
    return mongoClient;
  }

  public String getMongoUri() {
    return mongoUri;
  }

  public String getDbName() {
    return databaseName;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public String getAppName() {
    return appName;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(final String pV) {
    hostname = pV;
  }

  public String getHostAddress() {
    return hostAddress;
  }

  /**
   * Milliseconds between heartbeat checks.
   */
  public long getHeartbeatFrequency() {
    return _heartbeatFrequency;
  }

  public void setHeartbeatFrequency(final long pHeartbeatFrequency) {
    _heartbeatFrequency = pHeartbeatFrequency;
  }

  /**
   * Milliseconds between lock timeout checks.
   */
  public long getTimeoutFrequency() {
    return _timeoutFrequency;
  }

  public void setTimeoutFrequency(final long pTimeoutFrequency) {
    _timeoutFrequency = pTimeoutFrequency;
  }

  /**
   * Milliseconds between lock unlocked checks.
   */
  public long getLockUnlockedFrequency() {
    return _lockUnlockedFrequency;
  }

  public void setLockUnlockedFrequency(final long pLockUnlockedFrequency) {
    _lockUnlockedFrequency = pLockUnlockedFrequency;
  }

}

