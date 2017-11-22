public class Main {
	
	private TaxiScanner scanner;

	private int alpha; // Alpha for the cost function
	private int maxTime; // Maximum time between ordering a taxi and being dropped off
	private int taxiCapacity; // Maximum amount

	
	public void setup() {
		
		scanner = TaxiScanner.getInstance();
		
	}
	
	public void run() {
		
		setup();
		
	}

	public static void main(String[] args) {
		
		(new Main()).run();

	}

}
