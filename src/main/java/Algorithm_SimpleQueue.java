import java.util.ArrayList;

public class Algorithm_SimpleQueue extends Algorithm {

    
    private ArrayList<Customer> customerQueue;
    private ArrayList<Taxi> taxiReadyQueue;
    private ArrayList<Taxi> taxiInOperationList;


    @Override
    public void setup() {
        //Initialize the queues.
        customerQueue = new ArrayList<>();
        taxiReadyQueue = new ArrayList<>();
        taxiInOperationList = new ArrayList<>();

        taxiReadyQueue.addAll(sharedData.getTaxiList()); // Initially all taxis are unoccupied, so add them all to the ready queue.

    }

    @Override
    public String processMinute() {
        // First assign a taxi to each waiting customer as far as possible
        // Loop until there are no customers waiting anymore.
        // If there are no more ready taxis, the remaining customers will have to wait
        while (!customerQueue.isEmpty() && !taxiReadyQueue.isEmpty()) {
            // Get the first-up taxi, pop it from the queue and add it to the ones in operation
            Taxi taxi = taxiReadyQueue.remove(0);
            taxiInOperationList.add(taxi);

            // Pop the customer that is first-up
            Customer customer = customerQueue.remove(0);

            //Assign the taxi to the customer and make the taxi go towards the customer
            taxi.setCustomer(customer);
            taxi.setPath(sharedData.getGraph().getShortestPath(taxi.getPosition(), customer.getPosition()));
        }

        // Advance all taxis that have an operation
        String output = "";
        for (int i=0; i<taxiInOperationList.size(); i++) {
            Taxi taxi = taxiInOperationList.get(i);
            output += taxi.continueOperation(sharedData.getGraph());

            if (!taxi.getInOperation()) {
                // If the taxi is now done delivering its client, we can put it back in the queue
                taxiInOperationList.remove(taxi);
                i--;
                taxiReadyQueue.add(taxi);
            }
        }

        return output;
    }

    /**
     * Reads one line of input and processes it
     */
    private void readInput() {
        //TODO Adapt for getting call list

        /*if (scanner.hasNextLine()) {
            String[] input = scanner.nextLine().split(" ");

            int amountOfCalls = Integer.parseInt(input[0]);
            for (int i = 0; i < amountOfCalls; i++) {
                //Read in each new customer and add the customer to the waiting queue
                Vertex position = graph.getVertex(Integer.parseInt(input[i * 2 + 1]));
                Vertex destination = graph.getVertex(Integer.parseInt(input[i * 2 + 2]));
                customerQueue.add(new Customer(position, destination));
            }
        }*/
    }

    @Override
    public void haltExecution() {

    }

    @Override
    public void continueExecution() {

    }
}
