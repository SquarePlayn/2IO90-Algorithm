public class Scheduler {

    //ENUMS of the different algorithms

    private TaxiScanner scanner;
    private SharedData sharedData;

    private AlgorithmType activeAlgorithm;

    private int currentMinute;

    public Scheduler(TaxiScanner scanner) {
        this.scanner = scanner;
        this.sharedData = new SharedData(Preamble.graph);
        currentMinute = 0;
    }

    /**
     * Entry point. Runs the whole program over all callList minutes untill all customers delivered
     */
    public void run() {
        createTaxiList();
        testMinutes();
        realMinutes();
    }

    /**
     * Runs the program over the test minutes
     */
    public void testMinutes() {
        //Read over all test case info without delivering people
        initializeTaxis(); //TODO Might consider doing a simpler taxi initializer such as always random at this position
        for (int i = 1; i < Preamble.testMinutes; i++) {
            Main.debug("Starting minute "+i);
            scanner.nextLine();
            scanner.println("c");
        }
        scanner.nextLine();
    }

    /**
     * Runs all the real minutes. This includes the reading of input, deciding which algo to use, make sure to continue
     * until all customers have been delivered
     */
    public void realMinutes() {
        //From here on, the Actual calling and being called of taxis starts
        initializeTaxis();

        //TODO Make sure to redecide which algorithm to use at certain times
        //TODO Make sure in reschedule that algo initialised and up to date
        reschedule();

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

    private void reschedule() {
        //TODO Add something better
        activeAlgorithm = AlgorithmType.SIMPLEQUEUE;

        if(!activeAlgorithm.getAlgorithm().isInitialized()){
            activeAlgorithm.getAlgorithm().initialize(sharedData);
        }

    }

    /**
     * Reads one line of input and correctly stores it in the SharedData function
     */
    private void readInput() {
        if(!scanner.hasNextLine()) {
            Main.debug("Scheduler tried to read line while there is none.");
            return;
        }

        String[] input = scanner.nextLine().split(" ");
        Minute minute = new Minute();

        //Check each call
        int amountOfCalls = Integer.parseInt(input[0]);
        for (int i = 0; i < amountOfCalls; i++) {
            //Read in each new customer
            Vertex position = sharedData.getGraph().getVertex(Integer.parseInt(input[i * 2 + 1]));
            Vertex destination = sharedData.getGraph().getVertex(Integer.parseInt(input[i * 2 + 2]));
            Customer customer = new Customer(position, destination);

            //Add the new customer to the customers
            sharedData.getCustomerList().add(customer);

            //Add the new call to the current minute we're constructing
            minute.add(new Call(customer, position, destination));
        }

        //Add the minute to the minutes
        sharedData.getCallList().addMinute(minute);
    }

    /**
     * Places each taxi on a starting position
     * @return Output string that will set each taxi on a starting position.
     */
    public void initializeTaxis() {
        //TODO Handle different taxi initialize options like we handle different advance minute options
        Main.debug("Starting initialize taxis");

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
        Main.debug("Using the random() type of taxi distribution");
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

    /**
     * Use the active algorithm to get the moves we want to make this turn.
     * @param callsLeft if there is still a call that has to be processed (if false we thus won't get any new customers anymore)
     */
    public void advanceMinute(boolean callsLeft) {
        String output = activeAlgorithm.getAlgorithm().advanceMinute(callsLeft, currentMinute);
        scanner.println(output);

        //We have advanced a minute, go to next
        currentMinute++;
    }

    /**
     * Creates/fills the taxiList variable with the right amount of taxis
     */
    private void createTaxiList() {
        // Create taxis based on amount read
        for (int i = 0; i < Preamble.amountOfTaxis; i++) {
            sharedData.getTaxiList().add(new Taxi(i));
        }
    }
}
