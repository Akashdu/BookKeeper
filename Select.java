package library.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.data.Book;
import library.data.Identifier;
import library.data.LibraryData;
import library.data.Patron;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract class for quickly building a data view dialog.
 *
 * @param <T> The type of the selectable object. Should extend {@link LibraryData}
 *
 * @author Srikavin Ramkumar
 */
abstract class AbstractView<T extends LibraryData> {
    /**
     * The table holding all of the given data objects of Type {@linkplain T}
     */
    @FXML
    protected TableView<T> table;
    /**
     * The stage containing the dialog
     */
    protected Stage stage;
    /**
     * The button that is used to select a given item.
     * Automatically removed from the scene graph if a view dialog is used instead.
     */
    @FXML
    protected Button selectButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField filter;
    @FXML
    private Label label;

    /**
     * Initialize this object with data and a callback
     *
     * @param dataSource Data to display in this object
     * @param stage      The stage this view is being displayed in
     */
    public void init(List<T> dataSource, Stage stage) {
        init(dataSource, stage, false);
    }

    /**
     * Initialize this object with data and a callback
     *
     * @param dataSource       Data to display in this object
     * @param stage            The stage this view is being displayed in
     * @param showSelectButton Whether or not to show the select button in the view
     */
    public void init(List<T> dataSource, Stage stage, boolean showSelectButton) {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(getTitle());
        label.setText(getTitle());
        this.stage = stage;

        if (!showSelectButton) {
            //Remove the select button from the view if undesired
            ((Pane) selectButton.getParent()).getChildren().remove(selectButton);
            //Change the button text to close if it is the only button
            cancelButton.setText("Close");
        }

        initTable(table);

        ObservableList<T> observableList = FXCollections.observableList(dataSource);
        //Setup filtering
        FilteredList<T> filteredList = new FilteredList<>(observableList);
        filter.textProperty().addListener((observable, oldValue, newValue) ->
                filteredList.setPredicate((e) -> filterPredicate(filter.getText().toLowerCase(), e)));
        //Setup sorting
        SortedList<T> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);
        //Set the list to the table
        Platform.runLater(() -> table.refresh());
    }

    /**
     * Called when the cancel button is selected
     */
    @FXML
    private void cancel(ActionEvent event) {
        stage.close();
    }

    /**
     * A method to initialize the specified table with the necessary columns and converters for the specific datatype
     *
     * @param table The table to configure with the correct settings
     */
    protected abstract void initTable(TableView<T> table);

    /**
     * Returns the desired title of the select window
     *
     * @return The desired title of the window
     */
    protected abstract String getTitle();

    /**
     * A checking if the specified object matches the filter text
     *
     * @param filterText The text to filter the current object on
     * @param current    The current object being checked by the filter
     *
     * @return True if the current object matches the filter text; otherwise, false
     */
    protected abstract boolean filterPredicate(String filterText, T current);
}

/**
 * This class includes commonly used selection dialogs for {@link Patron}s and {@link Book}s.
 * The select dialog includes items of the specified type and calls the specified callback when an item is chosen by the user.
 *
 * @param <T> The type of DataType used. Must extend {@link LibraryData}
 *
 * @author Srikavin Ramkumar
 */
abstract class AbstractSelect<T extends LibraryData> extends AbstractView<T> {
    /**
     * Used to return to the original caller with the identifier of the Patron object selected
     */
    private Consumer<Identifier> callback;

    /**
     * {@inheritDoc}
     *
     * @param callback Used to return the original caller the identifier of the selected Patron object
     */
    public void init(Consumer<Identifier> callback, List<T> dataSource, Stage stage) {
        super.init(dataSource, stage, true);
        this.callback = callback;
        selectButton.setOnAction(this::select);
    }

    /**
     * Called when the select button is selected
     */
    @FXML
    private void select(ActionEvent event) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (!table.getSelectionModel().isEmpty() && selected != null) {
            callback.accept(selected.getIdentifier());
            stage.close();
        }
    }
}

/**
 * Class that allows for selecting of a specific commonly-used Library Data Types
 *
 * @author Srikavin Ramkumar
 */
public class Select {
    /**
     * Allows for easy viewing of a Book object from the given dataset
     */
    public static class BookView extends AbstractView<Book> {
        /**
         * The title to display on the window bar
         */
        private final String title;

        /**
         * Creates an instance of this class with the specified dialog window title.
         * Used to view a list of books
         *
         * @param title The title to display on the window bar
         */
        public BookView(String title) {
            this.title = title;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initTable(TableView<Book> table) {
            Books.initializeTable(table);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTitle() {
            return title;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean filterPredicate(String filterText, Book current) {
            return current.getIdentifier().getId().toLowerCase().contains(filterText)
                    || current.getIdentifier().getId().contains(filterText)
                    || current.getIsbn().toLowerCase().contains(filterText)
                    || current.getAuthor().toLowerCase().contains(filterText);
        }
    }

    /**
     * Allows for easy selection of a Patron object from the given dataset
     */
    public static class PatronSelect extends AbstractSelect<Patron> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void initTable(TableView<Patron> table) {
            Patrons.initializeTable(table);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTitle() {
            return "Select a Patron";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean filterPredicate(String filterText, Patron current) {
            String lowerCaseValue = filterText.toLowerCase();
            return current.getFirstName().toLowerCase().contains(lowerCaseValue) ||
                    current.getLastName().toLowerCase().contains(lowerCaseValue) ||
                    current.getIdentifier().getId().toLowerCase().contains(lowerCaseValue);
        }
    }

    /**
     * Allows for easy selection of a Book object from the given dataset
     */
    public static class BookSelect extends AbstractSelect<Book> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void initTable(TableView<Book> table) {
            Books.initializeTable(table);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTitle() {
            return "Select a Book";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean filterPredicate(String filterText, Book current) {
            return current.getIdentifier().getId().toLowerCase().contains(filterText)
                    || current.getIdentifier().getId().contains(filterText)
                    || current.getIsbn().toLowerCase().contains(filterText)
                    || current.getAuthor().toLowerCase().contains(filterText);
        }
    }
}
