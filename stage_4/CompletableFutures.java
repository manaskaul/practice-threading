package stage_4;

import java.util.concurrent.CompletableFuture;

public class CompletableFutures {
    public static void main(String[] args) {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 10);

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> 20);

        int result = f1.thenCombine(f2, (a, b) -> a + b).join();

        System.out.println(result); // 30
    }
}
