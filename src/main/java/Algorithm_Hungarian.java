import java.util.*;

public class Algorithm_Hungarian extends Algorithm {

    private ArrayList<Customer> customerQueue;
    private ArrayList<Taxi> taxiReadyQueue;
    private HashSet<Taxi> taxiInOperationList;

    @Override
    public void readMinute(ArrayList<Call> calls) {
        for(Call call : calls) {
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

    public static long time_process_minute = 0;
    public static long time_match_entities = 0;
    public static long time_apply_hungarian = 0;

    public static long time_new_ha_class_instance = 0;
    public static long time_execute_hungarian = 0;

    public static long time_reduce = 0;
    public static long time_compute_initial = 0;
    public static long time_greedy_match = 0;
    public static long time_rest_of_loop = 0;

    @Override
    public ArrayList<Move> processMinute(boolean callsLeft) {

        long start = System.nanoTime();

        ArrayList<Move> output = new ArrayList<>();

        // First check whether a taxi has already arrived at the destination of the customer it wants to pick up.
        // If that is the case it should be removed from the queue, so it won't get used by the Hungarian algo.

        for(Taxi taxi : taxiInOperationList) {
            if(taxi.getInOperation()
                    && taxi.getPosition().equals(taxi.getCustomer().getPosition())
                    && !taxi.getCustomer().hasBeenPickedUp()
                    && taxi.getPassengers().isEmpty()) {

                taxiReadyQueue.remove(taxi);
                customerQueue.remove(taxi.getCustomer());
                // Clear its path, so the advanceTaxi knows we have to pick up a customer.
                taxi.getPath().clear();
            }
        }

        long start2 = System.nanoTime();

        if(!customerQueue.isEmpty() && !taxiReadyQueue.isEmpty()) {
            long start3 = System.nanoTime();
            HashMap<Taxi, Customer> hungOut = applyHungarian();

            this.time_apply_hungarian += System.nanoTime() - start3;

            // Bind all customers to taxis returned by the Hungarian Algorithm.
            for(Map.Entry<Taxi, Customer> entry : hungOut.entrySet()) {
                Taxi taxi = entry.getKey();
                Customer customer = entry.getValue();

                taxiInOperationList.add(taxi);

                taxi.setCustomer(customer);
                taxi.setPath(sharedData.getGraph().getShortestPath(taxi.getPosition(), customer.getPosition()));
                taxi.setInOperation(true);
            }
        }

        this.time_match_entities += System.nanoTime() - start2;

        // Advance all taxis that have an operation
        Iterator<Taxi> it = taxiInOperationList.iterator();
        while(it.hasNext()) {
            Taxi taxi = it.next();

            if(!taxi.getInOperation()) {
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

        this.time_process_minute += System.nanoTime() - start;

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

    /**
     * Applies the Hungarian Algorithm on the current taxi and customer queue.
     *
     * @return A HashMap containing the best taxi/customer combinations.
     */
    private HashMap<Taxi, Customer> applyHungarian() {
        HashMap<Taxi, Customer> output = new HashMap<>(taxiReadyQueue.size());
        double[][] costMatrix = new double[taxiReadyQueue.size()][customerQueue.size()];

        // Calculate the cost matrix, using distances to the customer positions.
        for(int t = 0; t < taxiReadyQueue.size(); t++) {
            for(int c = 0; c < customerQueue.size(); c++) {
                Taxi taxi = taxiReadyQueue.get(t);
                Customer customer = customerQueue.get(c);

                costMatrix[t][c] = taxi.getPosition().getDistanceTo(customer.getPosition());
            }
        }



        // Execute the Hungarian Algorithm.
        long start6 = System.nanoTime();
        HungarianAlgorithm ha = new HungarianAlgorithm(costMatrix);
        this.time_new_ha_class_instance += System.nanoTime() - start6;

        long start5 = System.nanoTime();
        int[] result = ha.execute();
        this.time_execute_hungarian += System.nanoTime() - start5;

        // Analyse the result and populate the resulting HashMap.
        for(int t = 0; t < result.length; t++) {
            Taxi taxi = taxiReadyQueue.get(t);
            int c = result[t];

            if(c == -1) {
                // This taxi does not have a task, make sure it's not in operation anymore in case it was before.
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
        if(taxi.getInOperation()) {
            if(!taxi.getPath().isEmpty()) {

                //We are still driving. Advance to next spot
                output.add(new Move(taxi, taxi.getPath().remove(0)));
            } else {
                if(taxi.getPassengers().isEmpty()) {
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

        for(Move move : moves) {
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

        for(int i = lastUpdatedMinute + 1; i < uptoMinute; i++) {
            Minute minute = sharedData.getIOHistory().getMinute(i);
            readMinute(minute.getCalls());
            processMoves(minute.getMoves());
            lastUpdatedMinute++;
        }

        // TODO Make sure amount of passengers is maximum one.
    }

    /**
     * An implementation of the Hungarian algorithm for solving the assignment
     * problem. An instance of the assignment problem consists of a number of
     * workers along with a number of jobs and a cost matrix which gives the cost of
     * assigning the i'th worker to the j'th job at position (i, j). The goal is to
     * find an assignment of workers to jobs so that no job is assigned more than
     * one worker and so that no worker is assigned to more than one job in such a
     * manner so as to minimize the total cost of completing the jobs.
     * <p>
     *
     * An assignment for a cost matrix that has more workers than jobs will
     * necessarily include unassigned workers, indicated by an assignment value of
     * -1; in no other circumstance will there be unassigned workers. Similarly, an
     * assignment for a cost matrix that has more jobs than workers will necessarily
     * include unassigned jobs; in no other circumstance will there be unassigned
     * jobs. For completeness, an assignment for a square cost matrix will give
     * exactly one unique worker to each job.
     * <p>
     *
     * This version of the Hungarian algorithm runs in time O(n^3), where n is the
     * maximum among the number of workers and the number of jobs.
     *
     * @author Kevin L. Stern
     */
    public static class HungarianAlgorithm {
        private final double[][] costMatrix;
        private final int rows, cols, dim;
        private final double[] labelByWorker, labelByJob;
        private final int[] minSlackWorkerByJob;
        private final double[] minSlackValueByJob;
        private final int[] matchJobByWorker, matchWorkerByJob;
        private final int[] parentWorkerByCommittedJob;
        private final boolean[] committedWorkers;

        /**
         * Construct an instance of the algorithm.
         *
         * @param costMatrix
         *          the cost matrix, where matrix[i][j] holds the cost of assigning
         *          worker i to job j, for all i, j. The cost matrix must not be
         *          irregular in the sense that all rows must be the same length; in
         *          addition, all entries must be non-infinite numbers.
         */
        public HungarianAlgorithm(double[][] costMatrix) {
            this.dim = Math.max(costMatrix.length, costMatrix[0].length);
            this.rows = costMatrix.length;
            this.cols = costMatrix[0].length;
            this.costMatrix = new double[this.dim][this.dim];
            for (int w = 0; w < this.dim; w++) {
                if (w < costMatrix.length) {
                    if (costMatrix[w].length != this.cols) {
                        throw new IllegalArgumentException("Irregular cost matrix");
                    }
                    for (int j = 0; j < this.cols; j++) {
                        if (Double.isInfinite(costMatrix[w][j])) {
                            throw new IllegalArgumentException("Infinite cost");
                        }
                        if (Double.isNaN(costMatrix[w][j])) {
                            throw new IllegalArgumentException("NaN cost");
                        }
                    }
                    this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
                } else {
                    this.costMatrix[w] = new double[this.dim];
                }
            }
            labelByWorker = new double[this.dim];
            labelByJob = new double[this.dim];
            minSlackWorkerByJob = new int[this.dim];
            minSlackValueByJob = new double[this.dim];
            committedWorkers = new boolean[this.dim];
            parentWorkerByCommittedJob = new int[this.dim];
            matchJobByWorker = new int[this.dim];
            Arrays.fill(matchJobByWorker, -1);
            matchWorkerByJob = new int[this.dim];
            Arrays.fill(matchWorkerByJob, -1);
        }

        /**
         * Compute an initial feasible solution by assigning zero labels to the
         * workers and by assigning to each job a label equal to the minimum cost
         * among its incident edges.
         */
        private void computeInitialFeasibleSolution() {
            for (int j = 0; j < dim; j++) {
                labelByJob[j] = Double.POSITIVE_INFINITY;
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < labelByJob[j]) {
                        labelByJob[j] = costMatrix[w][j];
                    }
                }
            }
        }

        /**
         * Execute the algorithm.
         *
         * @return the minimum cost matching of workers to jobs based upon the
         *         provided cost matrix. A matching value of -1 indicates that the
         *         corresponding worker is unassigned.
         */
        public int[] execute() {
            /*
             * Heuristics to improve performance: Reduce rows and columns by their
             * smallest element, compute an initial non-zero dual feasible solution and
             * create a greedy matching from workers to jobs of the cost matrix.
             */
            long startA = System.nanoTime();
            reduce();
            Algorithm_Hungarian.time_reduce += System.nanoTime() - startA;

            long startB = System.nanoTime();
            computeInitialFeasibleSolution();
            Algorithm_Hungarian.time_compute_initial += System.nanoTime() - startB;

            long startC = System.nanoTime();
            greedyMatch();
            Algorithm_Hungarian.time_greedy_match += System.nanoTime() - startC;

            long startD = System.nanoTime();

            int w = fetchUnmatchedWorker();
            while (w < dim) {
                initializePhase(w);
                executePhase();
                w = fetchUnmatchedWorker();
            }
            int[] result = Arrays.copyOf(matchJobByWorker, rows);
            for (w = 0; w < result.length; w++) {
                if (result[w] >= cols) {
                    result[w] = -1;
                }
            }

            Algorithm_Hungarian.time_rest_of_loop += System.nanoTime() - startD;
            return result;
        }

        /**
         * Execute a single phase of the algorithm. A phase of the Hungarian algorithm
         * consists of building a set of committed workers and a set of committed jobs
         * from a root unmatched worker by following alternating unmatched/matched
         * zero-slack edges. If an unmatched job is encountered, then an augmenting
         * path has been found and the matching is grown. If the connected zero-slack
         * edges have been exhausted, the labels of committed workers are increased by
         * the minimum slack among committed workers and non-committed jobs to create
         * more zero-slack edges (the labels of committed jobs are simultaneously
         * decreased by the same amount in order to maintain a feasible labeling).
         * <p>
         *
         * The runtime of a single phase of the algorithm is O(n^2), where n is the
         * dimension of the internal square cost matrix, since each edge is visited at
         * most once and since increasing the labeling is accomplished in time O(n) by
         * maintaining the minimum slack values among non-committed jobs. When a phase
         * completes, the matching will have increased in size.
         */
        private void executePhase() {
            while (true) {
                int minSlackWorker = -1, minSlackJob = -1;
                double minSlackValue = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dim; j++) {
                    if (parentWorkerByCommittedJob[j] == -1) {
                        if (minSlackValueByJob[j] < minSlackValue) {
                            minSlackValue = minSlackValueByJob[j];
                            minSlackWorker = minSlackWorkerByJob[j];
                            minSlackJob = j;
                        }
                    }
                }
                if (minSlackValue > 0) {
                    updateLabeling(minSlackValue);
                }
                parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
                if (matchWorkerByJob[minSlackJob] == -1) {
                    /*
                     * An augmenting path has been found.
                     */
                    int committedJob = minSlackJob;
                    int parentWorker = parentWorkerByCommittedJob[committedJob];
                    while (true) {
                        int temp = matchJobByWorker[parentWorker];
                        match(parentWorker, committedJob);
                        committedJob = temp;
                        if (committedJob == -1) {
                            break;
                        }
                        parentWorker = parentWorkerByCommittedJob[committedJob];
                    }
                    return;
                } else {
                    /*
                     * Update slack values since we increased the size of the committed
                     * workers set.
                     */
                    int worker = matchWorkerByJob[minSlackJob];
                    committedWorkers[worker] = true;
                    for (int j = 0; j < dim; j++) {
                        if (parentWorkerByCommittedJob[j] == -1) {
                            double slack = costMatrix[worker][j] - labelByWorker[worker]
                                    - labelByJob[j];
                            if (minSlackValueByJob[j] > slack) {
                                minSlackValueByJob[j] = slack;
                                minSlackWorkerByJob[j] = worker;
                            }
                        }
                    }
                }
            }
        }

        /**
         *
         * @return the first unmatched worker or {@link #dim} if none.
         */
        private int fetchUnmatchedWorker() {
            int w;
            for (w = 0; w < dim; w++) {
                if (matchJobByWorker[w] == -1) {
                    break;
                }
            }
            return w;
        }

        /**
         * Find a valid matching by greedily selecting among zero-cost matchings. This
         * is a heuristic to jump-start the augmentation algorithm.
         */
        private void greedyMatch() {
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (matchJobByWorker[w] == -1 && matchWorkerByJob[j] == -1
                            && costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0) {
                        match(w, j);
                    }
                }
            }
        }

        /**
         * Initialize the next phase of the algorithm by clearing the committed
         * workers and jobs sets and by initializing the slack arrays to the values
         * corresponding to the specified root worker.
         *
         * @param w
         *          the worker at which to root the next phase.
         */
        private void initializePhase(int w) {
            Arrays.fill(committedWorkers, false);
            Arrays.fill(parentWorkerByCommittedJob, -1);
            committedWorkers[w] = true;
            for (int j = 0; j < dim; j++) {
                minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w]
                        - labelByJob[j];
                minSlackWorkerByJob[j] = w;
            }
        }

        /**
         * Helper method to record a matching between worker w and job j.
         */
        private void match(int w, int j) {
            matchJobByWorker[w] = j;
            matchWorkerByJob[j] = w;
        }

        /**
         * Reduce the cost matrix by subtracting the smallest element of each row from
         * all elements of the row as well as the smallest element of each column from
         * all elements of the column. Note that an optimal assignment for a reduced
         * cost matrix is optimal for the original cost matrix.
         */
        private void reduce() {
            for (int w = 0; w < dim; w++) {
                double min = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < min) {
                        min = costMatrix[w][j];
                    }
                }
                for (int j = 0; j < dim; j++) {
                    costMatrix[w][j] -= min;
                }
            }
            double[] min = new double[dim];
            for (int j = 0; j < dim; j++) {
                min[j] = Double.POSITIVE_INFINITY;
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < min[j]) {
                        min[j] = costMatrix[w][j];
                    }
                }
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    costMatrix[w][j] -= min[j];
                }
            }
        }

        /**
         * Update labels with the specified slack by adding the slack value for
         * committed workers and by subtracting the slack value for committed jobs. In
         * addition, update the minimum slack values appropriately.
         */
        private void updateLabeling(double slack) {
            for (int w = 0; w < dim; w++) {
                if (committedWorkers[w]) {
                    labelByWorker[w] += slack;
                }
            }
            for (int j = 0; j < dim; j++) {
                if (parentWorkerByCommittedJob[j] != -1) {
                    labelByJob[j] -= slack;
                } else {
                    minSlackValueByJob[j] -= slack;
                }
            }
        }
    }
}
