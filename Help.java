package library.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The controller for the help class. This controller is responsible for loading the information from the help file into
 * the list view and the web view. Includes implementation for comments with '#' as the first character in the line.
 *
 * @author Srikavin Ramkumar
 */
public class Help extends BaseController {
    @FXML
    private ListView<String> helpList;
    @FXML
    private WebView helpWebView;

    private Map<String, String> categoryFileMap = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeData() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Help.class.getResourceAsStream("help/help.txt")))) {

            ObservableList<String> list = helpList.getItems();


            reader.lines()
                    // Filter out lines starting with the comment string
                    .filter((e) -> !e.startsWith("#"))
                    .forEach((e) -> {

                        //Split the string on '---' to separate the name and the html filename
                        String[] split = e.split("---");

                        //Load the data into the category-file map
                        categoryFileMap.put(split[0].trim(), split[1].trim());

                        //Add the category name to the list
                        list.add(split[0].trim());
                    });
        } catch (IOException e) {
            //Display an error message after catching an exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading help files!");
            alert.setContentText(e.getLocalizedMessage());

            alert.showAndWait();
            e.printStackTrace();
        }

        //On selecting the list, load the specified html page
        helpList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            WebEngine engine = helpWebView.getEngine();
            engine.load(Help.class.getResource("help/" + categoryFileMap.get(newValue)).toExternalForm());
        });
    }
}
