import java.util.ArrayList;
import java.util.HashMap;

public class Algorithm_SimpleQueue extends Algorithm {

    private ArrayList<Customer> customerQueue;
    private ArrayList<Taxi> taxiReadyQueue;
    private ArrayList<Taxi> taxiInOperationList;

    @Override
    public void readMinute(ArrayList<Call> calls) {
        for (Call call: calls) {
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
    public ArrayList<Move> processMinute(boolean callsLeft) {
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
        ArrayList<Move> output = new ArrayList<>();
        for (int i=0; i<taxiInOperationList.size(); i++) {
            Taxi taxi = taxiInOperationList.get(i);
            output.addAll(advanceTaxi(taxi));

            if (!taxi.getInOperation()) {
                // If the taxi is now done delivering its client, we can put it back in the queue
                taxiInOperationList.remove(taxi);
                i--;
                taxiReadyQueue.add(taxi);
            }
        }

        //Make sure the info stays updated before we go back
        processMoves(output);

        return output;
    }

    @Override
    public void haltExecution() {

    }

    @Override
    public boolean doesUpdate(AlgoVar var) {
        return var == AlgoVar.TAXI_CUSTOMER ||
                var == AlgoVar.TAXI_IN_OPERATION ||
                var == AlgoVar.TAXI_PATH;
    }

    public ArrayList<Move> advanceTaxi(Taxi taxi) {
        //Sanitycheck if we are indeed in operation

        ArrayList<Move> output = new ArrayList<>();

        if(taxi.getInOperation()) {
            if (!taxi.getPath().isEmpty()) {

                //We are still driving. Advance to next spot
                output.add(new Move(taxi, taxi.getPath().remove(0)));

            } else {
                if (taxi.getPassengers().isEmpty()) {
                    //We still have to pick up the passenger

                    //TODO Sanitycheck if customer is indeed at the position we are at
                    //TODO Sanitycheck if first node is actually correct

                    output.add(new Move('p', taxi, taxi.getCustomer()));

                } else {
                    //We are done driving, and have already picked up our customer, so that means we are at the destination
                    // so we can drop the customer of

                    output.add(new Move('d',taxi, taxi.getCustomer()));
                }
            }
        }

        return output;

    }

    private void processMoves(ArrayList<Move> moves) {

        //TODO Check if moving the processing to in here doesn't drastically increase run time, since (very) rough testing seemed like about a 10% increase

        for(Move move: moves) {
            char action = move.getAction();
            Taxi taxi = move.getTaxi();

            if(action == 'm') {

                //Moving to another node
                taxi.setPosition(move.getNode());

            } else if(action == 'p') {
                Customer customer = move.getCustomer();

                //Picking up a passenger
                taxi.pickup(customer, sharedData);

                //Since we have somewhere to go, we are in operation
                taxi.setInOperation(true);
                taxi.setPath(sharedData.getGraph().getShortestPath(taxi.getPosition(), customer.getDestination()));

            } else if(action == 'd') {
                Customer customer = move.getCustomer();

                //Dropping off a passenger
                taxi.drop(customer, sharedData);
                taxi.setInOperation(false);
                taxi.setCustomer(null);
            }
        }

    }


    @Override
    public void continueExecution(int uptoMinute, HashMap<AlgoVar, Integer> lastUpdated) {
        //fixme not sure how to check which taxi is going towards a destination yet.

        // TODO use lastUpdated.

        for (int i = lastUpdatedMinute + 1; i < uptoMinute; i++) {
            Minute minute = sharedData.getIOHistory().getMinute(i);
            readMinute(minute.getCalls());
            processMoves(minute.getMoves());
            lastUpdatedMinute++;
        }

        // TODO Make sure amount of passengers is maximum one.
    }
}
