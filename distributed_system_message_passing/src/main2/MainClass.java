package main2;

import java.io.File;

public class MainClass {

    public static void main(String[] args) {

    	File file = new File("Logfiles");
        if(!file.exists()) {
            file.mkdir();
        }
        MessageGeneratorProgram mgp = new MessageGeneratorProgram();
        mgp.run(args);
        
    }
}