public class Preamble {

    public static double alpha; // Alpha for the cost function
    public static int maxTime; // Maximum time between ordering a taxi and being dropped off
    public static int amountOfTaxis;
    public static int testMinutes;
    public static int callMinutes;

    public static Graph graph;

    private TaxiScanner scanner;


    private int linesLeft = -1;

    public Preamble(TaxiScanner scanner) {
        this.scanner = scanner;
        graph = new Graph();

    }

    public boolean read() {

        Main.debug("Starting preamble read...");

        //TODO Fix from main setters to set in local class

        linesLeft = Integer.parseInt(scanner.nextLine()); // Line 1: preamble length

        alpha = Double.parseDouble(scanner.nextLine()); // Line 2: variable alpha
        maxTime = Integer.parseInt(scanner.nextLine()); // Line 3: maximum time

        String taxiLine = scanner.nextLine(); // Line 4: x and c, number of taxis available and max capacity
        String[] taxiLineSplit = taxiLine.split(" ");

        amountOfTaxis = Integer.parseInt(taxiLineSplit[0]); // process x - amount of taxis

        Taxi.setMaxCapacity(Integer.parseInt(taxiLineSplit[1])); // process c - max taxi capacity

        graph.addVertices(Integer.parseInt(scanner.nextLine())); // Line 5: number n of nodes in the network

        createEdges(); //Read all edge lines and put them into the graph

        Main.debug("Finished reading all node information");

        String testCallMinutesLine = scanner.nextLine(); // Line 6 + n: test minutes t and call minutes t'

        Main.debug("Finished reading the 'minutes' line");

        String[] testCallMinutesSplit = testCallMinutesLine.split(" ");

        testMinutes = Integer.parseInt(testCallMinutesSplit[0]);
        callMinutes = Integer.parseInt(testCallMinutesSplit[1]);

        Main.debug("Completed preamble read");

        return true;

    }

    private void createEdges() {
        Main.debug("Start reading "+ graph.getSize()+" lines of node information");
        for (int i = 0; i < graph.getSize(); i++) { // Line 6 + i

            String graphLine = scanner.nextLine(); // Read a line of the graph information
            String[] graphLineSplit = graphLine.split(" ");

            int neighbours = Integer.parseInt(graphLineSplit[0]);

            for (int k = 1; k < neighbours + 1; k++) {

                int neighbour = Integer.parseInt(graphLineSplit[k]);

                graph.getVertex(i).addNeigbour(graph.getVertex(neighbour));

            }

        }
    }

}