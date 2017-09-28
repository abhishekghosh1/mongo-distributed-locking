package com.insparx.mongo.util.dao;

/**
 * The distributed lock db data fields.
 */
enum LockDef {
  ID("_id"), // This is the lock key
  UPDATED("lastUpdated"),
  LAST_HEARTBEAT("lastHeartbeat"),
  LOCK_ACQUIRED_TIME("lockAcquired"), // The time the lock was granted

  STATE("lockState"), // The current state of this lock

  LOCK_ID("lockId"), // The generated id is used to ensure multiple thread/processes don't step on each other

  OWNER_APP_NAME("appName"), // The name of the application who has the lock (optional)

  OWNER_ADDRESS("ownerAddress"),
  OWNER_HOSTNAME("ownerHostname"),
  OWNER_THREAD_ID("ownerThreadId"),
  OWNER_THREAD_NAME("ownerThreadName"),
  OWNER_THREAD_GROUP_NAME("ownerThreadGroupName"),

  INACTIVE_LOCK_TIMEOUT("inactiveLockTimeout"), // The number of ms before timeout (since last heartbeat)
  LOCK_TIMEOUT_TIME("lockTimeoutTime"),

  LOCK_ATTEMPT_COUNT("lockAttemptCount"); // The number of times another thread/process has requested this lock (since locked)

  LockDef(final String lockField) {
    this.lockField = lockField;
  }

  final String lockField;
}

