import java.util.ArrayList;

/**
 * Keeps track of entire call list history and can process a call from input
 */
public class CallList {

    ArrayList<Minute> callList = new ArrayList<>();

    public Minute getMinute(int number) {
        return callList.get(number);
    }

    public void readMinute(String input) {
        //TODO Read input make minute
    }
}
