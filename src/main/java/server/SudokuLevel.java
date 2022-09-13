package server;

public enum SudokuLevel {
    Easy(32, 45, 5),
    Medium(46, 49, 6),
    Hard(50, 53, 7);

    public final int missingValuesNumMin;
    public final int missingValuesNumMax;
    public final int missingValuesUpperBound;

    private SudokuLevel(int missingValuesNumMin, int missingValuesNumMax, int missingValuesUpperBound){
        this.missingValuesNumMin = missingValuesNumMin;
        this.missingValuesNumMax = missingValuesNumMax;
        this.missingValuesUpperBound = missingValuesUpperBound;
    }
}
