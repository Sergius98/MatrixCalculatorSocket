package model;

import view.IMessages;
import view.Printer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.UnexpectedException;
import java.util.stream.IntStream;

public class SocketServer {
    ServerSocket serverSocket;
    private Matrix matrixA;
    private Matrix matrixB;
    private Matrix result;
    private int port;
    private boolean doRun = true;
    private ProtectedArrayList<Integer> unresolved = new ProtectedArrayList<>();
    private int calculators = 0; // numbers of threads who currently is proceeding clients
    Printer printer;

    public SocketServer(int port, Matrix matrixA, Matrix matrixB) throws NumberFormatException {
        printer = new Printer("Server");

        this.port = port;

        printer.print(IMessages.MATRIX_A);
        IntStream.range(0, matrixA.size()).forEach(i -> printer.print(matrixA.getLine(i)));
        this.matrixA = matrixA;

        printer.print(IMessages.MATRIX_B);
        IntStream.range(0, matrixA.size()).forEach(i -> printer.print(matrixB.getLine(i)));
        this.matrixB = matrixB;

        result = new Matrix(matrixA.size());
        if (matrixA.size() != matrixB.size()){
            throw new NumberFormatException("matrix should be equal");
        }
    }

    private synchronized int getUnresolved(){
        unresolved.block();

        int i = unresolved.get().size() != 0 ? unresolved.get().remove(0) : -1;

        try {
            unresolved.release();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        return i;
    }

    private void addUnresolved(Integer i) {
        unresolved.block();

        unresolved.get().add(i);

        try {
            unresolved.release();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
    }
    private void stopUnresolved(Integer i) {
        unresolved.block();

        unresolved.get().remove(i);

        try {
            unresolved.release();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
    }

    private int checkUnresolved() {
        unresolved.block();

        int i = unresolved.get().size();

        try {
            unresolved.release();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        return i;
    }

    public synchronized int unresolved(String action, Integer... args){
        switch (action) {
            case "get":
                return getUnresolved();
            case "add":
                addUnresolved(args[0]);
                return -1;
            case  "stop":
                stopUnresolved(args[0]);
                return -1;
            case  "check":
            default:
                return checkUnresolved();
        }
    }

    public synchronized int calculation(String action){
        switch (action) {
            case "start":
                return ++calculators;
            case "stop":
                return --calculators;
            case  "get":
                default:
                return calculators;
        }
    }

    public void doStop(){
        doRun = false;
        try {
            // I close the socket because I want to get out of accept() loop
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void work() throws IOException {
        printer.print(IMessages.START_OF_CONNECTION);
        serverSocket = new ServerSocket(port);

        int i = 0;
        String inputLine;

        while (doRun) {
            try{
                new Thread(new SocketServerClientManager(this, serverSocket.accept(), i++)).start();
            } catch (SocketException ex){
                // It is expected, because I closed the socket in doStop();
                //ex.printStackTrace();
            }
        }

        printer.print(IMessages.END_OF_CONNECTION);
        printer.print(IMessages.RESULT);
        IntStream.range(0, result.size()).forEach(l -> printer.print(result.getLine(l)));
    }

    public Matrix getMatrixA() {
        return matrixA;
    }

    public Matrix getMatrixB() {
        return matrixB;
    }

    public Matrix getResult() {
        return result;
    }
}
