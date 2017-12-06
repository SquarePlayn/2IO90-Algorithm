import java.util.ArrayList;

public class Graph {

    private ArrayList<Vertex> vertices;

    Graph() {
        this.vertices = new ArrayList<>();
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

}
