package de.tu_berlin.cit.vs.jms.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import de.tu_berlin.cit.vs.jms.common.BuyMessage;
import de.tu_berlin.cit.vs.jms.common.ListMessage;
import de.tu_berlin.cit.vs.jms.common.RegisterMessage;
import de.tu_berlin.cit.vs.jms.common.RequestListMessage;
import de.tu_berlin.cit.vs.jms.common.SellMessage;
import de.tu_berlin.cit.vs.jms.common.Stock;
import de.tu_berlin.cit.vs.jms.common.UnregisterMessage;

import org.apache.activemq.ActiveMQConnectionFactory;


public class JmsBrokerClient {

	String clientName;

    Connection con;
	static Session session;

	// registration
	Queue regqueue;
	MessageProducer regproducer;

	// input queue of broker
	Queue inputqueue;
	MessageProducer inputproducer;

	// output queue of broker
	Queue outputqueue;
	MessageConsumer outputconsumer;

	ArrayList<MessageConsumer> topicconsumers;
	ArrayList<String> topicconsumername;


	// listen for available-stocks-messages
    private final MessageListener listener = new MessageListener() {

        @Override
        public void onMessage(Message msg) {

            if(msg instanceof ObjectMessage) {
				try {
					// if ListMessage sent from broker
					if(((ObjectMessage) msg).getObject() instanceof ListMessage) {
						System.out.println("listmessage");
						List<Stock> stockList = ((ListMessage) ((ObjectMessage) msg).getObject()).getStocks();
						// print Stocks
						for(Stock stock : stockList) {
							System.out.println(stock.toString());
						}
					}
					if(((ObjectMessage) msg).getObject() instanceof String) {
						System.out.println((String)((ObjectMessage) msg).getObject());
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}     
            }
        }
    };


    /**
     * Register with broker. Create input and output queue. Set listener
     *
     * @param clientName name of the client
     * @throws JMSException
     */
    public JmsBrokerClient(String clientName) throws JMSException {

        this.clientName = clientName;
        this.topicconsumers = new ArrayList<>();
        this.topicconsumername = new ArrayList<>();

        // create connection
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();

        // create session
        this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        // register with broker
        this.regqueue = session.createQueue("registration");
        this.regproducer = session.createProducer(regqueue);
        RegisterMessage regmsg = new RegisterMessage(clientName);
        ObjectMessage objectmessage = session.createObjectMessage(regmsg);
        this.regproducer.send(objectmessage);

        // input queue of broker
        this.inputqueue = session.createQueue(this.clientName+"input"); 
        this.inputproducer = session.createProducer(this.inputqueue);

        // output queue of broker
        this.outputqueue= session.createQueue(this.clientName+ "output");
        this.outputconsumer= session.createConsumer(this.outputqueue);

        // set listener
        this.outputconsumer.setMessageListener(listener);
    }


    /**
     * Request a list of available stocks
     *
     * @throws JMSException
     */
    public void requestList() throws JMSException {

    	RequestListMessage listmsg = new RequestListMessage();
    	ObjectMessage objectmessage = session.createObjectMessage(listmsg);
    	objectmessage.setStringProperty("clientname",this.clientName);
    	this.inputproducer.send(objectmessage);
    }


    /**
     * Buy stocks
     *
     * @param stockName stocks to buy
     * @param amount amount to buy
     * @throws JMSException
     */
    public void buy(String stockName, int amount) throws JMSException {

    	BuyMessage buymsg = new BuyMessage(stockName, amount);
    	ObjectMessage objectmessage = session.createObjectMessage(buymsg);
    	this.inputproducer.send(objectmessage);
    }


    /**
     * Sell stocks
     *
     * @param stockName stock to sell
     * @param amount amount to sell
     * @throws JMSException
     */
    public void sell(String stockName, int amount) throws JMSException {

    	SellMessage sellmsg = new SellMessage(stockName, amount);
    	ObjectMessage objectmessage = session.createObjectMessage(sellmsg);
    	this.inputproducer.send(objectmessage);
    }


    /**
     * Start watching stock topic
     *
     * @param stockName topic name
     * @throws JMSException
     */
    public void watch(String stockName) throws JMSException {

    	Topic topic = session.createTopic(stockName);
    	MessageConsumer topicConsumer = session.createConsumer(topic);
    	MessageListener topiclistener = new MessageListener() {
    		public void onMessage(Message msg) {
    			if(msg instanceof ObjectMessage) {
    				try {
    					Stock stock = (Stock) ((ObjectMessage) msg).getObject();
    					System.out.println("***** TOPIC INFO *****\n" + stock.toString() + "\n***** END *****\n");
					} catch (JMSException e) {
						e.printStackTrace();
					}
    			}
    		}
    	};
    	topicConsumer.setMessageListener(topiclistener);
    	topicconsumers.add(topicConsumer);
    	topicconsumername.add(stockName);
    }


    /**
     * Stop watching stock topic
     *
     * @param stockName topic name
     * @throws JMSException
     */
    public void unwatch(String stockName) throws JMSException {

    	for(int i = 0; i<topicconsumername.size();i++) {
    		if(stockName.contains(topicconsumername.get(i))) {
    			topicconsumers.get(i).close();
    		}
    	}
    }


    /**
     * Unregister client with broker. Close connection
     *
     * @throws JMSException
     */
    public void quit() throws JMSException {

    	UnregisterMessage unregmsg = new UnregisterMessage(clientName);
        ObjectMessage objectmessage = session.createObjectMessage(unregmsg);
        this.regproducer.send(objectmessage);
    	con.close();
    }


    /**
     * Create client. Process commands
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            // ask for client name
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the client name:");
            String clientName = reader.readLine();

            // create client
            JmsBrokerClient client = new JmsBrokerClient(clientName);

            // process commands
            boolean running = true;
            while(running) {
                System.out.println("Enter command:");
                String[] task = reader.readLine().split(" ");
                
                synchronized(client) {
                    switch(task[0].toLowerCase()) {
                        case "quit":
                            client.quit();
                            System.out.println("Bye bye");
                            running = false;
                            break;
                        case "list":
                            client.requestList();
                            break;
                        case "buy":
                            if(task.length == 3) {
                                client.buy(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: buy [stock] [amount]");
                            }
                            break;
                        case "sell":
                            if(task.length == 3) {
                                client.sell(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: sell [stock] [amount]");
                            }
                            break;
                        case "watch":
                            if(task.length == 2) {
                                client.watch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        case "unwatch":
                            if(task.length == 2) {
                                client.unwatch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        default:
                            System.out.println("Unknown command. Try one of:");
                            System.out.println("quit, list, buy, sell, watch, unwatch");
                    }
                }
            }
        } catch (JMSException | IOException ex) {
            Logger.getLogger(JmsBrokerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
