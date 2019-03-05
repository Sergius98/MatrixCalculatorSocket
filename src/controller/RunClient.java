package controller;

import model.SocketClient;
import java.io.IOException;
import java.util.stream.IntStream;

public class RunClient implements Runnable{
    private int id = 0;
    RunClient(int i){
        id = i;
    }
    public void run(){
        SocketClient sc = new SocketClient(id);
        try {
            sc.calulate("127.0.0.1", 6644);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static public void main(String... args){
        IntStream.range(0, 10).forEach(i -> new Thread(new RunClient(i)).start());
    }
}
