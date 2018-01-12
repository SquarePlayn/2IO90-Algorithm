import com.sun.org.apache.xpath.internal.SourceTree;

import java.lang.reflect.Array;
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

        //output the found cluster origins (for testing purposes)
        /*ArrayList<Integer> kCentersIds = new ArrayList<>();
        for(int i=0; i<sharedData.getGraph().getClusterOrigins().size(); i++) {
            kCentersIds.add(sharedData.getGraph().getClusterOrigins().get(i).getId());
        }
        System.out.println("Origins: " + kCentersIds.toString());*/

        //output the found cluster centers (for testing purposes)
        /*ArrayList<Integer> clusterCenterIds = new ArrayList<>();
        for(int i=0; i<sharedData.getGraph().getKCenters().size(); i++) {
            clusterCenterIds.add(sharedData.getGraph().getKCenters().get(i).getId());
        }
        System.out.println("K-centers: " + clusterCenterIds.toString());*/

        double time_findCenters = ((double) Graph.findCenters)/1000000000;
        double time_findClusters = ((double) Graph.findClusters)/1000000000;
        double time_kCenters = time_findCenters+time_findClusters;

        System.out.println("Find centers: " + time_findCenters + "s");
        System.out.println("Find clusters: " + time_findClusters + "s");
        System.out.println("Total time: " + time_kCenters + "s");
        System.out.println("Amount of origins: " + sharedData.getGraph().getClusterOrigins().size());
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

        sharedData.getGraph().findKCenters();

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
