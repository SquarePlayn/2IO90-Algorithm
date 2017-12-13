public enum AlgorithmType {

    SIMPLEQUEUE() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_SimpleQueue();
        }
    },

    LCC() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_LCC();
        }
    };

    protected Algorithm algorithm;

    AlgorithmType() {
        this.reset();
    }

    public abstract void reset();

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
