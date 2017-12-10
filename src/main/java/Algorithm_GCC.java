import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Marijn van der Horst on 6-12-2017.
 */
public class Algorithm_GCC extends Algorithm {
    private ArrayList<Customer> customerQueue;
    private final int SEARCH_DISTANCE = 3 ;

    @Override
    public void readMinute(ArrayList<Call> calls) {
        for (Call call : calls) {
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

        for (Customer customer : customerQueue) {
            //For each customer that is outside, init to be waiting
            customer.setBeingHandled(false);
            customer.setHasBeenPickedUp(false);
        }

        String debug = "Outside are people at [";
        for(Customer customer : customerQueue) {
            debug += customer.getPosition().getId()+"->"+customer.getDestination().getId()+",";
        }
        Main.debug(debug+="]");

        /*//TODO Remove debug
        for(int c1=0; c1<customerQueue.size(); c1++) {
            for(int c2=0; c2<customerQueue.size(); c2++) {
                if(c1 != c2) {
                    if(customerQueue.get(c1).equals(customerQueue.get(c2))) {
                        Main.debug("[ERROR] Duplicates in the customerQueue");
                    }
                }
            }
        }*/

        for (Taxi taxi : sharedData.getTaxiList()) {

            if (taxi.getPassengers().isEmpty()) {
                //Empty taxi, we must pick someone up
                Customer closestCustomer = findClosestCustomer(taxi);

                if (closestCustomer == null) {
                    //Nobody to pickup, this taxi has nothing to do so lets wait
                    continue;
                }

                if (!closestCustomer.getPosition().equals(taxi.getPosition())) {
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
            if (!customer.isBeingHandled()) {
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
                .filter(c -> c.getPosition().equals(taxi.getPosition()) && !c.getHasBeenPickedUp())
                .collect(Collectors.toList());

        candidatePassengers.addAll(outsidePassengers);

        Vertex bestNeighbour = null;           // Connected graph -> always neighbor -> never null.
        Vertex bestEndpoint = null;            // Connected graph -> always neighbor -> never null.
        int bestScore = Integer.MIN_VALUE;

        for (Vertex neighbor : taxi.getPosition().getNeigbours()) {
            HashSet<Customer> candidatesHash = new HashSet<>(candidatePassengers);
            HashSet<Vertex> path = new HashSet<>();

            Pair<Integer, Vertex> bestOption = computeBestScore(taxi, neighbor, path, SEARCH_DISTANCE, candidatesHash);
            int score = bestOption.getKey();

            // TODO if equal decide based on amount of dropoffs e.g.
            if (score > bestScore) {
                bestScore = score;
                bestNeighbour = neighbor;
                bestEndpoint = bestOption.getValue();
            }
        }

        //fixme debug
        String debug = "   t="+lastUpdatedMinute+" T"+taxi.getOutputId()+"("+taxi.getPosition().getId()+") [";
        for(Customer c : taxi.getPassengers()) {
            debug += c.getDestination().getId()+",";
        }
        debug += "] path ["+bestNeighbour.getId()+","+bestEndpoint.getId()+"] score "+bestScore;
        Main.debug(debug);

        boolean mayMove = true;

        int amountPassengers = taxi.getPassengers().size();

        for (Customer intaxi : taxi.getPassengers()) {
            int oldDistance = intaxi.getDestination().getDistanceTo(taxi.getPosition());
            int newDistance = intaxi.getDestination().getDistanceTo(bestEndpoint);
            newDistance = Math.min(newDistance, intaxi.getDestination().getDistanceTo(bestNeighbour));

            if (newDistance > oldDistance || intaxi.getDestination().equals(taxi.getPosition())) { //TODO Let this depend on recursion depth
                // Throw passenger out of our taxi.
                mayMove = false;
                minute.add(new Move('d', taxi, intaxi));
                amountPassengers--;
            }
        }

        for (Customer outside : outsidePassengers) {
            if (outside.getHasBeenPickedUp()) {
                Main.debug("Customer "+outside.getPosition().getId()+"->"+outside.getDestination().getId()+" has already been picked up");
                //This customer is already being taken care of by another taxi
                continue;
            }

            if (amountPassengers >= Taxi.MAX_CAPACITY) {
                Main.debug("Taxi "+taxi.getOutputId()+" is full");
                break;
            }

            int oldDistance = outside.getDestination().getDistanceTo(taxi.getPosition());
            int newDistance = outside.getDestination().getDistanceTo(bestEndpoint);
            newDistance = Math.min(newDistance, outside.getDestination().getDistanceTo(bestNeighbour));

            if (newDistance <= oldDistance) { //TODO Let this depend on the recursion depth
                // Pick up the passenger
                mayMove = false;
                minute.add(new Move('p', taxi, outside));
                customerQueue.remove(outside);
                amountPassengers++;
                outside.setBeingHandled(true);
                outside.setHasBeenPickedUp(true);
            } else {
                Main.debug("T("+taxi.getOutputId()+") is going too far away for c "+outside.getPosition().getId()+"->"+outside.getDestination().getId());
            }
        }

        if (mayMove) {
            minute.add(new Move(taxi, bestNeighbour));
        }
    }

    private Pair<Integer, Vertex> computeBestScore(Taxi taxi, Vertex vertex, HashSet<Vertex> path, int remainingDepth, HashSet<Customer> candidates) {

        Vertex taxiPosition = taxi.getPosition();

        if(remainingDepth > 0) {

            //Update candidates
            HashSet<Customer> newCandidates = new HashSet<>(candidates);
            if(!taxi.isFull()) {
                newCandidates.addAll(vertex.getCustomers());
            }

            for(Customer c: vertex.getCustomers()) {
                if(!c.getPosition().equals(vertex)) {
                    Main.debug("Vertex "+vertex.getId()+" contains faulty customer "+c.getPosition().getId()+"->"+c.getDestination().getId());

                }
            }
            HashSet<Vertex> newPath = new HashSet<Vertex>(path);
            newPath.add(vertex);

            int score = Integer.MIN_VALUE;
            Vertex bestVertex = null;

            for(Vertex neighbour : vertex.getNeigbours()) {

                Pair<Integer, Vertex> result = computeBestScore(taxi, neighbour, newPath,
                       remainingDepth-1, newCandidates);

                if(result.getKey() > score) {
                    score = result.getKey();
                    bestVertex = result.getValue();
                }
            }

            return new Pair<>(score, bestVertex);
        } else {
            //Calculate score over all candidates
            int score = 0;

            Customer lastCustomer = null;


            String d = "[";
            for(Customer c: candidates) {
                d += c.getPosition().getId()+"->"+c.getDestination().getId()+",";
            }
//            Main.debug("Candidates: "+candidates.size()+": "+d+"]");


            for (Customer customer : candidates) {
                if(lastCustomer != null) {
                    if(customer.equals(lastCustomer)) {
                        Main.debug("[ERROR] HashSet duplicate fail");
                    }
                }
                lastCustomer = customer;
                //TODO Remove debug

                int oldDistance = customer.getDestination().getDistanceTo(customer.getPosition());
                int newDistance = customer.getDestination().getDistanceTo(vertex);

                for(Vertex v : path) {
                    newDistance = Math.min(newDistance, customer.getDestination().getDistanceTo(v));
                }

                if(path.contains(customer.getDestination())) {
                    //Main.debug("T("+taxi.getOutputId()+") can bring "+customer.getPosition().getId()+"->"+customer.getDestination().getId()+" to destination");
                    score += 2*(SEARCH_DISTANCE-customer.getDestination().getDistanceTo(taxi.getPosition()));
                }

                if (newDistance < oldDistance) {
                    score += 2 * (oldDistance-newDistance); //TODO Check if needs adjusting
                } else if (newDistance == oldDistance) {
                    score++;
                }
            }
            //Main.debug("Score T("+taxi.getOutputId()+") = "+score);

            return new Pair<>(score, vertex);
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
     *
     * @param moves The moves the algorithm did this move.
     */
    private void processMoves(ArrayList<Move> moves) {

        //TODO Check if moving the processing to in here doesn't drastically increase run time, since (very) rough
        // TODO testing seemed like about a 10% increase

        for (Move move : moves) {
            char action = move.getAction();
            Taxi taxi = move.getTaxi();

            if (action == 'm') {

                //Moving to another node
                taxi.setPosition(move.getNode());

            } else if (action == 'p') {
                Customer customer = move.getCustomer();

                //Picking up a passenger
                taxi.pickup(customer, sharedData);

                //Remove him from the people outside queue
                customerQueue.remove(customer);

            } else if (action == 'd') {
                Customer customer = move.getCustomer();

                //Dropping off a passenger
                taxi.drop(customer, sharedData);

                if (!customer.isAtDestination()) {
                    customerQueue.add(customer);
                }
            }
        }

    }
}
