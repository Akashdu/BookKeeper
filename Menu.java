package library.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.data.Library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Implements the controller for the FileMenu. This controller is responsible for delegating calls to the
 * {@link FXInitializer} and the {@link Library} object. Handles all of the operations within the file menu, including
 * File, Preferences, and Help
 * @author Srikavin Ramkumar
 */
public class Menu extends BaseController {
    @FXML
    private CheckMenuItem useAnimations;
    private boolean isTempData = true;

    /**
     * Initializes the menu with the specified stage. This stage is used to detect close requests.
     *
     * @param stage The main window stage
     */
    public Menu(Stage stage) {
        stage.setOnCloseRequest(this::quit);
    }

    /**
     * {@inheritDoc}
     * Used to initialize default values to the existing preferences
     */
    public void initialize(FXInitializer initializer, Library library) {
        super.initialize(initializer, library);
        //When the user tries to close the window, make sure they intended to not save any changes
        super.initializeData();
        //Update use of animations when the option is toggled
        useAnimations.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getInitializer().getPreferenceManager().setValue("use_transitions", newValue);
            //clear loaded cache to prevent blank screens from showing
            getInitializer().resetCache();
        });
        useAnimations.setSelected(getInitializer().getPreferenceManager()
                .getValueAsBoolean("use_transitions", true));
    }

    @FXML
    private void about(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About BookKeeper");
        alert.setHeaderText("About");
        alert.setContentText("Made by Srikavin Ramkumar for FBLA's Coding and Programming Competition.");
        alert.showAndWait();
    }

    @FXML
    private void documentation(ActionEvent event) {
        getInitializer().loadHelp();
    }

    @FXML
    private void editSchoolName(ActionEvent event) {
        String currentSchoolName = getInitializer().getPreferenceManager().getValue("school_name", "Robinson High School");

        TextInputDialog dialog = new TextInputDialog(currentSchoolName);
        dialog.setTitle("Change School Name");
        dialog.setHeaderText("Change School Name");
        dialog.setContentText("Please enter your desired school name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(chosenName -> {
            getInitializer().getPreferenceManager().setValue("school_name", chosenName);
            getInitializer().setContent("MainWindow.fxml");
        });

    }

    @FXML
    private void open(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Paths.get("").toAbsolutePath().toFile());
        directoryChooser.setTitle("Open library data file");

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        File file = directoryChooser.showDialog(stage);

        if (file == null) {
            return;
        }
        Path path = file.toPath();
        unsavedChanges(() -> {
            try {
                isTempData = false;
                getInitializer().loadDataFile(path);
            } catch (IOException e) {
                showError("opening the data file", e);
            }
        });
    }

    private void unsavedChanges(Runnable quitCallback) {
        Library library = getLibrary();
        if (library.isModified()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Unsaved changes");
            alert.setHeaderText("Unsaved changes exist");
            alert.setContentText("Warning! All unsaved data will be lost if you continue without saving.");

            ButtonType quit = new ButtonType("Don't Save", ButtonBar.ButtonData.RIGHT);
            ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.RIGHT);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.LEFT);

            alert.getButtonTypes().setAll(quit, save, cancel);

            //Get the result of the alert dialog
            Optional<ButtonType> result = alert.showAndWait();

            //If the user chooses the quit button
            if (result.isPresent() && result.get() == quit) {
                //Exit if the user chooses quit
                quitCallback.run();
            }
            //If the user chooses the save button
            if (result.isPresent() && result.get() == save) {
                //Reuse the save method
                save(null);
            }
        } else {
            //If the library was not modified at all, simply run the quit callback
            quitCallback.run();
        }
    }

    @FXML
    private void quit(Event event) {
        //If the users clicks quit, exit
        unsavedChanges(Platform::exit);
        //Otherwise, ignore the event
        event.consume();
    }

    @FXML
    private void newLibrary(ActionEvent event) {
        unsavedChanges(() -> {
            try {
                isTempData = true;
                getInitializer().loadDataFile(null);
            } catch (IOException e) {
                //Display an error message if an exception occurs when saving
                showError("creating a new library", e);
            }
        });
    }

    private void showError(String error, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Request");
        alert.setHeaderText("There was an error in " + error + "!");
        alert.setContentText(e.getLocalizedMessage());
        alert.showAndWait();
    }

    @FXML
    private void saveAs(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Paths.get("").toAbsolutePath().toFile());
        directoryChooser.setTitle("Save As...");

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        File file = directoryChooser.showDialog(stage);

        if (file == null) {
            return;
        }
        Path path = file.toPath();

        FXInitializer initializer = getInitializer();
        try {
            initializer.saveDataFileTo(path);
        } catch (IOException e) {
            showError("saving the library", e);
        }
        isTempData = false;
    }

    @FXML
    private void save(ActionEvent event) {
        try {
            if (!isTempData) {
                FXInitializer initializer = getInitializer();
                initializer.saveDataFile();
            } else {
                //Show the save as dialog if the user tries to save the sample data
                saveAs(event);
            }
        } catch (IOException e) {
            //Display an error message if an exception occurs when saving
            showError("saving the library", e);
        }
    }

    @FXML
    private void loadSampleData(ActionEvent event) {
        try {
            isTempData = true;
            Path temp = Files.createTempDirectory("sampleData");
            Files.copy(getClass().getResourceAsStream("data.txt"), temp.resolve("data.txt"));
            getInitializer().loadDataFile(temp);
            initializeData();
        } catch (IOException e) {
            showError("loading sample data", e);
        }
    }
}
