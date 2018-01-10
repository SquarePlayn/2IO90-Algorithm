public class Customer {

    private final Vertex initialPosition;
    private final Vertex destination;
    private final int creationMinute;

    private Vertex position; //Position of the customer. Null if in taxi
    private Taxi taxi; //The taxi the customer is in. Null if not in any taxi.

    //Keeps track of if another taxi already handles this customer. Does not need updating between minutes
    private boolean hasBeenPickedUp;
    private boolean hasBeenChecked;
    private boolean isBeingHandled; //NB Hubs requires this to be up to date

    public Customer(Vertex initialPosition, Vertex destination, int creationMinute) {
        this.initialPosition = initialPosition;
        this.position = initialPosition;
        this.destination = destination;
        this.creationMinute = creationMinute;
        this.position.addCustomer(this);
        this.hasBeenChecked = false;
        this.hasBeenPickedUp = false;
        this.isBeingHandled = false;
    }

    public void drop(Vertex position) {
        this.taxi = null;
        this.position = position;
        if(!isAtDestination()) {
            this.position.addCustomer(this);
        }
    }

    public void pickup(Taxi taxi) {
        this.taxi = taxi;
        this.position.removeCustomer(this);
        this.position = null;
    }

    public Vertex getPosition() {
        if (isInTaxi()) {
            return taxi.getPosition();
        } else {
            return position;
        }
    }

    public Vertex getPositionVariable() {
        return position;
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

    public int getCreationMinute() {
        return creationMinute;
    }

    public boolean hasBeenPickedUp() {
        return hasBeenPickedUp;
    }

    public void setHasBeenPickedUp(boolean hasBeenPickedUp) {
        this.hasBeenPickedUp = hasBeenPickedUp;
    }

    public boolean hasBeenChecked() {
        return hasBeenChecked;
    }

    public void setHasBeenChecked(boolean hasBeenChecked) {
        this.hasBeenChecked = hasBeenChecked;
    }

    public boolean isBeingHandled() {
        return isBeingHandled;
    }

    public void setBeingHandled(boolean beingHandled) {
        isBeingHandled = beingHandled;
    }
}