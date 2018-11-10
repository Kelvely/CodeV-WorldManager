package ink.aquar.util.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Locker<K> {

    // Use Guava map maker can improve the performance by 50%
    private final ConcurrentHashMap<K, Drawer> drawers = new ConcurrentHashMap<>();

    public void lock(K key) {
        Drawer drawer = drawers.compute(key, (k, d) -> {
            if (d == null) {
                d = new Drawer();
            }
            d.numOfAcquirement++;
            return d;
        });
        drawer.lock.lock();
        drawer.currentThread = Thread.currentThread();
    }

    public void unlock(K key) {
        drawers.computeIfPresent(key, (k, d) -> {
            if(d.currentThread == Thread.currentThread()) {
                d.numOfAcquirement--;
                d.currentThread = null;
                d.lock.unlock();
                if(d.numOfAcquirement == 0) {
                    return null;
                } else {
                    return d;
                }
            } else {
                return d;
            }
        });
    }


    private static class Drawer {
        private final Lock lock = new ReentrantLock();
        private volatile int numOfAcquirement = 0;
        private volatile Thread currentThread;
    }

}
