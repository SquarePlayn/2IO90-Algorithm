public class Customer {

    private Vertex position;
    private Vertex destination;

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
