import javafx.util.Pair;
import java.util.*;
import java.util.stream.Collectors;

public class Algorithm_LSD extends Algorithm {

    private static final int RECURSE_MODIFIER = 10;
    private static final int MAX_LOOKAHEAD = 11;
    private static final int UPDATE_FREQUENCY = 1;
    private int lookaheadDist = 5;
    //The length of the path that the algo will consider ( min = 1 = only check neighbours).

    private ArrayList<Customer> customerOutsideList;

    private static final int DESTINATION_WEIGHT = 10;
    // How much is added to the score for each part of the path a customer would not travel over because it has been delivered
    // before that point. Min 2. Seems higher = worse

    @Override
    public void readMinute(ArrayList<Call> calls) {
        for(Call call : calls) {
            customerOutsideList.add(call.getCustomer());
        }
    }

    @Override
    public void setup() {
        customerOutsideList = new ArrayList<>(sharedData.getCustomerOutsideList());

        int posLookahead = RECURSE_MODIFIER -(int)(Math.log(sharedData.getGraph().getSize())/Math.log(2));

        if(sharedData.getGraph().getSize() < 300) {
            if (Preamble.amountOfTaxis < 50) posLookahead += 2;
            if (Preamble.amountOfTaxis < 10) posLookahead += 2;
        }

        setLookaheadDist(lookaheadDist = Math.max(1,Math.min(MAX_LOOKAHEAD,posLookahead)));
        System.err.println("LSD using depth of "+lookaheadDist);
    }

    @Override
    public ArrayList<Move> processMinute(boolean callsLeft) {
        ArrayList<Move> minute = new ArrayList<>();

        //LDS On all taxis that have a customer to deliver or pick up
        for (Taxi taxi : sharedData.getTaxiList()) {

            if (taxi.getPassengers().isEmpty()) {
                //Empty taxi, we must pick someone up
                if (taxi.getPosition().getCustomers().isEmpty()) {
                    continue;
                }
                //If we get here, there's a customer at out position, let's go do our pickup and dropoff logic
            }

            // Taxi has at least one passenger in our taxi or on our spot.
            addLsdMoves(minute, taxi);
        }

        // Handle all other taxis that still need to move towards a customer.

        //Build 2 lists: Taxis in operation and taxis not in operation (= have customer in or on spot)
        List<Taxi> taxisNotInOperation = sharedData.getTaxiList().stream()
                .filter(taxi -> taxi.getPassengers().isEmpty() && taxi.getPosition().getCustomers().isEmpty())
                .collect(Collectors.toList());

        //For taxis not in operation: Do hungarian (or not) and make the taxis move over there.
        if(!(taxisNotInOperation.isEmpty() || customerOutsideList.isEmpty())) {
            HashMap<Taxi, Customer> hungOut = applyHungarian(taxisNotInOperation, customerOutsideList);

            for(Taxi taxi : taxisNotInOperation) {
                if(hungOut.containsKey(taxi)) {
                    Customer customer = hungOut.get(taxi);
                    Vertex nextTowardsCustomer = taxi.getPosition().getNextTowards(customer.getPosition());

                    minute.add(new Move(taxi, nextTowardsCustomer));
                } else {
                    if(sharedData.getGraph().getHubs().size() > 0) {
                        //Hubs set up, move towards center
                        Vertex toCenter = taxi.getPosition().getVertexTowardsCenter();
                        if (toCenter != null) {
                            minute.add(new Move(taxi, toCenter));
                        }
                    }
                }
            }
        }

        processMoves(minute);

        return minute;
    }

    private void addLsdMoves(ArrayList<Move> minute, Taxi taxi) {
       if(taxi.getTurnsLeft() <= 0) {
           int bestScore = Integer.MIN_VALUE;
           MoveOption bestOption = null;        // Connected graph -> always neighbor -> never null.

           for (Vertex neighbor : taxi.getPosition().getNeigbours()) {

               ArrayList<Vertex> path = new ArrayList<>();
               path.add(taxi.getPosition());
               MoveOption option = computeBestScore(lookaheadDist, path, neighbor, taxi);

               boolean better = option.getScore() > bestScore;

               if (better) {
                   bestScore = option.getScore();
                   bestOption = option;
               } else {
                   path = new ArrayList<>();
               }
           }

           taxi.setPath(bestOption.getPath());
           taxi.setTurnsLeft(Math.min(UPDATE_FREQUENCY, lookaheadDist));
       }
        taxi.setTurnsLeft(taxi.getTurnsLeft() - 1);

        ArrayList<Pair<Integer,Customer>> candidates = new ArrayList<>();
        for(Customer customer : taxi.getPassengers()) {
            checkAddCustomer(candidates, customer, calculateCustomerScore(customer, taxi.getPath(), 0));
        }

        for(Customer customer : taxi.getPosition().getCustomers()) {
            if(customer.hasBeenPickedUp()) continue;
            checkAddCustomer(candidates, customer, calculateCustomerScore(customer, taxi.getPath(), 0));
        }

        ArrayList<Customer> toDropOff = new ArrayList<>();
        ArrayList<Customer> toPickUp = new ArrayList<>();

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

        boolean mayMove = true;
        int amountPassengers = taxi.getPassengers().size();

        for(Customer customer : toDropOff) {
            customer.setHasBeenPickedUp(false);
            amountPassengers--;
            mayMove = false;
            minute.add(new Move('d', taxi, customer));
        }

        for(Customer customer : toPickUp) {
            if(amountPassengers < Taxi.MAX_CAPACITY) {
                customer.setHasBeenPickedUp(true);
                amountPassengers++;
                mayMove = false;
                minute.add(new Move('p', taxi, customer));
            } else {
                break;
            }
        }

        if (mayMove) {

            taxi.getPath().remove(0);

            minute.add(new Move(taxi, taxi.getPath().get(0)));
        }
    }

