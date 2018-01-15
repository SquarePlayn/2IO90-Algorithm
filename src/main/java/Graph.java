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
    private ArrayList<Vertex> kCenters;
    private ArrayList<Vertex> clusterOrigins;
    private Vertex graphCenter;


    public static long findCenters = 0;
    public static long findClusters = 0;
    public static long findGraphCenter = 0;


    Graph() {
        this.vertices = new ArrayList<>();
        this.hubs = new ArrayList<>();
        this.kCenters = new ArrayList<>();
        this.clusterOrigins = new ArrayList<>();
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
     * Find centers for the K-center problem:
     * To do this, we will create several clusters of vertices. (Equally many clusters as taxis (k)).
     * To nicely spread out these clusters over the graph, we first pick k vertices, that are decently spread out,
     * to become the origins of these clusters and thus roughly mark the location of the clusters.
     * Then, we will create the clusters by having each Vertex join the same cluster as the closest cluster origin.
     * The centers of these clusters will then be chosen as the K-centers.
     */
    public void findKCenters() {
        long start1 = System.nanoTime();
        if(kCenters.size() > 0) {
            Main.debug("Requested to find K-centers while already found");
            return;
        }

        //Find the cluster origins
        ArrayList<Vertex> queue = new ArrayList<>();
        if(Preamble.amountOfTaxis < getSize()/3) { //Use greedy approximation for few taxis
            System.out.println("Strategy A");
            //Figure out if the calls are roughly uniformly distributed or not (using standard deviation of #calls per vertex)
            double avgAmountOfTrainingCalls = 0;
            boolean uniformCallDistribution = true;
            for(Vertex v : vertices) {
                avgAmountOfTrainingCalls += v.getAmountOfTrainingCalls();
            }
            avgAmountOfTrainingCalls /= getSize();
            double stdDevOfTrainingCalls = 0;
            for(Vertex v : vertices)
                stdDevOfTrainingCalls += (v.getAmountOfTrainingCalls()-avgAmountOfTrainingCalls)*
                        (v.getAmountOfTrainingCalls()-avgAmountOfTrainingCalls);
            stdDevOfTrainingCalls = Math.sqrt(stdDevOfTrainingCalls/(getSize()-1));
            if(stdDevOfTrainingCalls > 1) {
                uniformCallDistribution = false;
            }

            if(uniformCallDistribution) { //If the calls are (roughly) uniformly distributed...
                System.out.println("uniform");
                //Make vertex 0 the first cluster origin
                makeClusterOrigin(getVertex(0));
            }else { //If there appears to be some pattern in the calls, not occurring by chance...
                System.out.println("not uniform");
                //Sort vertices by decreasing amount of training calls
                ArrayList<Vertex> verticesByTrainingCalls = new ArrayList<>();
                verticesByTrainingCalls.addAll(vertices);
                verticesByTrainingCalls.sort((o1, o2) ->
                        Integer.compare(o2.getAmountOfTrainingCalls(), o1.getAmountOfTrainingCalls()));

                //Make the vertices with the most calls, the first third of the cluster origins
                for(int i=0; i<Preamble.amountOfTaxis/3; i++) {
                    makeClusterOrigin(verticesByTrainingCalls.get(i));
                }
            }
            //Find the other origins by consecutively finding which vertex is furthest away from any already chosen origin
            while (clusterOrigins.size() < Preamble.amountOfTaxis) {
                //Start the BFS from the already found origins
                queue.addAll(clusterOrigins);

                while(!queue.isEmpty()) {
                    //Run a BFS
                    Vertex v = queue.remove(0);
                    for(Vertex neighbor : v.getNeigbours()) {
                        if(!neighbor.isClusterOrigin() && v.getkCenterVisited() > neighbor.getkCenterVisited()) {
                            queue.add(neighbor);
                            neighbor.increaseKCenterVisited();
                        }
                    }

                    //If this is the last vertex to be visited, make it a cluster origin
                    if(queue.isEmpty()) {
                        makeClusterOrigin(v);
                    }
                }
            }
        } else { //Choose origins randomly for many taxis
            System.out.println("Strategy B");
            //Randomize the order of the vertices
            ArrayList<Vertex> randomOrder = new ArrayList<>();
            randomOrder.addAll(vertices);
            Collections.shuffle(randomOrder);

            for(int i=0; i<Math.min(getSize(), Preamble.amountOfTaxis); i++) {
                makeClusterOrigin(randomOrder.get(i));
            }
        }
        findCenters += System.nanoTime() - start1;

        //TODO: Delete this in final version
        //Values that give an indication of how well the origins were chosen
        /*int maxDist = 0;
        double avgDist = 0;
        for (Vertex v : vertices) {
            int closest = Integer.MAX_VALUE;
            for (Vertex center : clusterOrigins) {
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

        System.out.println("Max. distance to origin: " + maxDist);
        System.out.println("Avg. distance to origin: " + avgDist);
        System.out.println("  (size = " + clusterOrigins.size() + ")");*/
        //end of test stuff

        long start2 = System.nanoTime();

        //Give each origin a unique clusterID
        for (int i=0; i<clusterOrigins.size(); i++) {
            clusterOrigins.get(i).setClusterID(i);
        }

        //Form a cluster around each origin
        queue = new ArrayList<>();
        queue.addAll(clusterOrigins);
        while(!queue.isEmpty()) {
            Vertex v = queue.remove(0);
            for(Vertex neighbor : v.getNeigbours()) {
                //If a neighbor doesn't belong to a cluster yet, add it to the same cluster as the current vertex
                if(neighbor.getClusterID() == -1) {
                    queue.add(neighbor);
                    neighbor.setClusterID(v.getClusterID());
                }
            }
        }

        //Find the center of each cluster
        for (Vertex origin : clusterOrigins) {
            //Find furthest vertex from the cluster origin
            Vertex longestPathStart = null;
            Vertex longestPathEnd = null;

            queue = new ArrayList<>();
            queue.add(origin);
            origin.setKCenterVisited(0);
            while(!queue.isEmpty()) {
                Vertex v = queue.remove(0);
                for(Vertex neighbor : v.getNeigbours()) {
                    if(neighbor.getClusterID() == v.getClusterID() && neighbor.getkCenterVisited() != 0) {
                        queue.add(neighbor);
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
                    if(neighbor.getClusterID() == v.getClusterID() && neighbor.getkCenterVisited() != 1) {
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
                if(v != longestPathEnd) {
                    queue.add(v.getNextVertexInLongestPath());
                    longestPath.add(v.getNextVertexInLongestPath());
                }
            }
            //Set the midway point in this path as the cluster center
            makeKCenter(longestPath.get(longestPath.size()/2));
        }

        findClusters += System.nanoTime() - start2;

        //TODO: Delete this in final version
        //Values that give an indication of how well the K-centers were chosen
        int maxDist2 = 0;
        double avgDist2 = 0;
        for (Vertex v : vertices) {
            int closest = Integer.MAX_VALUE;
            for (Vertex center2 : kCenters) {
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

        System.out.println("Max. distance to K-center: " + maxDist2);
        System.out.println("Avg. distance to K-center: " + avgDist2);
        //end of test stuff

    }

    public void findGraphCenter() {
        long start3 = System.nanoTime();
        //Find furthest vertex from node 0
        Vertex furthestA = null;
        Vertex furthestB = null;

        ArrayList<Vertex> queue = new ArrayList<>();
        queue.add(getVertex(0));
        getVertex(0).increaseKCenterVisited();
        while(!queue.isEmpty()) {
            Vertex v = queue.remove(0);
            for(Vertex neighbor : v.getNeigbours()) {
                if(v.getkCenterVisited() > neighbor.getkCenterVisited()) {
                    queue.add(neighbor);
                    neighbor.increaseKCenterVisited();
                }
            }

            if(queue.isEmpty()) {
                furthestA = v;
            }
        }

        //Find the furthest away vertex from there
        queue = new ArrayList<>();
        queue.add(furthestA);
        furthestA.increaseKCenterVisited();
        while(!queue.isEmpty()) {
            Vertex v = queue.remove(0);
            for(Vertex neighbor : v.getNeigbours()) {
                if(v.getkCenterVisited() > neighbor.getkCenterVisited()) {
                    queue.add(neighbor);
                    neighbor.setNextVertexInLongestPath(v);
                    neighbor.increaseKCenterVisited();
                }
            }

            if(queue.isEmpty()) {
                furthestB = v;
            }
        }

        //Retrieve path between these most remote vertices
        ArrayList<Vertex> longestPath = new ArrayList<>();
        queue = new ArrayList<>();
        queue.add(furthestB);
        longestPath.add(furthestB);
        while(!queue.isEmpty()) {
            Vertex v = queue.remove(0);
            if(v != furthestA) {
                queue.add(v.getNextVertexInLongestPath());
                longestPath.add(v.getNextVertexInLongestPath());
            }
        }
        //Set the midway point in this path as the graph center
        graphCenter =  longestPath.get(longestPath.size()/2);
        findGraphCenter += System.nanoTime() - start3;
    }

    public void makeKCenter(Vertex v) {
        kCenters.add(v);
        v.setKCenter(true);
    }

    public void makeClusterOrigin(Vertex v) {
        clusterOrigins.add(v);
        v.setClusterOrigin(true);
    }

    public ArrayList<Vertex> getKCenters() {
        return kCenters;
    }

    public ArrayList<Vertex> getClusterOrigins() {
        return clusterOrigins;
    }

    public Vertex getClosestKCenter(Vertex from) {
        if(from.isKCenter()) {
            return from;
        }
        for (Vertex center : kCenters) {
            if(from.getClusterID() == center.getClusterID()) {
                return center;
            }
        }
        return null;
    }

    public Vertex getGraphCenter() {
        return graphCenter;
    }
}
