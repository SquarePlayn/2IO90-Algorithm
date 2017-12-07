public class Customer {

    private Vertex position;
    private Vertex destination;

    //Needed by GCC. Keeps track of if another taxi already handles this customer. Does not need updating between minutes
    private boolean isBeingHandled;

    //TODO Do we want to keep track of in which Taxi the customer is and where it is when dropped and such?
    //fixme because currently the position is not being updated (it's not even possible to do, all private)

    public Customer(Vertex initialPosition, Vertex destination) {
        this.position = initialPosition;
        this.destination = destination;
    }

    public void updatePosition(Vertex position) {
        this.position = position;
    }

    public Vertex getPosition() {
        return position;
    }

    public Vertex getDestination() {
        return destination;
    }

    public boolean isBeingHandled() {
        return isBeingHandled;
    }

    public void setBeingHandled(boolean beingHandled) {
        isBeingHandled = beingHandled;
    }
}
