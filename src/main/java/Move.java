public class Move {

    private char action;
    private Taxi taxi;

    private Vertex node; // In case of move
    private Customer customer; // In case of drop or pickup

    public Move(char action, Taxi taxi, Vertex node) {
        this.action = action;
        this.taxi = taxi;
        this.node = node;
    }

    public Move(char action, Taxi taxi, Customer customer) {
        this.action = action;
        this.taxi = taxi;
        this.customer = customer;
    }

    public String getString() {
        StringBuilder output = new StringBuilder();
        output.append(action).
                append(" ").
                append(taxi.getOutputId()).
                append(" ");

        if(action == 'm') {
            //Move
            output.append(node.getId());
        } else {
            //Pickup or dropoff
            output.append(customer.getDestination().getId());
        }

        output.append(" ");
        return output.toString();
    }

    public char getAction() {
        return action;
    }

    public Taxi getTaxi() {
        return taxi;
    }

    public Vertex getNode() {
        if(action == 'm') {
            return node;
        } else {
            Main.debug("[ERROR] Requested getNode on Move that is a pickup or dropoff");
            return null;
        }
    }

    public Customer getCustomer() {
        if(action == 'p' || action == 'd') {
            return customer;
        } else {
            Main.debug("[ERROR] Requested getCustomer on Move that is not a pickup or dropoff");
            return null;
        }
    }
}
