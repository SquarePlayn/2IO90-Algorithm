import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract class framework for algorithm that can produce the output for a minute.
 */
public abstract class Algorithm {
    protected int lastUpdatedMinute;
    protected boolean isInitialized;

    protected SharedData sharedData;

    public Algorithm() {
        this.lastUpdatedMinute = -1;
        this.isInitialized = false;
    }

    /**
     * Called by Scheduler. Does general initialization of an algorithm and calls the {@link Algorithm#setup}
     * method. Should only be called once in the entire execution of the entire program.
     */
    public void initialize(SharedData sharedData) {
        if (isInitialized) {
            return;
        }

        this.sharedData = sharedData;
        this.isInitialized = true;

        setup();
    }

    /**
     * Called by main minute loop / Scheduler, to advance one minute.
     * initialize shall have been called before this (but only once in the entire execution of the program)
     * @return Output for this minute.
     * @param callsLeft states if there are calls left to be read (false = when we deliver all current customers, we're done)
     */
    public ArrayList<Move> advanceMinute(boolean callsLeft, int minuteNumber) {

        //If there was a call list left this minute, read it
        if(callsLeft) {
            readMinute(sharedData.getIOHistory().getMinute(minuteNumber).getCalls());
        }

        ArrayList<Move> output = processMinute(callsLeft);

        lastUpdatedMinute++;

        return output;
    }

    /**
     * Process the data for the minute that was given.
     * @param calls The arrayList of calls to be processed/read
     */
    public abstract void readMinute(ArrayList<Call> calls);

    /**
     * Abstract function the algorithm will use to setup its dependencies it only needs to create once.
     */
    public abstract void setup();

    /**
     * Implemented by each algorithm to specify the output for each minute.
     * @return Output for this minute.
     */
    public abstract ArrayList<Move> processMinute(boolean callsLeft);

    /**
     * Called by scheduler to signal when this algorithm is put in halt.
     */
    public abstract void haltExecution();

    /**
     * Should return true for all AlgoVars that are updated by this algorithm.
     * @param var
     * @return
     */
    public abstract boolean doesUpdate(AlgoVar var);

    /**
     * Called by scheduler to signal when this algorithm resumes execution, after this algorithm has missed some
     * minutes.
     */
    public abstract void continueExecution(int upToMinute, HashMap<AlgoVar, Integer> lastUpdated);

    public boolean isInitialized() {
        return isInitialized;
    }

    public abstract void upscale(int i);

    public enum AlgoVar {
        TAXI_CUSTOMER, TAXI_IN_OPERATION, TAXI_PATH;
    }
}
