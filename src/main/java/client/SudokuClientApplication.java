package client;

import javafx.application.Application;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.SudokuLevel;
import server.SudokuServerInterface;
import server.SudokuServerInterfaceImpl;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.function.UnaryOperator;

public class SudokuClientApplication extends Application {
    private SudokuServerInterface sudokuServer;

    private TextField[][] sudokuGrid;
    private MenuButton btnStartSudoku;
    private Button btnShowAnswer;
    private Label lblEndGame;


    protected void initializeRMI(){
        String host = "localhost";
        try{
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            sudokuServer = (SudokuServerInterface) registry.lookup("SudokuServerInterfaceImpl");
            System.out.println("server object" + sudokuServer + " found");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean isReady(){
        try{
            HashMap<Integer, Integer> answers = sudokuServer.showAnswer();
            for(int index : answers.keySet()){
                int row = index / 9;
                int col = index % 9;
                if(sudokuGrid[row][col].isEditable())
                    return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private UnaryOperator<TextFormatter.Change> createFilter(int i, int j) {
        final int row = i;
        final int col = j;
        return change -> {
            String input = change.getText();
            String oldValue = change.getControlText();
            if(!oldValue.isEmpty() && !change.isDeleted())
                return null;
            if (change.isDeleted())
                return change;
            if (input.matches("[1-9]") || input.isEmpty()) {
                if(!input.isEmpty()){
                    boolean isCorrect = false;
                    try {
                        isCorrect = sudokuServer.checkAnswer(row, col, Integer.parseInt(input));
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    if(!isCorrect){
                        sudokuGrid[row][col].setStyle(sudokuGrid[row][col].getStyle() + "-fx-text-fill:red;");
                    } else{
                        sudokuGrid[row][col].setStyle(sudokuGrid[row][col].getStyle() + "-fx-text-fill:black;");
                        sudokuGrid[row][col].setEditable(false);
                        if(isReady()){
                            endGame();
                        }
                    }
                }

                return change;
            }
            return null;
        };
    }

    //зарежда ново судоку от сървъра
    private void startGame(SudokuLevel level){
        try {
            int[][] board = sudokuServer.generateSudoku(level);
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if(board[i][j] == 0){

                        sudokuGrid[i][j].setTextFormatter(new TextFormatter<>(createFilter(i, j)));
                        sudokuGrid[i][j].setText("");
                        sudokuGrid[i][j].setEditable(true);
                    }
                    else{
                        sudokuGrid[i][j].setText(String.valueOf(board[i][j]));
                        sudokuGrid[i][j].setStyle(sudokuGrid[i][j].getStyle() + "-fx-text-fill: darkblue; ");
                    }
                }
            }

            lblEndGame.setVisible(false);
            btnStartSudoku.setVisible(false);
            btnShowAnswer.setVisible(true);



        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void endGame(){
        lblEndGame.setVisible(true);
        lblEndGame.setStyle("-fx-background-color: green; -fx-font-size: 20; -fx-text-alignment: center");
        btnStartSudoku.setVisible(true);
        btnShowAnswer.setVisible(false);
    }

    private void showAnswer(Event e){
        try{
            HashMap<Integer, Integer> answers = sudokuServer.showAnswer();
            answers.forEach((index, value) -> {
                int row = index / 9;
                int col = index % 9;
                sudokuGrid[row][col].setTextFormatter(null);
                sudokuGrid[row][col].setText(String.valueOf(value));
                sudokuGrid[row][col].setStyle(sudokuGrid[row][col].getStyle() + "-fx-text-fill: black;");
                sudokuGrid[row][col].setEditable(false);
            });

            lblEndGame.setVisible(true);
            lblEndGame.setStyle("-fx-background-color: red; -fx-font-size: 20; -fx-text-alignment: center");
            btnShowAnswer.setVisible(false);
            btnStartSudoku.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws RemoteException {
        sudokuServer = new SudokuServerInterfaceImpl();
        sudokuGrid = new TextField[9][9];
        btnStartSudoku = new MenuButton();
        lblEndGame = new Label("End of game");


        HBox hbox = new HBox();

        GridPane gridPaneSudoku = new GridPane();


        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudokuGrid[i][j] = new TextField("");
                sudokuGrid[i][j].setPrefWidth(50);
                sudokuGrid[i][j].setPrefHeight(50);
                sudokuGrid[i][j].setStyle("-fx-font-size: 25px; -fx-text-alignment: center; -fx-border-color: black;");
                if((i >= 3 && i <= 5 && (j <= 2 || j >=6)) || (j >= 3 && j <= 5 && (i <= 2 || i >= 6))){
                    sudokuGrid[i][j].setStyle(sudokuGrid[i][j].getStyle() + "-fx-background-color: #dad7d7;");
                }
                sudokuGrid[i][j].setEditable(false);

                gridPaneSudoku.add(sudokuGrid[i][j], i, j);

            }
        }

        VBox vBoxButtons = new VBox();

        MenuItem easySudoku = new MenuItem("Easy");
        easySudoku.setOnAction(e -> startGame(SudokuLevel.Easy));
        MenuItem mediumSudoku = new MenuItem("Medium");
        mediumSudoku.setOnAction(e -> startGame(SudokuLevel.Medium));
        MenuItem hardSudoku = new MenuItem("Hard");
        hardSudoku.setOnAction(e -> startGame(SudokuLevel.Hard));
        btnStartSudoku = new MenuButton("New Game", null, easySudoku, mediumSudoku, hardSudoku);
        vBoxButtons.getChildren().add(btnStartSudoku);

        btnShowAnswer = new Button("Give up");
        btnShowAnswer.setOnAction(this::showAnswer);
        btnShowAnswer.setVisible(false);
        vBoxButtons.getChildren().add(btnShowAnswer);

        vBoxButtons.getChildren().add(lblEndGame);
        lblEndGame.setVisible(false);

        Button btnExit = new Button("Exit");
        btnExit.setOnAction(actionEvent -> System.exit(0));
        vBoxButtons.getChildren().add(btnExit);

        hbox.getChildren().addAll(gridPaneSudoku, vBoxButtons);
        Scene scene= new Scene(hbox, 600, 500);
        initializeRMI();
        stage.setTitle("Sudoku");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}