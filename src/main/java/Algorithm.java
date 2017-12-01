/**
 * Abstract class framework for algorithm that can produce the output for a minute.
 */
public abstract class Algorithm {
    protected int lastUpdatedMinute = -1;
    protected boolean isInitialized = false;

    protected SharedData sharedData;

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
     * @return Output for this minute.
     * @param callsLeft
     */
    public String advanceMinute(boolean callsLeft, int minuteNumber) {

        //If there was a call list left this minute, read it
        if(callsLeft) {
            readMinute(sharedData.getCallList().getMinute(minuteNumber));
        }

        String output = processMinute(callsLeft);

        lastUpdatedMinute++;

        output += "c";

        return output;
    }

    /**
     * Process the data for the minute that was given.
     * @param minute the minute to be processed. Note the minutes should be processed in ascending order
     */
    public abstract void readMinute(Minute minute);

    /**
     * Abstract function the algorithm will use to setup its dependencies it only needs to create once.
     * Do NOT include trailing 'c', but DO include a space at the end if you made any moves!
     */
    public abstract void setup();

    /**
     * Implemented by each algorithm to specify the output for each minute.
     * @return Output for this minute.
     */
    public abstract String processMinute(boolean callsLeft);

    /**
     * Called by scheduler to signal when this algorithm is put in halt.
     */
    public abstract void haltExecution();

    /**
     * Called by scheduler to signal when this algorithm resumes execution, after this algorithm has missed some
     * minutes.
     */
    public abstract void continueExecution(int upToMinute);

    public boolean isInitialized() {
        return isInitialized;
    }
}
