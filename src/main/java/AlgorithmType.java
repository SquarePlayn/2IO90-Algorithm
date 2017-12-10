public enum AlgorithmType {

    SIMPLEQUEUE() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_SimpleQueue();
        }
    },

    GCC() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_GCC();
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
