import java.util.ArrayList;

public class Taxi {

    public static int MAX_CAPACITY;

    private int id;
    private Vertex position;
    private ArrayList<Customer> passengers;

    // Needed for ALGO SimpleQueue
    private Customer customer; // Which customer we're on our way to pick up.
    private boolean inOperation; // Is doing something
    private ArrayList<Vertex> path; // Either the path towards the customer or the goal, depending on current state.

    public Taxi(int id) {
        this.id = id;
        this.passengers = new ArrayList<>();
        this.inOperation = false;
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

    public boolean removePassenger(ArrayList<Customer> customerList, Customer customer) {
        if(passengers.contains(customer)) {
            customer.updatePosition(position);
            customer.setTaxi(null);
            if(customer.isAtDestination()) {
                customerList.remove(customer);
                customer.getPosition().removeCustomer(customer);
            }
        } else {
            Main.debug("[ERROR] Tried removing a customer from taxi ("+getOutputId()+") that is not in the taxi!");
        }
        return passengers.remove(customer);
    }

    public void addPassenger(Customer customer) {
        customer.setTaxi(this);
        passengers.add(customer);
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

}
