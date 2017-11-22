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

        int taxiAmount = Integer.parseInt(taxiLineSplit[0]);

        ArrayList<Taxi> taxiList = main.getTaxiList();

        for (int i = 0; i < taxiAmount; i++) {
            taxiList.add(new Taxi()); // Create taxis based on amount read
        }

        Taxi.setMaxCapacity(Integer.parseInt(taxiLineSplit[1]));

        main.getGraph().addVertices(Integer.parseInt(scanner.nextLine())); // Line 5: number n of nodes in the network

        return true;

    }

}