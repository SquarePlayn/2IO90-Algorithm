import java.util.ArrayList;

public class Vertex {

    //List all adjacent vertices
    private ArrayList<Vertex> connections;
    private int id;

    //Shortest path variables
    private boolean[] cached;
    private int[] distanceTo;
    private Vertex[] nextTowards;
    private boolean[] visited;


    public Vertex(int id) {
        this.id = id;
        connections = new ArrayList<>();

        int amountOfNodes = Preamble.graphSize;
        cached = new boolean[amountOfNodes];
        distanceTo = new int[amountOfNodes];
        nextTowards = new Vertex[amountOfNodes];
        visited = new boolean[amountOfNodes];
    }

    public void updateDistanceInfo(Vertex startedPoint, int distance, Vertex nextTowards) {
        this.distanceTo[startedPoint.getId()] = distance;
        this.nextTowards[startedPoint.getId()] = nextTowards;
        this.cached[startedPoint.getId()] = true;
    }

    public int getDistanceTo(Vertex vertex) {
        if(!cached[vertex.getId()]) {
            vertex.bfs();
        }
        return distanceTo[vertex.getId()];
    }

    public Vertex getNextTowards(Vertex vertex) {
        if(!cached[vertex.getId()]) {
            vertex.bfs();
        }
        return nextTowards[vertex.getId()];
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
        visited[vertex.getId()] = value;
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
        cached[getId()] = true;
        distanceTo[getId()] = 0;
        visited[getId()] = true;

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
        return visited[vertex.getId()];
    }
}
