package main.java.penny.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class LockTest {

    @Test(timeout = 2000)
    public void testLock() {
        Lock lock = new Lock();

        lock.lock();
        Assert.assertTrue(lock.isLocked());

        lock.unlock();
        Assert.assertFalse(lock.isLocked());
    }

    @Test(timeout = 2000)
    public void testLockUnlock() {
        AtomicBoolean ack = new AtomicBoolean(false);

        Lock lock = new Lock();
        lock.lock();

        new Thread(() -> waitAndUnlock(lock, 1000, ack)).start();

        lock.lock();
        Assert.assertTrue(ack.get());
        Assert.assertTrue(lock.isLocked());

        lock.unlock();
        Assert.assertFalse(lock.isLocked());
    }

    @Test
    public void testLockBlocksIndefinitely() throws InterruptedException {
        Lock lock = new Lock();
        lock.lock();

        // Attempt to acquire lock again, should block (because wasn't unlocked)
        Thread indefinitelyBlocked = new Thread(() -> lock.lock());
        indefinitelyBlocked.start();

        Thread.sleep(1000);

        Assert.assertTrue(indefinitelyBlocked.isAlive());
    }

    @Test
    public void testLockOnlyAcquiresOnce() throws InterruptedException {
        Lock lock = new Lock();

        Thread thread = new Thread(() -> acquireLockTwice(lock));
        thread.start();

        Thread.sleep(1000);

        Assert.assertTrue(thread.isAlive());
    }

    @Test
    public void testConcurrentLockContention() throws InterruptedException {
        Lock lock = new Lock();
        Thread contention = new Thread(() -> lockAndUnlock(lock));

        // One thread should block and resolve when other thread finishes
        contention.start();
        lock.lock();

        Thread.sleep(1000);

        lock.unlock();

        // Sleep long enough for both threads to resolve (regardless of acquire order)
        Thread.sleep(2000);
        Assert.assertFalse(contention.isAlive());
        Assert.assertFalse(lock.isLocked());
    }

    @Test
    public void testRegisterCallback() {
        Lock lock = new Lock();
        lock.registerCallback((callbackLock) -> Assert.assertSame(lock, callbackLock));

        lock.lock();

        lock.unlock();
    }

    private void waitAndUnlock(Lock lock, int waitMillis, AtomicBoolean ack) {
        Assert.assertTrue(lock.isLocked());
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e) {
            // Simply continue on interrupt
        }

        ack.set(true);
        lock.unlock();
    }

    private void lockAndUnlock(Lock lock) {
        lock.lock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Simply continue with unlock
        }

        lock.unlock();
    }

    private void acquireLockTwice(Lock lock) {
        lock.lock();
        lock.lock();
    }
}