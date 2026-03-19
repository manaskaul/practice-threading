# Processes vs Threads

## Process

A process is a running instance of a program.

**Example:** Chrome browser, IntelliJ, Your Java application
Each process has its own:
- Memory space
- Heap
- Stack
- System resources

Processes cannot directly access each other's memory.

## Thread

A thread is a lightweight unit of execution inside a process.
A single process can have multiple threads running simultaneously.

**Example inside a Java server:**
```
    Java Process
        |
        |--- Thread 1 → Handle HTTP request
        |--- Thread 2 → Handle HTTP request
        |--- Thread 3 → Background logging
        |--- Thread 4 → Metrics reporting
```

All threads share:
- Heap memory
- Objects
- Variables
- Resources

But each thread has its own:
- Stack
- Program counter
- Local variables

```java
class MyThread extends Thread {

    public void run() {
        for(int i = 0; i < 5; i++) {
            System.out.println("Thread: " + i);
        }
    }
}

public class Main {

    public static void main(String[] args) {

        MyThread t1 = new MyThread();

        t1.start();

        for(int i = 0; i < 5; i++) {
            System.out.println("Main: " + i);
        }

    }
}
```

# Thread Lifecycle

A Java thread moves through several states during its life.

## States
- **NEW**
    - Thread object created but not started. `Thread t = new Thread(task);`
- **RUNNABLE**
    - When you call: `t.start();`
    - Thread enters RUNNABLE state.
    - RUNNABLE means eligible to run, not necessarily running. The OS scheduler decides when it actually runs.
- **RUNNING**
    - The thread is currently executing on a CPU core.
- **BLOCKED / WAITING / TIMED_WAITING**
    - **BLOCKED**
        - Waiting for a lock.
        - **Example:**
            - Thread1 holds synchronized lock
            - Thread2 tries to enter → BLOCKED
    - **WAITING**
        - Waiting indefinitely.
        - **Example:**
            - `Thread.join()`
    - **TIMED_WAITING**
        - Waiting with timeout.
            - `Thread.sleep()`
            - `wait(timeout)`
            - `join(timeout)`
- **TERMINATED**
    - Thread finishes execution.
    - `run()` completes
    - Thread cannot restart.

# Ways to Create Threads

There are 3 ways in Java:
1.  `Thread`
2.  `Runnable`
3.  `Callable` + `Future`

## Why Runnable exists?

Instead of extending `Thread`, Java prefers separating the task from the thread.
Think like this: **Thread = worker ; Runnable = job**

**Example:**

```java
class Task implements Runnable {

    public void run() {
        for(int i = 0; i < 5; i++) {
            System.out.println(
                Thread.currentThread().getName() + " : " + i
            );
        }
    }
}

public class Main {

    public static void main(String[] args) {

        Task task = new Task();

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();

    }
}
```

## Why Runnable is preferred?

If you extend `Thread`: `class MyThread extends Thread`
you lose the ability to extend another class.

But with `Runnable`: `class Task implements Runnable`
you keep normal inheritance.

That’s why Executors, thread pools, and Spring all use `Runnable` internally.

# Context Switching

CPUs have limited cores.

**Example:**
- 8 CPU cores
- 100 threads

The OS rapidly switches between threads.
- Thread1 runs
- ↓
- Thread2 runs
- ↓
- Thread3 runs
- ↓
- Thread1 runs again

This switching is called **context switching**.

## Why context switching is expensive

Because it involves:
- CPU state saving
- memory operations
- scheduler decisions

This is why too many threads hurt performance.

# Concurrency and Race Condition

The moment multiple threads touch the same data (like incrementing a counter), problems start.
This is called a **Race Condition**.

When multiple threads execute this simultaneously, race conditions occur.
**Synchronization** solves this by introducing mutual exclusion.

```java
    public synchronized void increment() {
        counter++;
    }
```

## Method vs Block Synchronization

### Method level

```java
    public synchronized void increment() {
        counter++;
    }
```

This locks the current object.
Equivalent to: `synchronized(this)`

### Block level (more flexible)

```java
    public void increment() {
        synchronized(this) {
            counter++;
        }
    }
```

This allows locking only part of a method. Example:

```java
    public void process() {

        readFile(); // no lock

        synchronized(this) {
            counter++;
        }

        logResult(); // no lock
    }
```

## What Exactly Gets Locked?

Java locks **objects**, not methods.

When you write: `synchronized(this)`
You lock the current object instance.

If you had:

