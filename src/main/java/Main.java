public class Main {

    private TaxiScanner scanner;
    private Preamble preamble;

    private static final boolean DEBUG = false;

    private Scheduler scheduler;

    private void setup() {
        scanner = TaxiScanner.getInstance();

        preamble = new Preamble(scanner);

        if (!preamble.read()) {
            throw new RuntimeException("An error occurred while reading preamble.");
        }

        scheduler = new Scheduler(scanner);
    }

    public void run() {

        setup();

        //Hand execution over to the scheduler
        scheduler.run();
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
