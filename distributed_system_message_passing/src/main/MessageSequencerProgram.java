package main;

import java.util.ArrayList;

public class MessageSequencerProgram {

	private ArrayList<StandardThread> threads;
    /**
     * Parse command line inputs. Return number of threads to be executed. Default is five
     *
     * @param args command line arguments
     * @return number of threads to execute
     */
    private int parseArgs(String[] args,int i) {

        int num = 0;

        if(args.length == 0) {
            System.out.println("Number of messages/threads to send not specified. Use default.");
            if(i == 0) {
            	num = 5;
            }else {
            	num = 1000;
            }
        }
        if(args.length == 2) {
        	num = Integer.parseInt(args[i]);
        }

        return num;
    }
    
    public ArrayList<StandardThread> getThreads(){
    	return this.threads;
    }
 
    /**
     * Run message sequence program. Create StandardThreads and MessageSequencer. Generate external messages
     *
     * @param args command line arguments
     */
    public void run(String[] args) {

        // number of threads to be executed
        int numT = parseArgs(args,0);
        // number of messages to be send
        int numM = parseArgs(args,1);
        // create a message sequencer and start it
        MessageSequencer m = new MessageSequencer(numM);
        System.out.println(m.getName());
        m.start();

        // create list of threads and pass them a reference to the message sequencer
        ArrayList<StandardThread> ts = new ArrayList<>();
        for(int i = 0; i < numT; i++) {
            StandardThread t = new StandardThread(m,numM);
            System.out.println(t.getName());
            ts.add(t);
            t.start();
        }
        // pass a reference to the threads to the ms
        m.setThreads(ts);
        
        this.threads = ts;
        
        

    }
}
