import java.util.ArrayList;

/**
 * Keeps track of entire call list history and can process a call from input
 */
public class IOHistory {

    ArrayList<Minute> minutes;

    public IOHistory() {
        minutes = new ArrayList<>();
    }

    public Minute getMinute(int number) {
        while(number >= minutes.size()) {
            minutes.add(new Minute(minutes.size()));
        }
            return minutes.get(number);
    }

    public void readMinute(String input, int minute, SharedData sharedData) {
        getMinute(minute).setCalls(input, sharedData);
    }

    public void addMinute(Minute minute) {
        minutes.add(minute);
    }
}
