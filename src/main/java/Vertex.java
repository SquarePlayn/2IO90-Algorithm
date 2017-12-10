import java.util.ArrayList;
import java.util.HashMap;

public class Vertex {

    //List all adjacent vertices
    private ArrayList<Vertex> connections;
    private int id;
    private ArrayList<Customer> customers;
    private ArrayList<Taxi> taxis;

    //Shortest path variables
    private HashMap<Integer, Integer> distanceTo;
    private HashMap<Integer, Vertex> nextTowards;
    private HashMap<Integer, Boolean> visited;


    public Vertex(int id) {
        this.id = id;
        connections = new ArrayList<>();

        customers = new ArrayList<>();
        taxis = new ArrayList<>();

        distanceTo = new HashMap<>();
        nextTowards = new HashMap<>();
        visited = new HashMap<>();
    }

    public void updateDistanceInfo(Vertex startedPoint, int distance, Vertex nextTowards) {
        this.distanceTo.put(startedPoint.getId(), distance);
        this.nextTowards.put(startedPoint.getId(), nextTowards);
    }

    public int getDistanceTo(Vertex vertex) {
        if(!distanceTo.containsKey(vertex.getId())) {
            vertex.bfs();
        }

        return distanceTo.get(vertex.getId());
    }

    public Vertex getNextTowards(Vertex vertex) {
        if(!nextTowards.containsKey(vertex.getId())) {
            vertex.bfs();
        }
        return nextTowards.get(vertex.getId());
    }

    public void addNeigbour(Vertex v) {
        connections.add(v);
    }

    public ArrayList<Vertex> getNeigbours() {
        return connections;
    }

    public int getId() {
        return id;
    }

    public void setVisited(Vertex vertex, boolean value) {
        visited.put(vertex.getId(), value);
    }

    /**
     * Implementation of Breadth First Search
     * When ran, it sets in each vertice the minimal distance to the "start" node,
     *  as well as the next node to go to when you want to get to this "start" node as quick as possible.
     * Runs in an expanding fashion: First explore 1 layer, then another and so forth untill the entire
     *  graph has been visited
     * Erases/overwrites any BSF results of earlier calls
     */
    private void bfs() {
        //Make an empty queue and array to keep track of which vertices we have already visited.
        ArrayList<Vertex> queue = new ArrayList<>();

        //Start with the start. Since we have added it now, set visited to true.
        distanceTo.put(getId(), 0);
        visited.put(getId(),true);

        queue.add(this);

        //Now make sure we empty the whole queue, resulting on going over each vertice in the connected graph
        while(!queue.isEmpty()) {
            //Pop the item that is first up
            Vertex now = queue.remove(0);

            //For each connection:
            for(Vertex towards: now.getNeigbours()) {
                if(!towards.getVisited(this)) {
                    //If we have not yet done anything with this node, we have not seen it before
                    //That means that we have now taken the shortest route to it. So the distance is 1 greater
                    // than the distance to the node we are considering, and the shortest path back is trough the node
                    // that we are considering.
                    //Also add it to the (end of the) queue to traverse further
                    towards.setVisited(this, true);
                    queue.add(towards);
                    towards.updateDistanceInfo(this, now.getDistanceTo(this)+1, now);
                }
            }
        }
    }

    public boolean getVisited(Vertex vertex) {
        return visited.getOrDefault(vertex.getId(), false);
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public ArrayList<Taxi> getTaxis() {
        return taxis;
    }

    public Customer addCustomer(Customer customer) {
        customers.add(customer);
        return customer;
    }

    public Taxi addTaxi(Taxi taxi) {
        taxis.add(taxi);
        return taxi;
    }

    public boolean removeCustomer(Customer customer) {
        return customers.remove(customer);
    }

    public boolean removeTaxi(Taxi taxi) {
        return taxis.remove(taxi);
    }
}
