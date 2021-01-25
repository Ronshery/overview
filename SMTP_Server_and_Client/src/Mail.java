
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Mail {

	private String mailFrom;
	private ArrayList<String> mailTo;
	private int messageid;
	private String data = "";
	private ArrayList<FileChannel> channellist;
	private String state; //state tells us which command we have received before
	private String request = "";
	
	public Mail() {
		this.messageid = randomNumber();
		this.mailTo = new ArrayList<>();
		this.channellist = new ArrayList<FileChannel>();
	}
	
	
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
		System.out.println("setted mailfrom: " + this.mailFrom);
	}
	
	public void setMailTo(String mailTo) {
		try{
			this.mailTo.add(mailTo);
		} catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("added " + mailTo + " to mailTo list");
	}
	
	/**
	 * Stores the given data
	 * @param data
	 * @return 0, if its not the end of the mail and 1, if end of mail is reached
	 */
	public int setData(String data) {
		//System.out.println("data:\n" + data + "\n datalength: "+data.length());
		if(data.length()!= 0 && data.charAt(data.length()-5) == '\r' && data.charAt(data.length()-4) == '\n' &&
				data.charAt(data.length()-3) == '.' && data.charAt(data.length()-2) == '\r' &&
				data.charAt(data.length()-1) == '\n') {
			this.data = data.substring(0, data.length()-5);
			this.state = "messagereceived";
			System.out.println("setted data: " + this.data);
			return 1;
		} else {
			this.data += data;
			System.out.println(this.data);
			return 0;
		}
		
	}
	public String getData() {
		return this.data;
	}
	
	public String getMailFrom() {
		return this.mailFrom;
	}
	
	public List<String> getMailTo() {
		return this.mailTo;
	}
	
	public int getMessageID() {
		return this.messageid;
	}
	public int randomNumber() {
		double randomDouble = Math.random();
		randomDouble = randomDouble * 9999;
		int randomInt = (int) randomDouble;
		
		return randomInt;
	}
	
	public void setFileChannel(FileChannel channel) {
		this.channellist.add(channel);
	}
	
	public List<FileChannel> getFileChannels() {
		return this.channellist;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getState() {
		return this.state;
	}
	
	public void setRequest(String request) {
		this.request += request;
	}
	
	public String getRequest() {
		return this.request;
	}
	
	public void resetRequest() {
		this.request = "";
	}
	
	
	

}