```java
    Counter c1 = new Counter();
    Counter c2 = new Counter();
```

Then:
- Thread1 locking `c1`
- Thread2 locking `c2`

These do NOT block each other. Different objects → different locks.

### Important Insight -

`synchronized` gives two guarantees:
1.  **Mutual Exclusion**
    - Only one thread executes critical section.
2.  **Memory Visibility**
    - All threads see the latest value of variables.

# Intrinsic Locks (Monitors)

When you write:

```java
    synchronized(this) {
        counter++;
    }
```

Java uses an **intrinsic lock** attached to every object.
Every object in Java has a **monitor lock**.

Conceptually:
```
    Object
    ├── data
    └── monitor (lock)
```

## How synchronized works

- Thread tries to enter `synchronized` block
- ↓
- Acquire monitor lock
- ↓
- Execute critical section
- ↓
- Release monitor

If another thread tries to enter:
- Thread → BLOCKED

# volatile vs synchronized

Modern CPUs do not read variables directly from main memory every time.
Each CPU core has its own **local cache**.

Simplified architecture:

```
        Main Memory (RAM)
             |
   ---------------------------
   |            |            |
CPU Core 1   CPU Core 2   CPU Core 3
   |            |            |
L1 Cache     L1 Cache     L1 Cache
```

Threads running on different cores may read different cached copies of the same variable.

```java
class FlagExample {

    boolean running = true;

    void stop() {
        running = false;
    }

    void work() {
        while(running) {
            System.out.println("Working...");
        }
    }
}
```

- Thread 1: `start()`
- Thread 2: `stop()`

You might expect the loop to stop.
But sometimes it never stops.

**Why?**
Because Thread 1 may keep reading its cached copy of `running`. Thread1 never sees the change.
This is a **visibility problem**.

Declaring a variable `volatile`:
`volatile boolean running = true;`
tells the JVM: Do NOT cache this variable per thread. Always read/write from main memory.

Now when Thread 2 writes: `running = false;`
Thread 1 will immediately see the update.

- Without `volatile`, the loop may run forever.
- With `volatile`, it stops correctly.

`volatile` does **not** make operations atomic.
Example:
```java
    volatile int counter = 0;

    counter++;
```

Still unsafe. Because `counter++` is still:
- read
- add
- write

Two threads can still collide. So this is still a race condition.
Even though `volatile` ensured both threads read the latest value, the update itself wasn't protected.

# The Problem With synchronized

`synchronized` guarantees correctness, but it introduces **blocking**.
If one thread holds the lock:
- Thread1 → acquires lock
- Thread2 → BLOCKED
- Thread3 → BLOCKED

Threads may:
- wait
- context switch
- wake up later

All of this costs performance.
High-performance systems try to avoid blocking.

Instead of locking, try this approach:
1.  Read value
2.  Attempt update
3.  If another thread changed it → retry

This technique uses a CPU instruction called: **Compare-And-Swap (CAS)**

## CAS (Compare-And-Swap)

CAS is an atomic CPU instruction.
It works like this:
- compare `current_value` with `expected_value`
- if equal → replace with `new_value`
- if not equal → do nothing

**Example:**
- `counter = 5`
- Thread1: `CAS(5 → 6)` ✔ success
- Thread2: `CAS(5 → 6)` ✖ fail

Thread2 sees the value changed and retries.

Meaning:
- try update
- if fail → retry

This is called **optimistic concurrency**. Instead of locking first, you try and retry if needed.

Java exposes CAS via classes like:
- `AtomicInteger`
- `AtomicLong`
- `AtomicReference`

## Why CAS Is Fast

CAS avoids:
- thread blocking
- OS scheduling
- monitor locks

Instead:
- try update
- retry if conflict

For low contention, this is extremely fast. That’s why high-performance frameworks rely on it.

But **CAS Is Not Perfect**. CAS can struggle under heavy contention.
Example:
- 100 threads updating same variable

Many CAS operations fail → many retries → CPU spinning.

To solve this, Java introduced things like:
- `LongAdder`
- Striped counters

These spread updates across multiple variables.
Instead of one shared counter, `LongAdder` uses multiple counters.
Threads update different cells. Much less contention.

## When to Use CAS vs Locks

| Tool            | Best for                 |
| --------------- | ------------------------ |
| `synchronized`  | simple locking           |
| `ReentrantLock` | advanced lock control    |
| `AtomicInteger` | lock-free counters       |
| `LongAdder`     | high-contention counters |
