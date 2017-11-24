import java.util.ArrayList;

public class Graph {

    private ArrayList<Vertex> vertices;

    Graph() {
        vertices = new ArrayList<Vertex>();
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
        bfs(to); //Run the BFS algorithm to store the distances in each node with regards to our destination
        return from.bfsDistance; //Return how far our start is from our destination
    }

    /**
     * Get all vertices that make up the shortest path from "from" to "to".
     * The vertices will be in the correct order with 0 == from and len(path)-1 == to
     * @param from the vertex to start your path from
     * @param to the vertex where your path will lead to
     * @return a list of all vertices which you will travel over to get from "from" to "to" in as short as possible
     */
    public ArrayList<Vertex> getShortestPath(Vertex from, Vertex to) {
        ArrayList<Vertex> path = new ArrayList<Vertex>();
        Vertex next = from;
        bfs(to); //Run the BFS algorithm to store the next vertex to go to in each node with regards to our destination

        //Check each node for which node would be the next one until we have arrived at the destination
        while(next != null){
            path.add(next);
            next = next.bfsFrom;
        }
        return path;
    }

    /**
     * Implementation of Breadth First Search
     * When ran, it sets in each vertice the minimal distance to the "start" node,
     *  as well as the next node to go to when you want to get to this "start" node as quick as possible.
     * Runs in an expanding fashion: First explore 1 layer, then another and so forth untill the entire
     *  graph has been visited
     * Erases/overwrites any BSF results of earlier calls
     * @param start Vertex to execute the BFS relative to (distance to / shortest path towards)
     */
    public void bfs(Vertex start) {

        //Reset all bfs values of earlier BFS calls
        for(Vertex vertex: vertices) {
            vertex.bfsDistance = Integer.MAX_VALUE;
            vertex.bfsFrom = null;
        }

        //Make an empty queue and array to keep track of which vertices we have already visited.
        ArrayList<Vertex> queue = new ArrayList<Vertex>();
        boolean visited[] = new boolean[getSize()];

        //Start with the start. Since we have added it now, set visited to true.
        visited[start.getId()] = true;
        queue.add(start);

        //Now make sure we empty the whole queue, resulting on going over each vertice in the connected graph
        while(!queue.isEmpty()) {
            //Pop the item that is first up
            Vertex now = queue.get(0);
            queue.remove(0);

            //For each connection:
            for(Vertex towards: now.getNeigbours()) {
                if(!visited[towards.getId()]) {
                    //If we have not yet done anything with this node, we have not seen it before
                    //That means that we have now taken the shortest route to it. So the distance is 1 greater
                    // than the distance to the node we are considering, and the shortest path back is trough the node
                    // that we are considering.
                    //Also add it to the (end of the) queue to traverse further
                    visited[towards.getId()] = true;
                    queue.add(towards);
                    towards.bfsFrom = now;
                    towards.bfsDistance = now.bfsDistance + 1;
                }
            }
        }



    }

}
