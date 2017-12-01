import java.util.ArrayList;

public class Algorithm_SimpleQueue extends Algorithm {

    
    private ArrayList<Customer> customerQueue;
    private ArrayList<Taxi> taxiReadyQueue;
    private ArrayList<Taxi> taxiInOperationList;


    @Override
    public void readMinute(Minute minute) {
        for (Call call: minute) {
            customerQueue.add(call.getCustomer());
        }
    }

    @Override
    public void setup() {
        //Initialize the queues.
        customerQueue = new ArrayList<>();
        taxiReadyQueue = new ArrayList<>();
        taxiInOperationList = new ArrayList<>();

        taxiReadyQueue.addAll(sharedData.getTaxiList()); // Initially all taxis are unoccupied, so add them all to the ready queue.

    }

    @Override
    public String processMinute(boolean callsLeft) {
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
            taxi.setInOperation(true);
        }

        // Advance all taxis that have an operation
        StringBuilder output = new StringBuilder();
        for (int i=0; i<taxiInOperationList.size(); i++) {
            Taxi taxi = taxiInOperationList.get(i);
            output.append(advanceTaxi(taxi));

            if (!taxi.getInOperation()) {
                // If the taxi is now done delivering its client, we can put it back in the queue
                taxiInOperationList.remove(taxi);
                i--;
                taxiReadyQueue.add(taxi);
            }
        }

        return output.toString();
    }

    @Override
    public void haltExecution() {

    }

    public String advanceTaxi(Taxi taxi) {
        //Sanitycheck if we are indeed in operation

        if(!taxi.getInOperation()) {
            return "";
        }

        if (!taxi.getPath().isEmpty()) {
            //We are still driving. Advance to next spot
            taxi.setPosition(taxi.getPath().get(0));
            taxi.getPath().remove(0);

            return "m " + taxi.getOutputId() + " " + taxi.getPosition().getId() + " ";

        } else {
            if (taxi.getPassengers().isEmpty()) {
                //We still have to pick up the passenger

                //TODO Sanitycheck if customer is indeed at the position we are at

                taxi.getPassengers().add(taxi.getCustomer());

                //Since we have somewhere to go, we are in operation
                taxi.setInOperation(true);
                taxi.setPath(sharedData.getGraph().getShortestPath(taxi.getPosition(), taxi.getCustomer().getDestination()));

                //TODO Sanitycheck if first node is actually correct

                return "p " + taxi.getOutputId() + " " + taxi.getCustomer().getDestination().getId() + " ";

            } else {
                //We are done driving, and have already picked up our customer, so that means we are at the destination
                // so we can drop the customer of
                taxi.getPassengers().remove(taxi.getCustomer());
                taxi.setInOperation(false);

                sharedData.getCustomerList().remove(taxi.getCustomer());
                taxi.setCustomer(null);

                return "d " + taxi.getOutputId() + " " + taxi.getPosition().getId() + " ";

            }
        }
    }


    @Override
    public void continueExecution(int uptoMinute) {
        //fixme not sure how to check which taxi is going towards a destination yet.

    }
}
