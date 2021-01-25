package main;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class MessageSequencer extends Thread{

    // reference to the threads
    private ArrayList<StandardThread> threads;
    // inbox queue
    public ArrayBlockingQueue<Message> inbox;
    private int numM;
    private int messagecounter; 

    public MessageSequencer(int numM) {
        this.inbox = new ArrayBlockingQueue<>(numM);
        this.numM = numM;
        this.messagecounter = 0;
    }

    public void setThreads(ArrayList<StandardThread> threads) {
        this.threads = threads;
    }

    @Override
    public void run() {
    	Message msg;
    	int messagenumber = 1;
    	while(this.messagecounter != this.numM) {
    		msg = this.inbox.poll();
    		if(msg != null) {
    			//set number of message
    			msg.setMessageNumber(messagenumber);
        		//send messages to all threads
    			for(int i = 0; i<this.threads.size();i++) {
    				try {
						this.threads.get(i).inbox.put(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			messagenumber++;
				this.messagecounter++;
    		}
    	}
    }
}
