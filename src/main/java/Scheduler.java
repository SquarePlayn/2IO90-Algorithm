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

    //TODO: pls delete
    public static long time_kcenters;

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

        /**public static long time_taxi_has_arrived = 0;
         * public static long time_match_entities = 0;
         * public static long time_advance_taxis = 0;
         * public static long time_process_moves = 0;
         */

        // output
        double process_minute = (double) (Algorithm_Hungarian.time_process_minute / 1000000000.0);
        double match_entities = (double) (Algorithm_Hungarian.time_match_entities / 1000000000.0);
        double new_ha_class_instance = (double) (Algorithm_Hungarian.time_new_ha_class_instance / 1000000000.0);
        double apply_hungarian = (double) (Algorithm_Hungarian.time_apply_hungarian / 1000000000.0);
        double execute_hungarian = (double) (Algorithm_Hungarian.time_execute_hungarian / 1000000000.0);
        double reduce = (double) (Algorithm_Hungarian.time_reduce / 1000000000.0);
        double compute_initial = (double) (Algorithm_Hungarian.time_compute_initial / 1000000000.0);
        double greedy_match = (double) (Algorithm_Hungarian.time_greedy_match / 1000000000.0);
        double rest_of_loop = (double) (Algorithm_Hungarian.time_rest_of_loop / 1000000000.0);
        double kcenters = (double) (time_kcenters / 1000000000.0);


        System.out.println(process_minute);
        System.out.println(match_entities);
        System.out.println(apply_hungarian);
        System.out.println("\n");
        System.out.println(new_ha_class_instance);
        System.out.println(execute_hungarian);
        System.out.println("\n");
        System.out.println(reduce);
        System.out.println(compute_initial);
        System.out.println(greedy_match);
        System.out.println(rest_of_loop);

        System.out.println("Find K-centers:" + kcenters + "s");
        ArrayList<Vertex> kCentersVertices = new ArrayList<>();
        kCentersVertices = sharedData.getGraph().getkCenters();
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
        initializeTaxis();

        sharedData.getGraph().buildHubs(sharedData.getRandom());

        long start = System.nanoTime();
        sharedData.getGraph().findKCenters();
        time_kcenters += System.nanoTime() - start;

        //TODO Make sure to redecide which algorithm to use at certain times
        //TODO Make sure in reschedule that algo initialised and up to date
        reschedule();

        //While there are lines to read, read them and advance to next minute
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
