import java.util.ArrayList;

public class Main {

	private TaxiScanner scanner;
	private PreambleReader preambleReader;

	private int alpha; // Alpha for the cost function
	private int maxTime; // Maximum time between ordering a taxi and being dropped off

	private ArrayList<Taxi> taxiList;
	private ArrayList<Customer> customerList;

	private Graph graph;

	public void setup() {
		
		scanner = TaxiScanner.getInstance();

		taxiList = new ArrayList<Taxi>();
		customerList = new ArrayList<Customer>();

		graph = new Graph();

		preambleReader = new PreambleReader(this);

		preambleReader.read();
		
	}
	
	public void run() {
		
		setup();
		
	}

	public static void main(String[] args) {
		
		(new Main()).run();

	}

	public TaxiScanner getScanner() {
		return scanner;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
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
