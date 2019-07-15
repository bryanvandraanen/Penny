package main.java.penny.concurrent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LockManagerTest {

    private static int TEST_NUM_LOCKS = 4;

    @Before
    public void setup() {
        LockManager.init(TEST_NUM_LOCKS);
    }

    @After
    public void cleanup() {
        LockManager.cleanup();
    }

    @Test
    public void testGetSameLockForSameId() {
        int id = 1;

        Lock lock1 = LockManager.getInstance().getLock(id);
        Lock lock2 = LockManager.getInstance().getLock(id);

        Assert.assertSame(lock1, lock2);
    }

    @Test(timeout = 1000)
    public void testGetMaxLocks() {
        List<Lock> locks = getAllLocks();

        // Lock all locks, shouldn't be blocked because all unique
        for (Lock lock : locks) {
            lock.lock();
        }
    }

    @Test
    public void testTryGettingBeyondMaxLocks() throws InterruptedException {
        // Get all locks so none are free
        getAllLocks();

        // Expect this thread to block indefinitely since all locks acquired previously
        Thread indefinitelyBlocked = new Thread(() -> LockManager.getInstance().getLock(TEST_NUM_LOCKS + 1));
        indefinitelyBlocked.start();

        Thread.sleep(1000);

        Assert.assertTrue(indefinitelyBlocked.isAlive());
    }

    @Test
    public void testGetNextReleasedLock() throws InterruptedException {
        List<Lock> locks = getAllLocks();

        // Expect this thread to block indefinitely since all locks acquired previously
        Thread blocked = new Thread(() -> LockManager.getInstance().getLock(TEST_NUM_LOCKS + 1));
        blocked.start();

        Thread.sleep(1000);

        Assert.assertTrue(blocked.isAlive());

        // Free a single lock
        locks.get(0).unlock();

        Thread.sleep(1000);

        // Thread should no longer be alive because it acquired a lock and exited
        Assert.assertFalse(blocked.isAlive());
    }

    @Test
    public void testGetAllLocksAfterAllReleased() throws InterruptedException {
        List<Lock> locks = getAllLocks();

        for (Lock lock : locks) {
            lock.lock();
        }

        // Schedule new thread to unlock all locks after delay
        new Thread(() -> freeLocks(locks)).start();

        // Should block initially until previous locks freed
        List<Lock> newLocks = getAllLocks(TEST_NUM_LOCKS);

        Assert.assertEquals(locks.size(), newLocks.size());

        for (Lock lock : locks) {
            Assert.assertTrue(newLocks.contains(lock));
        }
    }

    @Test
    public void testLocksRecycled() {
        // Get locks initially
        List<Lock> locks1 = getAllLocks();

        // Lock and unlock so now eligible to be received from lock manager
        for (Lock lock : locks1) {
            lock.lock();
        }

        for (Lock lock : locks1) {
            lock.unlock();
        }

        List<Lock> locks2 = getAllLocks();

        // Assert all the same locks are used
        Assert.assertEquals(locks1.size(), locks2.size());
        for (Lock lock : locks1) {
            Assert.assertTrue(locks2.contains(lock));
        }
    }

    @Test
    public void testOnUnlockMakesLockAvailable() {
        Lock lock = LockManager.getInstance().getLock(0);

        LockManager.getInstance().onUnlock(lock);

        List<Lock> locks = getAllLocks();

        Assert.assertTrue(locks.contains(lock));
    }

    private void freeLocks(List<Lock> locks) {
        // Wait before freeing locks
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Simply continue with unlocking
        }

        for (Lock lock : locks) {
            lock.unlock();
        }
    }

    private List<Lock> getAllLocks() {
        return getAllLocks(0);
    }

    private List<Lock> getAllLocks(int start) {
        List<Lock> locks = new ArrayList<Lock>();

        for (int i = start; i < start + TEST_NUM_LOCKS; i++) {
            locks.add(LockManager.getInstance().getLock(i));
        }

        return locks;
    }
}