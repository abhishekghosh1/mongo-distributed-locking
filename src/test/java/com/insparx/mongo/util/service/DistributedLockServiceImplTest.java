package com.insparx.mongo.util.service;


import com.insparx.mongo.util.dao.LockDao;
import com.insparx.mongo.util.domain.DistributedLock;
import com.insparx.mongo.util.domain.DistributedLockServiceConfig;
import com.insparx.mongo.util.domain.DistributedLockTimeOutOptions;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(LockDao.class)
public class DistributedLockServiceImplTest {


  @Mock
  private MongoClient mongoClient;

  @Mock
  private DistributedLockTimeOutOptions distributedLockTimeOutOptions;

  @Mock
  private DistributedLockServiceConfig distributedLockServiceConfig;

  @InjectMocks
  private DistributedLockServiceImpl distributedLockService;

  @Test
  public void shouldReturnTrueWhenAcquiredALock() {

    //GIVEN
    String key = "test";
    ObjectId objectId = new ObjectId();

    //WHEN
    PowerMockito.mockStatic(LockDao.class);
    when(LockDao.lock(mongoClient, key, distributedLockServiceConfig, distributedLockTimeOutOptions)).thenReturn(Optional.of(objectId));

    Boolean isLocked = distributedLockService.tryLock(key);

    //THEN
    assertThat(isLocked).isTrue();
  }

  @Test
  public void shouldReturnFalseWhenAcquiredALockFails() {

    //GIVEN
    String key = "test";

    //WHEN
    PowerMockito.mockStatic(LockDao.class);
    when(LockDao.lock(mongoClient, key, distributedLockServiceConfig, distributedLockTimeOutOptions)).thenReturn(Optional.empty());

    Boolean isLocked = distributedLockService.tryLock(key);

    //THEN
    assertThat(isLocked).isFalse();
  }


  @Test
  public void shouldReturnFalseWhenALockReleasedAlready() {

    //GIVEN
    String key = "test";

    //WHEN
    Boolean isReleaseLocked = distributedLockService.releaseLock(key);

    //THEN
    assertThat(isReleaseLocked).isFalse();
  }

  @Test
  public void shouldReturnTrueWhenALockReleased() {

    //GIVEN
    String key = "test";
    ObjectId objectId = new ObjectId();

    DistributedLock distributedLock = new DistributedLock(objectId, new AtomicBoolean(true));
    //WHEN
    PowerMockito.mockStatic(LockDao.class);
    when(LockDao.lock(mongoClient, key, distributedLockServiceConfig, distributedLockTimeOutOptions)).thenReturn(Optional.of(objectId));
    when(LockDao.unlock(mongoClient, key, distributedLockServiceConfig, distributedLock.getLockedId()))
            .thenReturn(Optional.of(objectId));

    distributedLockService.tryLock(key);
    Boolean isReleaseLocked = distributedLockService.releaseLock(key);

    //THEN
    assertThat(isReleaseLocked).isTrue();
  }
}
