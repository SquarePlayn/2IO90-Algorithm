import java.util.ArrayList;

public class Minute {

    private ArrayList<Call> calls = new ArrayList<>();
    private ArrayList<Move> moves = new ArrayList<>();


    public void addCall(Call call) {
        calls.add(call);
    }

    public void addMove(Move move) {
        moves.add(move);
    }

    public ArrayList<Call> getCalls() {
        return calls;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
    }

    /**
     * Decripts a line of input, stores the calls in the calls list and updates the global CustomerList.
     * @param inputString String straight from input: one line
     * @param sharedData the shared data instance so we can update the customer list and such
     */
    public void setCalls(String inputString, SharedData sharedData) {
        String[] input = inputString.split(" ");
        int amountOfCalls = Integer.parseInt(input[0]);

        for (int i = 0; i < amountOfCalls; i++) {

            //Read in each new customer
            Vertex position = sharedData.getGraph().getVertex(Integer.parseInt(input[i * 2 + 1]));
            Vertex destination = sharedData.getGraph().getVertex(Integer.parseInt(input[i * 2 + 2]));
            Customer customer = new Customer(position, destination);

            //Add the new customer to the customers
            sharedData.getCustomerList().add(customer);

            //Add the new call to the current minute we're constructing
            addCall(new Call(customer, position, destination));
        }

    }
}