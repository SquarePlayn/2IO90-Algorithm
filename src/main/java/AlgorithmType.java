public enum AlgorithmType {
    SIMPLEQUEUE (new Algorithm_SimpleQueue());

    private Algorithm algorithm;

    AlgorithmType(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
