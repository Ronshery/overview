
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Server {

	private static Integer buffSize = 1024;

    public static Charset messagecharset = null;

    //keylist in der ich alle keys die zum client gehören speicher um sie zu verarbeiten
    public static ArrayList<SelectionKey> keylist = new ArrayList<SelectionKey>();

    //maillist in der ich alle mails speicher um sie zu verarbeiten
    public static ArrayList<Mail> maillist = new ArrayList<Mail>();

    //Hier werden alle Ordner, Unterordner, Files erstellt
    public static String maindirectory = "Mail-Ordner/";

	/**
	 * Extracts the command from the client request
	 * 
	 * @param string string which contains response from client
	 * @return returns command from client
	 * @throws IOException
	 */
	public static String getCommand(String string) {
		String command = "";
		for(int i=0;i<string.length();i++) {

			if(command.toUpperCase().contains("MAIL")) {// && string.charAt(9) == ':') {
				//command = "MAIL FROM";
				if(command.toUpperCase().contains("MAIL FROM")) break;
				command = command + string.charAt(i);
				continue;
			}
			
			if(command.toUpperCase().contains("RCPT")) {// && string.charAt(7) == ':') {
				//command = "RCPT TO";
				if(command.toUpperCase().contains("RCPT TO")) break;
				command = command + string.charAt(i);
				continue;
			}
			
			if(string.charAt(i) == ' ') break;
			
			command = command + string.charAt(i);
		}
		command = command.toUpperCase();
		//Herausfiltern von <CRLF>
		if(command.contains("DATA")) command = "DATA";
		if(command.contains("HELP")) command = "HELP";
		if(command.contains("QUIT")) command = "QUIT";

		System.out.println("Client Command: " + command.toUpperCase() + "\n");

		return command.toUpperCase();
	}
	
	/**
	 * Checks whether command is vaild.
	 * @param command
	 * @return returns true, if command is valid, false if not.
	 */
	public static boolean validCommand(String command) {
		command = command.toUpperCase();
	if((command.contains("HELO") || command.contains("DATA") || command.contains("QUIT") || command.contains("HELP"))
			&& command.length() == 4)
		return true;

	if(command.contains("MAIL FROM") && command.length()==9) return true;
	if(command.contains("RCPT TO") && command.length()==7) return true;
	
	return false;

	}
	

	/**
	 * Sends the server response to the client
	 * 
	 * @param channel channel to send response to
	 * @param buf buffer to store the response
	 * @param msg the response
	 * @throws IOException
	 */
	public static void sendResponse(SocketChannel channel, ByteBuffer buf, String msg) {
		
		buf.clear();
		
		msg = msg + " \r\n";
		byte [] b = msg.getBytes(messagecharset);
		
		buf.put(b);
		
		buf.flip();
		
		try {
			channel.write(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		buf.clear();
	}
	
	/**
	 * Adds the key to the keylist and attaches mail
	 *
	 * @param key key which will be added once to keylist
	 */
	public static void addKey(SelectionKey key) {

		if(keylist.isEmpty()) {
			Mail mail = new Mail();
			addMail(mail);
			key.attach(mail);
			keylist.add(key);

		} else if(!keylist.contains(key)) {
			Mail mail = new Mail();
			addMail(mail);
			key.attach(mail);
			keylist.add(key);

		}
	}


	/**
	 * Extracts the reverse-path of the client request
	 * 
	 * @param clientresponse response from client
	 * @return returns the reverse-path
	 */
	public static String getMailAddress(String command, String clientresponse) {
		// cut off command string, colon, space, \r,\n
		return clientresponse.substring(command.length()+2, clientresponse.length()-2);
	}

	/**
	 * Adds mail to the maillist
	 * @param mail mail which be added to maillist
	 */
	public static void addMail(Mail mail) {
		maillist.add(mail);
	}

	/**
	 * Creates directory for each recipient of the given mail
	 * @param mail
	 */
	public static void createDirectory(Mail mail) {
		/*
		for(String addr: mail.getMailTo()){
			File file = new File(maindirectory + addr);
			if(!file.exists()) {
				//create directory
				file.mkdir();
				System.out.println("created directory: " + mail.getMailTo());
			}
			//create file in this directory
			putMailInDirectory(file.getAbsolutePath(),mail);
		}*/
		for(String addr: mail.getMailTo()) {
			Path pathobj = Paths.get(maindirectory+addr);
			if(Files.exists(pathobj)) {
				System.out.println("ordner existiert bereits");
			} else {
				try {
					Files.createDirectories(pathobj);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			putMailInDirectory(pathobj.toString(),mail);
		}
	}

	/**
	 * Creates mail file and puts it in the given directory
	 * and creates FileChannel for the file, which can be accessed with mail.getFileChannel.
	 * @param mail
	 * @param directorypath
	 */
	public static void putMailInDirectory(String directorypath,Mail mail) {
		try {
			FileOutputStream f = new FileOutputStream(directorypath + "/" + mail.getMailFrom() + "_" + mail.getMessageID() + ".txt");
			System.out.println("file created: " + mail.getMailFrom() + "_" + mail.getMessageID() + ".txt");
			FileChannel channel = f.getChannel();
			mail.setFileChannel(channel);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * Puts the given data in the corresponding text-file for the given mail.
	 * @param mail
	 */
	public static void putDataInFile(Mail mail) {

		byte[] bytear = mail.getData().getBytes(messagecharset);
		ByteBuffer buff = ByteBuffer.allocate(bytear.length);
		buff.put(bytear);
		buff.flip();

		ArrayList<FileChannel> channellist = (ArrayList<FileChannel>) mail.getFileChannels();
		FileChannel channel;
		if(!channellist.isEmpty()) {
			int i = 0;
			while(i < channellist.size()) {
				try {
					channel = channellist.get(i);
					channel.write(buff);
					buff.position(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
				i++;
			}
		}
	}

	public static boolean checkCommandsSequence(String command, String state) {
		if(command.contains("HELP")) return true;
		//helo expected
		if(state == null && command.contains("HELO")) return true;

		//mail from expected
		if(state.contains("heloreceived") && command.contains("MAIL FROM")) return true;
		//rcpt to expected
		if(state.contains("mailfromreceived") && command.contains("RCPT TO")) return true;
		//rcpt to or data expected
		if(state.contains("mailtoreceived") && (command.contains("RCPT TO") || command.contains("DATA"))) return true;
		if(state.contains("datareceived")) return true;
		if((state.contains("messagereceived") && command.contains("QUIT"))|| command.contains("QUIT")) return true;

		return false;
	}


	public static boolean checkRequestSyntax(String request,String command, Mail mail) {
		if(mail.getState() == null || !mail.getState().contains("datareceived")){
			if(command.contains("HELO") && request.charAt(4) == ' ') {
				return true;
			}
			if((command.contains("MAIL FROM") || command.contains("RCPT TO")) && request.charAt(command.length())== ':'){
				return true;
			}
			if((command.contains("DATA") || command.contains("QUIT") || command.contains("HELP")) && request.length() == 6 )
				return true;
		}



		return false;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static int validRequest(String request, String command, Mail mail) {
		if(request.isEmpty()) return 1;
		if(command.isEmpty()) return 1;

		//for HELO, state is not initialized yet
		if(mail.getState() == null) {
			if(command.length() != 4 && (command.contains("HELO") == false || command.contains("HELP"))) {
				return 500;
			}
		}


		//checks if request ends with \r\n
		if((mail.getState() == null || !mail.getState().contains("datareceived")) && (request.charAt(request.length()-1) != '\n' || request.charAt(request.length()-2) != '\r')) {
			// Dont do anything if <CRLF> doesnt exist.
			System.out.println("kein crlf");
			return 1001;
		}


		//checks if command message is valid
        if(mail.getState() != null) {
            if(!validCommand(command) && !mail.getState().contains("datareceived")) {
            	return 500;
            }
        } else 
        	if(!validCommand(command)) {
            	return 500;
        	}

        if( (mail.getState() == null || !mail.getState().contains("datareceived")) &&!checkRequestSyntax(request, command, mail)) {
        	return 500;
        }
        
		if(!checkCommandsSequence(command,mail.getState())) return 1000;



		/*
		if(mail.getState() != null && !mail.getState().contains("datareceived")) {
			if(!validCommand(command)) return 500;
		}
		if(mail.getState() == null) {
			if(!validCommand(command)) return 500;
		}*/
		
       
		//command line including command word and <CRLF> maximum 512 characters
        if (mail.getState() != null && !mail.getState().contains("datareceived") &&request.length() > 512) {
        	return 5001;
        }
        //domain-name should be max 64 characters
        if(command.contains("HELO") && mail.getState() == null && request.substring(command.length()+1, request.length()-2).length() > 64) {
        	return 5002;
        }
        //reversepath = sender
        //forwardpath = empfänger
        //
        
        //reversepath or forwardpath too short
        if(mail.getState() != null && !mail.getState().contains("datareceived")) {
	        if( (command.contains("MAIL FROM") || command.contains("RCPT TO")) && (request.length()-2 < command.length()+2)) {
	        	return 5004;
	        }
	        // reversepath or forwardpath too short
	        if((command.contains("MAIL FROM") || command.contains("RCPT TO")) && request.substring(command.length()+2, request.length()-2).length() < 1 ) return 5004;
	        
	        if(((command.contains("MAIL FROM") || command.contains("RCPT TO"))) && request.substring(command.length()+2, request.length()-2).length() > 256  ) {
	        	return 501;
	        }
        }
        //maximum 100 recipients!
        //return 5521;
        //
		if (mail.getState()!= null && mail.getState().contains("datareceived") && request.length() > 1000) {
			return 552;
		}
		
		// maximum 1000 characters including \r\n.\r\n
		if(mail.getState() != null && mail.getState().contains("datareceived") && request.length() == 1000) {
			if(!request.substring(request.length()-5).contains("\r\n.\r\n")) return 5003;
		}
	
		return 1;
		
	}
	
	
	
    public static void main(String[] args) throws IOException{

		if(args.length != 1){
			System.out.println("usage: server port");
			System.exit(1);
		}

        // charset to use for decoding messages
        Selector selector = null;
        ByteBuffer buf = ByteBuffer.allocate(buffSize);
        
		try {
			messagecharset = Charset.forName("US-ASCII");
		} catch(UnsupportedCharsetException uce) {
			System.err.println("Cannot create charset for this application. Exiting...");
			System.exit(1);
		}

		try {
			selector = Selector.open();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		try {
	        // create server socket, register it with selector
	        ServerSocketChannel serverSocket = ServerSocketChannel.open();
	        serverSocket.configureBlocking(false);
	        serverSocket.socket().bind(new InetSocketAddress(Integer.parseInt(args[0])));
	        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	        
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}


		// create directory where all mails will be stored
		/*File file = new File(maindirectory);
		file.mkdir();*/
		//create directory where alle mails will be stored with nio
		Path pathobj = Paths.get(maindirectory);
		System.out.println(pathobj);
		if(Files.exists(pathobj)) {
			System.out.println("existiert bereits");
		} else {
			try {
				Files.createDirectories(pathobj);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		System.out.println("Ready to accept connections");
        // wait for something to happen
        while(true){
            // if nothing happened on any channel continue waiting
			try {
				if(selector.select() == 0)
					continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            process:
            // iterate through channels on which something has happened
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                // on the channel a connection may get accepted
                if(key.isAcceptable()){
                    // accept connection
                    ServerSocketChannel socket = (ServerSocketChannel) key.channel();
                    SocketChannel client = socket.accept();
                    client.configureBlocking(false);
           
                    // register new socket with selector, check if there is something to read or write
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    sendResponse(client,buf,"220 Service Ready");

                    System.out.println("Client accepted: " + client.getLocalAddress());
                }
                //on the channel there is something to read
                if(key.isReadable()){
                    addKey(key);

                	//gets mail information from that specific key/channel/client/socket
                	Mail mail = (Mail) key.attachment();
                    // read bytes into byte buffer
					String request = "";
					int bytesRead;
                    SocketChannel channel = (SocketChannel) key.channel();
                    do{
						buf.clear();
                    	try{
							bytesRead = channel.read(buf);
						}catch(Exception e){
                    		e.printStackTrace();
                    		break process;
						}

                    	if(bytesRead == -1){
							System.out.println("Reached end-of-stream");
							key.cancel();
							channel.close();
							break;
						}
						buf.flip();

                    	CharBuffer charBuffer;
						// decode bytes
						CharsetDecoder decoder = messagecharset.newDecoder();
                    	try{
							charBuffer = decoder.decode(buf);
						}catch(Exception e){
                    		e.printStackTrace();
                    		break process;
						}

						// print out contents of char buffer
						request = request.concat(charBuffer.toString());
					}while(bytesRead == buffSize);
                    mail.setRequest(request);
                    request = mail.getRequest();
                    System.out.print("Client says: " + request);

                    // extracts command from client request
                    String command = getCommand(request);
                    // checks whether command is valid
                    
                    int validity = validRequest(request,command,mail);
                    
                    switch(validity) 
                    {
                    case 500:
                    	sendResponse(channel,buf,"500 unrecognized command");
                    	break;
                    case 5001:
                    	sendResponse(channel,buf,"500 command line too long, maximum 512 characters including <CRLF>");
                    	break;
                    case 5002:
                    	sendResponse(channel,buf,"500 domain name too long, maximum 64 characters");
                    	break;
                    case 5003:
                    	sendResponse(channel,buf,"500 Mail data too long, maximum 1000 characters including <CRLF>.<CRLF>");
                    case 5004:
                    	sendResponse(channel,buf,"500 reverspath or forwardpath too short");
                    	break;
                    case 501:
                    	sendResponse(channel,buf,"501 reverse-path or forward-path too long, maximum 256 characters");
                    	break;
                    //case 5521: 
                    	//sendResponse(channel,buf,"552 Too many recipients");
                    	//break;
                    case 552:
                    	sendResponse(channel,buf,"552 Too much mail data, maximum 1000 characters including <CRLF>");
                    	break;
                    case 1000:
                    	sendResponse(channel,buf,"503 Bad sequence of commands");
                    	break;
                    }
                    if(validity != 1) {
                    	if(validity != 1001) mail.resetRequest();
                    	break;
                    }
                    
                    //decide what to do if command == x |       x = HELO,MAIL,RCPT,HELP,DATA,QUIT

                    if(command.contains("HELO") && mail.getState() == null) {
                    	mail.setState("heloreceived");
                    	mail.resetRequest();
                    	sendResponse(channel, buf, "250 OK Antwort auf HELO");
                    	break;
                    } else

                    if(command.contains("MAIL FROM") && mail.getState() != null && !mail.getState().contains("datareceived")){
                    	mail.setState("mailfromreceived");
                    	mail.resetRequest();
						mail.setMailFrom(getMailAddress(command, request));
						System.out.println("Mail from: " + mail.getMailFrom());
						sendResponse(channel,buf, "250 OK Antwort auf MAIL FROM");
                    	break;
                    } else

                    if(command.contains("RCPT TO")&& mail.getState() != null && !mail.getState().contains("datareceived") ) {
                    	mail.setState("mailtoreceived");
                    	mail.resetRequest();
						mail.setMailTo(getMailAddress(command, request));
						System.out.print("Mail to: ");
						for(String addr: mail.getMailTo()){
							System.out.print(addr + " ");
						}
						System.out.print("\r\n");

						sendResponse(channel, buf, "250 OK Antwort auf RCPT TO");

                    	//creates directory
                    	createDirectory(mail);

                    	break;
                    } else

                    if(command.contains("DATA") && mail.getState() != null && !mail.getState().contains("datareceived")) {
                    	mail.setState("datareceived");
                    	mail.resetRequest();
                    	sendResponse(channel,buf,"354 OK Antwort auf DATA");
                    	
                    	break;
                    } else

                    if(command.contains("HELP") && mail.getState() != null && !mail.getState().contains("datareceived")) {
                    	if(mail.getState() == null)
                    		sendResponse(channel,buf,"214 Please send HELO with domainname");
                    	else if(mail.getState().contains("heloreceived"))
                    		sendResponse(channel,buf,"214 Please send MAIL FROM");
                    	else if(mail.getState().contains("mailfromreceived"))
                    		sendResponse(channel,buf,"214 Please send RCPT TO");
                    	else if(mail.getState().contains("mailtoreceived"))
                    		sendResponse(channel,buf,"214 Please send RCPT TO or DATA. Please note that everything after DATA will be interpreted as the message before you end the message with <CRLF>.<CRLF>");
                    	else if(mail.getState().contains("messagereceived"))
                    		sendResponse(channel,buf,"214 Please send QUIT.");
                    	mail.resetRequest();
                    	break;
                    } else

                    if(command.contains("QUIT") && mail.getState() != null && !mail.getState().contains("datareceived")) {
                    	mail.setState("quitreceived");
                    	// registration of key's channel with its selector be cancelled
                    	key.cancel();
                    	sendResponse(channel,buf,"221 OK Antwort auf QUIT");
                    	// close channel
                    	channel.close();

                    	
                    	break;

                    } else{
                    	// no command -> DATA of Mail
                    	if(mail.getState() != null && mail.getState().contains("datareceived") && mail.setData(request) == 1) {
                    		putDataInFile(mail);
                    		mail.resetRequest();
                    		sendResponse(channel, buf, "250 OK End of Data");
                    	};
                    }



                    //System.out.println("keylistlength: " + keylist.size());
                    //System.out.println(maillist.get(0));

                }
                iter.remove();
            }
        }
    }
}