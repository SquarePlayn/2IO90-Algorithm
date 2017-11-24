import java.util.ArrayList;

public class PreambleReader {

    private int linesLeft = -1;

    private Main main;

    public PreambleReader(Main main) {
        this.main = main;
    }

    public boolean read() {

        Main.debug("Starting preamble read...");

        TaxiScanner scanner = main.getScanner();

        linesLeft = Integer.parseInt(scanner.nextLine()); // Line 1: preamble length

        main.setAlpha(Double.parseDouble(scanner.nextLine())); // Line 2: variable alpha
        main.setMaxTime(Integer.parseInt(scanner.nextLine())); // Line 3: maximum time

        String taxiLine = scanner.nextLine(); // Line 4: x and c, number of taxis available and max capacity
        String[] taxiLineSplit = taxiLine.split(" ");

        main.setAmountOfTaxis(Integer.parseInt(taxiLineSplit[0])); // process x - amount of taxis

        Taxi.setMaxCapacity(Integer.parseInt(taxiLineSplit[1])); // process c - max taxi capacity

        main.getGraph().addVertices(Integer.parseInt(scanner.nextLine())); // Line 5: number n of nodes in the network

        Main.debug("Start reading "+ main.getGraph().getSize()+" lines of node information");
        for (int i = 0; i < main.getGraph().getSize(); i++) { // Line 6 + i

            String graphLine = scanner.nextLine(); // Read a line of the graph information
            String[] graphLineSplit = graphLine.split(" ");

            int neighbours = Integer.parseInt(graphLineSplit[0]);

            for (int k = 1; k < neighbours + 1; k++) {

                int neighbour = Integer.parseInt(graphLineSplit[k]);

                main.getGraph().getVertex(i).addNeigbour(main.getGraph().getVertex(neighbour));

            }

        }

        Main.debug("Finished reading all node information");

        String testCallMinutesLine = scanner.nextLine(); // Line 6 + n: test minutes t and call minutes t'

        Main.debug("Finished reading the 'minutes' line");

        String[] testCallMinutesSplit = testCallMinutesLine.split(" ");

        main.setTestMinutes(Integer.parseInt(testCallMinutesSplit[0]));
        main.setCallMinutes(Integer.parseInt(testCallMinutesSplit[1]));

        Main.debug("Completed preamble read");

        return true;

    }

}