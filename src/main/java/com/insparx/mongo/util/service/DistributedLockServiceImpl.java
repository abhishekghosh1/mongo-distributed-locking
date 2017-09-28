package com.insparx.mongo.util.service;

import com.insparx.mongo.util.dao.LockDao;
import com.insparx.mongo.util.domain.DistributedLock;
import com.insparx.mongo.util.domain.DistributedLockServiceConfig;
import com.insparx.mongo.util.domain.DistributedLockTimeOutOptions;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The distributed lock object.
 */
public class DistributedLockServiceImpl implements DistributedLockService {

  private final MongoClient mongoClient;
  private final DistributedLockTimeOutOptions distributedLockTimeOutOptions;
  private final DistributedLockServiceConfig distributedLockServiceConfig;
  private volatile ConcurrentHashMap<String, DistributedLock> currentLockedMap = new ConcurrentHashMap<>();


  /**
   * Construct the object with params.
   */
  DistributedLockServiceImpl(final MongoClient mongoClient,
                             final DistributedLockTimeOutOptions distributedLockTimeOutOptions,
                             final DistributedLockServiceConfig distributedLockServiceConfig) {
    this.mongoClient = mongoClient;
    this.distributedLockTimeOutOptions = distributedLockTimeOutOptions;
    this.distributedLockServiceConfig = distributedLockServiceConfig;
  }


  @Override
  public boolean tryLock(final String key) {
    return tryDistributedLock(key);
  }

  @Override
  public boolean releaseLock(final String key) {
    Optional<DistributedLock> distributedLock = Optional.ofNullable(currentLockedMap.get(key));
    if (!distributedLock.isPresent() || !distributedLock.get().getLockedStatus().get()) {
      return false;
    } else {
      final Optional<ObjectId> lockId = LockDao.unlock(mongoClient, key, distributedLockServiceConfig, distributedLock.get().getLockedId());
      if (!lockId.isPresent()) return false;

      currentLockedMap.remove(key);
      return true;
    }
  }

  /**
   * Try and lock the distributed lock.
   */
  private boolean tryDistributedLock(final String key) {
    if (isLocked(key)) return false;

    final Optional<ObjectId> lockId = LockDao.lock(mongoClient, key, distributedLockServiceConfig, distributedLockTimeOutOptions);

    if (!lockId.isPresent()) return false;

    currentLockedMap.put(key, new DistributedLock(lockId.get(), new AtomicBoolean(true)));
    return true;
  }

  /**
   * Returns true if the lock is currently locked.
   */
  private boolean isLocked(final String key) {
    Optional<DistributedLock> isLockedByKey = Optional.ofNullable(currentLockedMap.get(key));
    return isLockedByKey.isPresent() && isLockedByKey.get().getLockedStatus().get();
  }

}