    private MoveOption computeBestScore(int depthLeft, ArrayList<Vertex> path, Vertex vertex, Taxi taxi) {

        path.add(vertex);

        if(depthLeft > 1) {

            //Recurse
            MoveOption bestOption = null;
            int bestScore = Integer.MIN_VALUE;
            for(Vertex neighbour : vertex.getNeigbours()) {
                ArrayList<Vertex> recursePath = new ArrayList<>(path);
                MoveOption candidate = computeBestScore(depthLeft-1, recursePath, neighbour, taxi);

                if(candidate.getScore() > bestScore) {
                    bestOption = candidate;
                    bestScore = candidate.getScore();
                }
            }
            return bestOption;

        } else {

            int score = 0;

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

            return new MoveOption(path, score);
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
            return Integer.MIN_VALUE;
        }

        for(int dist = startPos+1; dist < path.size(); dist++) {
            Vertex v = path.get(dist);
            int vDist = v.getDistanceTo(customer.getDestination());
            int score = startDist - vDist;
            if(Preamble.amountOfTaxis == 1) {
                score = (int) Math.pow(2, score);
            }
            if(score > 0 && vDist < 2 * lookaheadDist) {
                score += 1;
            }
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
        for(Customer customer : sharedData.getCustomerList()) {
            customer.setHasBeenPickedUp(customer.isInTaxi());
        }
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
                customerOutsideList.remove(customer); //TODO Find better way

            } else if (action == 'd') {
                Customer customer = move.getCustomer();

                //Dropping off a passenger
                taxi.drop(customer, sharedData);

                if(!customer.isAtDestination()) {
                    customerOutsideList.add(customer);
                }
            }
        }

    }

    public void setLookaheadDist(int lookaheadDist) {
        this.lookaheadDist = lookaheadDist;
    }

    private class MoveOption {
        private ArrayList<Vertex> path;
        private int score;

        public MoveOption(ArrayList<Vertex> path, int score) {
            this.path = path;
            this.score = score;
        }

        public ArrayList<Vertex> getPath() {
            return path;
        }

        public int getScore() {
            return score;
        }
    }

    /**
     * Applies the Hungarian Algorithm on the current taxi and customer queue.
     *
     * @return A HashMap containing the best taxi/customer combinations.
     */
    private HashMap<Taxi, Customer> applyHungarian(List<Taxi> taxiReadyQueue, List<Customer> customerQueue) {
        HashMap<Taxi, Customer> output = new HashMap<>();
        double[][] costMatrix = new double[taxiReadyQueue.size()][customerQueue.size()];

        // Calculate the cost matrix, using distances to the customer positions.
        for(int t = 0; t < taxiReadyQueue.size(); t++) {
            for(int c = 0; c < customerQueue.size(); c++) {
                Taxi taxi = taxiReadyQueue.get(t);
                Customer customer = customerQueue.get(c);

                costMatrix[t][c] = taxi.getPosition().getDistanceTo(customer.getPosition()); //* 10 / Math.max(1, lastUpdatedMinute - customer.getCreationMinute() + 2);
            }
        }

        // Execute the Hungarian Algorithm.
        Algorithm_Hungarian.HungarianAlgorithm ha = new Algorithm_Hungarian.HungarianAlgorithm(costMatrix);
        int[] result = ha.execute();

        // Analyse the result and populate the resulting HashMap.
        for(int t = 0; t < result.length; t++) {
            Taxi taxi = taxiReadyQueue.get(t);
            int c = result[t];

            if(c == -1) {
                // This taxi does not have a task
                continue;
            }

            Customer customer = customerQueue.get(c);

            output.put(taxi, customer);
        }

        return output;
    }

    public void upscale(int up) {
        lookaheadDist += up;
        lookaheadDist = Math.min(1,Math.max(MAX_LOOKAHEAD, lookaheadDist));
    }

}
