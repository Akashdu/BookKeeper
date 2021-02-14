package library;

import javafx.application.Application;
import library.ui.FXInitializer;

/**
 * The starting point of the application
 *
 * @author Srikavin Ramkumar
 */
public class Main {
    /**
     * The application entry point -- this is where the application will begin.
     *
     * @param args Any command line arguments that are passed to the program
     */
    public static void main(String[] args) {
        //Start the JavaFX GUI
        Application.launch(FXInitializer.class);
    }
}
