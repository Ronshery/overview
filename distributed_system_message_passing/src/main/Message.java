package main;

public class Message {

    private /*final*/ boolean external;
    private final int payload;
    private final int rcptthread;
    private int messagenumber; // which message is it? first,second,..

    /**
     * Create external or internal message with a payload
     *
     * @param ext true if external message
     * @param pay message payload
     * @param rcptthread recipient thread
     */
    public Message(boolean ext, int pay, int rcptthread) {
        this.external = ext;
        this.payload = pay;
        this.rcptthread = rcptthread;
    }
    
    /**
     * Get the rcptthread int
     * @return rcptthread
     */
    public int getRcptthread(){
    	return this.rcptthread;
    }
    
    public void switchMessageType() {
    	if(external == true) {
    		external = false;
    		
    	}else {
    		external = true;
    	}
    		
    		
    }
    
    public int getMessageNumber() {
    	return this.messagenumber;
    }
    
    public void setMessageNumber(int messagenumber) {
    	this.messagenumber = messagenumber;
    }


    /**
     * Check whether message is an external message
     *
     * @return true if message is an external message
     */
    public boolean external() {
        return this.external;
    }

    /**
     * Get message payload
     *
     * @return message payload
     */
    public int getPayload() {
        return this.payload;
    }
}
