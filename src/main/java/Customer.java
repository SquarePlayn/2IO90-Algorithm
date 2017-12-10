public class Customer {

    private Vertex initialPosition;
    private Vertex destination;

    private Vertex position; //Position of the customer. Null if in taxi
    private Taxi taxi; //The taxi the customer is in. Null if not in any taxi.

    //Needed by GCC. Keeps track of if another taxi already handles this customer. Does not need updating between minutes
    private boolean isBeingHandled;

    //TODO Do we want to keep track of in which Taxi the customer is and where it is when dropped and such?
    //fixme because currently the position is not being updated (it's not even possible to do, all private)

    public Customer(Vertex initialPosition, Vertex destination) {
        this.initialPosition = initialPosition;
        this.position = initialPosition;
        this.destination = destination;
    }

    public void drop(Vertex position) {
        this.taxi = null;
        this.position = position;
    }

    public void pickup(Taxi taxi) {
        this.taxi = taxi;
        this.position = null;
    }

    public Vertex getPosition() {
        if (isInTaxi()) {
            return taxi.getPosition();
        } else {
            return position;
        }
    }

    public boolean isAtDestination() {
        return getPosition().equals(destination) && !isInTaxi();
    }

    public boolean isInTaxi() {
        return taxi != null;
    }

    public Vertex getDestination() {
        return destination;
    }

    public  Vertex getInitialPosition() {
        return initialPosition;
    }

    public Taxi getTaxi() {
        return taxi;
    }

    public boolean isBeingHandled() {
        return isBeingHandled;
    }

    public void setBeingHandled(boolean beingHandled) {
        isBeingHandled = beingHandled;
    }
}
