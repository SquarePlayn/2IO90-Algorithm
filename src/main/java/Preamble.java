public class Preamble {

    public static double alpha; // Alpha for the cost function
    public static int maxTime; // Maximum time between ordering a taxi and being dropped off
    public static int amountOfTaxis; //The amount of taxis allowed to be existent in our graph
    public static int testMinutes; // The amount of minutes the test call list will take
    public static int callMinutes; // the amount of minutes in the total call list (including the testMinutes)
    public static int graphSize; // The amount of nodes in the graph

    public static Graph graph;

    private TaxiScanner scanner;

    private int linesLeft = -1;

    public Preamble(TaxiScanner scanner) {
        this.scanner = scanner;
        this.graph = new Graph();

    }

    public boolean read() {

        Main.debug("Starting preamble read...");

        linesLeft = Integer.parseInt(scanner.nextLine()); // Line 1: preamble length

        alpha = Double.parseDouble(scanner.nextLine()); // Line 2: variable alpha
        maxTime = Integer.parseInt(scanner.nextLine()); // Line 3: maximum time

        String taxiLine = scanner.nextLine(); // Line 4: x and c, number of taxis available and max capacity
        String[] taxiLineSplit = taxiLine.split(" ");
        amountOfTaxis = Integer.parseInt(taxiLineSplit[0]); // process x - amount of taxis
        Taxi.setMaxCapacity(Integer.parseInt(taxiLineSplit[1])); // process c - max taxi capacity

        graphSize = Integer.parseInt(scanner.nextLine()); // Line 5: number n of nodes in the network
        graph.addVertices(graphSize);

        createEdges(); //Read all edge lines and put them into the graph

        String testCallMinutesLine = scanner.nextLine(); // Line 6 + n: test minutes t and call minutes t'

        Main.debug("Finished reading the 'minutes' line");

        String[] testCallMinutesSplit = testCallMinutesLine.split(" ");

        testMinutes = Integer.parseInt(testCallMinutesSplit[0]);
        callMinutes = Integer.parseInt(testCallMinutesSplit[1]);

        Main.debug("Completed preamble read");

        return true;

    }

    /**
     * Reads all the lines of the edges and builds the graph with it accordingly
     */
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
        Main.debug("Finished reading all node information");
    }

    public static String getInformationString() {
        return "A:" + alpha
                + " Taxis:" + amountOfTaxis
                + " CallMinutes:" + callMinutes
                + " GraphSize:" + graphSize
                + " maxTime:" + maxTime
                + " testMinutes:" + testMinutes;
    }

}