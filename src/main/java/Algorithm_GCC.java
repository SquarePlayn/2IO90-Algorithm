import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Marijn van der Horst on 6-12-2017.
 */
public class Algorithm_GCC extends Algorithm {
    private ArrayList<Customer> customerQueue;

    @Override
    public void readMinute(ArrayList<Call> calls) {
        for (Call call: calls) {
            customerQueue.add(call.getCustomer());
        }
    }

    @Override
    public void setup() {
        customerQueue = new ArrayList<>();
    }

    @Override
    public ArrayList<Move> processMinute(boolean callsLeft) {
        ArrayList<Move> minute = new ArrayList<>();

        for (Taxi taxi : sharedData.getTaxiList()) {
            if (!taxi.getInOperation()) {
                if (customerQueue.isEmpty()) {
                    continue;
                }

                // Go the the closest customer.
                Customer closest = findClosestCustomer(taxi);

                taxi.setInOperation(true);
                taxi.setCustomer(closest);

                customerQueue.remove(closest);
            }
            // Taxi definitely in operation.

            if (taxi.getPassengers().isEmpty()) {
                if (taxi.getPosition().equals(taxi.getCustomer().getPosition())) {
                    minute.add(new Move('p', taxi, taxi.getCustomer()));
                } else {
                    Vertex next = taxi.getPosition().getNextTowards(taxi.getCustomer().getPosition());

                    minute.add(new Move(taxi, next));
                }
                continue;
            }
            // Taxi has at least one passenger.
            addGccMoves(minute, taxi);
        }

        processMoves(minute);

        return minute;
    }

    private Customer findClosestCustomer(Taxi taxi) {
        Customer closest = null;
        int shortestDistance = Integer.MAX_VALUE;

        for (Customer customer : customerQueue) {
            int distance = sharedData.getGraph().getDistance(customer.getPosition(), taxi.getPosition());

            if (distance < shortestDistance) {
                closest = customer;
                shortestDistance = distance;
            }
        }

        return closest;
    }

    private void addGccMoves(ArrayList<Move> minute, Taxi taxi) {
        ArrayList<Customer> candidatePassengers = new ArrayList<>(taxi.getPassengers());

        List<Customer> outsidePassengers = customerQueue.stream()
                .filter(c -> c.getPosition().equals(taxi.getPosition()))
                .collect(Collectors.toList());

        candidatePassengers.addAll(outsidePassengers);

        Vertex bestVertex = null;           // Connected graph -> always neighbor -> never null.
        int bestScore = Integer.MAX_VALUE;

        for (Vertex neighbor : taxi.getPosition().getNeigbours()) {
            int score = 0;

            for (Customer customer : candidatePassengers) {
                score += neighbor.getDistanceTo(customer.getPosition());
            }

            // TODO if equal decide based on amount of dropoffs e.g.
            if (score < bestScore) {
                bestScore = score;
                bestVertex = neighbor;
            }
        }

        boolean mayMove = true;

        int amountPassengers = taxi.getPassengers().size();

        for (Customer intaxi : taxi.getPassengers()) {
            int oldDistance = taxi.getPosition().getDistanceTo(intaxi.getDestination());
            int newDistance = bestVertex.getDistanceTo(intaxi.getDestination());

            if (newDistance > oldDistance) {
                // Throw passenger out of our taxi.
                mayMove = false;
                minute.add(new Move('d', taxi, intaxi));
                amountPassengers--;
            }
        }

        for (Customer outside : outsidePassengers) {
            if (amountPassengers >= Taxi.MAX_CAPACITY) {
                break;
            }

            int oldDistance = taxi.getPosition().getDistanceTo(outside.getDestination());
            int newDistance = bestVertex.getDistanceTo(outside.getDestination());

            if (newDistance <= oldDistance) {
                // Throw passenger out of our taxi.
                mayMove = false;
                minute.add(new Move('p', taxi, outside));
                amountPassengers++;
            }
        }

        if (mayMove) {
            minute.add(new Move(taxi, bestVertex));
        }
    }

    @Override
    public void haltExecution() {

    }

    @Override
    public boolean doesUpdate(AlgoVar var) {
        return var == AlgoVar.TAXI_CUSTOMER ||
                var == AlgoVar.TAXI_IN_OPERATION;
    }

    @Override
    public void continueExecution(int upToMinute, HashMap<AlgoVar, Integer> lastUpdated) {

    }

    /**
     * Process the output of a minute.
     * @param moves
     */
    private void processMoves(ArrayList<Move> moves) {

        //TODO Check if moving the processing to in here doesn't drastically increase run time, since (very) rough
        // TODO testing seemed like about a 10% increase

        for(Move move : moves) {
            char action = move.getAction();
            Taxi taxi = move.getTaxi();

            if(action == 'm') {

                //Moving to another node
                taxi.setPosition(move.getNode());

            } else if(action == 'p') {
                Customer customer = move.getCustomer();

                //Picking up a passenger
                taxi.getPassengers().add(customer);

                //Since we have somewhere to go, we are in operation
                taxi.setInOperation(true);

            } else if(action == 'd') {
                Customer customer = move.getCustomer();

                //Dropping off a passenger
                taxi.getPassengers().remove(customer);
                taxi.setInOperation(!taxi.getPassengers().isEmpty());

                customer.updatePosition(taxi.getPosition());
                customerQueue.add(customer);

                //We have delivered our customer, let's drop him
                sharedData.getCustomerList().remove(taxi.getCustomer());
                taxi.setCustomer(null);
            }
        }

    }
}
