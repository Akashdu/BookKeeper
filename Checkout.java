package library.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import library.data.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The UI View for checkouts. Implements {@link DataViewController}. Holds a {@link Patron} and a {@link Book}
 * instance to update. Uses event-driven operations to maintain state abd update the model layer.
 *
 * @author Srikavin Ramkumar
 */
public class Checkout extends DataViewController<Book> {
    @FXML
    private TextField patronName;
    @FXML
    private Text booksCheckedOut;
    @FXML
    private ChoiceBox<BookStatus> bookStatus;
    @FXML
    private TextField bookAuthor;
    @FXML
    private TextField bookTitle;
    @FXML
    private TextField currentBook;
    @FXML
    private TextField currentPatron;
    @FXML
    private Button checkoutButton;
    @FXML
    private Button returnButton;
    @FXML
    private Pane booksCheckedOutContainer;
    private Books books = new Books();

    /**
     * {@inheritDoc}
     * Overrides {@link DataViewController#registerSpotlightItems(SpotlightManager)} to register custom buttons in the
     * Checkout view.
     */
    @Override
    protected void registerSpotlightItems(SpotlightManager manager) {
        manager.registerSpotlight(filter, "Filter", "Can be used to search the records based on entered keywords.");
        manager.registerSpotlight(table, "Records", "View the current records. Records can be sorted by clicking on the column name. Rows can be selected.");
        registerSpotlightFields(manager);
        manager.registerSpotlight(returnButton, "Update Record", "Updates the currently selected record from the library based on the value of the fields above.");
        manager.registerSpotlight(checkoutButton, "Delete Record", "Deletes the currently selected record from the library.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSpotlightFields(SpotlightManager manager) {
        manager.registerSpotlight(currentPatron, "Patron Identifier", "The patron to show books for. " +
                "Must be a valid Patron Identifier. Valid values can be selected by clicking the search icon.");
        manager.registerSpotlight(patronName, "Current Patron Name", "The name of the current patron. " +
                "This field can not modifiable. To modify this information, go the Patrons view.");
        manager.registerSpotlight(currentBook, "Book Identifier", "The book to checkout or return. " +
                "Must be a valid Book Identifier. Valid values can be selected by clicking the search icon.");
        manager.registerSpotlight(bookTitle, "Current Book Title", "The title of the current book. " +
                "This field can not be modified. To edit this information, go to the the Books view.");
        manager.registerSpotlight(bookAuthor, "Current Book Author", "The author of the current book. " +
                "This field can not be modified. To edit this information, go to the the Books view.");
        manager.registerSpotlight(bookStatus, "Current Book Status", "The status of the current book. " +
                "If it is checked out or lost, a librarian override is required. This field will automatically be set to " +
                "Checked Out if the book is checked out.");
        manager.registerSpotlight(booksCheckedOutContainer, "Number of Checked out Books",
                "The number of books currently checked out by the patron. If the number is highlighted in red, " +
                        "a librarian override is required.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Predicate<Book> getFilterPredicate(String filter) {
        return book -> book.getIdentifier().getId().toLowerCase().contains(filter)
                || book.getTitle().toLowerCase().contains(filter)
                || book.getIsbn().toLowerCase().contains(filter)
                || book.getAuthor().toLowerCase().contains(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Book> getDataSource() {
        return FXCollections.observableList(new ArrayList<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean validate() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupColumns(TableView<Book> table) {
        books.setupColumns(table);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCurrentState(Book current) {
        updateBook(current.getIdentifier());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Book createNewItem(Identifier identifier) {
        return books.createNewItem(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void update(Book toUpdate) {

    }

    @FXML
    private void findPatron(MouseEvent event) {
        Select.PatronSelect select = new Select.PatronSelect();
        Library library = getLibrary();
        Stage stage = getInitializer().getDialog("Select.fxml", select);
        select.init(this::updatePatron, library.getPatrons(), stage);
        stage.showAndWait();
    }

    private void updatePatron(Identifier id) {
        currentPatron.pseudoClassStateChanged(errorClass, false);
        Library library = getLibrary();
        Patron p = library.getPatronFromID(id);
        currentPatron.setText(p.getIdentifier().getId());
        patronName.setText(p.getFirstName() + " " + p.getLastName());
        updateTable();
    }

    @FXML
    private void updateCurrentPatron(ActionEvent event) {
        Identifier id = new Identifier(currentPatron.getText());
        Library library = getLibrary();
        Patron patron = library.getPatronFromID(id);
        if (patron == null) {
            currentPatron.pseudoClassStateChanged(errorClass, true);
            return;
        }
        updatePatron(id);
    }

    private void updateBook(Identifier id) {
        currentBook.pseudoClassStateChanged(errorClass, false);
        Library library = getLibrary();
        Book book = library.getBookFromID(id);
        currentBook.setText(book.getIdentifier().getId());
        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        bookStatus.getSelectionModel().select(book.getStatus());
        bookStatus.pseudoClassStateChanged(errorClass, false);
        if (book.getStatus() == BookStatus.CHECKED_OUT) {
            bookStatus.pseudoClassStateChanged(errorClass, true);
        }
    }

    @FXML
    private void updateCurrentBook(ActionEvent event) {
        Identifier id = new Identifier(currentBook.getText());
        Library library = getLibrary();
        Book book = library.getBookFromID(id);
        if (book == null) {
            currentBook.pseudoClassStateChanged(errorClass, true);
            return;
        }

        updateBook(id);
    }

    private ObservableList<Book> getCheckedOutBooks(Identifier id) {
        Library library = getLibrary();
        List<Book> allCheckedOut = library.getReportGenerator().getCheckedOutBooks();
        List<Book> toReturn = new ArrayList<>();
        for (Book e : allCheckedOut) {
            if (e.getCurrentPatron().getIdentifier().equals(id)) {
                toReturn.add(e);
            }
        }
        return FXCollections.observableArrayList(toReturn);
    }

    @FXML
    private void findBook(MouseEvent event) {
        Select.BookSelect select = new Select.BookSelect();
        Library library = getLibrary();
        Stage stage = getInitializer().getDialog("Select.fxml", select);
        select.init(this::updateBook, library.getBooks(), stage);
        stage.showAndWait();
    }

    private void updateTable() {
        Library library = getLibrary();

        Identifier patronID = new Identifier(currentPatron.getText());
        Patron patron = library.getPatronFromID(patronID);
        ObservableList<Book> books = getCheckedOutBooks(patronID);
        setData(books);

        int currentlyCheckedOut = books.size();
        int maxCheckedOut = patron.getPatronType().getMaxCheckedOutBooks();
        booksCheckedOut.setText(currentlyCheckedOut + "/" + maxCheckedOut);
        if (currentlyCheckedOut >= maxCheckedOut) {
            booksCheckedOut.setFill(Color.RED);
        } else {
            booksCheckedOut.setFill(Color.BLACK);
        }
    }

    @FXML
    private void returnBook(ActionEvent event) {
        Book selected = getCurrentlySelected();
        if (selected != null) {
            //Create and add transaction
            List<Transaction> transactions = getLibrary().getTransactions();
            Transaction transaction = new Transaction(getNextIdentifier(transactions), selected.getCurrentPatron(), selected,
                    Transaction.Action.RETURN, Instant.now());
            transactions.add(transaction);

            //Update table's data
            selected.setCurrentPatron(null);
            selected.setCheckOutDate(null);
            selected.setStatus(BookStatus.AVAILABLE);
            updateTable();
        }
    }

    private boolean isOverLimit(Patron patron) {
        List<Book> books = getCheckedOutBooks(patron.getIdentifier());
        return books.size() >= patron.getPatronType().getMaxCheckedOutBooks();
    }

    @FXML
    private void checkoutBook(ActionEvent event) {
        Library library = getLibrary();
        Book book = library.getBookFromID(new Identifier(currentBook.getText()));
        Patron patron = library.getPatronFromID(new Identifier(currentPatron.getText()));
        if (patron == null) {
            currentPatron.pseudoClassStateChanged(errorClass, true);
            return;
        }
        if (book == null) {
            currentBook.pseudoClassStateChanged(errorClass, true);
            return;
        }
        if (book.getStatus() == BookStatus.CHECKED_OUT || book.getStatus() == BookStatus.LOST) {
            bookStatus.pseudoClassStateChanged(errorClass, true);
            Alert alert = new Alert(Alert.AlertType.WARNING, "Book is checked out or lost!", ButtonType.CANCEL, ButtonType.YES);
            alert.setHeaderText("Override checkout?");
            Optional<ButtonType> buttonTypeOptional = alert.showAndWait();
            if (!buttonTypeOptional.isPresent()) {
                return;
            }
            if (buttonTypeOptional.get().equals(ButtonType.CANCEL)) {
                return;
            }
        }

        if (isOverLimit(patron)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Max book limit reached for patron!", ButtonType.CANCEL, ButtonType.YES);
            alert.setHeaderText("Override book limit?");
            Optional<ButtonType> buttonTypeOptional = alert.showAndWait();
            if (!buttonTypeOptional.isPresent()) {
                return;
            }
            if (buttonTypeOptional.get().equals(ButtonType.CANCEL)) {
                return;
            }
        }

        //Update book
        book.setCheckOutDate(Instant.now());
        book.setCurrentPatron(patron);
        book.setStatus(BookStatus.CHECKED_OUT);

        List<Transaction> transactions = getLibrary().getTransactions();
        //Create transaction
        Transaction transaction = new Transaction(getNextIdentifier(transactions), patron, book,
                Transaction.Action.CHECKOUT, Instant.now());
        transactions.add(transaction);

        //Update table
        updateTable();
        setCurrentState(book);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeData() {
        super.initializeData();
        table.setPlaceholder(new Text("No books checked out by current patron"));
        bookStatus.setItems(FXCollections.observableArrayList(BookStatus.values()));
        bookStatus.setConverter(new StringConverter<BookStatus>() {
            @Override
            public String toString(BookStatus object) {
                return object.name();
            }

            @Override
            public BookStatus fromString(String string) {
                return BookStatus.valueOf(string);
            }
        });
    }
}
