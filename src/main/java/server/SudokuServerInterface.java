package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface SudokuServerInterface extends Remote {
    public int[][] generateSudoku(SudokuLevel level) throws RemoteException;
    public HashMap<Integer, Integer> showAnswer() throws RemoteException;
    public boolean checkAnswer(int row, int col, int val) throws RemoteException;
}
