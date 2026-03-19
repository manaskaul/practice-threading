package stage_3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueues {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        try {
            new Thread(() -> {
                try {
                    for(int i = 1; i <= 10; i++) {
                        queue.put(i);
                        System.out.println("Produced: " + i);
                        Thread.sleep(100);
                    }
                } catch (Exception e) {}
            }).start();

            Thread.sleep(800);

            new Thread(() -> {
                try {
                    while(true) {
                        int val = queue.take();
                        System.out.println("Consumed: " + val);
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {}
            }).start();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        
    }
}
