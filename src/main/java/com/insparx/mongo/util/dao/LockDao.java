package com.insparx.mongo.util.dao;

import com.insparx.mongo.util.domain.DistributedLockServiceConfig;
import com.insparx.mongo.util.domain.DistributedLockTimeOutOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Optional;


/**
 * The distributed lock dao. These are a set of static methods
 * that are responsible for data access.
 */
public final class LockDao extends BaseDao {


  /**
   * Try and get the lock. If unable to do so, this returns false.
   */
  public static synchronized Optional<ObjectId> lock(final MongoClient mongoClient,
                                                     final String key,
                                                     final DistributedLockServiceConfig distributedLockServiceConfig,
                                                     final DistributedLockTimeOutOptions distributedLockTimeOutOptions) {
    // Lookup the lock object.
    Optional<Document> lockDoc = findById(mongoClient, key, distributedLockServiceConfig);

    final long serverTime = getServerTime(mongoClient, distributedLockServiceConfig);
    final long startTime = System.currentTimeMillis();

    // The doc was not there so we are going to try and insert a new doc.
    if (!lockDoc.isPresent()) {
      final Optional<ObjectId> lockId
              = tryInsertNew(mongoClient, key, distributedLockServiceConfig, distributedLockTimeOutOptions, serverTime, startTime);
      if (lockId.isPresent()) return lockId;
    }

    // retry to make sure the locdoc present in DB
    if (!lockDoc.isPresent()) lockDoc = findById(mongoClient, key, distributedLockServiceConfig);

    if (lockDoc.isPresent()) {

      // Get the state.
      final LockState lockState = LockState.findByCode(lockDoc.get().getString(LockDef.STATE.lockField));

      final ObjectId currentLockId = lockDoc.get().getObjectId(LockDef.LOCK_ID.lockField);

      // If it is unlocked, then try and lock.
      if (lockState.isUnlocked()) {
        final Optional<ObjectId> lockId
                = tryLockingExisting(mongoClient, key, currentLockId, distributedLockServiceConfig, distributedLockTimeOutOptions, serverTime, startTime);
        if (lockId.isPresent()) return lockId;
      }

      final ObjectId lockId = (ObjectId) lockDoc.get().get(LockDef.LOCK_ID.lockField);

      // Could not get the lock.
      incrementLockAttemptCount(mongoClient, key, lockId, distributedLockServiceConfig);
    }

    return Optional.empty();

  }

