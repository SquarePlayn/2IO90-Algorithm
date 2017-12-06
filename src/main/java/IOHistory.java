import java.util.ArrayList;

/**
 * Keeps track of entire call list history and can process a call from input
 */
public class IOHistory {

    ArrayList<Minute> minutes = new ArrayList<>();

    public Minute getMinute(int number) {
        while(number >= minutes.size()) {
            minutes.add(new Minute());
        }
            return minutes.get(number);
    }

    public void readMinute(String input, int minute, SharedData sharedData) {
        getMinute(minute).setCalls(input, sharedData);
        //TODO Read input make minute
    }

    public void addMinute(Minute minute) {
        minutes.add(minute);
    }
}
