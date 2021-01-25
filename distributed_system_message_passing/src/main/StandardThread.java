package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class StandardThread extends Thread{

    // reference to the message sequencer
    private MessageSequencer m;
    private File file;
    private FileWriter writer;
    private int numM;
    private int messagecounter;
    // inbox queue
    public ArrayBlockingQueue<Message> inbox;
    private ArrayList<Message> messages;
    
    /**
     * Create thread with reference to the ms and a bounded inbox queue
     *
     * @param m message sequencer
     */
    public StandardThread(MessageSequencer m,int numM) {
        this.m = m;
        this.inbox = new ArrayBlockingQueue<>(numM*2);
        this.file = new File("Logfiles/"+this.getName()+"_logfile.txt");
        try {
			this.writer = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       /* try {
			this.writer.write(this.getName() + " Logfile\n");
			//this.writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        this.numM = numM;
        this.messagecounter = 0;
        this.messages = new ArrayList<>();
    }

    @Override
    public void run() {
    	Message msg;
    	while(this.messagecounter != numM) {
    		msg = this.inbox.poll();
    		if(msg != null) {
    			//if generator sent message
	    		if(msg.external()){
	    			//switch external msg to internal msg
	    			msg.switchMessageType();
					//put msg into inbox from sequencer
	    			try {
						m.inbox.put(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}else {//sequencer sent message
	    			//count messages to know when to break the loop
	    			this.messagecounter++;
	    			this.messages.add(msg);
	    			//System.out.println();
	    		}

    		}

    	}
    	
    	//write in log...

    	int msgnumber;
    	for(int i = 0; i<numM;i++) {
    		msgnumber = i+1;//sortedmsg[i];
    		for(int j = 0; j<this.messages.size();j++) {
    			msg = this.messages.get(j);
    			System.out.println(this.getName() + " Iteration "+ i+"\n nextmsgwrite: " + msgnumber +"\n actual msgnumber: " + msg.getMessageNumber() + "\n\n" );
    			if(msg.getMessageNumber() == msgnumber) {
    				try {
    					System.out.println("written");
						this.writer.write(Integer.toString(msg.getPayload()) + " msgnumber: "+ msg.getMessageNumber() +"\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				this.messages.remove(msg);
    				break;
    			}
    		}
    	}
    	
    	
    	try {
			this.writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
