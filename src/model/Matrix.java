package model;

import java.util.Random;
import java.util.stream.IntStream;

public class Matrix {
    private int[][] value;
    private int size;
    private int pointer;
    Matrix(int size){
        value = new int[size][size];
        this.size = size;
    }
    void initRandom(){
        pointer = 0;
        int max = (Integer.MAX_VALUE / 2) - 1;
        Random random = new Random();
        IntStream.range(0, size).forEach(i -> IntStream.range(0, size).forEach(j -> value[i][j] = random.nextInt(max)));
    }
    void initZero(){
        pointer = 0;
        IntStream.range(0, size).forEach(i -> IntStream.range(0, size).forEach(j -> value[i][j] = 0));
    }
    public synchronized int getIndex(){
        return pointer++;
    }
    public int[] getLine(int index){
        return value[index];
    }
    public void setLine(int index, int[] newLine){
        value[index] = newLine;
    }
    static int[] addLines(int[] matrixA, int[] matrixB){
        return IntStream.range(0, matrixA.length).map(i -> matrixA[i] + matrixB[i]).toArray();
    }
}
