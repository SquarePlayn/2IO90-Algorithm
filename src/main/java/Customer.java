public class Customer {

    private Vertex position;
    private Vertex destination;
    private Taxi taxi;

    //Needed by GCC. Keeps track of if another taxi already handles this customer. Does not need updating between minutes
    private boolean isBeingHandled;
    private boolean hasBeenPickedUp;

    //TODO Do we want to keep track of in which Taxi the customer is and where it is when dropped and such?
    //fixme because currently the position is not being updated (it's not even possible to do, all private)

    public Customer(Vertex initialPosition, Vertex destination) {
        this.updatePosition(initialPosition);
        this.destination = destination;

    }

    public void updatePosition(Vertex position) {
        if(this.position != null) {
            this.position.removeCustomer(this);
        }
        this.position = position;
        this.position.addCustomer(this);
    }

    public Vertex getPosition() {
        if(taxi == null) {
            return position;
        } else {
            return taxi.getPosition();
        }
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

    public Taxi getTaxi() {
        return taxi;
    }

    public void setTaxi(Taxi taxi) {
        this.taxi = taxi;
        if(taxi != null) {
            this.position.removeCustomer(this);
            this.position = null;
        }
    }

    public boolean isAtDestination() {
        return position.equals(destination);
    }

    public boolean getHasBeenPickedUp() {
        return hasBeenPickedUp;
    }

    public void setHasBeenPickedUp(boolean hasBeenPickedUp) {
        this.hasBeenPickedUp = hasBeenPickedUp;
    }
}
