package stage_2;

import java.util.concurrent.*;

public class CallableAndFuture {
    public static void main(String[] args) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);

            // Runnable task returns nothing
            Runnable runnableTask = () -> {
                try {
                    Thread.sleep(2000);
                    System.out.println("Runnable Finished");
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            };

            // Callable task returns Integer
            Callable<Integer> callableTask = () -> {
                Thread.sleep(3000);
                System.out.println("Callable Finished");
                return 101;
            };

            // .submit() does two things:
            // 1. wraps the task in a FutureTask
            // 2. puts it in the thread pool queue
            // Then a worker thread executes it
            executorService.submit(runnableTask);
            Future<Integer> callableFuture = executorService.submit(callableTask);

            // this is blocking call and until the result is returned from the callable
            Integer callableResult = callableFuture.get();
            System.out.println(callableResult);

            System.out.println("Program End..");

            // Thread pools create non-daemon threads.
            // which means, even after tasks finish, the pool threads are still waiting for new tasks.
            // and hence the program doesn't end, if shutdown is not applied
            executorService.shutdown();

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
