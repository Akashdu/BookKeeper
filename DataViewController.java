package library.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import library.data.Identifier;
import library.data.LibraryData;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * This is an abstract class for controllers with {@link TableView}s in it.
 *
 * @param <T> The data type of the records represented by this controller.
 * @author Srikavin Ramkumar
 */
public abstract class DataViewController<T extends LibraryData> extends BaseController implements Initializable {
    /**
     * The table that contains the data of this controller
     */
    @FXML
    protected TableView<T> table;
    /**
     * The original datasource of this object
     */
    @FXML
    protected ObservableList<T> dataSource;
    /**
     * The filtered list that wraps the original datasource
     * Only displays items that match the predicate
     */
    @FXML
    protected FilteredList<T> filteredList;
    /**
     * The sorted list that wraps the filtered list
     * Displays items in the specified sort order
     */
    @FXML
    protected SortedList<T> sortedList;
    /**
     * The textfield that users can enter a filter into
     */
    @FXML
    protected TextField filter;

    @FXML
    private Button newItemButton;
    @FXML
    private Button deleteItemButton;
    @FXML
    private Button updateItemButton;
    private T currentlyCreating;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSpotlightItems(SpotlightManager manager) {
        manager.registerSpotlight(filter, "Filter", "Can be used to search the records based on entered keywords.");
        manager.registerSpotlight(table, "Records", "View the current records. Records can be sorted by clicking on the column name. Rows can be selected.");
        registerSpotlightFields(manager);
        manager.registerSpotlight(newItemButton, "New Record", "Can be used to create new records. " +
                "After entering the values into the fields above, update should be selected to add the new entry to the library.");
        manager.registerSpotlight(deleteItemButton, "Delete Record", "Deletes the currently selected record from the library.");
        manager.registerSpotlight(updateItemButton, "Update Record", "Updates the currently selected record from the library based on the value of the fields above.");
    }

    /**
     * Registers the fields specific to this controller to the spotlight manager
     *
     * @param manager The manager to register the fields to
     */
    protected abstract void registerSpotlightFields(SpotlightManager manager);

    /**
     * Returns a predicate that can be used for filtering large sets of data efficiently.
     *
     * @param filterText The text to be filtered on
     * @return A {@link Predicate} that accepts the specified data type and returns a boolean
     */
    protected abstract Predicate<T> getFilterPredicate(String filterText);

    /**
     * Returns the raw unwrapped data source.
     *
     * @return An non-observable list that includes the given data
     */
    protected abstract List<T> getDataSource();

    /**
     * Set the data to be displayed in this table. Should be called in {@link #initializeData()}. The given data should
     * be able to be shown in the table configured by {@link #setupColumns(TableView)}
     *
     * @param data The data to be shown in the table
     */
    protected void setData(ObservableList<T> data) {
        filteredList = new FilteredList<>(data);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);

        filter.textProperty().addListener((observable, oldValue, newValue) ->
                filteredList.setPredicate(getFilterPredicate(newValue.toLowerCase())));
    }

    /**
     * Validates the fields present in the view currently. Should indicate to the user that errors are present by
     * using {@link #errorClass} to style an invalid field.
     *
     * @return True if no errors exist in the form; otherwise, false
     */
    protected abstract boolean validate();

    /**
     * Should create all columns and bindings necessary to display objects of Type {@link T} in the given table.
     *
     * @param table The table to setup.
     */
    protected abstract void setupColumns(TableView<T> table);

    /**
     * Set the state of the forms in the object to the given object.
     * The values of this object should be set in their respective fields
     *
     * @param current The object to set the current state to
     */
    protected abstract void setCurrentState(T current);

    /**
     * Creates a new object of Type {@link T} with the given identifier.
     * The object should contain the default values in all of its fields.
     *
     * @param identifier The {@link Identifier} to use when creating the object
     * @return An object of Type {@link T} created using the given identifier
     */
    protected abstract T createNewItem(Identifier identifier);

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeData() {
        dataSource = FXCollections.observableList(getDataSource());
        setData(dataSource);

        Platform.runLater(() -> table.refresh());
    }

    /**
     * Returns the currently selected object from the table. Returns {@code null} if no object is currently selected.
     *
     * @return The currently selected object or null, if none are selected
     */
    protected T getCurrentlySelected() {
        return table.getSelectionModel().getSelectedItem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns(table);

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //Ensure the user finishes creating the new item
            if (currentlyCreating != null) {
                if (newValue != currentlyCreating) {
                    validate();
                    Platform.runLater(() -> table.getSelectionModel().select(currentlyCreating));
                }
                return;
            }
            //Ensure that a change actually occurred in the selection
            if (newValue != null && oldValue != newValue) {
                setCurrentState(newValue);
                validate();
            }
        });
    }

    @FXML
    private void update(ActionEvent event) {
        T current = getCurrentlySelected();
        //Make sure something is selected
        if (current == null) {
            return;
        }
        //Make sure the entered data is valid
        if (validate()) {
            //If we are creating a new object, we can set it as created because it passes validation
            if (currentlyCreating != null) {
                currentlyCreating = null;
            }
            update(current);
            getLibrary().modify();
            table.refresh();
        }
    }

    /**
     * Updates the given object using the current values of the fields.
     * All fields will be validated using {@link #validate()} before this method is called.
     *
     * @param toUpdate The object to update with the currently entered values
     */
    protected abstract void update(T toUpdate);

    /**
     * Checks if the given object can be deleted.
     *
     * @param object The object to check if it can be deleted
     *
     * @return True if it can be deleted; false otherwise
     */
    protected boolean canDelete(T object) {
        return true;
    }

    @FXML
    private void delete(ActionEvent event) {
        if (currentlyCreating != null) {
            //Set currently creating to a temp variable to set that nothing is being created before deleting the object
            //to prevent visual glitches
            T temp = currentlyCreating;
            currentlyCreating = null;
            dataSource.remove(temp);
            return;
        }
        T current = getCurrentlySelected();
        if (current != null && canDelete(current)) {
            dataSource.remove(current);
            getLibrary().modify();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void goHome(Event event) {
        //Before going home, delete the object being currently created
        dataSource.remove(currentlyCreating);
        currentlyCreating = null;
        super.goHome(event);
    }

    @FXML
    private void newItem(ActionEvent event) {
        if (currentlyCreating != null) {
            dataSource.remove(currentlyCreating);
            currentlyCreating = null;
        }
        T newItem = createNewItem(getNextIdentifier(dataSource));
        dataSource.add(newItem);
        table.scrollTo(newItem);
        table.getSelectionModel().select(newItem);
        currentlyCreating = newItem;
    }

    /**
     * Gets the next unique identifier not present in the given list of {@link LibraryData} objects
     *
     * @param list The list to traverse to provide an identifier unique to it
     * @return An identifier unique to the given list
     */
    protected final Identifier getNextIdentifier(List<? extends LibraryData> list) {
        int cur = list.size() + 1;
        Identifier curId = new Identifier(cur);
        for (LibraryData e : list) {
            if (e.getIdentifier().equals(curId)) {
                cur++;
                curId = new Identifier(cur);
            }
        }
        return curId;
    }
}
