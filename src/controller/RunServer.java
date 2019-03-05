package controller;

import model.Matrix;
import model.SocketServer;
import java.io.IOException;

public class RunServer {
    static public void main(String... args) {
        int length = 10;
        Matrix matrixA = new Matrix(length);
        Matrix matrixB = new Matrix(length);
        Matrix matrixC;

        matrixA.initRandom();
        matrixB.initRandom();

        SocketServer ss = new SocketServer(6644, matrixA, matrixB);

        try {
            ss.work();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