  private static Optional<ObjectId> tryLockingExisting(final MongoClient mongoClient,
                                                       final String key,
                                                       final ObjectId currentLockId,
                                                       final DistributedLockServiceConfig distributedLockServiceConfig,
                                                       final DistributedLockTimeOutOptions distributedLockTimeOutOptions,
                                                       final long pServerTime,
                                                       final long pStartTime) {
    final long adjustTime = System.currentTimeMillis() - pStartTime;

    final long serverTime = pServerTime + adjustTime;
    final Date now = new Date(serverTime);

    final ObjectId lockId = ObjectId.get();

    final BasicDBObject query = new BasicDBObject(LockDef.ID.lockField, key);
    query.put(LockDef.LOCK_ID.lockField, currentLockId);
    query.put(LockDef.STATE.lockField, LockState.UNLOCKED.code());

    final BasicDBObject toSet = new BasicDBObject();
    toSet.put(LockDef.UPDATED.lockField, now);
    toSet.put(LockDef.LAST_HEARTBEAT.lockField, now);
    toSet.put(LockDef.LOCK_ACQUIRED_TIME.lockField, now);
    toSet.put(LockDef.LOCK_TIMEOUT_TIME.lockField, new Date((serverTime + distributedLockTimeOutOptions.getInactiveLockTimeout())));
    toSet.put(LockDef.LOCK_ID.lockField, lockId);
    toSet.put(LockDef.STATE.lockField, LockState.LOCKED.code());
    toSet.put(LockDef.OWNER_APP_NAME.lockField, distributedLockServiceConfig.getAppName());
    toSet.put(LockDef.OWNER_ADDRESS.lockField, distributedLockServiceConfig.getHostAddress());
    toSet.put(LockDef.OWNER_HOSTNAME.lockField, distributedLockServiceConfig.getHostname());
    toSet.put(LockDef.OWNER_THREAD_ID.lockField, Thread.currentThread().getId());
    toSet.put(LockDef.OWNER_THREAD_NAME.lockField, Thread.currentThread().getName());
    toSet.put(LockDef.OWNER_THREAD_GROUP_NAME.lockField, Thread.currentThread().getThreadGroup().getName());
    toSet.put(LockDef.LOCK_ATTEMPT_COUNT.lockField, 0);
    toSet.put(LockDef.INACTIVE_LOCK_TIMEOUT.lockField, distributedLockTimeOutOptions.getInactiveLockTimeout());

    // Try and modify the existing lock.
    final Document lockDoc
            = getDbCollection(mongoClient, distributedLockServiceConfig).findOneAndUpdate(query, toSet);

    if (lockDoc == null) return Optional.empty();
    if (!lockDoc.containsKey(LockDef.LOCK_ID.lockField)) return Optional.empty();

    final ObjectId returnedLockId = lockDoc.getObjectId(LockDef.LOCK_ID.lockField);
    if (returnedLockId == null) return Optional.empty();
    if (!returnedLockId.equals(lockId)) Optional.empty();

    // Yay... we have the lock.
    return Optional.of(lockId);
  }

  /**
   * This will try and create the object. If successful, it will return the lock id.
   * Otherwise, it will return null (i.e., no lock).
   */
  private static Optional<ObjectId> tryInsertNew(final MongoClient pMongo,
                                                 final String key,
                                                 final DistributedLockServiceConfig distributedLockServiceConfig,
                                                 final DistributedLockTimeOutOptions distributedLockTimeOutOptions,
                                                 final long pServerTime,
                                                 final long pStartTime) {
    final long adjustTime = System.currentTimeMillis() - pStartTime;

    final long serverTime = pServerTime + adjustTime;
    final Date now = new Date(serverTime);
    final ObjectId lockId = ObjectId.get();

    final Thread currentThread = Thread.currentThread();

    // final BasicDBObject lockDoc = new BasicDBObject(LockDef.ID.lockField, key);
    final Document lockDoc = new Document(LockDef.ID.lockField, key);
    lockDoc.put(LockDef.UPDATED.lockField, now);
    lockDoc.put(LockDef.LAST_HEARTBEAT.lockField, now);
    lockDoc.put(LockDef.LOCK_ACQUIRED_TIME.lockField, now);
    lockDoc.put(LockDef.LOCK_ID.lockField, lockId);
    lockDoc.put(LockDef.STATE.lockField, LockState.LOCKED.code());
    lockDoc.put(LockDef.LOCK_TIMEOUT_TIME.lockField, new Date((serverTime + distributedLockTimeOutOptions.getInactiveLockTimeout())));
    lockDoc.put(LockDef.OWNER_APP_NAME.lockField, distributedLockServiceConfig.getAppName());
    lockDoc.put(LockDef.OWNER_ADDRESS.lockField, distributedLockServiceConfig.getHostAddress());
    lockDoc.put(LockDef.OWNER_HOSTNAME.lockField, distributedLockServiceConfig.getHostname());
    lockDoc.put(LockDef.OWNER_THREAD_ID.lockField, currentThread.getId());
    lockDoc.put(LockDef.OWNER_THREAD_NAME.lockField, currentThread.getName());
    lockDoc.put(LockDef.OWNER_THREAD_GROUP_NAME.lockField, currentThread.getThreadGroup().getName());
    lockDoc.put(LockDef.LOCK_ATTEMPT_COUNT.lockField, 0);
    lockDoc.put(LockDef.INACTIVE_LOCK_TIMEOUT.lockField, distributedLockTimeOutOptions.getInactiveLockTimeout());

    // Insert, if successful then get out of here.
    try {
      getDbCollection(pMongo, distributedLockServiceConfig).insertOne(lockDoc);
    } catch (MongoException e) {
      return Optional.empty();
    }

    return Optional.of(lockId);
  }

