package main;
import java.util.ArrayList;

public class MessageGeneratorProgram{
	
	
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

    public int randomNumber(int numT) {
		double randomDouble = Math.random();
		randomDouble = randomDouble * numT;
		int randomInt = (int) randomDouble;
		
		return randomInt;
    	
	}
    
    /**
     * Run message generator program. Create Messages and MessageGenerator
     * 
     * @param args 
     * @param threads
     */
	
	public void run(String[] args,ArrayList<StandardThread> threads) {
		
        // number of threads to be executed
        int numT = parseArgs(args,0);
        // number of messages to be send
        int numM = parseArgs(args,1);
        // create a message generator
        MessageGenerator m = new MessageGenerator();

		
		// create list of messages and pass them a reference to the message generator
		ArrayList<Message> messages = new ArrayList<>();
		for(int i = 0; i<numM;i++) {
			Message msg = new Message(true,randomNumber(10000),randomNumber(numT)); //(external,randompayload,randomthreadrcpt)
			messages.add(msg);
		}
		// pass a reference to the message and to the message generator
		m.setMessages(messages);
		// pass a reference to the threads and to the message generator
		m.setThreads(threads);
		
		//start message generator
        m.start();
        		
	}
	

}
