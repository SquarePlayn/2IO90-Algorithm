import java.util.ArrayList;

public class PreambleReader {

    private int linesLeft = -1;

    private Main main;

    public PreambleReader(Main main) {
        this.main = main;
    }

    public boolean read() {

        TaxiScanner scanner = main.getScanner();

        linesLeft = Integer.parseInt(scanner.nextLine()); // Line 1: preamble length

        main.setAlpha(Integer.parseInt(scanner.nextLine())); // Line 2: variable alpha
        main.setMaxTime(Integer.parseInt(scanner.nextLine())); // Line 3: maximum time

        String taxiLine = scanner.nextLine(); // Line 4: x and c, number of taxis available and max capacity
        String[] taxiLineSplit = taxiLine.split(" ");

        main.setAmountOfTaxis(Integer.parseInt(taxiLineSplit[0])); // process x - amount of taxis

        Taxi.setMaxCapacity(Integer.parseInt(taxiLineSplit[1])); // process c - max taxi capacity

        main.getGraph().addVertices(Integer.parseInt(scanner.nextLine())); // Line 5: number n of nodes in the network

        String testCallMinutesLine = scanner.nextLine(); // Line 6: test minutes t and call minutes t'
        String[] testCallMinutesSplit = testCallMinutesLine.split(" ");

        main.setTestMinutes(Integer.parseInt(testCallMinutesSplit[0]));
        main.setCallMinutes(Integer.parseInt(testCallMinutesSplit[1]));

        return true;

    }

}