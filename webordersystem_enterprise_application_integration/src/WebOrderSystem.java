import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class WebOrderSystem {

    public String run() {

        // get order from user
		Scanner s = new Scanner(System.in);
        System.out.print("Enter order <first name, last name, number of ordered surfboards, number of ordered diving suits, customer ID>: ");
        String order = s.nextLine();

        // return order as string
        return order;
    }

    public static void main(String[] args) {

        // run system
        WebOrderSystem w = new WebOrderSystem();
        
        try {
        	 // create connection
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setTrustAllPackages(true);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // create session
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create order queue
            Queue webOrder = session.createQueue("webOrder");

           
            MessageProducer producer = session.createProducer(webOrder);
            
            //Scan input multiple times until 'q'
            while(true) {
            	
            	String order = w.run();
            	//quit option
            	if(order.contains("q")) {
            		connection.close();
            		break;
            	}
            	
            	// create and send answer
                ObjectMessage answer = session.createObjectMessage(order);
                producer.send(answer);
                
                // print order
            	System.out.println("Your order: " + order);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
