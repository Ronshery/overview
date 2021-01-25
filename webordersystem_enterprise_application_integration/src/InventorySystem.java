import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class InventorySystem {

    public static void main(String[] args) throws Exception {

        try {
            // create connection
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setTrustAllPackages(true);
            Connection connection = connectionFactory.createConnection();

            // create session
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // create in topic and out queue
            Topic orderInTopic = session.createTopic("orderIn");
            Queue orderOutQueue = session.createQueue("orderOut");

            // listen for messages
            MessageConsumer consumer = session.createConsumer(orderInTopic);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        // get order
                        Order order = (Order) ((ObjectMessage) message).getObject();

                        // decide whether order valid or not
                        boolean valid = order.getNumDivingSuits() + order.getNumSurfboards() < 10;

                        // create answer, set valid attribute and header
                        order.setValid(valid);
                        ObjectMessage answer = session.createObjectMessage(order);
                        answer.setIntProperty("orderID", order.getOrderID());

                        // print order
                        System.out.println(order.toString());

                        // send answer
                        MessageProducer producer = session.createProducer(orderOutQueue);
                        producer.send(answer);
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
