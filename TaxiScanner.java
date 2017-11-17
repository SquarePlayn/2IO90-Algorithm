import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Singleton class that manages IO for the Taxi Scheduling project for DBL Algorithms Q2 2016/2017. Enforces proper
 * ordering of IO. That is:
 * 1. First the line is read informing how long the preamble is.
 * 2. Then the preamble is read.
 * After that alternately:
 * 3. Commands are printed. When the last character of a string with commands is 'c', go to step 4.
 * 4. A single line is read. Go to step 3.
 *
 * Deviating from this ordering will result in no action taken, and an exception being thrown.
 *
 * Created by Noud de Kroon on 11/16/2016.
 */
public class TaxiScanner {

    private static TaxiScanner instance = null;
    private State state;
    private int preambleLinesLeft;
    private Scanner scanner;

    private enum State {INITIAL, PREAMBLE, AWAITINGPRINT, AWAITINGNEXTLINE}

    /**
     * Constructor is private to ensure singleton behaviour.
     */
    private TaxiScanner(){
        state = State.INITIAL;
        scanner = new Scanner(System.in);
    }

    /**
     * Retrieves the single instance of this class. Creates the instance on first call.
     * @return Singleton instance of TaxiScanner
     */
    public static TaxiScanner getInstance() {
        if (instance == null) {
            instance = new TaxiScanner();
        }
        return instance;
    }

    /**
     * Check if input has a next line. Note: Does not check if reading the next line is actually correct ordering!
     * @return True if input remaining
     */
    public boolean hasNextLine(){
        return scanner.hasNextLine();
    }

    /**
     * Retrieve next line of input.
     * @return Next line of input
     * @throws IllegalStateException if called when a print is expected by the ordering.
     * @throws NoSuchElementException if !this.hasNextLine()
     * @throws NumberFormatException if first line of input is not an integer
     */
    public String nextLine(){
        if (!this.hasNextLine()) {
            throw new NoSuchElementException("No line remaining on input!");
        }

        switch(state)
        {
            case INITIAL:
                String lineRead = scanner.nextLine();
                try {
                    preambleLinesLeft = Integer.parseInt(lineRead);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("First line should be integer");
                }
                state = State.PREAMBLE;
                return lineRead;
            case PREAMBLE:
                preambleLinesLeft--;
                if (preambleLinesLeft == 0) {
                    state = State.AWAITINGPRINT;
                }
                return scanner.nextLine();
            case AWAITINGPRINT:
                throw new IllegalStateException("nextLine called when print expected");
            case AWAITINGNEXTLINE:
                state = State.AWAITINGPRINT;
                return scanner.nextLine();
            default:
                throw new IllegalStateException("Switch should never default");
        }
    }

    /**
     * Prints line of output (i.e. commands for the taxi's).
     * @param s String to be printed. If last character is 'c', nextLine will become enabled.
     * @throws IllegalStateException if called when readLine is expected by the ordering.
     */
    public void println(String s){
        if (state != State.AWAITINGPRINT) {
            throw new IllegalStateException("Print called while not in AWAITINGPRINT state");
        }

        if (s.charAt(s.length() - 1) == 'c' && hasNextLine()) {
            state = State.AWAITINGNEXTLINE;
        }

        System.out.println(s);
    }
}
