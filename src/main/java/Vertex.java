import java.util.ArrayList;

public class Vertex {

    ArrayList<Vertex> connections;
    int number;
    Vertex bfsFrom;
    int bfsDistance;

    public Vertex(int nr) {
        number = nr;
        connections = new ArrayList<Vertex>();


    }

}
