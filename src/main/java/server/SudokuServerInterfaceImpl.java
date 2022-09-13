package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SudokuServerInterfaceImpl extends UnicastRemoteObject implements SudokuServerInterface {
    private int[][] board;
    private int missingValuesNum; // броят празни клетки, които искаме да има
    private int upperBoundMissingValues; // горна граница на празните клетки в ред или колона
    private HashMap<Integer, Integer> correctMissingValues; // индекс на празна клетка -> правилната стойност на тази клетка

    public void setBoard(int[][] board) {
        this.board = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                this.board[i][j] = board[i][j];
            }
        }
    }

    public void setMissingValuesNum(int missingValuesNum) {
        this.missingValuesNum = missingValuesNum < 0 ? 32 : missingValuesNum;
    }

    public void setUpperBoundMissingValues(int upperBoundMissingValues) {
        this.upperBoundMissingValues = upperBoundMissingValues < 0 ? 5 : upperBoundMissingValues;
    }

    public void setCorrectMissingValues(HashMap<Integer, Integer> correctMissingValues) {
        this.correctMissingValues = correctMissingValues;
    }

    public SudokuServerInterfaceImpl() throws RemoteException {
        board = new int[9][9];
        correctMissingValues = new HashMap<>();
    }

    public SudokuServerInterfaceImpl(int[][] board, int missingValuesNum, int upperBoundMissingValues, HashMap<Integer, Integer> correctMissingValues) throws RemoteException {
        setBoard(board);
        setMissingValuesNum(missingValuesNum);
        setUpperBoundMissingValues(upperBoundMissingValues);
        setCorrectMissingValues(correctMissingValues);
    }

    public SudokuServerInterfaceImpl(SudokuServerInterfaceImpl src) throws RemoteException{
        this(src.board, src.missingValuesNum, src.upperBoundMissingValues, src.correctMissingValues);
    }

    // проверява дали value я има вече в ред row
    private boolean checkRowHasValue(int row, int value){
        for (int j = 0; j < 9; j++) {
            if(board[row][j] == value)
                return true;
        }
        return false;
    }

    // проверява дали value я има вече в колона col
    private boolean checkColumnHasValue(int col, int value){
        for (int i = 0; i < 9; i++) {
            if(board[i][col] == value)
                return true;
        }
        return false;
    }

    // проверява дали value я има вече в квадрата, който съдържа клетката [row][col]
    private boolean checkSquareHasValue(int row, int col, int value){
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for(int i = startRow ; i < startRow + 3 ; i++){
            for(int j = startCol ; j < startCol + 3 ; j++){
                if(board[i][j] == value)
                    return true;
            }
        }
        return false;
    }

    // проверява дали value може да се сложи в клетката [row][col] без да наруши правилата
    private boolean isValidValue(int row, int col, int value){
        return !(checkRowHasValue(row, value) || checkColumnHasValue(col, value) || checkSquareHasValue(row, col, value));
    }

    // проверява дали всички клетки са запълнени
    private boolean isFull(){
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if(board[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    // опитва се да реши судоку със стойностите на board. Ако успее връща true, иначе връща false. Във всички случаи променя board
    private boolean solve(){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (isFull())
                    return true;
                if(board[i][j] == 0){
                    for(int value = 1 ; value <=9 ; value++){
                        if(isValidValue(i, j, value)) {
                            board[i][j] = value;
                            if (solve())
                                return true;
                            else
                                board[i][j] = 0;
                        }
                    }
                    board[i][j] = 0;
                    return false;
                }
            }
        }
        return false;
    }

    // запълва произволно квадрата с начална клетка горе вдясно [startI][startJ] с 1-9
    private void fillSquare(int startI, int startJ){
        int counter = 0;
        List<Integer> randomValues = IntStream.range(1, 10).boxed().collect(Collectors.toList());
        Collections.shuffle(randomValues);
        for (int i = startI; i < startI + 3; i++) {
            for (int j = startJ; j < startJ + 3; j++) {
                board[i][j] = randomValues.get(counter);
                counter++;
                if (counter > 8)
                    return;
            }
        }
    }

    // генерира пълна дъска, която отговаря на правилата
    private void generateBoard(){
        fillSquare(0, 0);
        fillSquare(3, 3);
        fillSquare(6, 6);
        solve();
    }

    // проверява дали, ако клетката [row][col] се нулира, съдокуто ще има единствено решение
    private boolean checkUniqueSolution(int row, int col, int value){
        try{
            SudokuServerInterfaceImpl copy;
            for (int i = 1; i <= 9 ; i++) {
                if(i != value){
                    copy = new SudokuServerInterfaceImpl(this);
                    if(copy.isValidValue(row, col, i)) {
                        copy.board[row][col] = i;
                        if (copy.solve())
                            return false;
                    }
                }
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    // нулира missingValuesNum броя клетки, така че полученото судоку да има единствено решение. Запълва correctMissingValues
    private void digHoles(){
        int[] missingValuesInRow, missingValuesInColumn;
        int digedCellsNum;
        List<Integer> digableCells;

        missingValuesInRow = new int[9];
        missingValuesInColumn = new int[9];
        digedCellsNum = 0;

        digableCells = IntStream.range(0, 81).boxed().collect(Collectors.toList());
        Collections.shuffle(digableCells);

        while(digedCellsNum < missingValuesNum){
            int cellIndex = digableCells.remove(0);
            int row = cellIndex / 9;
            int col = cellIndex % 9;
            if(missingValuesInRow[row] < upperBoundMissingValues && missingValuesInColumn[col] < upperBoundMissingValues){
                if(digedCellsNum == 0 || checkUniqueSolution(row, col, board[row][col])){
                    correctMissingValues.put(cellIndex, board[row][col]);
                    board[row][col] = 0;
                    missingValuesInRow[row]++;
                    missingValuesInColumn[col]++;
                    digedCellsNum++;
                }
            }
        }

    }

    public void print(){
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(board[i][j] + " | ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        try {
            SudokuServerInterfaceImpl s = new SudokuServerInterfaceImpl();
            s.generateBoard();
            s.missingValuesNum = 35;
            s.upperBoundMissingValues = 5;
            s.digHoles();
            s.print();
            System.exit(0);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int[][] generateSudoku(SudokuLevel level) throws RemoteException {
        setBoard(new int[9][9]);
        setCorrectMissingValues(new HashMap<>());
        setUpperBoundMissingValues(level.missingValuesUpperBound);

        Random rand = new Random();
        setMissingValuesNum(rand.nextInt(level.missingValuesNumMax + 1 - level.missingValuesNumMin) + level.missingValuesNumMin);

        generateBoard();
        digHoles();
        return board;
    }

    @Override
    public HashMap<Integer, Integer> showAnswer() throws RemoteException {
        return correctMissingValues;
    }

    @Override
    public boolean checkAnswer(int row, int col, int value) throws RemoteException {
        return correctMissingValues.get(row * 9 + col) == value;
    }
}
