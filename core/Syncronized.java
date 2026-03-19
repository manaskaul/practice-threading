package core;

public class Syncronized {
    public static void main(String[] args) throws InterruptedException {
        MyCounter2 counter = new MyCounter2();

        Runnable runnable = () -> {
            for(int i=0; i<100000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        counter.display();
    }    
}

class MyCounter2 {
    int counter;

    MyCounter2() {
        this.counter = 0;
    }

    public synchronized void increment() {
        counter++;
    }

    public void display() {
        System.out.println(counter);
    }
}
