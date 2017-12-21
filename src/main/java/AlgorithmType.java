public enum AlgorithmType {

    SIMPLEQUEUE() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_SimpleQueue();
        }
    },

    LSD() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_LSD();
        }
    },

    HUNGARIAN() {
        @Override
        public void reset() {
            this.algorithm = new Algorithm_Hungarian();
        }
    },

    ;

    protected Algorithm algorithm;

    AlgorithmType() {
        this.reset();
    }

    public abstract void reset();

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
