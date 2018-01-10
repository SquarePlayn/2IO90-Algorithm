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
    private ArrayList<Vertex> kClusterCenters;


    public static long findCenters = 0;
    public static long findClusters = 0;


    Graph() {
        this.vertices = new ArrayList<>();
        this.hubs = new ArrayList<>();
        this.kCenters = new ArrayList<>();
        this.kClusterCenters = new ArrayList<>();
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
        long start1 = System.nanoTime();
        if(kCenters.size() > 0) {
            Main.debug("Requested to find K-centers while already found");
            return;
        }

        //Pick the vertex with the most neighbors as the first center
        int candidateNeighborCount = 0;
        Vertex candidate = getVertex(0);
        for (Vertex v : vertices) {
            if(v.getNeigbours().size() > candidateNeighborCount) {
                candidate = v;
                candidateNeighborCount = v.getNeigbours().size();
            }
        }


        //Vertex candidate = getVertex(0);


        makeTempKCenter(candidate);
        System.out.println("First center: " + candidate.getId());

        //Find the other centers by consecutively finding which vertex is furthest away from the already chosen centers
        ArrayList<Vertex> queue = new ArrayList<>();
        for(int i=0; i<Preamble.amountOfTaxis; i++) {
            //Start the BFS from the already found centers
            queue.addAll(tempKCenters);

            while(!queue.isEmpty()) {
                //Run a BFS
                Vertex v = queue.remove(0);
                for(Vertex neighbor : v.getNeigbours()) {
                    if(!neighbor.isTempKCenter() && v.getkCenterVisited() > neighbor.getkCenterVisited()) {
                        queue.add(neighbor);
                        neighbor.increaseKCenterVisited();
                    }
                }

                //If this is the last vertex to be visited, make it a K-center
                if (queue.isEmpty()) {
                    makeTempKCenter(v);
                }
            }
        }

        //Choose the most recently found centers as the final ones
        for(int i=1; i<Preamble.amountOfTaxis+1; i++) {
            makeKCenter(tempKCenters.get( tempKCenters.size()-i ));
        }

        findCenters += System.nanoTime() - start1;

        //TODO: Delete this in final version
        //Values that give an indication of how well the centers were chosen
        int maxDist = 0;
        double avgDist = 0;
        for (Vertex v : vertices) {
            int closest = Integer.MAX_VALUE;
            for (Vertex center : kCenters) {
                if (v.getDistanceTo(center) < closest) {
                    closest = v.getDistanceTo(center);
                }
            }

            if (closest > maxDist) {
                maxDist = closest;
            }
            avgDist += closest;
        }

        avgDist /= getSize();

        System.out.println("Max. distance to center: " + maxDist);
        System.out.println("Avg. distance to center: " + avgDist);
        //end of test stuff

        long start2 = System.nanoTime();

        //Give each center a unique kClusterID
        for (int i=0; i<kCenters.size(); i++) {
            kCenters.get(i).setKClusterID(i);
        }

        //Form a cluster around each center
        queue = new ArrayList<>();
        queue.addAll(kCenters);
        while(!queue.isEmpty()) {
            Vertex v = queue.remove(0);
            for(Vertex neighbor : v.getNeigbours()) {
                if(neighbor.getKClusterID() == -1) {
                    queue.add(neighbor);
                    neighbor.setKClusterID(v.getKClusterID());
                }
            }
        }

        //Find the center of each cluster
        for (Vertex center : kCenters) {
            //Find furthest vertex from the K-center in this cluster
            Vertex longestPathStart = null;
            Vertex longestPathEnd = null;

            queue = new ArrayList<>();
            queue.add(center);
            center.setKCenterVisited(0);
            while(!queue.isEmpty()) {
                Vertex v = queue.remove(0);
                for(Vertex neighbor : v.getNeigbours()) {
                    if(neighbor.getKClusterID() == v.getKClusterID() && neighbor.getkCenterVisited() != 0) {
                        queue.add(neighbor);
                        neighbor.setKClusterID(v.getKClusterID());
                        neighbor.setKCenterVisited(0);
                    }
                }

                if(queue.isEmpty()) {
                    longestPathEnd = v;
                }
            }

            //Find the furthest away vertex from there
            queue = new ArrayList<>();
            queue.add(longestPathEnd);
            longestPathEnd.setKCenterVisited(1);
            while(!queue.isEmpty()) {
                Vertex v = queue.remove(0);
                for(Vertex neighbor : v.getNeigbours()) {
                    if(neighbor.getKClusterID() == v.getKClusterID() && neighbor.getkCenterVisited() != 1) {
                        queue.add(neighbor);
                        neighbor.setNextVertexInLongestPath(v);
                        neighbor.setKCenterVisited(1);
                    }
                }

                if(queue.isEmpty()) {
                    longestPathStart = v;
                }
            }

            //Retrieve path between these most remote vertices
            ArrayList<Vertex> longestPath = new ArrayList<>();
            queue = new ArrayList<>();
            queue.add(longestPathStart);
            longestPath.add(longestPathStart);
            while(!queue.isEmpty()) {
                Vertex v = queue.remove(0);
                if(v.getNextVertexInLongestPath() != null) {
                    queue.add(v.getNextVertexInLongestPath());
                    longestPath.add(v.getNextVertexInLongestPath());
                }
            }

            //Set the midway point in this path as the cluster center
            makeKClusterCenter(longestPath.get(longestPath.size()/2));
        }

        findClusters += System.nanoTime() - start2;

        //TODO: Delete this in final version
        //Values that give an indication of how well the centers were chosen
        int maxDist2 = 0;
        double avgDist2 = 0;
        for (Vertex v : vertices) {
            int closest = Integer.MAX_VALUE;
            for (Vertex center2 : kClusterCenters) {
                if (v.getDistanceTo(center2) < closest) {
                    closest = v.getDistanceTo(center2);
                }
            }

            if (closest > maxDist2) {
                maxDist2 = closest;
            }
            avgDist2 += closest;
        }

        avgDist2 /= getSize();

        System.out.println("Max. distance to cluster center: " + maxDist2);
        System.out.println("Avg. distance to cluster center: " + avgDist2);
        //end of test stuff

    }

    public void makeKCenter(Vertex v) {
        kCenters.add(v);
        v.setKCenter(true);
    }

    public void makeTempKCenter(Vertex v) {
        tempKCenters.add(v);
        v.setTempKCenter(true);
    }

    public void makeKClusterCenter(Vertex v) {
        kClusterCenters.add(v);
        v.setKClusterCenter(true);
    }

    public ArrayList<Vertex> getKCenters() {
        return kCenters;
    }

    public ArrayList<Vertex> getkClusterCenters() {
        return kClusterCenters;
    }
}
