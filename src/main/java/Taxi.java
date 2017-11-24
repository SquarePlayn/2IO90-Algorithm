import java.util.ArrayList;

public class Taxi {

    private static int MAX_CAPACITY;

    private int id;
    private Vertex position;
    private ArrayList<Customer> passengers;
    private Customer customer;
    private boolean inOperation;

    //For our prototype it will go directly to a given endpoint and drop a passenger there
    private ArrayList<Vertex> path;

    public Taxi(int id) {
        this.id = id;
        this.passengers = new ArrayList<>();
        this.inOperation = false;
    }

    /**
     * Continue the operation of delivering their customer
     * Output can be sent straight to the scanner.
     *
     * @return required operation to be done in order to continue operation
     */
    public String continueOperation(Graph graph) {
        //TODO Sanitycheck if we are indeed in operation

        if (!path.isEmpty()) {
            //We are still driving. Advance to next spot
            position = path.get(0);
            path.remove(0);

            return "m " + id + " " + position.getId() + " ";

        } else {
            if (passengers.isEmpty()) {
                //We still have to pick up the passenger

                //TODO Sanitycheck if customer is indeed at the position we are at

                passengers.add(customer);
                setPath(graph.getShortestPath(position, customer.getDestination()));

                return "p " + id + " " + customer.getDestination().getId() + " ";

            } else {
                //We are done driving, and have already picked up our customer, so that means we are at the destination
                // so we can drop the customer of
                passengers.remove(customer);
                customer = null;
                inOperation = false;

                return "d " + id + " " + position.getId() + " ";

            }
        }

    }

    public static void setMaxCapacity(int maxCapacity) {
        MAX_CAPACITY = maxCapacity;
    }

    public int getId() {
        return id;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public Vertex getPosition() {
        return position;
    }

    public boolean getInOperation() {
        return inOperation;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Set the customer the taxi will have to help
     * Not that this does not yet mean the taxi has already picked up this customer
     *
     * @param customer
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;

        //Since we now have a customer to bring to a destination, we are in operation
        inOperation = true;
    }

    /**
     * Set the path of this taxi
     * It will now drive to this path, then see if it has to pick up or drop of its passenger
     *
     * @param path The path the taxi will follow
     */
    public void setPath(ArrayList<Vertex> path) {
        this.path = path;

        // Remove the first vertex of the path since that indicates the vertex to start, which we are already on.
        //TODO Sanitycheck if first node is actually correct
        path.remove(0);

        //Since we have somewhere to go, we are in operation
        inOperation = true;
    }


}
