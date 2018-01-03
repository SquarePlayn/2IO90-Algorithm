import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Graph {

    private ArrayList<Vertex> vertices;

    //Needed for the organizing of hubs code
    private ArrayList<Vertex> hubs;
    private int HUB_RADIUS = 4;
    private static final boolean HUB_OVERWRITE_SET = true; // If, when finding a vertex is closer to another hub,
    // it should be assigned to the closer one
    private static final boolean HUB_OVERWRITE_RECURSE = true; // If when you find a closer one, you also check its neighbours

    //ArrayList of all vertices that are a K-center
    private ArrayList<Vertex> tempKCenters = new ArrayList<>();
    private ArrayList<Vertex> kCenters;

    Graph() {
        this.vertices = new ArrayList<>();
        this.hubs = new ArrayList<>();
        this.kCenters = new ArrayList<>();
    }

    /**
     * Add a new vertex to the graph
     */
    public void addVertex() {
        vertices.add(new Vertex(getSize()));
    }

    /**
     * Get the size of the Graph, which equals the amount of nodes/vertices
     * @return size of Graph
     */
    public int getSize() {
        return vertices.size();
    }

    /**
     * Get the vertex with the given ID
     * @param id The id of the vertex you want
     * @return the Vertex with the number/id "ID"
     */
    public Vertex getVertex(int id) {
        return vertices.get(id);
    }

    /**
     * Add n vertices to the graph.
     * @param n amount of vertices to be added
     */
    public void addVertices(int n) {
        for(int i=0; i<n; i++){
            addVertex();
        }
    }

    /**
     * Add edge to the graph from vertex 1 to 2 and the other way around.
     * Vertices 1 and 2 have to exist in the graph
     * @param a vertex 1
     * @param b vertex 2
     */
    public void addEdge(int a, int b) {
        if(a >= getSize()) {
            throw new IllegalArgumentException("Vertex 1 does not exist in the graph.");
        } else if(b >= getSize()) {
            throw new IllegalArgumentException("Vertex 2 does not exist in the graph.");
        }

        //Since the graph is bi-directional/undirected, add a connection to both vertices
        vertices.get(a).addNeigbour(vertices.get(b));
        vertices.get(b).addNeigbour(vertices.get(a));
    }

    /**
     * Get the length of the shortest path between two nodes
     * Note that from and to can be interchanged while achieving the same effect
     * @param from Vertex from where you want to start
     * @param to Vertex where you want to go
     * @return Length of shortest path between from and to
     */
    public int getDistance(Vertex from, Vertex to) {
        return from.getDistanceTo(to); //Return how far our start is from our destination
    }

    /**
     * Get all vertices that make up the shortest path from "from" to "to".
     * The vertices will be in the correct order with 0 == one after from and len(path)-1 == to
     * @param from the vertex to start your path from
     * @param to the vertex where your path will lead to
     * @return a list of all vertices which you will travel over to get from "from" to "to" in as short as possible
     * excluding the FROM vertex, but including the TO vertex
     */
    public ArrayList<Vertex> getShortestPath(Vertex from, Vertex to) {
        ArrayList<Vertex> path = new ArrayList<Vertex>();

        Vertex next = from.getNextTowards(to);

        //Check each node for which node would be the next one until we have arrived at the destination
        while(next != null){
            path.add(next);
            next = next.getNextTowards(to);
        }

        return path;
    }

    public ArrayList<Vertex> getHubs() {
        return hubs;
    }

    public Vertex getHub(int hubID) {
        return hubs.get(hubID);
    }

    public void addHub(Vertex vertex) {
        vertex.initializeHub(hubs.size());
        hubs.add(vertex);
    }

    public int getHUB_RADIUS() {
        return HUB_RADIUS;
    }

    public void setHUB_RADIUS(int HUB_RADIUS) {
        this.HUB_RADIUS = HUB_RADIUS;
    }

    /**
     * Build all the hubs
     * @param random the randomness decider from sharedData
     */
    public void buildHubs(Random random) {

        if(hubs.size() > 0) {
            Main.debug("Requested to build hubs while already built");
            return;
        }

        //First make an array that will have the vertice IDs in random order
        ArrayList<Integer> randomOrder = new ArrayList<Integer>(getSize());
        for(int i=0; i<getSize(); i++) {
            randomOrder.add(i);
        }
        Collections.shuffle(randomOrder, random);

        //Go over each vertice in a random-like order
        for(int i=0; i<getSize(); i++) {
            Vertex checkVertice = getVertex(randomOrder.get(i));

            //Only advance if it doesn't already belong to a hub
            if(checkVertice.getHubID() < 0) {
                Vertex newHub = checkVertice;
                //If not, make it a new hub and go over the radius
                addHub(newHub);

                ArrayList<Vertex> queue = new ArrayList<>();
                queue.add(newHub);

                while (!queue.isEmpty()) {
                    Vertex v = queue.remove(0);

                    for(Vertex neighbour : v.getNeigbours()) {
                        if(neighbour.getHubID() != v.getHubID()) {
                            //We are not going backwards

                            if (neighbour.getDistToHubCenter() <= v.getDistToHubCenter()) {
                                //This one is as close or closer to the new hub (may just be a new one)
                                if(neighbour.getHubID() < 0 || HUB_OVERWRITE_SET) {
                                    //Update all the new values

                                    if(neighbour.getHubID() >= 0) {
                                        //Remove it from the old hub first
                                        neighbour.getHub().removeHubVertice(neighbour);
                                    }

                                    newHub.addHubVertice(neighbour); //Add to the new hub
                                    neighbour.setHub(newHub); //Set to the new hub
                                    neighbour.setHubID(newHub.getHubID()); // and its ID
                                    neighbour.setDistToHubCenter(v.getDistToHubCenter()+1);
                                    neighbour.setVertexTowardsCenter(v);

                                    //Check if we should recurse
                                    if(neighbour.getDistToHubCenter() < HUB_RADIUS &&
                                            (neighbour.getHubID() < 0 || HUB_OVERWRITE_RECURSE)) {
                                        //We are not done with the radios and we should recurse
                                        queue.add(neighbour);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Find centers for the K-center problem
     */
    public void findKCenters() {
        if(kCenters.size() > 0) {
            Main.debug("Requested to find K-centers while already found");
            return;
        }

        Vertex firstKCenter = getVertex(0);
        firstKCenter.setKCenter(true);
        tempKCenters.add(firstKCenter);

        //TODO: I am making the assumption here that there are at least 2 vertices in the graph
        Vertex candidateCenter = getVertex(1);
        Vertex checkVertex;
        int distToNewestCenter;

        //Determine the order in which the approximation finds vertices as K-centers
        for(int i=0; i<Math.min(getSize(), Math.max(
                Math.floor(getSize()/10+Preamble.amountOfTaxis/2), Preamble.amountOfTaxis)
        ); i++) {
            for(int j=0; j<getSize(); j++) {
                checkVertex = getVertex(j);
                if(checkVertex.getDistToTempKCenter() != 0) {
                    //Find the distance from this vertex to the closest temporary K-center
                    distToNewestCenter = checkVertex.getDistanceTo(tempKCenters.get(tempKCenters.size()-1));
                    if(checkVertex.getDistToTempKCenter() > distToNewestCenter
                            ||  checkVertex.getDistToTempKCenter() == -1) {
                        checkVertex.setDistToTempKCenter(distToNewestCenter);
                    }

                    //Update what is the furthest vertex from any of the temporary K-center up until now
                    if (checkVertex.getDistToTempKCenter() > candidateCenter.getDistToTempKCenter()) {
                        candidateCenter = checkVertex;
                    }
                }
            }

            //Add the newly found K-center
            candidateCenter.setKCenter(true);
            tempKCenters.add(candidateCenter);
        }

        //Use the last found vertices as actual K-centers
        for(int i=1; i<Preamble.amountOfTaxis+1; i++) {
            kCenters.add(tempKCenters.get( tempKCenters.size()-i ));
        }

    }

    public ArrayList<Vertex> getKCenters() {
        return kCenters;
    }
}
