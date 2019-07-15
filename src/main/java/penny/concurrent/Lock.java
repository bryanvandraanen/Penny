package main.java.penny.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lock represents a simple locking mechanism that is not re-entrant (i.e. once lock is acquired, blocks all other
 * attempts to acquire the lock until it is unlocked).  Lock allows for convenient synchronization of resource
 * access multiple competing threads.  Lock also allows easy execution blocking and waiting until some event has
 * completed.  All of these uses and more are possible via appropriate calls to "lock" and "unlock" on a given instance.
 */
public class Lock {

    /**
     * Flag indicating whether the current instance is locked
     * and should prevent other threads from acquiring the lock
     */
    private boolean isLocked;

    /**
     * Registered callback functions the lock should execute following an unlock
     */
    private List<Consumer<Lock>> callbacks;

    /**
     * Constructs a new instance of a lock that is free and has no registered callbacks.
     */
    public Lock() {
        this.isLocked = false;
        this.callbacks = new ArrayList<Consumer<Lock>>();
    }

    /**
     * Locks the current lock.  If this instance is currently locked, blocks the acquiring thread
     * until it can successfully acquire the lock.  This method is thread-safe.
     */
    public synchronized void lock() {
        // Wait until this lock is no longer locked, then acquire
        while (this.isLocked()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Simply check to see if this is still locked, if so wait again, if not acquire lock
            }
        }

        this.isLocked = true;
    }

    /**
     * Unlocks the current lock.  Makes this instance now eligible to be locked and acquired by some other thread.
     * Additionally, executes all callbacks registered with this current lock prior to unlocking.
     */
    public synchronized void unlock() {
        this.isLocked = false;
        executeCallbacks();
        notifyAll();
    }

    /**
     * Returns true if this lock is currently acquired and locked.
     */
    public synchronized boolean isLocked() {
        return this.isLocked;
    }

    /**
     * Registers a new callback with this lock.  More specifically, adds the callback function provided
     * to this lock to be executed on unlocking this lock.
     *
     * @param callback The callback function to register with this lock.  The callback function provided
     *                 takes this lock as a parameter.
     */
    public synchronized void registerCallback(Consumer<Lock> callback) {
        this.callbacks.add(callback);
    }

    /**
     * Executes all the callbacks currently registered with this lock.
     */
    private void executeCallbacks() {
        for (Consumer<Lock> callback : this.callbacks) {
            callback.accept(this);
        }
    }
}
