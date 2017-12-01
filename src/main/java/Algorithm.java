/**
 * Abstract class framework for algorithm that can produce the output for a minute.
 */
public abstract class Algorithm {
    private int lastUpdatedMinute = -1;
    private boolean isInitialized = false;

    /**
     * Called by decision maker. Does general initialization of an algorithm and calls the {@link Algorithm#setup}
     * method. Should only be called once in the entire execution of the entire program.
     */
    public void initialize() {
        if (isInitialized) {
            return;
        }

        isInitialized = true;

        setup();
    }

    /**
     * Called by main minute loop / decision maker, to advance one minute.
     * @return Output for this minute.
     */
    public String advanceMinute() {
        String output = processMinute();

        lastUpdatedMinute++;

        return output;
    }

    /**
     * Abstract function the algorithm will use to setup its dependencies it only needs to create once.
     */
    public abstract void setup();

    /**
     * Implemented by each algorithm to specify the output for each minute.
     * @return Output for this minute.
     */
    public abstract String processMinute();

    /**
     * Called by decision maker to signal when this algorithm is put in halt.
     */
    public abstract void haltExecution();

    /**
     * Called by decision maker to signal when this algorithm resumes execution, after this algorithm has missed some
     * minutes.
     */
    public abstract void continueExecution();

    public boolean isInitialized() {
        return isInitialized;
    }
}
