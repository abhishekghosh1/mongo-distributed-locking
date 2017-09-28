package com.insparx.mongo.util.service;


/**
 * Distributed lock service which prevents race conditions and other concurrency related problems in a clustered environment.
 */
public interface DistributedLockService {

  /**
   * Try to acquire a lock. If lock with the provided key is already acquired, will return false.
   *
   * @param key lock key
   * @return true on success (lock is acquired), false on failure (lock is already acquired)
   */
  boolean tryLock(String key);

  /**
   * Release a lock. If lock with the provided key is not acquired yet, will return false.
   *
   * @param key lock key
   * @return true on success (acquired lock is released), false on failure (lock is not yet acquired)
   */
  boolean releaseLock(String key);
}