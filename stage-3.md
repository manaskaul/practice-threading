# ReentrantLock

## Limitations of `synchronized`

1.  **No try without blocking**
    -   No way to say: "try to get lock, if not available → do something else"
2.  **No timeout**
    -   You cannot do: "wait max 100ms for lock"
    -   This is critical in:
        -   high-performance systems
        -   avoiding deadlocks
        -   responsive systems
3.  **No interruptible lock**
    -   If a thread is blocked: Thread stuck forever
    -   You cannot: interrupt it safely
4.  **No fairness control**
    -   `synchronized` uses unfair locking.
        -   no guarantee of order
        -   thread starvation possible
5.  **No multiple conditions**
    -   With `synchronized`, you only get: `wait()`, `notify()`, `notifyAll()`
    -   Very limited.
    -   You cannot have: multiple wait conditions on same lock
6.  **Hard to debug / introspect**
    -   You cannot ask:
        -   who holds the lock?
        -   how many threads waiting?

## What ReentrantLock Gives You

-   `tryLock()`
-   `tryLock(timeout)`
-   `lockInterruptibly()`
-   fair locks
-   `Condition` variables
-   better control

-   `synchronized` → simple, implicit locking
-   `ReentrantLock` → advanced, explicit locking

### Basic usage

```java
Lock lock = new ReentrantLock();

lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

**Important:** `unlock()` must always be in `finally`.

### Features

1.  **Reentrancy**
    -   same thread can acquire the same lock multiple times
    -   **Example:**
        ```java
        lock.lock();
        lock.lock(); // allowed
        ```
    -   But you must unlock same number of times:
        ```java
        lock.unlock();
        lock.unlock();
        ```

2.  **`tryLock()`: non-blocking acquisition**
    -   This allows: attempt lock WITHOUT blocking
    -   This is heavily used in:
        -   high-performance systems
        -   avoiding deadlocks
        -   fallback strategies
    -   If lock is free → acquire immediately
    -   If not → return `false` (no waiting)
    -   **Example:**
        ```java
        if(lock.tryLock()) {
            try {
                // critical section
            } finally {
                lock.unlock();
            }
        } else {
            // do something else
        }
        ```

3.  **`tryLock(timeout)`: bounded waiting**
    -   Prevents indefinite blocking
    -   Useful in responsive systems
    -   **Example:**
        ```java
        if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
            try {
                // work
            } finally {
                lock.unlock();
            }
        } else {
            // timeout handling
        }
        ```

4.  **`lockInterruptibly()`: interruptible lock**
    ```java
    lock.lockInterruptibly();
    try {
        // critical section
    } finally {
        lock.unlock();
    }
    ```

5.  **Fair vs Unfair Locks**
    ```java
    Lock lock = new ReentrantLock(true); // fair
    ```
    -   **Unfair (default)**
        -   threads may "jump the queue"
        -   higher throughput
        -   possible starvation
    -   **Fair**
        -   first-come-first-serve
        -   no starvation
        -   lower throughput
    -   **Example**
        -   **Unfair:**
            -   Thread A waiting
            -   Thread B arrives later
            -   Thread B acquires lock first
        -   **Fair:**
            -   Thread A gets lock first

## `synchronized` `wait()`/`notify()` VS `ReentrantLock` `await()`/`signal()`

### The Problem

```java
Queue<Integer> queue = new LinkedList<>();

// Thread A (consumer):
if(queue.isEmpty()) {
    // what should it do?
}
```

-   **Bad approach (busy waiting)**
    ```java
    while(queue.isEmpty()) {
        // keep checking
    }
    ```
    -   CPU waste (spins continuously)
-   We want: "If queue is empty → SLEEP; When item is added → WAKE UP"
-   This is exactly what `wait()` / `notify()` solve.

### `wait()` (sleep properly)

```java
synchronized(lock) {
    while(queue.isEmpty()) {
        lock.wait();
    }
}
```

-   **What happens:**
    1.  Thread releases the lock
    2.  Thread goes to sleep
    3.  Thread is parked (no CPU usage)

### `notify()` (wake up)

```java
synchronized(lock) {
    while(queue.isEmpty()) {
        lock.wait();
    }
}
```

-   **What happens:**
    1.  One waiting thread is woken up
    2.  It tries to re-acquire the lock
    3.  Then continues execution

-   `wait()` → "I can't proceed, I'll sleep"
-   `notify()` → "Something changed, wake someone up"

### Problems with this model:

-   Only ONE waiting group per lock
-   `notify()` is vague (who wakes up?)
-   easy to make mistakes

With `ReentrantLock`, instead of ONE wait-set, we get MULTIPLE wait-queues, each with a purpose.

### Using `Condition`

```java
Lock lock = new ReentrantLock();
Condition notEmpty = lock.newCondition();

// Consumer:
lock.lock();
try {
    while(queue.isEmpty()) {
        notEmpty.await();
    }
    queue.remove();
} finally {
    lock.unlock();
}

