package core;

public class RaceCondition {
    public static void main(String[] args) throws InterruptedException {
        MyCounter1 counter1 = new MyCounter1();
        
        CounterUpdaterRunnable runnable = new CounterUpdaterRunnable(counter1);

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);

        // Starts two individual threads
        t1.start();
        t2.start();

        // .join() waits for the threads to finish
        t1.join();
        t2.join();

        counter1.display();


        MyCounter1 counter2 = new MyCounter1();

        Runnable r = () -> {
            for(int i=0; i<100000; i++) {
                counter2.increment();
            }
        };

        Thread t3 = new Thread(r);
        Thread t4 = new Thread(r);
        
        // Starts two individual threads
        t3.start();
        t4.start();

        // .join() waits for the threads to finish
        t3.join();
        t4.join();

        counter2.display();
    }


}
class CounterUpdaterRunnable implements Runnable {
    MyCounter1 counter;

    CounterUpdaterRunnable(MyCounter1 counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        for(int i=0; i<100000; i++) {
            counter.increment();
        }
    }
}

class MyCounter1 {
    int counter;

    MyCounter1() {
        this.counter = 0;
    }

    public void increment() {
        counter++;
    }

    public void display() {
        System.out.println(counter);
    }
}