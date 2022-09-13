module sudoku.sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;


    opens server;
    exports server;

    opens client;
    exports client;

}