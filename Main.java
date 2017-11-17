
public class Main {
	
	private TaxiScanner scanner;
	
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
