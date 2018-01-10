import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Scheduler {
    private static final int SCHEDULE_CUTOFF = 999;
    private static final int HUNGARIAN_MINSIZE = 100;
    private static final int HUBS_CUTOFF = 3000;

    private TaxiScanner scanner;
    private SharedData sharedData;

    private HashMap<Algorithm.AlgoVar, Integer> lastUpdatedVariables;// Keeps track of when a variable was last updated.

    private AlgorithmType activeAlgorithm;

    private int testCalls = 0;

    private int currentMinute;

    private long startTime;
    private boolean halfTimeReschedule = false;

    private float custFrequencyDensityRatio = -1;

    public Scheduler(TaxiScanner scanner) {
        this.scanner = scanner;
        this.sharedData = new SharedData(Preamble.graph);
        currentMinute = 0;
        startTime = System.nanoTime();
    }

    /**
     * Entry point. Runs the whole program over all callList minutes untill all customers delivered
     */
    public void run() {
        createTaxiList();
      
        if (Preamble.testMinutes > 0) {
            testMinutes();
        }
      
        if (Preamble.callMinutes - Preamble.testMinutes > 0) {
            realMinutes();
        }

    }

    /**
     * Runs the program over the test minutes
     */
    public void testMinutes() {
        //Read over all test case info without delivering people
        initializeTaxis(); //TODO Might consider doing a simpler taxi initializer such as always random at this position
        for (int i = 1; i < Preamble.testMinutes; i++) {
            Main.debug("Starting testMinute "+i);
            String input = scanner.nextLine();
            testCalls += Integer.parseInt(input.split(" ")[0]);

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

        startSchedule();

        //While there are lines to read, read them and advance to next minute
        while (scanner.hasNextLine()) {
            checkRescheduleTime();
            readInput();
            advanceMinute(true);
            outputMinute(currentMinute);
        }

        Main.debug("No more call minutes to be read, time to complete delivering everyone");

        //reschedule(RescheduleType.END_OF_CALL_LIST);

        //Since there are no more lines to read, advance until all customers are delivered
        while (!sharedData.getCustomerList().isEmpty()) {
            checkRescheduleTime();
            advanceMinute(false);
            outputMinute(currentMinute);
        }
    }

    /**
     * Check the execution time of the algorithm to know when to reschedule
     */
    private void checkRescheduleTime() {

        if (halfTimeReschedule) {
            return;
        }

        long difTime = System.nanoTime() - startTime;

        // DiffTIme > 25s
        if (difTime > 25000000000L) {

            // Comment line below out if you want to reschedule at 25s
            reschedule(RescheduleType.FIVE_SEC_LEFT);
            halfTimeReschedule = true;

        }

    }

    /**
     * Determines which algorithm to start with
     */
    private void startSchedule() {


        //if(sharedData.getGraph().getSize() > SCHEDULE_CUTOFF) {
        int expectedCalls = sharedData.getGraph().getSize(); // If no testminutes, go off of graph size
        if(Preamble.testMinutes > 1) {
            expectedCalls = testCalls * (Preamble.callMinutes - Preamble.testMinutes) / Preamble.testMinutes;
        }
        if(expectedCalls > SCHEDULE_CUTOFF) {
            if(sharedData.getGraph().getSize() > HUBS_CUTOFF) {
                activeAlgorithm = AlgorithmType.HUBS;
            } else {
                activeAlgorithm = AlgorithmType.SIMPLEQUEUE;
            }
        } else {
            if (Taxi.MAX_CAPACITY <= 1 && sharedData.getGraph().getSize() > HUNGARIAN_MINSIZE) {
                activeAlgorithm = AlgorithmType.HUNGARIAN;
            } else {
                activeAlgorithm = AlgorithmType.LSD;
            }
        }

        activeAlgorithm.getAlgorithm().initialize(sharedData);

        //System.out.println("Active:" + activeAlgorithm.toString());

    }

    /**
     * Determines whether a new algorithm should be scheduled
     */
    private void reschedule(RescheduleType rescheduleType) {

        switch (rescheduleType) {

            case END_OF_CALL_LIST:

                //TODO implement something reasonable to calculate if need to reschedule

                return;

            case FIVE_SEC_LEFT:
            case HALF_TIME:

                // Calculate time difference since start
                long difTime = System.nanoTime() - startTime;

                // Calculate amount of customers already delivered
                float customersDelivered = sharedData.getCustomerCallAmount() - sharedData.getCustomerList().size();

                // Calculate amount of time it took to deliver those
                float timeToDeliver = difTime / customersDelivered;

                // Calculate time needed to finish running with current pace
                float timeNeededToFinish = timeToDeliver * sharedData.getCustomerList().size();

                // 30s - difTime < timeNeededToFinish
                if (30000000000L - difTime < timeNeededToFinish) {

                    if (activeAlgorithm != AlgorithmType.HUBS) {

                        // Switching to SimpleQueue
                        //System.out.println("test");
                        activeAlgorithm = AlgorithmType.HUBS;

                    } else {
                        return;
                    }

                }

                break;

            default:
                break;

        }

        // TODO on switching algo make sure to update lastUpdatedVariables.

        if(!activeAlgorithm.getAlgorithm().isInitialized()){
            activeAlgorithm.getAlgorithm().initialize(sharedData);
        }

        activeAlgorithm.getAlgorithm().continueExecution(currentMinute, lastUpdatedVariables);

    }

    /**
     * Reads one line of input and correctly stores it in the SharedData function
     */
    private void readInput() {
        if(!scanner.hasNextLine()) {
            Main.debug("Scheduler tried to read line while there is none.");
            return;
        }

        String input = scanner.nextLine();
        Main.debug("[IN:"+currentMinute+"] "+input);
        sharedData.getIOHistory().readMinute(input, currentMinute, sharedData);
    }

    /**
     * Places each taxi on a starting position
     * @return Output string that will set each taxi on a starting position.
     */
    public void initializeTaxis() {
        //TODO Handle different taxi initialize options like we handle different advance minute options
        //TODO Set to use IOHistory
        Main.debug("Starting initialize taxis");

        String output;

        if(Preamble.amountOfTaxis > sharedData.getGraph().getSize()/2) {
            output = initializeTaxis_random();
        } else {
            output = initializeTaxis_hubs();
        }

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
        Random random = sharedData.getRandom();

        for (Taxi taxi : sharedData.getTaxiList()) {
            taxi.setPosition(sharedData.getGraph().getVertex(random.nextInt(sharedData.getGraph().getSize())));
            output.append("m ")
                    .append(taxi.getOutputId())
                    .append(" ")
                    .append(taxi.getPosition().getId())
                    .append(" ");
        }

        return output.toString();
    }

    private String initializeTaxis_hubs() {
        Main.debug("Using the hubs() type of taxi distribution");
        sharedData.getGraph().buildHubs(sharedData.getRandom());

        StringBuilder output = new StringBuilder();
        for(Taxi taxi : sharedData.getTaxiList()) {
            taxi.setPosition(sharedData.getGraph().getHub(
                    taxi.getId() % sharedData.getGraph().getHubs().size()));
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
        ArrayList<Move> moves = activeAlgorithm.getAlgorithm().advanceMinute(callsLeft, currentMinute);
        sharedData.getIOHistory().getMinute(currentMinute).setMoves(moves);
    }

    /**
     * Prints the output of the given minute to the scanner.
     * @param minute The minute to output.
     */
    public void outputMinute(int minute) {
        StringBuilder output = new StringBuilder();

        for(Move move: sharedData.getIOHistory().getMinute(minute).getMoves()) {
            output.append(move.getString());

        }

        output.append("c");
        scanner.println(output.toString());

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

    private enum RescheduleType {

        END_OF_CALL_LIST,
        HALF_TIME,
        FIVE_SEC_LEFT;

    }

}
