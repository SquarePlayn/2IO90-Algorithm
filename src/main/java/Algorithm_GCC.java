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

        for(Customer customer : customerQueue) {
            //For each customer that is outside, init to be waiting
            customer.setBeingHandled(false);
        }

        for (Taxi taxi : sharedData.getTaxiList()) {

            if (taxi.getPassengers().isEmpty()) {
                //Empty taxi, we must pick someone up
                Customer closestCustomer = findClosestCustomer(taxi);

                if(closestCustomer == null) {
                    //Nobody to pickup, this taxi has nothing to do so lets wait
                    continue;
                }

                if(!closestCustomer.getPosition().equals(taxi.getPosition())) {
                    //There's no customer at our position

                    //Go one towards the customer we chose to handle
                    Vertex next = taxi.getPosition().getNextTowards(closestCustomer.getPosition());
                    minute.add(new Move(taxi, next));

                    //Let other taxis know we've got this person covered
                    closestCustomer.setBeingHandled(true);

                    continue;
                }
                //If we get here, there's a customer at out position, let's go do our pickup and dropoff logic
            }

            // Taxi has at least one passenger in our taxi or on our spot.
            addGccMoves(minute, taxi);
        }

        processMoves(minute);

        return minute;
    }

    private Customer findClosestCustomer(Taxi taxi) {
        Customer closest = null;
        int shortestDistance = Integer.MAX_VALUE;

        for (Customer customer : customerQueue) {
            if(!customer.isBeingHandled()) {
                //If another taxi hasn't taken care of this customer yet (to prevent 2 taxis going to the same customer
                int distance = sharedData.getGraph().getDistance(customer.getPosition(), taxi.getPosition());

                if (distance < shortestDistance) {
                    closest = customer;
                    shortestDistance = distance;
                }
            }
        }

        return closest;
    }

    private void addGccMoves(ArrayList<Move> minute, Taxi taxi) {
        ArrayList<Customer> candidatePassengers = new ArrayList<>(taxi.getPassengers());

        List<Customer> outsidePassengers = customerQueue.stream()
                .filter(c -> c.getPosition().equals(taxi.getPosition()) && !c.isBeingHandled())
                .collect(Collectors.toList());
        int c = 0;
        for(Customer customer: outsidePassengers) { //TODO Remove these debugs
            if(!customer.isBeingHandled()) {
                c++;
            }
        }

        candidatePassengers.addAll(outsidePassengers);

        Vertex bestVertex = null;           // Connected graph -> always neighbor -> never null.
        int bestScore = Integer.MIN_VALUE;

        for (Vertex neighbor : taxi.getPosition().getNeigbours()) {
            int score = 0;

            for (Customer customer : candidatePassengers) {
                int oldDistance = customer.getDestination().getDistanceTo(taxi.getPosition());
                int newDistance = customer.getDestination().getDistanceTo(neighbor);

                if(newDistance < oldDistance) {
                    score += 2;
                } else if(newDistance == oldDistance) {
                    score++;
                }
            }

            // TODO if equal decide based on amount of dropoffs e.g.
            if (score > bestScore) {
                bestScore = score;
                bestVertex = neighbor;
            }
        }

        boolean mayMove = true;

        int amountPassengers = taxi.getPassengers().size();

        for (Customer intaxi : taxi.getPassengers()) {
            int oldDistance = intaxi.getDestination().getDistanceTo(taxi.getPosition());
            int newDistance = intaxi.getDestination().getDistanceTo(bestVertex);

            if (newDistance > oldDistance) {
                // Throw passenger out of our taxi.
                mayMove = false;
                minute.add(new Move('d', taxi, intaxi));
                amountPassengers--;
            }
        }

        for (Customer outside : outsidePassengers) {
            if(outside.isBeingHandled()) {
                //This customer is already being taken care of by another taxi
                continue;
            }

            if (amountPassengers >= Taxi.MAX_CAPACITY) {
                break;
            }

            int oldDistance = outside.getDestination().getDistanceTo(taxi.getPosition());
            int newDistance = outside.getDestination().getDistanceTo(bestVertex);

            if (newDistance <= oldDistance) {
                // Pick up the passenger
                mayMove = false;
                minute.add(new Move('p', taxi, outside));
                amountPassengers++;
                outside.setBeingHandled(true);
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
        return false;
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

                //Remove him from the people outside queue
                customerQueue.remove(customer);

            } else if(action == 'd') {
                Customer customer = move.getCustomer();

                //Dropping off a passenger
                taxi.getPassengers().remove(customer);

                if(taxi.getPosition().equals(customer.getDestination())) {
                    //We have delivered our customer, let's drop him
                    sharedData.getCustomerList().remove(customer);
                } else {
                    //Customer is not yet at its destination
                    customer.updatePosition(taxi.getPosition());
                    customerQueue.add(customer);
                }
            }
        }

    }
}
