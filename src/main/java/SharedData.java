import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class SharedData {
    private final long SEED = 12345678910L;

    private Random random;
    private ArrayList<Taxi> taxiList;
    private HashSet<Customer> customerList;
    private HashSet<Customer> customerOutsideList;
    private HashSet<Customer> customerInTaxiList;
    private IOHistory iOHistory;
    private Graph graph;
    private int customerCallAmount;

    public SharedData(Graph graph) {
        this.graph = graph;
        this.random = new Random(SEED);
        this.taxiList = new ArrayList<>();
        this.customerList = new HashSet<>();
        this.customerOutsideList = new HashSet<>();
        this.customerInTaxiList = new HashSet<>();
        this.iOHistory = new IOHistory();
        this.customerCallAmount = 0;
    }

    public Random getRandom() {
        return random;
    }

    public ArrayList<Taxi> getTaxiList() {
        return taxiList;
    }

    public HashSet<Customer> getCustomerList() {
        return customerList;
    }

    public HashSet<Customer> getCustomerOutsideList() {
        return customerOutsideList;
    }

    public HashSet<Customer> getCustomerInTaxiList() {
        return customerInTaxiList;
    }

    public Graph getGraph() {
        return graph;
    }

    public IOHistory getIOHistory() {
        return iOHistory;
    }

    public void addCustomerCallAmount(int calls) {
        this.customerCallAmount += calls;
    }

    public int getCustomerCallAmount() {
        return customerCallAmount;
    }

}
