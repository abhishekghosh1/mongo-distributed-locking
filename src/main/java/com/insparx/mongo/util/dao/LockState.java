package com.insparx.mongo.util.dao;

/**
 * The lock states.
 */
enum LockState {

  LOCKED("locked"),
  UNLOCKED("unlocked");

  private LockState(final String pCode) {
    lockStateCode = pCode;
  }

  private final String lockStateCode;

  public String code() {
    return lockStateCode;
  }

  public boolean isLocked() {
    return this == LOCKED;
  }

  public boolean isUnlocked() {
    return this == UNLOCKED;
  }

  public static LockState findByCode(final String pCode) {
    if (pCode == null) throw new IllegalArgumentException("Lockstate code cannot be null");
    for (final LockState s : values()) if (s.lockStateCode.equals(pCode)) return s;
    throw new IllegalArgumentException("Invalid lock state code: " + pCode);
  }
}

