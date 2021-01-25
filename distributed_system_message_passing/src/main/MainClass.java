package main;

import java.io.File;

public class MainClass {

    public static void main(String[] args) {

    	File file = new File("Logfiles");
    	if(!file.exists()) {
            file.mkdir();
        }
        MessageSequencerProgram msp = new MessageSequencerProgram(); 
        msp.run(args);
        MessageGeneratorProgram mgp = new MessageGeneratorProgram();
        mgp.run(args,msp.getThreads());
        
    }
}
