package main2;

import java.util.Comparator;

public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message m1, Message m2) {

        if(m1.getLamportstamp() < m2.getLamportstamp()){
            return -1;
        } else if(m1.getLamportstamp() > m2.getLamportstamp()) {
            return 1;
        } else if(m1.getRcptthread() < m2.getRcptthread()) {
            return -1;
        } else if(m1.getRcptthread() > m2.getRcptthread()) {
            return 1;
        }

        return 0;
    }
}
