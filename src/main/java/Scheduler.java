import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Scheduler {
    private static final int SHEDULE_CUTOFF = 1024;

    private TaxiScanner scanner;
    private SharedData sharedData;

    private HashMap<Algorithm.AlgoVar, Integer> lastUpdatedVariables;// Keeps track of when a variable was last updated.

    private AlgorithmType activeAlgorithm;

    private int currentMinute;

    public static long time_total;
    public static long time_kcenters;
    public static long time_initialize_taxis;
    public static long time_build_hubs;
    public static long time_reschedule;
    public static long time_do_things;

    public Scheduler(TaxiScanner scanner) {
        this.scanner = scanner;
        this.sharedData = new SharedData(Preamble.graph);
        currentMinute = 0;
    }

    /**
     * Entry point. Runs the whole program over all callList minutes untill all customers delivered
     */
    public void run() {
        long start5 = System.nanoTime();
        createTaxiList();
        testMinutes();
        realMinutes();
        time_total += System.nanoTime() - start5;

        /**public static long time_taxi_has_arrived = 0;
         * public static long time_match_entities = 0;
         * public static long time_advance_taxis = 0;
         * public static long time_process_moves = 0;
         */

        // output
        double total = (double) (time_total / 1000000000.0);
        double initialize_taxis = (double) (time_initialize_taxis / 1000000000.0);
        double build_hubs = (double) (time_build_hubs / 1000000000.0);
        double kcenters = (double) (time_kcenters / 1000000000.0);
        double reschedule = (double) (time_reschedule / 1000000000.0);
        double do_things = (double) (time_do_things / 1000000000.0);

        System.out.println("Total time:" + total + "s");
        System.out.println("  Initialize taxis:" + initialize_taxis + "s");
        System.out.println("  Build hubs:      " + build_hubs + "s");
        System.out.println("  Find K-centers:  " + kcenters + "s");
        System.out.println("  Reschedule:      " + reschedule + "s");
        System.out.println("  Do things:       " + do_things + "s");

        ArrayList<Vertex> kCentersVertices = new ArrayList<>();
        kCentersVertices = sharedData.getGraph().getKCenters();
        ArrayList<Integer> kCentersIds = new ArrayList<>();
        for(int i=0; i<kCentersVertices.size(); i++) {
            kCentersIds.add(kCentersVertices.get(i).getId());
        }
        System.out.println(kCentersIds.toString());
    }

    /**
     * Runs the program over the test minutes
     */
    public void testMinutes() {
        //Read over all test case info without delivering people
        initializeTaxis(); //TODO Might consider doing a simpler taxi initializer such as always random at this position
        for (int i = 1; i < Preamble.testMinutes; i++) {
            Main.debug("Starting testMinute "+i);
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
        long start1 = System.nanoTime();
        initializeTaxis();
        time_initialize_taxis += System.nanoTime() - start1;

        long start2 = System.nanoTime();
        sharedData.getGraph().buildHubs(sharedData.getRandom());
        time_build_hubs += System.nanoTime() - start2;

        long start = System.nanoTime();
        sharedData.getGraph().findKCenters();
        time_kcenters += System.nanoTime() - start;

        //TODO Make sure to redecide which algorithm to use at certain times
        //TODO Make sure in reschedule that algo initialised and up to date
        long start3 = System.nanoTime();
        reschedule();
        time_reschedule += System.nanoTime() - start3;

        //While there are lines to read, read them and advance to next minute
        long start4 = System.nanoTime();
        while (scanner.hasNextLine()) {
            readInput();
            advanceMinute(true);
            outputMinute(currentMinute);
        }

        Main.debug("No more call minutes to be read, time to complete delivering everyone");

        //Since there are no more lines to read, advance until all customers are delivered
        while (!sharedData.getCustomerList().isEmpty()) {
            advanceMinute(false);
            outputMinute(currentMinute);
        }
        time_do_things += System.nanoTime() - start4;
    }

    /**
     * Determines whether a new algorithm should be scheduled.
     */
    private void reschedule() {
        //TODO Add something better

        if(sharedData.getGraph().getSize() > SHEDULE_CUTOFF) {
            activeAlgorithm = AlgorithmType.SIMPLEQUEUE;
        } else {
            if (Taxi.MAX_CAPACITY <= 2) {
                activeAlgorithm = AlgorithmType.HUNGARIAN;
            } else {
                activeAlgorithm = AlgorithmType.LSD;
            }
        }

        activeAlgorithm = AlgorithmType.HUNGARIAN;

        if(!activeAlgorithm.getAlgorithm().isInitialized()){
            activeAlgorithm.getAlgorithm().initialize(sharedData);
        }

        // TODO on switching algo make sure to update lastUpdatedVariables.
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
}
