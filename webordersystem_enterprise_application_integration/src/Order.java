import java.io.Serializable;

public class Order implements Serializable {

    private int customerID;
    private String firstName;
    private String lastName;
    private int overallItems;
    private int numDivingSuits;
    private int numSurfboards;
    private int orderID;
    private boolean valid;
    private boolean validationResult;

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getOverallItems() {
        return overallItems;
    }

    public void setOverallItems(int overallItems) {
        this.overallItems = overallItems;
    }

    public int getNumDivingSuits() {
        return numDivingSuits;
    }

    public void setNumDivingSuits(int numDivingSuits) {
        this.numDivingSuits = numDivingSuits;
    }

    public int getNumSurfboards() {
        return numSurfboards;
    }

    public void setNumSurfboards(int numSurfboards) {
        this.numSurfboards = numSurfboards;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValidationResult() {
        return validationResult;
    }

    public void setValidationResult(boolean validationResult) {
        this.validationResult = validationResult;
    }

    @Override
    public String toString() {
        return "Order{" +
                "customerID=" + customerID +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", overallItems=" + overallItems +
                ", numDivingSuits=" + numDivingSuits +
                ", numSurfboards=" + numSurfboards +
                ", orderID=" + orderID +
                ", valid=" + valid +
                ", validationResult=" + validationResult +
                '}';
    }
}
