package model;

import java.rmi.UnexpectedException;
import java.util.ArrayList;

// I wrote it to check if the problem was caused by removing made by other threads.
// It turned out to be a problem caused by Stream(I used forEach instead of filter.findFirst)
// in the end I ended up using remove(Integer i) in wich I wasnt sure before.
// So this class is useless, even more so since I use just while(blocked); instead of sleep or alike
public class ProtectedArrayList <E> {
    private ArrayList<E> list;
    Boolean blocked = false;
    public ProtectedArrayList(){
        list = new ArrayList<>();
    }

    public synchronized ArrayList<E> get() {
        return list;
    }
    public synchronized void block(){
        while(blocked) {
            //Thread.sleep(1000);
        }
        blocked = true;
    }
    public synchronized void release() throws UnexpectedException {
        if (blocked){
            blocked = false;
        } else {
            throw new UnexpectedException("Why isn't it blocked?");
        }
    }
}
