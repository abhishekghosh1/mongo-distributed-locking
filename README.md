
mongo-distributed-locking
=============

# PROBLEM: Simple distributed locking mechanism using MongoDB #
================================================================================================================

Create a simple distributed locking mechanism using MongoDB implementing the following interface:
```java
/**
* Distributed lock service which prevents race conditions and other concurrency related problems in a clustered environment.
*/
public interface DistributedLockService {

    /**
     * Try to acquire a lock. If lock with the provided key is already acquired, will return false.
     *
     * @param key lock key
     * @return true on success (lock is not acquired yet), false on failure (lock is already acquired)
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
```

## Output: ##

![Test Output](./Test-output.png?raw=true "Test Output")



## SOLUTION  ##
================================================================================================================

1. Building:
    - The solution is provided in the form of a Gradle project. It can
    thus be built by invoking the "build" goal so:
    ```gradle
    $ ./gradlew clean build
    ```
    Note that this goal runs all JUnit tests included in the solution.

2. How to use this Library:
    - This library can be used by injecting DistributedLockServiceImpl.java as a bean. As a constructor the application
    expects MongoClient, Mongo database connection details and inactiveLockTimeout in milliseconds.

    ```java
     DistributedLockServiceImpl(final MongoClient mongoClient,
                                 final DistributedLockTimeOutOptions distributedLockTimeOutOptions,
                                 final DistributedLockServiceConfig distributedLockServiceConfig) {
        this.mongoClient = mongoClient;
        this.distributedLockTimeOutOptions = distributedLockTimeOutOptions;
        this.distributedLockServiceConfig = distributedLockServiceConfig;
      }
    ```

    There are two helpers method tryLock and releaseLock.

3. Approach:
    - Basically when a client try to create a Lock, the application inserts lockState as Locked and track them by key/UniqueId.
    There is a concurrent HashMap which holds the state of distributed lock. The application states get refreshed
    once the client requests to release the lock.

4. TODO:
      - Add embedded MongoDb plugin to create an integration tests for LockDao.class
      - More Unit test coverage to support the actual logic
      - Add lock heartbeat thread is responsible for sending updates to the lock
        doc every X seconds (when the lock is owned by the current process). This
        library uses missing/stale/old heartbeats to timeout locks that have not been
        closed properly (based on the lock/unlock) contract. This can happen when processes
        die unexpectedly (e.g., out of memory) or when they are not stopped properly (e.g., kill -9).
      - add LockTimeout and LockUnlocked Thread as a Monitor.

