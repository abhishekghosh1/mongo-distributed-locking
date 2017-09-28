
package com.insparx.mongo.util.domain;

/**
 * The options/configuration on a per lock basis. If this is not passed when creating the lock,
 * the library defaults are used.
 */
public class DistributedLockTimeOutOptions {

  /**
   * Set the inactive lock timeout (time in milliseconds). The default is one minute.
   * This means that if your lock process dies or is killed without unlocking first,
   * the lock will be reset in one minute (120,000 ms).
   */
  public void setInactiveLockTimeout(final int pV) {
    inactiveLockTimeout = pV;
  }

  /**
   * Returns the inactive lock timeout.
   */
  public int getInactiveLockTimeout() {
    return inactiveLockTimeout;
  }

  private int inactiveLockTimeout = 120000;
}