// Producer:
lock.lock();
try {
    queue.add(1);
    notEmpty.signal();
} finally {
    lock.unlock();
}
```

# BlockingQueue (practical concurrency)

A `BlockingQueue` is: a thread-safe queue that can BLOCK threads.

Two key behaviors:
-   When queue is **EMPTY**: `queue.take();` → Thread waits (blocks) until item is available.
-   When queue is **FULL**: `queue.put(item);` → Thread waits (blocks) until space is available.

Without `BlockingQueue`, you'd write locks, `wait()`, `notify()`, condition handling, and handle edge cases. With `BlockingQueue`, all of this is handled internally.

| Method  | Behavior          |
| ------- | ----------------- |
| `put()` | blocks if full    |
| `take()`| blocks if empty   |
| `offer()`| non-blocking add  |
| `poll()`| non-blocking remove|

Internally, `BlockingQueue` uses `ReentrantLock` + `Condition` (`notEmpty`, `notFull`).

## Types of `BlockingQueue`

1.  **`ArrayBlockingQueue`**: `new ArrayBlockingQueue<>(capacity)`
    -   fixed size, bounded, predictable memory. Most common in production.
2.  **`LinkedBlockingQueue`**: `new LinkedBlockingQueue<>()`
    -   optional capacity, can be unbounded. Risk of memory growth (OOM).
3.  **`PriorityBlockingQueue`**
    -   elements ordered by priority, not FIFO.
4.  **`SynchronousQueue`**
    -   NO storage, producer hands directly to consumer. Used in `CachedThreadPool`.

## Common Patterns

-   **Pattern 1 — Worker Pool**: tasks → queue → worker threads process
-   **Pattern 2 — Pipeline**: stage1 → queue → stage2 → queue → stage3
-   **Pattern 3 — Rate limiting / buffering**: fast producer, slow consumer, queue absorbs load.

# Semaphore

A `Semaphore` is: a counter that controls how many threads can access something at the same time.

```java
Semaphore semaphore = new Semaphore(3); // only 3 threads allowed at the same time
```

### Core Operations

-   **Acquire (enter)**: `semaphore.acquire();` → takes 1 permit, if none available → BLOCKS
-   **Release (exit)**: `semaphore.release();` → returns permit, wakes waiting thread

-   **Locks**: allow ONLY 1 thread
-   **Semaphore**: allow N threads

A binary Semaphore is equivalent to a lock, but less strict than `ReentrantLock`.

### Fair vs Unfair Semaphore

```java
new Semaphore(3, true); // fair
```

-   **Unfair (default)**: higher performance, no order guarantee.
-   **Fair**: FIFO ordering, lower performance.

### Common Use Cases

1.  Limit concurrency (e.g., only 10 DB connections allowed)
2.  Rate limiting (e.g., only N requests at a time)
3.  Resource pooling (e.g., thread pool, connection pool, API calls)

### `tryAcquire()`: Non-blocking version

```java
if(semaphore.tryAcquire()) {
    // proceed
} else {
    // skip
}
```

### Acquire with timeout: Wait only for limited time

```java
semaphore.tryAcquire(100, TimeUnit.MILLISECONDS);
```

# CountDownLatch

A `CountDownLatch` is a counter. It solves a common coordination problem: One or more threads must WAIT until some other tasks finish.

-   Threads can: wait until the counter reaches zero
-   Other threads: decrease the counter

When it reaches 0 → waiting threads are released.

```java
CountDownLatch latch = new CountDownLatch(3); // 3 events must happen before proceeding
```

### Two important methods

-   `await()`: `latch.await();` → Thread blocks until count becomes 0.
-   `countDown()`: `latch.countDown();` → decreases counter by 1.

### Difference from Semaphore

| Feature        | Semaphore         | CountDownLatch      |
| -------------- | ----------------- | ------------------- |
| **Purpose**    | limit concurrency | wait for completion |
| **Reusable**   | yes               | no                  |
| **Tracks threads**| no              | counts events       |

-   **Semaphore** → how many threads allowed
-   **CountDownLatch** → wait until tasks finish

# CyclicBarrier

-   `CountDownLatch` → one thread waits for others
-   `CyclicBarrier` → ALL threads wait for each other

### Core idea

```java
CyclicBarrier barrier = new CyclicBarrier(3); // 3 threads must reach barrier before ANY proceed
```

### Key Difference vs `CountDownLatch`

| Feature      | CountDownLatch        | CyclicBarrier         |
| ------------ | --------------------- | --------------------- |
| **Who waits**| one thread            | all threads           |
| **Resettable**| No                    | Yes                   |
| **Use case** | wait for completion   | sync phases           |

-   **`CountDownLatch`** → "wait until work finishes", "counts DOWN to 0"
-   **`CyclicBarrier`** → "wait for everyone before moving forward", "counts UP to required parties", "all must arrive or nobody proceeds"

**NOTE**: If one thread fails or gets interrupted, the barrier becomes **BROKEN**. Then all waiting threads throw `BrokenBarrierException`.

# ConcurrentHashMap

-   Normal `HashMap` in multithreading:
    -   ❌ NOT thread-safe
    -   ❌ data corruption possible

```java
ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();
```

-   thread-safe + high performance
-   fine-grained locking + lock-free reads

### Internal Design

-   array of buckets (like `HashMap`)
-   each bucket handled independently

### Real Use Cases

1.  **Caching**: store computed results, avoid recomputation.
2.  **Counters**: `map.compute(key, (k, v) -> v == null ? 1 : v + 1);`
3.  **Session tracking**: userId → session
4.  **Shared state in services**: config maps, in-memory stores, metrics

-   `map.putIfAbsent()`
-   `map.computeIfAbsent()`
