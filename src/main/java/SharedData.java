import java.util.ArrayList;
import java.util.Random;

public class SharedData {
    private final long SEED = 12345678910L;

    private Random random;
    private ArrayList<Taxi> taxiList;
    private ArrayList<Customer> customerList;
    private IOHistory iOHistory;
    private Graph graph;

    public SharedData(Graph graph) {
        this.graph = graph;
        this.random = new Random(SEED);
        this.taxiList = new ArrayList<>();
        this.customerList = new ArrayList<>();
        this.iOHistory = new IOHistory();
    }

    public Random getRandom() {
        return random;
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

    public IOHistory getIOHistory() {
        return iOHistory;
    }
}
