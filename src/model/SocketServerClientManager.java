package model;

import view.IMessages;
import view.Printer;
import java.io.*;
import java.net.Socket;
import java.util.stream.IntStream;

@SuppressWarnings("Duplicates")
public class SocketServerClientManager implements Runnable{
    DataOutputStream out;
    DataInputStream in;
    private Matrix matrixA;
    private Matrix matrixB;
    private Matrix result;
    SocketServer socketServer;
    Socket clientSocket;
    int threadNumber;
    Boolean doRun = true;
    Boolean broken = false;
    int length;
    Printer printer;

    public SocketServerClientManager(SocketServer socketServer, Socket clientSocket, int threadNumber) {
        matrixA = socketServer.getMatrixA();
        matrixB = socketServer.getMatrixB();
        result = socketServer.getResult();
        this.socketServer = socketServer;
        this.clientSocket = clientSocket;
        this.threadNumber = threadNumber;
        length = result.size();
        printer = new Printer(IMessages.THREAD + threadNumber);
    }

    private int available() {
        int i = -1;
        try {
            i = in.available();
        }  catch (IOException e){
            broken = true;
            e.printStackTrace();
        }
        return i;
    }

    private void writeInt(int num){
        try{
            out.writeInt(num);
        } catch (IOException e){
            broken = true;
            e.printStackTrace();
        }
    }

    private int readInt(int num){
        int i = -1;
        try{
            i = in.readInt();
        } catch (IOException e){
            broken = true;
            e.printStackTrace();
        }
        return i;
    }

    private void init(){
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            // say to client size of our array
            out.writeInt(result.size());
            // say to server that we are working
            socketServer.calculation("start");
        } catch (IOException e) {
            e.printStackTrace();
            doRun = false;
        }
    }

    private void sendLine(int[] arr, String name, String row){
        IntStream.range(0, length).forEach(index -> writeInt(arr[index]));
        printer.print(IMessages.SENT, name, row);
        printer.print(arr);
    }

    @Override
    public void run() {
        printer.print(IMessages.START_OF_THREAD);
        init();

        int message = length * 4;//number of ints to receive from client

        process:
        while (doRun){
            int i = result.getIndex();
            if (i < length || (i = socketServer.unresolved("get")) >= 0 ){
                socketServer.unresolved("add", i);
                writeInt(100); // say client there is a line to read
                sendLine(matrixA.getLine(i), IMessages.MATRIX_A, String.valueOf(i));
                sendLine(matrixB.getLine(i), IMessages.MATRIX_B, String.valueOf(i));
                try {
                    out.flush();
                } catch (IOException e) {
                    broken = true;
                    e.printStackTrace();
                } finally {
                    printer.print(IMessages.SENT, IMessages.SUCCESSFULLY);
                }
            } else if (socketServer.unresolved("check") == 0) {
                try {
                    out.writeInt(-100); // say client there is nothing to read
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doRun = false;
                break;
            } else {
                continue;
            }
            if (broken){
                printer.print(IMessages.FAILED);
                break;
            }

            while(available() < message){
                if (broken){
                    printer.print(IMessages.FAILED);
                    break process;
                }
                if (socketServer.unresolved("check") <= 0){
                    break process;
                }
            }

            result.setLine(i, IntStream.range(0, length).map(this::readInt).toArray());
            if (broken){
                printer.print(IMessages.FAILED);
                break;
            }
            printer.print(IMessages.RECEIVED, IMessages.SUCCESSFULLY, IMessages.RESULT);
            printer.print(result.getLine(i));
            socketServer.unresolved("stop", i);
        }

        //  check if the task is finished
        if (socketServer.unresolved("check") == 0 && socketServer.calculation("get") == 1){
            socketServer.calculation("stop");
            //  check twice, just in case I forgot something
            if (socketServer.unresolved("check") == 0 && socketServer.calculation("get") == 0 && result.isEnd()){
                socketServer.doStop();
            }
        } else {
            socketServer.calculation("stop");
        }

        printer.print(IMessages.END_OF_THREAD);
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
