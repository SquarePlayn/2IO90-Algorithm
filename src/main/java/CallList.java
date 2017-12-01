import java.util.ArrayList;

/**
 * Keeps track of entire call list history and can process a call from input
 */
public class CallList {

    ArrayList<Minute> minutes = new ArrayList<>();

    public Minute getMinute(int number) {
        return minutes.get(number);
    }

    public void readMinute(String input) {
        //TODO Read input make minute
    }

    public void addMinute(Minute minute) {
        minutes.add(minute);
    }
}
