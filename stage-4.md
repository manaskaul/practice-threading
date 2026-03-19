# CompletableFuture

## 1. The Old Way (blocking)

```java
int result = service.call();
System.out.println(result);
```

**Problem:**
-   main thread waits (blocked)
-   nothing else happens

## 2. Slightly better (Thread)

```java
new Thread(() -> {
    int result = service.call();
    System.out.println(result);
}).start();
```

**Problems:**
-   no return value
-   hard to coordinate
-   manual thread management
-   messy

## 3. Future (improvement)

```java
Future<Integer> future = executor.submit(() -> service.call());
int result = future.get();
```

**Better, but:**
-   `get()` BLOCKS
-   no chaining
-   no composition

## 4. The Real Need

We want to:
-   run tasks asynchronously
-   do other work
-   chain operations
-   combine results
-   handle errors cleanly

**Solution: `CompletableFuture` = async + composable + non-blocking**

```java
CompletableFuture<Integer> future =
    CompletableFuture.supplyAsync(() -> {
        return 10;
    });

System.out.println("Doing something else...");

int result = future.join();
System.out.println(result);
```

**What’s happening:**
-   task runs in background thread
-   main thread continues
-   `join()` waits only when needed

## `supplyAsync()` vs `runAsync()`

-   `supplyAsync`: returns value
    ```java
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 42);
    ```
-   `runAsync`: no return
    ```java
    CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                                        System.out.println("Hello");
                                    });
    ```

## `thenApply()` vs `thenAccept()` vs `thenRun()`

-   `thenApply` → transforms result
-   `thenAccept` → consumes result
-   `thenRun` → ignores result

## `thenApply()` vs `thenApplyAsync()`

-   `thenApply()` → runs in **SAME** thread that completed previous stage (lightweight transformations)
-   `thenApplyAsync()` → runs in **DIFFERENT** thread (usually thread pool) (heavy/blocking work)

## Combining Futures

### Parallel execution

```java
CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> task1());
CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> task2());
```

These run: **IN PARALLEL**

### Combine results

```java
CompletableFuture<Integer> result = f1.thenCombine(f2, (a, b) -> a + b);
```

`thenCombine` waits for **BOTH** futures.

-   `thenApply` → chain
-   `thenCombine` → merge
-   `allOf` → wait all
-   `anyOf` → wait first

## Error Handling

```java
CompletableFuture<Integer> f =
    CompletableFuture.supplyAsync(() -> {
        throw new RuntimeException("fail");
    });

f.join(); // 💥 exception thrown here
```

-   The exception is captured inside the `CompletableFuture`.
-   The future completes **EXCEPTIONALLY**.
-   The error is stored inside the future.
