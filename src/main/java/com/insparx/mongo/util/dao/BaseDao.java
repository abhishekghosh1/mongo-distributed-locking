package com.insparx.mongo.util.dao;


import com.insparx.mongo.util.domain.DistributedLockServiceConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Date;

/**
 * The base dao.
 */
abstract class BaseDao {

  static final String INC = "$inc";

  private static final String LOCAL_TIME_FIELD = "localTime";
  private static final int SERVER_TIME_TRIES = 3;

  private static final BasicDBObject SERVER_STATUS_CMD = new BasicDBObject("serverStatus", 1);

  /**
   * Returns the db.
   */
  static MongoDatabase getDb(final MongoClient mongoClient,
                                   final DistributedLockServiceConfig distributedLockServiceConfig) {
    return mongoClient.getDatabase(distributedLockServiceConfig.getDbName());
  }

  /**
   * Returns the current server time. This makes a few requests to the server to try and adjust for
   * network latency.
   */
  static long getServerTime(final MongoClient mongoClient,
                                  final DistributedLockServiceConfig distributedLockServiceConfig) {

    final long[] localTimes = new long[SERVER_TIME_TRIES];
    final int[] latency = new int[SERVER_TIME_TRIES];

    long startTime;

    for (int idx = 0; idx < SERVER_TIME_TRIES; idx++) {
      startTime = System.currentTimeMillis();
      Document serverStatus = getDb(mongoClient, distributedLockServiceConfig).runCommand(SERVER_STATUS_CMD);
      latency[idx] = (int) (System.currentTimeMillis() - startTime);
      localTimes[idx] = ((Date) serverStatus.get(LOCAL_TIME_FIELD)).getTime();
    }

    final long serverTime = localTimes[(SERVER_TIME_TRIES - 1)];

    // Adjust based on latency.
    return (serverTime + getHalfRoundedAvg(latency));
  }

  /**
   * assume that latency is 50% each way.
   */
  private static int getHalfRoundedAvg(final int[] pV) {
    int total = 0;
    for (int aPV : pV) total += aPV;
    return Math.round((((float) total / (float) pV.length) / (float) 2));
  }
}

