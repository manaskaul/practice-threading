package core;

public class RunnableBasics {
    public static void main(String[] args) {
        
        MyRunnable mr = new MyRunnable();
        
        Thread t1 = new Thread(mr);
        t1.start();
    
    }
}

class MyRunnable implements Runnable {

    @Override
    public void run() {
        System.out.println("This is my runnable");
    }
    
}