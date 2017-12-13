import javafx.util.Pair;

import java.util.*;

public class Algorithm_GCC extends Algorithm {

    private static final int RECURSE_MODIFIER = 10;
    private int lookaheadDist = 5;
    //The length of the path that the algo will consider ( min = 1 = only check neighbours).

    private static final int DESTINATION_WEIGHT = 3;
    // How much is added to the score for each part of the path a customer would not travel over because it has been delivered
    // before that point. Min 2. Seems higher = worse

    private static final int RANDOMSEED = 123456789;
    Random random = new Random(RANDOMSEED);

    @Override
    public void readMinute(ArrayList<Call> calls) {

    }

    @Override
    public void setup() {
        int posLookahead = RECURSE_MODIFIER -(int)(Math.log(sharedData.getGraph().getSize())/Math.log(2));
        setLookaheadDist(lookaheadDist = Math.max(1,Math.min(6,posLookahead)));
        Main.debug("Chose lookahead distance of "+lookaheadDist);
    }

    @Override
    public ArrayList<Move> processMinute(boolean callsLeft) {
        ArrayList<Move> minute = new ArrayList<>();

        //TODO Remove debug VVV
        StringBuilder debug = new StringBuilder("        Starting minute " + lastUpdatedMinute + ". Outside are [");
        for(Customer c : sharedData.getCustomerOutsideList()) {
            debug.append(c.getPosition().getId()).append(">").append(c.getDestination().getId()).append(",");
        }
        Main.debug(debug+"] -------------------------");

        for(Customer customer : sharedData.getCustomerList()) {
            customer.setBeingHandled(customer.isInTaxi());
            customer.setHasBeenPickedUp(customer.isInTaxi());
        }

        for (Taxi taxi : sharedData.getTaxiList()) {

            if (taxi.getPassengers().isEmpty()) {
                //Empty taxi, we must pick someone up

                if (taxi.getPosition().getCustomers().isEmpty()) {
                    Customer closestCustomer = findClosestCustomer(taxi);

                    if (closestCustomer == null) {
                        //Nobody to pickup, this taxi has nothing to do so lets wait
                        continue;
                    }
                    //There's no customer at our position

                    //Go one towards the customer we chose to handle
                    Vertex next = taxi.getPosition().getNextTowards(closestCustomer.getPosition());
                    minute.add(new Move(taxi, next));

                    //Let other taxis know we've got this person covered
                    closestCustomer.setBeingHandled(true);

                    continue;
                } else {
                    //TODO Remove debug VVV
                    StringBuilder d = new StringBuilder("T" + taxi.getOutputId() + "(" + taxi.getPosition().getId() + ") may pick up [");
                    for(Customer c : taxi.getPosition().getCustomers()) {
                        d.append(c.getPosition().getId()).append(">").append(c.getDestination().getId()).append(",");
                    }
                    Main.debug(d+"]");
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

        for (Customer customer : sharedData.getCustomerOutsideList()) {
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

        //TODO Exclude customers already picked up
        ArrayList<Customer> outsidePassengers = new ArrayList<>(sharedData.getCustomerOutsideList());

        candidatePassengers.addAll(outsidePassengers);

        Vertex bestVertex = null;
        int bestScore = Integer.MIN_VALUE;
        MoveOption bestOption = null;        // Connected graph -> always neighbor -> never null.

        for (Vertex neighbor : taxi.getPosition().getNeigbours()) {

            ArrayList<Vertex> path = new ArrayList<>();
            path.add(taxi.getPosition());
            MoveOption option = computeBestScore(lookaheadDist, path, neighbor, taxi);

            boolean better;
            if (option.getScore() > bestScore) {
                better = true;
            } else if (option.getScore() == bestScore) {

                //Need to tiebreak so that we don't always pick the same if the're literally equally good
                if(bestOption.getToBeDropped().isEmpty() && bestOption.getToBePickedUp().isEmpty()) {
                    //current best doesn't have to drop/pickup passengers
                    if(!option.getToBePickedUp().isEmpty() || !option.getToBeDropped().isEmpty()) {
                        //but the new one does have to
                        better = false;
                    } else {
                        //new one doesn't either

                        //cases are equal, must decide on something but not always the same
                        better = random.nextBoolean();
                    }
                } else {
                    //Current best has to do pickup / drop
                    if(option.getToBeDropped().isEmpty() && option.getToBePickedUp().isEmpty()) {
                        // but new one doesn't
                        better = true;
                    } else {
                        //cases are pretty (but not necessarily fully) equal, go random
                        better = random.nextBoolean();
                    }
                }
            } else {
                better = false;
            }

            if(better) {
                bestScore = option.getScore();
                bestVertex = neighbor;
                bestOption = option;
            }
        }

        boolean mayMove = true;



        int amountPassengers = taxi.getPassengers().size();

        for(Customer customer : bestOption.getToBeDropped()) {
            customer.setHasBeenPickedUp(false);
            customer.setBeingHandled(false);
            amountPassengers--;
            mayMove = false;
            minute.add(new Move('d', taxi, customer));
        }

        for(Customer customer : bestOption.getToBePickedUp()) {
            if(amountPassengers < Taxi.MAX_CAPACITY) {
                customer.setHasBeenPickedUp(true);
                customer.setBeingHandled(true);
                amountPassengers++;
                mayMove = false;
                minute.add(new Move('p', taxi, customer));
            } else {
                Main.debug("[TAXI] Wanted to pick up passenger but full");
                break;
            }
        }

        if (mayMove) {
            minute.add(new Move(taxi, bestVertex));
        }
    }

    private MoveOption computeBestScore(int depthLeft, ArrayList<Vertex> path, Vertex vertex, Taxi taxi) {

        path.add(vertex);

        if(depthLeft > 1) {

            //Recurse
            MoveOption bestOption = null;
            int bestScore = Integer.MIN_VALUE;
            for(Vertex neighbour : vertex.getNeigbours()) {
                MoveOption candidate = computeBestScore(depthLeft-1, new ArrayList<>(path), neighbour, taxi);

                if(candidate.getScore() > bestScore) {
                    bestOption = candidate;
                    bestScore = candidate.getScore();
                }
            }
            return bestOption;

        } else {

            int score = 0;
            HashSet<Customer> toPickUp = new HashSet<>();
            HashSet<Customer> toDropOff = new HashSet<>();

            //Reset all the HasBeenChecked scores for the outside people
            for(Vertex v : path) {
                for (Customer c : v.getCustomers()) {
                    c.setHasBeenChecked(false);
                }
            }

            ArrayList<Pair<Integer, Customer>> candidates = new ArrayList<>();

            //Check scores for already in taxi and drop if no benefit or 0-abble
            for(Customer customer : taxi.getPassengers()) {
                int customerScore = calculateCustomerScore(customer, path, 0);
                checkAddCustomer(candidates, customer, customerScore);
            }

            for(int dist=0; dist < path.size()-1; dist++) {
                for(Customer customer : path.get(dist).getCustomers()) {
                    if(!customer.hasBeenPickedUp() && !customer.hasBeenChecked()) {
                        int customerScore = calculateCustomerScore(customer, path, dist);
                        customer.setHasBeenChecked(true);
                        checkAddCustomer(candidates, customer, customerScore);
                    }
                }
            }

            //Build score
            for(Pair<Integer, Customer> candidate : candidates) {
                score += Math.max(0, candidate.getKey());
            }

            //Build drop ones
            for(Customer customer : taxi.getPassengers()) {
                boolean contains = false;
                for(Pair<Integer, Customer> pair : candidates) {
                    if(pair.getValue().equals(customer)) {
                        contains = true;
                        break;
                    }
                }
                if(!contains) {
                    toDropOff.add(customer);
                }
            }

            //Build pickup ones
            for(Customer customer : taxi.getPosition().getCustomers()) {
                for(Pair<Integer, Customer> pair : candidates) {
                    if(pair.getValue().equals(customer)) {
                        toPickUp.add(customer);
                        break;
                    }
                }
            }

            //TODO Remove debug VVV
            String debug = "T"+taxi.getOutputId()+"("+taxi.getPosition().getId()+")->("+path.get(1).getId()+")[";
            for(Customer c : taxi.getPassengers()) {
                debug += c.getDestination().getId()+",";
            }
            debug += "] s="+score+" p=[";
            for(Customer c : toPickUp) {
                debug += c.getDestination().getId()+",";
            }
            debug += "] d=[";
            for(Customer c : toDropOff) {
                debug += c.getDestination().getId()+",";
            }
            debug += "] for path [";
            for(Vertex v : path) {
                debug += v.getId()+",";
            }
            debug += "]";
            Main.debug(debug);

            return new MoveOption(toDropOff, toPickUp, score);
        }
    }

    private void checkAddCustomer(ArrayList<Pair<Integer, Customer>> candidates, Customer customer, int customerScore) {
        if(customerScore > 0) {
            if (candidates.size() < Taxi.MAX_CAPACITY) {
                //Fits extra customers
                candidates.add(new Pair<>(customerScore, customer));
            } else {
                candidates.sort(Comparator.comparing(Pair::getKey));
                if (customerScore > candidates.get(0).getKey()) {
                    candidates.remove(0);
                    candidates.add(new Pair<>(customerScore, customer));
                }
            }
        }
    }

    private int calculateCustomerScore(Customer customer, ArrayList<Vertex> path, int startPos) {
        int bestScore = Integer.MIN_VALUE;
        int startDist = customer.getPosition().getDistanceTo(customer.getDestination());

        if(customer.getPosition().equals(customer.getDestination())) {
            //customer is already at right position
            // this customer would thus not benefit ever from going further, not from going back and forth either
            if(!customer.isInTaxi()) {
                Main.debug("[ERROR] Customer at position "+customer.getPosition()+" is outside and at destination but not removed");
            }
            return Integer.MIN_VALUE;
        }

        for(int dist = startPos+1; dist < path.size(); dist++) {
            Vertex v = path.get(dist);
            int vDist = v.getDistanceTo(customer.getDestination());
            int score = (int) Math.pow(2,startDist - vDist);
            if(v.equals(customer.getDestination())) {
                score += (lookaheadDist - dist) * DESTINATION_WEIGHT; //Add 1 extra point for each closer reaching dist
                bestScore = score;
                break;
            }

            bestScore = Math.max(bestScore, score);
        }

        return bestScore;
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

            } else if (action == 'd') {
                Customer customer = move.getCustomer();

                //Dropping off a passenger
                taxi.drop(customer, sharedData);
            }
        }

    }

    public void setLookaheadDist(int lookaheadDist) {
        this.lookaheadDist = lookaheadDist;
    }

    private class MoveOption {
        private HashSet<Customer> toBeDropped;
        private HashSet<Customer> toBePickedUp;
        private int score;

        public MoveOption(HashSet<Customer> toBeDropped, HashSet<Customer> toBePickedUp, int score) {
            this.toBeDropped = toBeDropped;
            this.toBePickedUp = toBePickedUp;
            this.score = score;
        }

        public HashSet<Customer> getToBeDropped() {
            return toBeDropped;
        }

        public HashSet<Customer> getToBePickedUp() {
            return toBePickedUp;
        }

        public int getScore() {
            return score;
        }
    }

}
