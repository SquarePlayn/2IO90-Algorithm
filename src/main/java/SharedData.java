import java.util.ArrayList;

public class SharedData {

    private ArrayList<Taxi> taxiList = new ArrayList<>();
    private ArrayList<Customer> customerList = new ArrayList<>();
    private CallList callList = new CallList();
    private Graph graph;

    public SharedData(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<Taxi> getTaxiList() {
        return taxiList;
    }

    public ArrayList<Customer> getCustomerList() {
        return customerList;
    }

    public Graph getGraph() {
        return graph;
    }
}
