import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Vertex {

    private ArrayList<Vertex> connections; //List all adjacent vertices
    private int id;

    private HashSet<Customer> customers; //All customers outside on this spot
    private HashSet<Taxi> taxis; //All taxis outside on this spot

    //Shortest path variables
    private HashMap<Integer, Integer> distanceTo;
    private HashMap<Integer, Vertex> nextTowards;
    private HashMap<Integer, Boolean> visited;

    //Hub variables
    private int hubID;
    private Vertex hub;
    private boolean isHubCenter;
    private int distToHubCenter;
    private Vertex vertexTowardsCenter;
    //Only used if hubCenter
    private HashSet<Vertex> hubVertices;
    private HashSet<Vertex> adjacentHubs;
    private HashMap<Integer, Integer> distanceToHubCenter;
    private HashMap<Integer, ArrayList<Vertex>> pathToHubCenter;

    //K-Center variables
    private boolean isTempKCenter;
    private int distToTempKCenter;
    private boolean isKCenter;

    //BFS Improvements
    boolean bfsStarted = false;
    ArrayList<Vertex> queue;


    public Vertex(int id) {
        this.id = id;
        connections = new ArrayList<>();

        distanceTo = new HashMap<>();
        nextTowards = new HashMap<>();
        visited = new HashMap<>();

        customers = new HashSet<>();
        taxis = new HashSet<>();

        hubID = -1;
        isHubCenter = false;
        distToHubCenter = -1;
        vertexTowardsCenter = null;
        hub = null;

        isTempKCenter = false;
        distToTempKCenter = -1;
        isKCenter = false;
    }

    public void initializeHub(int hubID) {
        setHubID(hubID);
        setHub(this);
        setHubCenter(true);
        setDistToHubCenter(0);
        setVertexTowardsCenter(null);

        hubVertices = new HashSet<>();
        adjacentHubs = new HashSet<>();
        distanceToHubCenter = new HashMap<>();
        setDistanceToHubCenter(this, 0);
        pathToHubCenter = new HashMap<>();
        setPathToHubCenter(this, new ArrayList<>());
    }

    public void updateDistanceInfo(Vertex startedPoint, int distance, Vertex nextTowards) {
        setDistanceTo(startedPoint, distance);
        setNextTowards(startedPoint, nextTowards);
    }

    public void setDistanceTo(Vertex startedPoint, int distance) {
        this.distanceTo.put(startedPoint.getId(), distance);
    }

    public void setNextTowards(Vertex startedPoint, Vertex nextTowards) {
        this.nextTowards.put(startedPoint.getId(), nextTowards);
    }

    public int getDistanceTo(Vertex vertex) {
        if(!distanceTo.containsKey(vertex.getId())) {
            vertex.bfs(this, true);
        }

        return distanceTo.get(vertex.getId());
    }

    public Vertex getNextTowards(Vertex vertex) {
        if(!nextTowards.containsKey(vertex.getId())) {
            vertex.bfs(this, true);
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
    private void bfs(Vertex searchFor, boolean stopIfDone) {
        if(!bfsStarted) {
            //Make an empty queue and array to keep track of which vertices we have already visited.
            queue = new ArrayList<>();

            //Start with the start. Since we have added it now, set visited to true.
            distanceTo.put(getId(), 0);
            visited.put(getId(), true);

            queue.add(this);
            bfsStarted = true;
        }

        //Flip this if we have found "SearchFor" vertex
        boolean foundDestination = false;

        //Now make sure we empty the whole queue, resulting on going over each vertice in the connected graph
        while(!queue.isEmpty() && !(foundDestination && stopIfDone)) {
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
                    this.setDistanceTo(towards, now.getDistanceTo(this)+1);

                    if(towards.equals(searchFor)) {
                        foundDestination = true;
                    }
                }

            }
        }
    }

    public boolean getVisited(Vertex vertex) {
        return visited.getOrDefault(vertex.getId(), false);
    }

    public HashSet<Customer> getCustomers() {
        return customers;
    }

    public boolean addCustomer(Customer customer) {
        return getCustomers().add(customer);
    }

    public boolean removeCustomer(Customer customer) {
        return getCustomers().remove(customer);
    }

    public HashSet<Taxi> getTaxis() {
        return taxis;
    }

    public boolean addTaxi(Taxi taxi) {
        return getTaxis().add(taxi);
    }

    public boolean removeTaxi(Taxi taxi) {
        return  getTaxis().remove(taxi);
    }

    public int getHubID() {
        return hubID;
    }

    public void setHubID(int hubID) {
        this.hubID = hubID;
    }

    public boolean isHubCenter() {
        return isHubCenter;
    }

    public void setHubCenter(boolean hubCenter) {
        isHubCenter = hubCenter;
    }

    public int getDistToHubCenter() {
        return distToHubCenter;
    }

    public void setDistToHubCenter(int distToHubCenter) {
        this.distToHubCenter = distToHubCenter;
    }

    public HashSet<Vertex> getHubVertices() {
        return hubVertices;
    }

    public void addHubVertice(Vertex vertex) {
        hubVertices.add(vertex);
    }

    public void removeHubVertice(Vertex vertex) {
        hubVertices.remove(vertex);
    }

    public int getDistanceToHubCenter(Vertex hub) {
        return distanceToHubCenter.get(hub.getHubID());
    }

    public void setDistanceToHubCenter(Vertex hub, int dist) {
        this.distanceToHubCenter.put(hub.getHubID(), dist);
    }

    public ArrayList<Vertex> getPathToHubCenter(Vertex hub) {
        return pathToHubCenter.get(hub.getHubID());
    }

    public void setPathToHubCenter(Vertex hub, ArrayList<Vertex> pathToHubCenter) {
        this.pathToHubCenter.put(hub.getHubID(), pathToHubCenter);
    }

    public HashSet<Vertex> getAdjacentHubs() {
        return adjacentHubs;
    }

    public void addAdjacentHub(Vertex hub) {
        this.adjacentHubs.add(hub);
    }

    public Vertex getVertexTowardsCenter() {
        return vertexTowardsCenter;
    }

    public void setVertexTowardsCenter(Vertex vertexTowardsCenter) {
        this.vertexTowardsCenter = vertexTowardsCenter;
    }

    public Vertex getHub() {
        return hub;
    }

    public void setHub(Vertex hub) {
        this.hub = hub;
    }

    public int getDistToTempKCenter() {
        return distToTempKCenter;
    }

    public void setDistToTempKCenter(int distToTempKCenter) {
        this.distToTempKCenter = distToTempKCenter;
    }

    public boolean isTempKCenter() {
        return isTempKCenter;
    }

    public void setTempKCenter(boolean tempKCenter) {
        isTempKCenter = tempKCenter;
        if(tempKCenter) {
            setDistToTempKCenter(0);
        }
    }

    public boolean isKCenter() {
        return isKCenter;
    }

    public void setKCenter(boolean kCenter) {
        isKCenter = kCenter;
    }
}
