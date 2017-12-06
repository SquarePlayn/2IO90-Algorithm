public enum AlgorithmType {
    SIMPLEQUEUE (new Algorithm_SimpleQueue()),
    GCC (new Algorithm_GCC());

    private Algorithm algorithm;

    AlgorithmType(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
