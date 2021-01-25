import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ResultSystem {

    public static void main(String[] args) throws Exception {

        try {
            // create connection
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setTrustAllPackages(true);
            Connection connection = connectionFactory.createConnection();

            // create session
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create valid in queue and invalid in queue
            Queue validResultQueue = session.createQueue("validResult");
            Queue invalidResultQueue = session.createQueue("invalidResult");

            // create consumers
            MessageConsumer validConsumer = session.createConsumer(validResultQueue);
            MessageConsumer invalidConsumer = session.createConsumer(invalidResultQueue);

            // on incoming valid message
            validConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {

                    try {
                        // get order
                        Order order = (Order) ((ObjectMessage) message).getObject();

                        // print order
                        System.out.println("valid order: " + order.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // on incoming invalid message
            invalidConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {

                    try {
                        // get order
                        Order order = (Order) ((ObjectMessage) message).getObject();

                        // print order
                        System.out.println("invalid order: " + order.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            connection.start();
            System.in.read();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