  /**
   * Find by lock key/id.
   */
  private static Optional<Document> findById(final MongoClient pMongo,
                                             final String key,
                                             final DistributedLockServiceConfig distributedLockServiceConfig) {
    MongoCursor<Document> documentMongoCursor = getDbCollection(pMongo, distributedLockServiceConfig)
            .find(new BasicDBObject(LockDef.ID.lockField, key)).iterator();

    return documentMongoCursor.hasNext() ? Optional.of(documentMongoCursor.next()) : Optional.empty();
  }

  /**
   * Increment the waiting request count. This can be used by application developers
   * to diagnose problems with their applications.
   */
  private static void incrementLockAttemptCount(final MongoClient pMongo,
                                                final String pLockName,
                                                final ObjectId pLockId,
                                                final DistributedLockServiceConfig pSvcOptions) {

    final BasicDBObject query = new BasicDBObject(LockDef.ID.lockField, pLockName);
    query.put(LockDef.LOCK_ID.lockField, pLockId);

    getDbCollection(pMongo, pSvcOptions)
            .updateOne(query, new BasicDBObject(INC, new BasicDBObject(LockDef.LOCK_ATTEMPT_COUNT.lockField, 1)));
  }

  /**
   * Unlock the lock.
   */
  public static synchronized Optional<ObjectId> unlock(final MongoClient mongoClient,
                                                       final String key,
                                                       final DistributedLockServiceConfig distributedLockServiceConfig,
                                                       final ObjectId lockId) {
    final BasicDBObject toSet = new BasicDBObject();
    toSet.put(LockDef.UPDATED.lockField, new Date(getServerTime(mongoClient, distributedLockServiceConfig)));
    toSet.put(LockDef.LOCK_ACQUIRED_TIME.lockField, null);
    toSet.put(LockDef.LOCK_TIMEOUT_TIME.lockField, null);
    toSet.put(LockDef.LOCK_ID.lockField, null);
    toSet.put(LockDef.STATE.lockField, LockState.UNLOCKED.code());
    toSet.put(LockDef.OWNER_APP_NAME.lockField, null);
    toSet.put(LockDef.OWNER_ADDRESS.lockField, null);
    toSet.put(LockDef.OWNER_HOSTNAME.lockField, null);
    toSet.put(LockDef.OWNER_THREAD_ID.lockField, null);
    toSet.put(LockDef.OWNER_THREAD_NAME.lockField, null);
    toSet.put(LockDef.OWNER_THREAD_GROUP_NAME.lockField, null);
    toSet.put(LockDef.LOCK_ATTEMPT_COUNT.lockField, 0);
    toSet.put(LockDef.INACTIVE_LOCK_TIMEOUT.lockField, null);

    final BasicDBObject query = new BasicDBObject(LockDef.ID.lockField, key);
    query.put(LockDef.LOCK_ID.lockField, lockId);
    query.put(LockDef.STATE.lockField, LockState.LOCKED.code());

    try {
      getDbCollection(mongoClient, distributedLockServiceConfig).findOneAndUpdate(query, toSet);
    } catch (MongoException e) {
      return Optional.empty();
    }

    return Optional.of(lockId);
  }


  /**
   * Returns the collection.
   */
  private static MongoCollection<Document> getDbCollection(final MongoClient pMongo,
                                                           final DistributedLockServiceConfig pSvcOptions) {
    return getDb(pMongo, pSvcOptions).getCollection(pSvcOptions.getCollectionName());
  }
}

