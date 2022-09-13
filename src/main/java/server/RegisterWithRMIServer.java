package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegisterWithRMIServer {
    public static void main(String[] args)  {
        try {
            SudokuServerInterface obj = new SudokuServerInterfaceImpl();
            System.out.println("obj created");
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("registry created");
            registry.rebind("SudokuServerInterfaceImpl", obj);
            System.out.println("Student server " + obj + " registered");
            System.out.println("Press Return to quit.");
            int key = System.in.read();
            System.exit(0);
        } catch(Exception ex){

        }
    }
}
