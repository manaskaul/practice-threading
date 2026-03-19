package core;

public class Volatile {
    public static void main(String[] args) throws InterruptedException {
        MyFlag flag = new MyFlag();
        
        Runnable r1 = () -> {
            flag.work();
        };
        Thread t1 = new Thread(r1);
        t1.start();

        Thread.sleep(250);
        
        Runnable r2 = () -> {
            flag.stop();
        };
        Thread t2 = new Thread(r2);
        t2.start();
    }
}

class MyFlag {
    private volatile boolean isRunning = true;

    public void stop() {
        isRunning = false;
    }

    public void work() {
        while(isRunning) {
            // do some work
            // Program auto stops and ends when using volatile
        }
    }
}
