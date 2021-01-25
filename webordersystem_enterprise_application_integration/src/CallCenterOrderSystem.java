import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CallCenterOrderSystem {

    private String generateRandomOrder() {

        // generate random strings and numbers
        String firstName = RandomStringUtils.randomAlphabetic(5);
        String lastName = RandomStringUtils.randomAlphabetic(5);
        String numSurfboards = RandomStringUtils.randomNumeric(1);
        String numDivingSuits = RandomStringUtils. randomNumeric(1);

        // add strings to create order
        String order = firstName + " " + lastName + ", " + numSurfboards + ", " + numDivingSuits;

        // return order
        return order;
    }

    public void run() {

        Integer customerID = 0;
        ArrayList<String> orders = new ArrayList<>();

        while(true) {
            try {
                // number of orders to generate
                int numOrders = RandomUtils.nextInt(5, 10);

                // clear old orders from list
                orders.clear();

                // generate random orders
                for(int i = 0; i < numOrders; i++) {
                    String newOrder = generateRandomOrder();
                    // add customerID to order
                    newOrder = customerID.toString() + ", " + newOrder;
                    // increase customer id
                    customerID++;
                    orders.add(newOrder);
                }

                // write orders to file
                Path path = Paths.get("orders", "orders");
                Files.write(path, orders, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                // wait
                TimeUnit.MINUTES.sleep(2);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        // run system
        CallCenterOrderSystem c = new CallCenterOrderSystem();
        c.run();
    }
}
