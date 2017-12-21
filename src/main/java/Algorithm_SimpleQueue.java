import java.util.*;

public class Algorithm_SimpleQueue extends Algorithm {

    private ArrayList<Customer> customerQueue;
    private ArrayList<Taxi> taxiReadyQueue;
    private HashSet<Taxi> taxiInOperationList;

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
        taxiInOperationList = new HashSet<>();

        taxiReadyQueue.addAll(sharedData.getTaxiList()); // Initially all taxis are unoccupied, so add them all to the ready queue.

    }

    @Override
    public ArrayList<Move> processMinute(boolean callsLeft) {
        ArrayList<Move> output = new ArrayList<>();

        for(Taxi taxi : taxiInOperationList) {
            if (taxi.getInOperation()
                    && taxi.getPosition().equals(taxi.getCustomer().getPosition())
                    && !taxi.getCustomer().hasBeenPickedUp()
                    && taxi.getPassengers().isEmpty()) {

                taxiReadyQueue.remove(taxi);
                customerQueue.remove(taxi.getCustomer());
                taxi.getPath().clear();
            }
        }

        if (!customerQueue.isEmpty() && !taxiReadyQueue.isEmpty()) {
            HashMap<Taxi, Customer> hungOut = applyHungarian();

            for (Map.Entry<Taxi, Customer> entry : hungOut.entrySet()) {
                Taxi taxi = entry.getKey();
                Customer customer = entry.getValue();

                taxiInOperationList.add(taxi);

                taxi.setCustomer(customer);
                taxi.setPath(sharedData.getGraph().getShortestPath(taxi.getPosition(), customer.getPosition()));
                taxi.setInOperation(true);
            }
        }

        // Advance all taxis that have an operation
        Iterator<Taxi> it = taxiInOperationList.iterator();
        while (it.hasNext()) {
            Taxi taxi = it.next();

            if (!taxi.getInOperation()) {
                it.remove();
                // TODO see if we can find another way.
                if(!taxiReadyQueue.contains(taxi)) {
                    taxiReadyQueue.add(taxi);
                }
                continue;
            }

            output.addAll(advanceTaxi(taxi));
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

    private HashMap<Taxi, Customer> applyHungarian() {
        HashMap<Taxi, Customer> output = new HashMap<>(taxiReadyQueue.size());
        double[][] costMatrix = new double[taxiReadyQueue.size()][customerQueue.size()];

        for(int t = 0; t < taxiReadyQueue.size(); t++) {
            for(int c = 0; c < customerQueue.size(); c++) {
                Taxi taxi = taxiReadyQueue.get(t);
                Customer customer = customerQueue.get(c);

                costMatrix[t][c] = taxi.getPosition().getDistanceTo(customer.getPosition());
            }
        }

        HungarianAlgorithm ha = new HungarianAlgorithm(costMatrix);
        int[] result = ha.execute();

        for(int t = 0; t < result.length; t++) {
            Taxi taxi = taxiReadyQueue.get(t);
            int c = result[t];

            if (c == -1) {
                taxi.setInOperation(false);
                continue;
            }

            Customer customer = customerQueue.get(c);

            output.put(taxi, customer);
        }

        return output;
    }

    public ArrayList<Move> advanceTaxi(Taxi taxi) {
        ArrayList<Move> output = new ArrayList<>();

        //Sanitycheck if we are indeed in operation
        if (taxi.getInOperation()) {
            if (!taxi.getPath().isEmpty()) {

                //We are still driving. Advance to next spot
                output.add(new Move(taxi, taxi.getPath().remove(0)));
            } else {
                if (taxi.getPassengers().isEmpty()) {
                    //We still have to pick up the passenger

                    //TODO Sanitycheck if customer is indeed at the position we are at
                    //TODO Sanitycheck if first node is actually correct
                    taxiReadyQueue.remove(taxi);
                    customerQueue.remove(taxi.getCustomer());
                    output.add(new Move('p', taxi, taxi.getCustomer()));
                } else {
                    //We are done driving, and have already picked up our customer, so that means we are at the destination
                    // so we can drop the customer of
                    output.add(new Move('d', taxi, taxi.getCustomer()));
                }
            }
        } else {
            Main.debug("[ERROR] Tried to advance a taxi which is not in operation.");
        }

        return output;

    }

    private void processMoves(ArrayList<Move> moves) {

        for(Move move: moves) {
            char action = move.getAction();
            Taxi taxi = move.getTaxi();

            if(action == 'm') {

                //Moving to another node
                taxi.setPosition(move.getNode());
            } else if(action == 'p') {
                Customer customer = move.getCustomer();
                customer.setHasBeenPickedUp(true);

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
                taxiInOperationList.remove(taxi);
                taxiReadyQueue.add(taxi);
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
