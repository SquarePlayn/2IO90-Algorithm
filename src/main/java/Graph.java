import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Graph {

    private ArrayList<Vertex> vertices;

    //Needed for the organizing of hubs code
    private ArrayList<Vertex> hubs;
    private int HUB_RADIUS = 5;
    private static final boolean HUB_OVERWRITE_SET = true; // If, when finding a vertex is closer to another hub,
    // it should be assigned to the closer one
    private static final boolean HUB_OVERWRITE_RECURSE = true; // If when you find a closer one, you also check its neighbours
    private static final boolean HUB_BUILD_INTERHUBINFO = true; //If you want to also build the info like paths between hubs
    private static final boolean HUB_BUILD_HUBTOVERTEX_NEXTTOWARDS = true; //If you want to store the next-up vertices towards each vertex in the circle
    private static final boolean HUB_BUILD_INTERHUB_FULLPATHS = false; //If you want to store in the hub info the full path arrays to neighbouring vertices
    private static final boolean HUB_BUILD_INTERHUB_NEXTTOWARDS = true; //If you want to store in the vertices on the path to neighbouring hubs what the next vertex is towards it. Requires hubtovertex
    private static final boolean HUB_BUILD_NEXTHUBTOWARDSHUB = true; // If you want to build the DB of which hub is the next hub to go to to get to any hub

    Graph() {
        this.vertices = new ArrayList<>();
        this.hubs = new ArrayList<>();
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

        //Bigger hub range for bigger graphs to save time
        //TODO Make more intuitive function here
        if(getSize() > 15000) {
            HUB_RADIUS = 7;
        } else {
            HUB_RADIUS = 4;
        }

        //First make an array that will have the vertice IDs in random order
        ArrayList<Integer> randomOrder = new ArrayList<>(getSize());
        for(int i=0; i<getSize(); i++) {
            randomOrder.add(i);
        }
        Collections.shuffle(randomOrder, random);

        //Go over each vertice in a random-like order to make them all into hubs
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

                            if ((neighbour.getHubID() < 0 ||
                                    (v.getDistToHubCenter() + 1 < neighbour.getDistToHubCenter() && HUB_OVERWRITE_SET)) &&
                                    v.getDistToHubCenter() < HUB_RADIUS) {
                                //(New un-hubbed node or
                                // closer to new hub than old hub it was already assigned to and overwrite-setting is true)
                                //and not outside of the radius of the hub

                                boolean wasUnassigned = neighbour.getHubID() < 0;

                                //Update all the new values

                                if (neighbour.getHubID() >= 0) {
                                    //If it was previously assigned to a hub already
                                    //Remove it from the old hub first
                                    neighbour.getHub().removeHubVertice(neighbour);
                                }

                                newHub.addHubVertice(neighbour); //Add to the new hubs list
                                neighbour.setHub(newHub); //Set vertice-hubvalue to the new hub
                                neighbour.setHubID(newHub.getHubID()); // and its ID
                                neighbour.setVertexTowardsCenter(v); // and the vertex back
                                neighbour.setDistToHubCenter(v.getDistToHubCenter() + 1);

                                if(HUB_BUILD_HUBTOVERTEX_NEXTTOWARDS) {
                                    //Set the path on how to get to neighbour from the hub
                                    Vertex vPrev = neighbour;
                                    Vertex vNow = v;
                                    while (!vPrev.equals(v.getHub())) {
                                        vNow.setNextTowards(neighbour, vPrev);
                                        vPrev = vNow;
                                        vNow = vNow.getVertexTowardsCenter();
                                    }
                                }

                                //Check if we should recurse
                                if (neighbour.getDistToHubCenter() <= HUB_RADIUS &&
                                        (wasUnassigned || HUB_OVERWRITE_RECURSE)) {
                                    //We are not done with the radios and we should recurse
                                    queue.add(neighbour);
                                }
                            } else {
                                // Not expanding onto this neighbour



                                if(neighbour.getHubID() >= 0) {
                                    //This node was assigned to some hub

                                    if(HUB_BUILD_INTERHUBINFO) {
                                        //We want to update/enter interhub info


                                        //AdjacentHubs
                                        v.getHub().addAdjacentHub(neighbour.getHub());
                                        neighbour.getHub().addAdjacentHub(v.getHub());

                                        int newDist = v.getDistToHubCenter() + neighbour.getDistToHubCenter() + 1;

                                        if (newDist < v.getHub().getDistanceTo(neighbour.getHub())) {
                                            //New distance is smaller than the old one, update the distances
                                            v.getHub().setDistanceToHubCenter(neighbour.getHub(), newDist);
                                            neighbour.getHub().setDistanceToHubCenter(v.getHub(), newDist);

                                            if(HUB_BUILD_INTERHUB_FULLPATHS) {
                                                //also update pathToHubCenter
                                                ArrayList<Vertex> path = new ArrayList<>();

                                                //Make path v -> vHub
                                                Vertex pathVert = v;
                                                while (!pathVert.equals(v.getHub())) {
                                                    path.add(pathVert);
                                                    pathVert = pathVert.getVertexTowardsCenter();
                                                }

                                                Collections.reverse(path); //Turn it around so now it's vHub -> v

                                                //Add neighbour -> neighbourHub to the path
                                                pathVert = neighbour;
                                                while (!pathVert.equals(neighbour.getHub())) {
                                                    path.add(pathVert);
                                                    pathVert = pathVert.getVertexTowardsCenter();
                                                }

                                                //Now we have the path excluding the end hubs from vHub -> neighbourHub.

                                                //Make a new one that goes neighbourHub -> vHub.
                                                ArrayList<Vertex> pathBack = new ArrayList<>(path);
                                                Collections.reverse(pathBack);

                                                //Add the correct end bits
                                                path.add(neighbour.getHub());
                                                pathBack.add(v.getHub());

                                                //Now set the paths in the hubs
                                                v.getHub().setPathToHubCenter(neighbour.getHub(), path);
                                                neighbour.getHub().setPathToHubCenter(v.getHub(), pathBack);

                                            }

                                            if(HUB_BUILD_INTERHUB_NEXTTOWARDS) {
                                                if(!HUB_BUILD_HUBTOVERTEX_NEXTTOWARDS) {
                                                    Main.debug("[ERROR] Used nexttowards, with hubtovertex off");
                                                } else {
                                                    //update how to get between the hubs

                                                    //For all from vHub to v
                                                    Vertex vIter = v.getVertexTowardsCenter();
                                                    while(vIter != null) {
                                                        vIter.setNextTowards(neighbour.getHub(), vIter.getNextTowards(v));
                                                        vIter = vIter.getVertexTowardsCenter();
                                                    }

                                                    //For all from neighbourHub to neighbour
                                                    vIter = neighbour.getVertexTowardsCenter();
                                                    while(vIter != null) {
                                                        vIter.setNextTowards(v.getHub(), vIter.getNextTowards(neighbour));
                                                    }

                                                    //For v to neighbour
                                                    v.setNextTowards(neighbour.getHub(), neighbour);

                                                    //For neighbour to v
                                                    neighbour.setNextTowards(v.getHub(), v);
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        //If we want to build nexthubtohub info, let's do so in BFS-alike fashion
        if(HUB_BUILD_NEXTHUBTOWARDSHUB) {
            for(Vertex hub : hubs) {
                //Do so for each hub
                ArrayList<Vertex> hubQueue = new ArrayList<>();

                hub.setNextHubTowardsHub(hub, hub);
                hubQueue.add(hub);

                while(!hubQueue.isEmpty()) {
                    Vertex selected = hubQueue.remove(0);

                    for(Vertex neighbour : selected.getAdjacentHubs()) {
                        //Check each adjacent hub to see if it should be added

                        if(neighbour.getNextHubTowardsHub(hub) == null) {
                            //Not checked yet
                            neighbour.setNextHubTowardsHub(hub, selected);
                            hubQueue.add(neighbour);
                        }
                    }
                }
            }
        }

    }
}
