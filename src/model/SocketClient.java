package model;

import view.IMessages;
import view.Printer;
import java.io.*;
import java.net.Socket;
import java.util.stream.IntStream;

@SuppressWarnings("Duplicates")
public class SocketClient{
    // receive from server word "20""100" where 20 is length of a line in matrix
    // receive "/20 ints//20 ints/
    // stop when receive "-100", otherwise print error
    private Socket clientSocket;
    DataOutputStream out;
    DataInputStream in;
    boolean broken = false;
    Printer printer;

    public SocketClient(int i){
        printer = new Printer(IMessages.CLIENT + i);
    }

    private void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private int available() {
        int i = -1;

        try {
            i = in.available();
        }  catch (IOException e){
            broken = true;
            //e.printStackTrace();
        }
        return i;
    }

    private void writeInt(int num){
        try{
            out.writeInt(num);
        } catch (IOException e){
            broken = true;
            //e.printStackTrace();
        }
    }


    private int readInt(int num){
        int i = -1;

        try{
            i = in.readInt();
        } catch (IOException e){
            broken = true;
            //e.printStackTrace();
        }
        return i;
    }

    private int readInt(){
        while (available() < 4);
        return readInt(0);
    }

    public void calulate(String ip, int port) throws IOException {
        printer.print(IMessages.START_OF_CONNECTION);

        startConnection(ip, port);

        int size = readInt();
        int message = size * 4 * 2;
        int code=-1;

        proccess:
        while (true){
            while (available() < 4){
                if (broken){
                    break proccess;
                }
            }
            code = readInt();
            if (code != 100){
                if (code != -100){
                    broken = true;
                }
                break proccess;
            }

            while (available() < message){
                if (broken){
                    break proccess;
                }
            }

            int[] arr1 = IntStream.range(0, size).map(this::readInt).toArray();
            if (broken){
                break proccess;
            }
            printer.print(IMessages.RECEIVED, IMessages.MATRIX_A, IMessages.SUCCESSFULLY);
            printer.print(arr1);

            int[] arr2 = IntStream.range(0, size).map(this::readInt).toArray();
            if (broken){
                break proccess;
            }
            printer.print(IMessages.RECEIVED, IMessages.MATRIX_B, IMessages.SUCCESSFULLY);
            printer.print(arr2);

            int[] arr3 = Matrix.addLines(arr1, arr2);
            IntStream.range(0, size).forEach(index -> writeInt(arr3[index]));
            if (broken){
                break proccess;
            }
            printer.print(IMessages.SENT, IMessages.SUCCESSFULLY);
            printer.print(arr3);
        }

        if (broken){
            printer.print(IMessages.FAILED);
        } else {
            printer.print(IMessages.SUCCESSFULLY);
        }

        printer.print(IMessages.END_OF_CONNECTION);
        stopConnection();
    }
}
