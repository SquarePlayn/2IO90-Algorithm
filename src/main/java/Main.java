import java.util.ArrayList;

public class Main {

	private TaxiScanner scanner;
	private PreambleReader preambleReader;

	private int alpha; // Alpha for the cost function
	private int maxTime; // Maximum time between ordering a taxi and being dropped off

	private ArrayList<Taxi> taxiList;
	private ArrayList<Customer> customerList;

	private Graph graph;

	//The following variables are used for the prototype that uses queues for the taxis and customers
	private ArrayList<Taxi> taxiReadyQueue;
	private ArrayList<Taxi> taxiInOperationList;
	private ArrayList<Customer> customerQueue;
	int testMinutes = 15; //TODO Implement this from the preamble Reader to be the correct value
	int callMinutes = 30; //TODO Implement. testMinutes = t' and callMinutes is t as specified in the input document

	public void setup() {
		
		scanner = TaxiScanner.getInstance();

		taxiList = new ArrayList<>();
		customerList = new ArrayList<>();

		graph = new Graph();

		preambleReader = new PreambleReader(this);

		preambleReader.read();

	}
	
	public void run() {
		
		setup();

		//Read over all test case info without delivering people
		initialiseTaxis();
		for(int i=1; i<testMinutes; i++) {
			scanner.println("c");
		}

		//From here on, the Actual calling and being called of taxis starts
		initialiseTaxis();

		//Initialize the queues.
		customerQueue = new ArrayList<>();
		taxiReadyQueue = new ArrayList<>();
		taxiInOperationList = new ArrayList<>();

		taxiReadyQueue.addAll(taxiList); // Initially all taxis are unoccupied, so add them all to the ready queue.

		//While there are lines to read, read them and advance to next minute
		while(scanner.hasNextLine()) {
			readInput();
			advanceMinute();
		}

		//Since there are no more lines to read, advance untill all customers are delivered
		while(!customerQueue.isEmpty() || !taxiInOperationList.isEmpty()) {
			advanceMinute();
		}
		
	}

	/**
	 * Reads one line of input and processes it
	 */
	private void readInput() {
		if(scanner.hasNextLine()) {
			String[] input = scanner.nextLine().split(" ");

			int amountOfCalls = Integer.parseInt(input[0]);
			for(int i=0; i<amountOfCalls; i++) {
				//Read in each new customer and add the customer to the waiting queue
				Vertex position = graph.getVertex(Integer.parseInt(input[i*2+1]));
				Vertex destination = graph.getVertex(Integer.parseInt(input[i*2+2]));
				customerQueue.add(new Customer(position, destination));
			}
		}
	}

	/**
	 * Execute the advancing over the next minute. Basically move all taxis and output their actions.
	 * TODO Find better name
	 */
	private void advanceMinute() {
		//First assign a taxi to each waiting customer as far as possible
		//TODO Note: This leads to problems when a taxi for a later customer arrives before the earlier customer is picked up
		while(!customerQueue.isEmpty()) {
			if(taxiReadyQueue.isEmpty()) {
				//If there are no more ready taxis, the remaining customers will have to wait
				break;

			} else {
				// Get the first-up taxi, pop it from the queue and add it to the ones in operation
				Taxi taxi = taxiReadyQueue.get(0);
				taxiReadyQueue.remove(0);
				taxiInOperationList.add(taxi);

				//Pop the custoper that is first-up
				Customer customer = customerQueue.get(0);
				customerQueue.remove(0);

				//Assign the taxi to the customer and make the taxi go towards the customer
				taxi.setCustomer(customer);
				taxi.setPath(graph.getShortestPath(taxi.getPosition(), customer.getPosition()));
			}
		}


		//Advance all taxis that have an operation
		for(Taxi taxi: taxiInOperationList) {
			String output = taxi.continueOperation(graph);
			scanner.println(output);

			if(!taxi.getInOperation()){
				//If the taxi is now done delivering its client, we can put it back in the queue
				taxiInOperationList.remove(taxi);
				taxiReadyQueue.add(taxi);
			}
		}

		//After having advanced all, declare the current minute complete
		scanner.println("c");
	}

	/**
	 * Initializes each taxi to a random position and prints that to the output
	 * Only run when you need to output the first (init) line of output
	 */
	private void initialiseTaxis() {
		for(Taxi taxi: taxiList) {
			taxi.setPosition(graph.getVertex((int)(Math.random()*graph.getSize())));
			scanner.println("m "+ taxi.getNumber() + " " + taxi.getPosition().getNumber() + " ");
		}
		//Flush this line of initial (random) positions to advance to the next (first) minute of input
		scanner.println("c");
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
