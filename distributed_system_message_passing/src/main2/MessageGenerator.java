package main2;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class MessageGenerator extends Thread{
	
	private ArrayList<Message> messages;
	private ArrayList<StandardThread> threads;
	public ArrayBlockingQueue<Message> inbox;
	
	public MessageGenerator() {
		this.inbox = new ArrayBlockingQueue<>(10);
	}
	
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
	
	public void setThreads(ArrayList<StandardThread> threads) {
		this.threads = threads;
	}
    
    @Override
    public void run() {
    	int rcpt;
    	Message msg;
    	StandardThread t;
    	
    	//send message to individual rcpt (thread)
    	for(int i = 0; i<messages.size(); i++) {
    		msg = messages.get(i);
    		rcpt = msg.getRcptthread();
    		// get thread out of the threads list
    		t = threads.get(rcpt);
			//put message into inbox of thread
			try {
				t.inbox.put(msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
       	}
    	
    	System.out.println("generator done");
    	System.out.println("Threads are thinking...");
    }
}
