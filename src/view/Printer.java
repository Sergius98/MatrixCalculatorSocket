package view;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Printer {
    String name;

    //I want a proper output
    static Semaphore semaphore = new Semaphore(1);

    public Printer(String name){
        this.name = name + ": ";
    }

    public void print(String... messages) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.print(name);
        Arrays.stream(messages).forEach(System.out::print);
        System.out.println();
        semaphore.release();
    }
    public void print(int[] messages) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.print(name);
        Arrays.stream(messages).forEach(m -> System.out.print(":" + m));
        System.out.println(":");
        semaphore.release();
    }
}
/*
public class Printer {
    public static void print(String... messages) {
        Arrays.stream(messages).forEach(System.out::print);
        System.out.println();
    }
    public static void print(int[] messages) {
        Arrays.stream(messages).forEach(m -> System.out.print(":" + m));
        System.out.println(":");
    }
}
*/