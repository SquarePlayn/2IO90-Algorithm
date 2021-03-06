import java.util.ArrayList;

public class Taxi {

    public static int MAX_CAPACITY;

    private int id;
    private Vertex position;
    private ArrayList<Customer> passengers;

    // Needed for ALGO SimpleQueue and hubs
    private Customer customer; // Which customer we're on our way to pick up.
    private boolean inOperation; // Is doing something

    //Used by LSD, SimpleQueue
    private ArrayList<Vertex> path; // Either the path towards the customer or the goal, depending on current state.

    //Used by hubs
    private Vertex movingToHub;
    private boolean goingToCenter;

    // Needed for LSD.
    private int turnsLeft = -1;

    public Taxi(int id) {
        this.id = id;
        this.passengers = new ArrayList<>();
        this.inOperation = false;
    }

    public void pickup(Customer customer, SharedData sharedData) {
        customer.pickup(this);
        passengers.add(customer);

        sharedData.getCustomerOutsideList().remove(customer);
        sharedData.getCustomerInTaxiList().add(customer);

    }

    public void drop(Customer customer, SharedData sharedData) {
        customer.drop(position);
        passengers.remove(customer);

        sharedData.getCustomerInTaxiList().remove(customer);

        if(customer.isAtDestination()) {
            sharedData.getCustomerList().remove(customer);
        } else {
            sharedData.getCustomerOutsideList().add(customer);
        }
    }

    public static void setMaxCapacity(int maxCapacity) {
        MAX_CAPACITY = maxCapacity;
    }

    public boolean isFull() {
        return passengers.size() >= MAX_CAPACITY;
    }

    public int getId() {
        return id;
    }

    public int getOutputId() {
        return id + 1;
    }

    public void setPosition(Vertex position) {
        if(this.position != null) {
            this.position.removeTaxi(this);
        }
        this.position = position;
        this.position.addTaxi(this);
    }

    public Vertex getPosition() {
        return position;
    }

    public ArrayList<Customer> getPassengers() {
        return passengers;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** ----- Simple Queue ----- **/

    public boolean getInOperation() {
        return inOperation;
    }

    public void setInOperation(boolean inOperation) {
        this.inOperation = inOperation;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ArrayList<Vertex> getPath() {
        return path;
    }

    public void setPath(ArrayList<Vertex> path) {
        this.path = path;
    }

    public int getTurnsLeft() {
        return turnsLeft;
    }

    public void setTurnsLeft(int turnsLeft) {
        this.turnsLeft = turnsLeft;
    }

    public Vertex getMovingToHub() {
        return movingToHub;
    }

    public void setMovingToHub(Vertex movingToHub) {
        this.movingToHub = movingToHub;
    }

    public boolean isGoingToCenter() {
        return goingToCenter;
    }

    public void setGoingToCenter(boolean goingToCenter) {
        this.goingToCenter = goingToCenter;
    }
}
