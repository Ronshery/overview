package main2;
import java.util.ArrayList;

public class MessageGeneratorProgram{
	
	
    /**
     * Parse command line inputs. Return number of threads to be executed or messages to generate. Default is five/1000
     *
     * @param args command line arguments
     * @param i what to return: 0 number of threads, 1 number of messages
     * @return number of threads to execute
     */
    private int parseArgs(String[] args, int i) {

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
     */
	
	public void run(String[] args) {
		
        // number of threads to be executed
        int numT = parseArgs(args,0);
        // number of messages to be send
        int numM = parseArgs(args,1);
        // create a message generator
        MessageGenerator m = new MessageGenerator();

        //Liste mit allen threads wird erstellt
        ArrayList<StandardThread> threads = new ArrayList<>();
        for(int i = 0; i< numT ; i++) {
        	//thread wird erstellt und in die Liste hinzugefügt
        	StandardThread t = new StandardThread(numM);
        	t.start();
        	threads.add(t);
        }
        //jeder Thread in der Liste bekommt eine Liste aller Threads
        for(int i=0; i< threads.size(); i++) {
        	threads.get(i).setThreads(threads);
        }
		
		// create list of messages
		ArrayList<Message> messages = new ArrayList<>();
		for(int i = 0; i<numM;i++) {
			Message msg = new Message(true,randomNumber(numM*10000),randomNumber(numT)); //(external,randompayload,randomthreadrcpt)
			messages.add(msg);
		}
		// pass a reference to the messages to the message generator
		m.setMessages(messages);
		// pass a reference to the threads to the message generator
		m.setThreads(threads);
		
		//start message generator
        m.start();
	}
}
