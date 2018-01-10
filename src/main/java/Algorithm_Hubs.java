import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Algorithm_Hubs extends Algorithm {

    private HashSet<Customer> customerQueue;
    private HashSet<Taxi> taxiReadyQueue;
    private HashSet<Taxi> taxiInOperationList;

    private boolean dropEveryone;

    @Override
    public void readMinute(ArrayList<Call> calls) {
        for(Call call : calls) {
            Customer customer = call.getCustomer();

            //Add the customer to the queue
            customerQueue.add(customer);

            //Set to not be picked up, nor being handled
            customer.setHasBeenPickedUp(false);
            customer.setBeingHandled(false);

            if(!customer.getPosition().isHubCenter()) {
                //If it's not on a hub center already, add it to the hub's list
                customer.getPosition().getHub().addCustomer(customer);
            }
        }
    }

    @Override
    public void setup() {
        //Make sure the hubs have been built
        sharedData.getGraph().buildHubs(sharedData.getRandom());

        //Initialize the queues.
        customerQueue = new HashSet<>();
        taxiReadyQueue = new HashSet<>();
        taxiInOperationList = new HashSet<>();

        taxiReadyQueue.addAll(sharedData.getTaxiList()); // Initially all taxis are unoccupied, so add them all to the ready queue.

        dropEveryone = false;
    }

    @Override
    public ArrayList<Move> processMinute(boolean callsLeft) {
        ArrayList<Move> moves = new ArrayList<>();

        if(dropEveryone) {
            ArrayList<Customer> droppers = new ArrayList<>();
            for(Taxi taxi : sharedData.getTaxiList()) {
                for(Customer customer : taxi.getPassengers()) {
                    moves.add(new Move('d', taxi, customer) );
                }
            }

            for(Move move : moves) {
                taxiDrop(move.getTaxi(), move.getCustomer());
            }

            //Make all the customers be waiting
            customerQueue.addAll(sharedData.getCustomerList());
            dropEveryone = false;
            return moves;
        }

        //Assign waiting ready taxis to a customer
        while(!customerQueue.isEmpty() && !taxiReadyQueue.isEmpty()) {
            Taxi taxi = taxiReadyQueue.iterator().next(); //Getting some/*any* taxi that is ready
            Customer customer = customerQueue.iterator().next(); //Get some/*any* customer that is waiting

            taxi.setCustomer(customer);
            customer.setBeingHandled(true);
            customerQueue.remove(customer);
            taxiReadyQueue.remove(taxi);
            taxiInOperationList.add(taxi);
        }

        ArrayList<Taxi> taxisToBeRemoved = new ArrayList<>(); //ArrayList to catch the to-be-readyupped taxis
        for(Taxi taxi : taxiInOperationList) {
            if(taxi.isGoingToCenter()) {
                if(!taxi.getPosition().isHubCenter()) {
                    //Just making sure we aren't already on the center
                    moves.add(taxiMoveCenter(taxi, taxisToBeRemoved));
                } else {
                    //The case when dropping someone off or picking up on a centerpiece
                    if(taxi.getCustomer() == null) {
                        //If it is not carrying a customer or going to one, it's ready again
                        taxisToBeRemoved.add(taxi);
                        taxiReadyQueue.add(taxi);
                    }
                    taxi.setGoingToCenter(false);
                }
            } else {
                // This taxi is going places other than hub center
                if(taxi.getMovingToHub() != null) {
                    //Curerntly moving towards another hub
                    moves.add(taxiMoveHub(taxi));
                } else {
                    //Not curerntly moving towards another hub
                    if (taxi.getPassengers().isEmpty()) {
                        //hasn't picked up the passenger yet
                        if (taxi.getPosition().equals(taxi.getCustomer().getPosition())) {
                            //At customer
                            moves.add(taxiPickup(taxi, taxi.getCustomer()));
                        } else {
                            //Not yet at customer
                            if(taxi.getPosition().getHub().equals(taxi.getCustomer().getPosition().getHub())) {
                                //On the right hub
                                moves.add(taxiMove(taxi, taxi.getPosition().getNextTowards(taxi.getCustomer().getPosition())));
                            } else {
                                //Not on the right hub yet
                                taxi.setMovingToHub(taxi.getPosition().getHub().getNextHubTowardsHub(taxi.getCustomer().getPosition().getHub()));
                                moves.add(taxiMoveHub(taxi));
                            }
                        }
                    } else {
                        //Passenger is in the taxi
                        if (taxi.getPosition().equals(taxi.getCustomer().getDestination())) {
                            //At destination
                            moves.add(taxiDrop(taxi, taxi.getCustomer()));
                        } else {
                            //Not yet at destination
                            if (taxi.getPosition().getHub().equals(taxi.getCustomer().getPosition().getHub())) {
                                // If in the hub of the customers destination
                                moves.add(taxiMove(taxi, taxi.getPosition().getNextTowards(taxi.getCustomer().getDestination())));
                            } else {
                                //Not yet on the hub of the customer
                                taxi.setMovingToHub(taxi.getPosition().getHub().getNextHubTowardsHub(taxi.getCustomer().getPosition().getHub()));
                                moves.add(taxiMoveHub(taxi));
                            }
                        }
                    }
                }
            }
        }

        //since we cannot remove from a list we are iterating over, remove the taxis form the inoperationlist afterwards
        for(Taxi taxi : taxisToBeRemoved) {
            taxiInOperationList.remove(taxi);
        }

        return moves;
    }

    public Move taxiMove(Taxi taxi, Vertex towards) {
        taxi.setPosition(towards);
        return new Move(taxi, towards);
    }

    public Move taxiPickup(Taxi taxi, Customer customer) {
        taxi.pickup(customer, sharedData);
        taxi.setGoingToCenter(true);
        return new Move('p', taxi, customer);
    }

    public Move taxiDrop(Taxi taxi, Customer customer) {
        taxi.drop(customer, sharedData);
        taxi.setCustomer(null);
        taxi.setGoingToCenter(true);
        return new Move('d', taxi, customer);
    }

    public Move taxiMoveHub(Taxi taxi) {
        Vertex nextVertex = taxi.getPosition().getNextTowards(taxi.getMovingToHub());
        if(nextVertex.equals(taxi.getMovingToHub())) {
            taxi.setMovingToHub(null);
        }
        return taxiMove(taxi, nextVertex);
    }

    public Move taxiMoveCenter(Taxi taxi, ArrayList<Taxi> taxisToBeRemoved) {
        //Go back to center of hub
        Vertex nextVertex = taxi.getPosition().getVertexTowardsCenter();
        if(nextVertex.isHubCenter()) {
            //Next move it'll be at the center
            taxi.setGoingToCenter(false);

            if(taxi.getCustomer() == null) {
                //We are not carrying any customers or moving to any
                taxisToBeRemoved.add(taxi);
                taxiReadyQueue.add(taxi);
            }
        }
        return taxiMove(taxi, nextVertex);
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
        customerQueue.clear();
        taxiReadyQueue.clear();
        taxiInOperationList.clear();

        for(Taxi taxi : sharedData.getTaxiList()) {
            taxi.setCustomer(null);
            taxi.setInOperation(false);
        }

        taxiReadyQueue.addAll(sharedData.getTaxiList());

        dropEveryone = true;

    }
}
