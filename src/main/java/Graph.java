import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;

public class Graph {

    ArrayList<Vertex> vertices;

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
     *
     * @return
     */
    public int getSize() {
        return vertices.size();
    }

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
        vertices.get(a).connections.add(vertices.get(b));
        vertices.get(b).connections.add(vertices.get(a));
    }

    public int getDistance(Vertex from, Vertex to) {
        bfs(to);
        return from.bfsDistance;
    }

    public ArrayList<Vertex> getShortestPath(Vertex from, Vertex to) {
        ArrayList<Vertex> path = new ArrayList<Vertex>();
        Vertex next = from;
        bfs(to);
        while(next != null){
            path.add(next);
            next = next.bfsFrom;
        }
        return path;
    }

    public void bfs(Vertex start) {

        //Reset all bfs values
        for(Vertex vertex: vertices) {
            vertex.bfsDistance = Integer.MAX_VALUE;
            vertex.bfsFrom = null;
        }

        ArrayList<Vertex> queue = new ArrayList<Vertex>();
        boolean visited[] = new boolean[getSize()];
        visited[start.number] = true;
        queue.add(start);

        while(!queue.isEmpty()) {
            Vertex now = queue.get(0);
            queue.remove(0);

            for(Vertex towards: now.connections) {
                if(!visited[towards.number]) {
                    visited[towards.number] = true;
                    queue.add(towards);
                    towards.bfsFrom = now;
                    towards.bfsDistance = now.bfsDistance + 1;
                }
            }
        }



    }

}
