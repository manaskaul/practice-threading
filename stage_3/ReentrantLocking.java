package stage_3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLocking {
    public static void main(String[] args) {
        Lock lock = new ReentrantLock();

        lock.lock();
        try {
            // critical section
        } finally {
            lock.unlock();
        }
    }
}
