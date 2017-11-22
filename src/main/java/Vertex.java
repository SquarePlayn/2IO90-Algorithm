import java.util.ArrayList;

public class Vertex {

    //List all adjacent vertices
    ArrayList<Vertex> connections;

    int number;

    //After having run a BFS: stores the vertex to go to next if you want the shortest path to the node which you ran
    // the BFS on.
    Vertex bfsFrom;

    //Likewise, stores the length of the shortest path to the node you ran a BFS on after having ran.
    int bfsDistance;

    public Vertex(int nr) {
        number = nr;
        connections = new ArrayList<Vertex>();
    }

}
