package library.ui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import library.data.Identifier;
import library.data.Library;
import library.data.Patron;
import library.data.PatronType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class serves as a controller of the Patrons view defined in the FXML.
 * It handles updating and setting the content relating to the Patron class in the view as well as in the data layer.
 *
 * @author Srikavin Ramkumar
 */
public class Patrons extends DataViewController<Patron> {
    @FXML
    private ChoiceBox<PatronType> patronTypes;
    @FXML
    private TextField lastName;
    @FXML
    private TextField identifier;
    @FXML
    private TextField firstName;
    @FXML
    private Label patronTypesLink;

    /**
     * Used to initialize the table with the necessary columns to display patron objects in it.
     *
     * @param tableView The table to configure with the needed columns
     */
    public static void initializeTable(TableView<Patron> tableView) {
        //Create all columns
        ObservableList<TableColumn<Patron, ?>> columns = tableView.getColumns();

        TableColumn<Patron, String> idColumn = new TableColumn<>("Identifier");
        TableColumn<Patron, String> firstNameColumn = new TableColumn<>("First Name");
        TableColumn<Patron, String> lastNameColumn = new TableColumn<>("Last Name");
        TableColumn<Patron, String> patronTypeColumn = new TableColumn<>("Patron Type");

        idColumn.setCellValueFactory((value) -> new ReadOnlyStringWrapper(value.getValue().getIdentifier().getId()));
        firstNameColumn.setCellValueFactory((value) -> new ReadOnlyStringWrapper(value.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory((value) -> new ReadOnlyStringWrapper(value.getValue().getLastName()));
        patronTypeColumn.setCellValueFactory((value) -> new ReadOnlyStringWrapper(value.getValue().getPatronType().getName()));

        //Add columns to the table
        columns.setAll(idColumn, firstNameColumn, lastNameColumn, patronTypeColumn);
    }

    @FXML
    private void onPatronTypes(MouseEvent event) {
        getInitializer().setContent("PatronTypes.fxml");
    }

    /**
     * {@inheritDoc}
     */
    protected boolean validate() {
        //Set all fields to the valid state to reset error states
        firstName.pseudoClassStateChanged(errorClass, false);
        lastName.pseudoClassStateChanged(errorClass, false);
        patronTypes.pseudoClassStateChanged(errorClass, false);
        identifier.pseudoClassStateChanged(errorClass, false);

        //Get firstname and lastname
        String fName = firstName.getText();
        String lName = lastName.getText();

        //Keep track of the values
        Set<Node> errors = new HashSet<>();

        //Make sure the first name is not empty
        if (fName.isEmpty()) {
            errors.add(firstName);
        }
        //Make sure the last name is not empty
        if (lName.isEmpty()) {
            errors.add(lastName);
        }

        //Make sure the currently selected PatronType is not empty
        if (patronTypes.getSelectionModel().isEmpty()) {
            errors.add(patronTypes);
        }

        //Iterate through all errors and add a red outline to them
        for (Node e : errors) {
            e.pseudoClassStateChanged(errorClass, true);
        }

        //Return true if errors is empty and return false if errors have occurred
        return errors.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupColumns(TableView table) {
        //Initialize the table with default columns
        initializeTable(this.table);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCurrentState(Patron patron) {
        //Set the lastname
        lastName.setText(patron.getLastName());
        //Set the firstname
        firstName.setText(patron.getFirstName());
        //Set the identifier
        identifier.setText(patron.getIdentifier().toString());
        //Set the currently selected PatronType
        patronTypes.getSelectionModel().select(patron.getPatronType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Patron createNewItem(Identifier identifier) {
        List<PatronType> patronTypes = getLibrary().getPatronTypes();
        PatronType patronType = null;
        if (patronTypes.size() > 0) {
            patronType = getLibrary().getPatronTypes().get(0);
        }

        if (patronType == null) {
            patronType = new PatronType(new Identifier(""), "", 0, 0);
        }

        return new Patron(identifier, "", "", patronType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSpotlightFields(SpotlightManager manager) {
        manager.registerSpotlight(patronTypesLink, "Patron Types", "View and manage all patron types. \n" +
                "It is possible to add, create, and delete patrons. " +
                "Patron types can be applied to patrons to " +
                "set book checkout limits and time limits.");
        manager.registerSpotlight(identifier, "Patron Identifier", "The selected patrons's identifier. " +
                "It is autogenerated and cannot be changed.");
        manager.registerSpotlight(patronTypes, "Patron Types", "A patron type can be applied to this " +
                "Patron to impose checkout limits, including a total book limit, and maximum holding time of a book. This " +
                "value must not be left blank.");
        manager.registerSpotlight(firstName, "Patron First Name", "The first name of the selected patron.");
        manager.registerSpotlight(lastName, "Patron Last Name", "The last name of the selected patron.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Predicate<Patron> getFilterPredicate(String filter) {
        return (e) -> {
            String lowerCaseValue = filter.toLowerCase();
            return e.getFirstName().toLowerCase().contains(lowerCaseValue) ||
                    e.getLastName().toLowerCase().contains(lowerCaseValue) ||
                    e.getIdentifier().getId().toLowerCase().contains(lowerCaseValue) ||
                    e.getPatronType().getName().toLowerCase().contains(lowerCaseValue);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Patron> getDataSource() {
        Library library = getLibrary();
        return library.getPatrons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeData() {
        super.initializeData();
        //Set how to display PatronType objects in the UI
        patronTypes.setConverter(new StringConverter<PatronType>() {
            @Override
            public String toString(PatronType type) {
                return type.getName();
            }

            @Override
            public PatronType fromString(String typeString) {
                return getLibrary().getPatronTypeFromName(typeString);
            }
        });

        //Set the available patron types into the select box
        patronTypes.setItems(FXCollections.observableList(getLibrary().getPatronTypes()));
        //Update the table after everything has been added to it
        Platform.runLater(() -> table.refresh());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void update(Patron patron) {
        patron.setFirstName(firstName.getText());
        patron.setLastName(lastName.getText());
        patron.setPatronType(patronTypes.getSelectionModel().getSelectedItem());
    }
}
