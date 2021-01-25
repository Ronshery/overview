package de.tu_berlin.cit.vs.jms.broker;

import java.io.Serializable;
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

import de.tu_berlin.cit.vs.jms.common.*;

import org.apache.activemq.ActiveMQConnectionFactory;


public class SimpleBroker {

	private Connection con;
	public Session session;
	Queue regqueue; // registration queue
    MessageConsumer regconsumer; // registration consumer
    ArrayList<MessageProducer> topicproducer;
    ArrayList<MessageListener> listenerlist = new ArrayList<>();
    List<Stock> stockList = new ArrayList<>();


    // on registration of client create input and ouput queue and wait for requests
    private final MessageListener listener = new MessageListener() {

        @Override
        public void onMessage(Message msg) {

            if(msg instanceof ObjectMessage) {
            	try {
            		// If RegisterMessage sent from client
					if(((ObjectMessage) msg).getObject() instanceof RegisterMessage) {
						System.out.println("registermessage");
						String clientname = ((RegisterMessage) ((ObjectMessage) msg).getObject()).getClientName();

						// input queue
				        Queue inputqueue = session.createQueue(clientname+"input");
				        MessageConsumer inputconsumer = session.createConsumer(inputqueue);
				        
				        // output queue
				        Queue outputqueue= session.createQueue(clientname+"output");
				        MessageProducer outputproducer= session.createProducer(outputqueue);
				        
				        //MessageListener for each clientinput
				        final MessageListener listenerclientinput = new MessageListener() {

				            @Override
				            public void onMessage(Message msg) {

				                if(msg instanceof ObjectMessage) {
									try {
										if(((ObjectMessage) msg).getObject() instanceof UnregisterMessage) {
											System.out.println("unregistermessage");
										}
	
										if(((ObjectMessage) msg).getObject() instanceof BuyMessage) {
											System.out.println("buymessage");

											String stockname= ((BuyMessage) ((ObjectMessage) msg).getObject()).getStockName();
											int amount = ((BuyMessage) ((ObjectMessage) msg).getObject()).getAmount();
											int x = buy(stockname, amount);
											String buyanswer;
											if(x == -1) {
												buyanswer = "Ihr Einkauf wurde bestätigt. \n Sie sind nun stolzer Besitzer von " + amount+ "x "+ stockname +" Aktie";
												
											} else if(x == -2){
												buyanswer = "Die von Ihnen angegebene Aktie wird nicht zum Verkauf angeboten.";
												
											} else {
												buyanswer = "Ihr Einkauf wurde nicht bestätigt. \n Es sind leider nur noch "+ x + " Aktien ihrer Wahl verfügbar.";

											}
											ObjectMessage objectmessage = session.createObjectMessage(buyanswer);
											//send answer
											outputproducer.send(objectmessage);
										}

										if(((ObjectMessage) msg).getObject() instanceof RequestListMessage) {
											System.out.println("requestlistmessage");
											ListMessage listmsg = new ListMessage(getStockList());
									    	ObjectMessage objectmessage = session.createObjectMessage(listmsg);
									    	outputproducer.send(objectmessage);
										}

										if(((ObjectMessage) msg).getObject() instanceof SellMessage) {
											System.out.println("sellmessage");
											String stockname= ((SellMessage) ((ObjectMessage) msg).getObject()).getStockName();
											int amount = ((SellMessage) ((ObjectMessage) msg).getObject()).getAmount();
											int x = sell(stockname, amount);
											String sellanswer;
											if(x == 1) {
												sellanswer = "Ihr Verkauf wurde bestätigt. \n Sie haben "+ amount + " " + stockname + " Aktien verkauft.";
											} else {
												sellanswer = "Die von Ihnen angegebene Aktie wird nicht akzeptiert.";
											}
											ObjectMessage objectmessage = session.createObjectMessage(sellanswer);
											//send answer
											outputproducer.send(objectmessage);
										}	
									} catch (JMSException e) {
										e.printStackTrace();
									}
								}
				            }
				        };
				        listenerlist.add(listenerclientinput);
				        inputconsumer.setMessageListener(listenerclientinput);
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
            }
        }
    };


	/**
	 * Wait for registrations. Create topic for each stock
	 *
	 * @param stockList list of available stocks
	 * @throws JMSException
	 */
	public SimpleBroker(List<Stock> stockList) throws JMSException {

    	// create connection and session
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();
		this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // consume from registration queue
        this.regqueue = session.createQueue("registration");
        this.regconsumer = session.createConsumer(regqueue);

        // create one topic per stock
        topicproducer = new ArrayList<MessageProducer>();
        for(Stock stock : stockList) {
        	Topic topic = session.createTopic(stock.getName());
        	MessageProducer topicProducer = session.createProducer(topic);
        	this.topicproducer.add(topicProducer);
        }

        // wait for registrations
        this.regconsumer.setMessageListener(listener);
        
        this.stockList = stockList;
    }


	/**
	 * Close the connection (session etc.). Quit the application
	 *
	 * @throws JMSException
	 */
	public void stop() throws JMSException {

    	System.out.println("stop");
    	con.close();
    	System.exit(0);
    }


    /**
     * Process buy request
     * 
     * @param stockName name of stock to buy
     * @param amount amount to buy
     * @return -1 if buying process is fine. return -2 if stock doesnt exist. else returns the available count.
     * @throws JMSException
     */
    public synchronized int buy(String stockName, int amount) throws JMSException {

    	int x = -2;
    	List<Stock> stockList = getStockList();
    	//find stockName in list
    	for(Stock stock : stockList) {
    		if(stock.getName().contains(stockName)) {
    			//if there is enough to buy
    			if(stock.getAvailableCount()>= amount) {
    				stock.setAvailableCount(stock.getAvailableCount()-amount);
    				stock.setStockCount(stock.getStockCount()-amount);
    				x = -1;
    				sendTopicProducer(stock);
    				break;
    				
    			} else { // if there is not enough to buy
    				x =stock.getAvailableCount();
    				break;
    			}
    		}
    	}
        return x;
    }


    /**
     * Notify stock watchers that something has changed
	 *
     * @param stock name of stock that has changed
     * @return
     */
    public MessageProducer sendTopicProducer(Stock stock) {

    	for(MessageProducer topicproducer : topicproducer) {
    		try {
				if(topicproducer.getDestination().toString().contains(stock.getName())) {
					ObjectMessage objectmessage = session.createObjectMessage(stock);
		        	topicproducer.send(objectmessage);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
    	}
		return null;
    }


    /**
     * Process sell request
	 *
     * @param stockName name of stock to sell
     * @param amount amount to sell
     * @return 1 if stock is sold else return 0 if stock doesnt exist.
     * @throws JMSException
     */
    public synchronized int sell(String stockName, int amount) throws JMSException {

    	int x = 0;
    	List<Stock> stockList = getStockList();
    	//find stockName in list
    	for(Stock stock : stockList) {
    		if(stock.getName().contains(stockName)) {
				stock.setAvailableCount(stock.getAvailableCount()+amount);
				stock.setStockCount(stock.getStockCount()+amount);
				x = 1;
				sendTopicProducer(stock);
				break;
    		}
    	}
        return x;
    }


	/**
	 * Get available stocks
	 *
	 * @return available stocks
	 */
	public synchronized List<Stock> getStockList() {

        return this.stockList;
    }
}
