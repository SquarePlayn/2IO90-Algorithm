import java.util.ArrayList;
import java.util.Random;

public class SharedData {
    private final long SEED = 12345678910L;

    private Random random = new Random(SEED);
    private ArrayList<Taxi> taxiList = new ArrayList<>();
    private ArrayList<Customer> customerList = new ArrayList<>();
    private IOHistory iOHistory = new IOHistory();
    private Graph graph;

    public SharedData(Graph graph) {
        this.graph = graph;
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
