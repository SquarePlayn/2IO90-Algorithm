public class Scheduler {

    //ENUMS

    private TaxiScanner scanner;
    private SharedData sharedData;

    private AlgorithmType activeAlgorithm;

    public Scheduler(TaxiScanner scanner) {
        this.scanner = scanner;
        this.sharedData = new SharedData(Preamble.graph);
    }

    public void run() {
        createTaxiList();
        testMinutes();
        realMinutes();
    }

    public void testMinutes() {
        //Read over all test case info without delivering people
        initializeTaxis();
        for (int i = 1; i < Preamble.testMinutes; i++) {
            Main.debug("Starting minute "+i);
            scanner.nextLine();
            scanner.println("c");
        }
        scanner.nextLine();
    }

    public void realMinutes() {
        //From here on, the Actual calling and being called of taxis starts
        initializeTaxis();

        //TODO Make sure to redecide which algorithm to use at certain times
        //TODO Make sure in reschedule that algo initialised and up to date

        //While there are lines to read, read them and advance to next minute
        while (scanner.hasNextLine()) {
            readInput();
            advanceMinute(true);
        }

        //Since there are no more lines to read, advance until all customers are delivered
        while (!sharedData.getCustomerList().isEmpty()) {
            advanceMinute(false);
        }
    }

    private void readInput() {
        //TODO Read one line and store new minute in CallList (in sharedData)

    }

    /**
     * Places each taxi on a starting position
     * @return Output string that will set each taxi on a starting position.
     */
    public void initializeTaxis() {
        String output = initializeTaxis_random();
        output += "c";

        Main.debug("InitialiseTaxis() is now outputting "+output);
        scanner.println(output);
    }

    /**
     * Initializes each taxi to a random position and prints that to the output
     * Only run when you need to output the first (init) line of output
     */
    private String initializeTaxis_random() {
        //TODO Handle different taxi initialize options like we handle different advance minute options

        Main.debug("Starting initialize taxis");
        StringBuilder output = new StringBuilder();

        for (Taxi taxi : sharedData.getTaxiList()) {
            taxi.setPosition(sharedData.getGraph().getVertex((int) (Math.random() * sharedData.getGraph().getSize())));
            output.append("m ")
                    .append(taxi.getOutputId())
                    .append(" ")
                    .append(taxi.getPosition().getId())
                    .append(" ");
        }

        return output.toString();
    }

    public void advanceMinute(boolean callsLeft) {
        String output = activeAlgorithm.getAlgorithm().advanceMinute(callsLeft);
        scanner.println(output);
    }

    private void createTaxiList() {
        // Create taxis based on amount read
        for (int i = 0; i < Preamble.amountOfTaxis; i++) {
            sharedData.getTaxiList().add(new Taxi(i));
        }
    }
}
