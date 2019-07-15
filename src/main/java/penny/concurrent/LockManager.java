package main.java.penny.concurrent;

import main.java.penny.constants.MarketDataConstants;

import java.util.*;

/**
 * LockManager represents a singleton collection of Locks that manages distributing the next available lock from a
 * fixed pool of locks upon request.  When no locks are available, the LockManager blocks the current thread's
 * execution until a lock becomes available.  LockManager matches integer ids to lock instances until they are
 * released.  The same lock instance can be acquired from the manager by requesting a lock with the same id allowing
 * execution blocking coordination across multiple threads.
 */
public class LockManager {

    /** Fixed number of locks allocated by this manager */
    private static final int NUM_LOCKS = MarketDataConstants.MAX_CONCURRENT_MARKET_DATA_REQUESTS;

    /** Singleton LockManager */
    private static LockManager manager;

    /** Current queue of available locks */
    private Queue<Lock> freeLocks;

    /** Map from integer ids to currently distributed locks */
    private Map<Integer, Lock> activeLocks;

    /** Reverse mapping from currently distributed locks to requesting integer ids */
    private Map<Lock, Integer> lockOwners;

    /**
     * Constructs a new lock manager allocating the number of locks specified.
     */
    private LockManager(int numberOfLocks) {
        this.activeLocks = new HashMap<Integer, Lock>();
        this.lockOwners = new HashMap<Lock, Integer>();
        this.freeLocks = new LinkedList<Lock>();

        for (int i = 0; i < numberOfLocks; i++) {
            Lock lock = new Lock();
            lock.registerCallback(this::onUnlock);
            this.freeLocks.add(lock);
        }
    }

    /**
     * Initializes a new singleton LockManager with the static number of locks defined by LockManager.
     */
    public static void init() {
        init(NUM_LOCKS);
    }

    /**
     * Initializes a new singleton LockManager with the number of locks specified.
     *
     * @param numberOfLocks The number of available locks to allocate in the singleton LockManager
     */
    public static synchronized void init(int numberOfLocks) {
        manager = new LockManager(numberOfLocks);
    }

    /**
     * Returns the singleton instance of the current LockManager.
     */
    public static LockManager getInstance() {
        if (manager == null) {
            LockManager.init();
        }
        return manager;
    }

    /**
     * Retrieves the next available lock from this LockManager.  Associates the retrieved lock with the lock identifier
     * provided.  If no locks are available to acquire, the LockManager blocks the current thread until a lock is
     * freed and can be distributed.  If a lock is already associated with the provided identifier, the same instance
     * is returned instead.
     *
     * @param lockId The identifier to associate with the retrieved lock.  If a lock is already associated with
     *               this id (and has not been unlocked yet), the same lock instance is returned instead.
     * @return The next available lock from this LockManager or the previously retrieved lock associated with the
     *         specified id (given the associated lock has yet to be unlocked).
     */
    public synchronized Lock getLock(int lockId) {
        if (this.activeLocks.containsKey(lockId)) {
            return this.activeLocks.get(lockId);
        }

        // Wait to acquire the next available lock in this manager
        while (this.freeLocks.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // Simply check to see if this is still locked, if so wait again, if not acquire lock
            }
        }

        Lock lock = this.freeLocks.remove();
        this.activeLocks.put(lockId, lock);
        this.lockOwners.put(lock, lockId);

        return lock;
    }

    /**
     * Lock instance callback function.  When a lock is unlocked, executes this function updating the
     * LockManager accordingly.  Specifically, makes the provided lock newly available in this LockManager and
     * clears any identifiers previously associated with the lock.
     *
     * @param lock
     */
    synchronized void onUnlock(Lock lock) {
        if (this.lockOwners.containsKey(lock)) {
            int lockOwner = this.lockOwners.get(lock);

            this.lockOwners.remove(lock);
            this.activeLocks.remove(lockOwner);

            this.freeLocks.add(lock);
            this.notifyAll();
        }
    }

    /**
     * Testing utility function that clears the initialized Singleton LockManager
     */
    static synchronized void cleanup() {
        manager = null;
    }
}
