public class Customer {

    private Vertex position;
    private Vertex destination;

    //TODO Do we want to keep track of in which Taxi the customer is and where it is when dropped and such?
    //fixme because currently the position is not being updated (it's not even possible to do, all private)

    public Customer(Vertex initialPosition, Vertex destination) {
        this.position = initialPosition;
        this.destination = destination;
    }

    public Vertex getPosition() {
        return position;
    }

    public Vertex getDestination() {
        return destination;
    }
}
