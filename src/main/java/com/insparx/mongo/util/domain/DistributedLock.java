package com.insparx.mongo.util.domain;


import org.bson.types.ObjectId;

import java.util.concurrent.atomic.AtomicBoolean;

public class DistributedLock {

  private final ObjectId lockedId;

  private final AtomicBoolean lockedStatus;

  public DistributedLock(ObjectId lockedId, AtomicBoolean lockedStatus) {
    this.lockedId = lockedId;
    this.lockedStatus = lockedStatus;
  }

  public ObjectId getLockedId() {
    return lockedId;
  }

  public AtomicBoolean getLockedStatus() {
    return lockedStatus;
  }
}