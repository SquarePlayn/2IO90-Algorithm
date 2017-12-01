public class Call {

    private Customer customer;
    private Vertex location;
    private Vertex destination;

    public Call(Customer customer, Vertex location, Vertex destination) {
        this.customer = customer;
        this.location = location;
        this.destination = destination;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Vertex getLocation() {
        return location;
    }

    public Vertex getDestination() {
        return destination;
    }
}
