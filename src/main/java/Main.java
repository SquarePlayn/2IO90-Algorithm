public class Main {

    private static final boolean DEBUG = false;

    private TaxiScanner scanner;
    private Preamble preamble;
    private Scheduler scheduler;

    public Main() {
        scanner = TaxiScanner.getInstance();
        preamble = new Preamble(scanner);
        scheduler = new Scheduler(scanner);
    }

    public void run() {
        //Read the preamble
        if (!preamble.read()) {
            throw new RuntimeException("An error occurred while reading preamble.");
        }

        //Hand execution over to the scheduler
        scheduler.run();

        // Reset (create a new instance) each algorithm, this is required for the interpreter
        AlgorithmType.SIMPLEQUEUE.reset();
        AlgorithmType.GCC.reset();
    }

    public static void debug(String message) {

        if (DEBUG) {
            System.out.println(message);
        }

    }

    public static void main(String[] args) {

        (new Main()).run();

    }

}
