import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AggregationStrategy;


public class CamelMain {

    private static Processor callCenterTranslator = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {

            // split order
            String orderString = exchange.getIn().getBody(String.class);
            String[] orderList = orderString.split(", | ");

            // create order object
            Order order = new Order();
            order.setCustomerID(Integer.parseInt(orderList[0]));
            order.setFirstName(orderList[1]);
            order.setLastName(orderList[2]);
            order.setNumSurfboards(Integer.parseInt(orderList[3]));
            order.setNumDivingSuits(Integer.parseInt(""+orderList[4].charAt(0)));

            // attach order object to exchange
            exchange.getIn().setBody(order);
        }
    };

    private static Processor webOrderTranslator = new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
        	
            // split order
            String orderString = exchange.getIn().getBody(String.class);
            String[] orderList = orderString.split(", ");

            // create order object
            Order order = new Order();
            order.setCustomerID(Integer.parseInt(orderList[4]));
            order.setFirstName(orderList[0]);
            order.setLastName(orderList[1]);
            order.setNumSurfboards(Integer.parseInt(orderList[2]));
            order.setNumDivingSuits(Integer.parseInt(orderList[3]));

            // attach order object to exchange
            exchange.getIn().setBody(order);
        }
    };

    private static Processor orderEnricher = new Processor() {

        int orderID = 0;

        @Override
        public void process(Exchange exchange) throws Exception {

            // set overall items
            Order order = (Order) exchange.getIn().getBody();
            order.setOverallItems(order.getNumDivingSuits() + order.getNumSurfboards());

            // set order id attribute and header
            order.setOrderID(orderID);
            orderID++;
        }
    };

    private static class OrderAggregation implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        	if(oldExchange == null) {
        		return newExchange;
        	}

            // if billing system and inventory system reported valid set validation result true
            Order oldOrder = (Order) oldExchange.getIn().getBody();
            Order newOrder = (Order) newExchange.getIn().getBody();
            if(oldOrder.isValid() && newOrder.isValid()) {
                oldOrder.setValidationResult(true);
                oldExchange.getIn().setHeader("validationResult", true);
            } else {
                oldOrder.setValidationResult(false);
                oldExchange.getIn().setHeader("validationResult", false);
            }

            // return old order
            oldExchange.getIn().setBody(oldOrder);

            return oldExchange;
        }
    }


    public static void main(String[] args) throws Exception {

        DefaultCamelContext camelContext = new DefaultCamelContext();

        // add activemq component
        ActiveMQComponent activeMQComponent = new ActiveMQComponent().activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        camelContext.addComponent("activemq", activeMQComponent);

        // set routes
        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // call center to yet-to-be-processed order queue
                from("file:orders")
                        .split(body().tokenize("\n"))
                        .process(callCenterTranslator)
                        .process(orderEnricher)
                        .to("activemq:topic:orderIn");

                // web order to yet-to-be-processed order queue
                from("activemq:queue:webOrder")
                        .process(webOrderTranslator)
                        .process(orderEnricher)
                        .to("activemq:topic:orderIn");

                // aggregate and route processed orders to result system
                from("activemq:queue:orderOut")
                        // aggregate 2 orders with same order id
                        .aggregate(header("orderID"), new OrderAggregation()).completionSize(2)
                        // route valid orders ot valid queue, invalid orders to invalid queue
                        .choice()
                            .when(header("validationResult"))
                                .to("activemq:queue:validResult")
                            .otherwise()
                                .to("activemq:queue:invalidResult");
            }
        };

        camelContext.addRoutes(route);

        camelContext.start();
        System.in.read();
        camelContext.stop();
    }
}
