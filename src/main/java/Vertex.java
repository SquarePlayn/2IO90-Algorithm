import java.util.ArrayList;

public class Vertex {

    //List all adjacent vertices
    private ArrayList<Vertex> connections;

    private int id;

    //After having run a BFS: stores the vertex to go to next if you want the shortest path to the node which you ran
    // the BFS on.
    Vertex bfsFrom;

    //Likewise, stores the length of the shortest path to the node you ran a BFS on after having ran.
    int bfsDistance;

    public Vertex(int id) {
        this.id = id;
        connections = new ArrayList<Vertex>();
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

}
