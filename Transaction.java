package library.data;

import java.time.Instant;
import java.util.Arrays;

/**
 * Used to store all transactions that take place in a library instance.
 * Stores the affected patron, affected book, the action that took place, and a timestamp of when it occurred.
 * This object is identified by an {@link Identifier}.
 *
 * @author Srikavin Ramkumar
 */
public class Transaction implements LibraryData {
    private final static Patron deletedPatron = new Patron(new Identifier("DELETED"), "DELETED", "DELETED",
            new PatronType(new Identifier("DELETED"), "DELETED", 1, 1));
    private final static Book deletedBook = new Book(new Identifier("DELETED"), "DELETED", "DELETED", "0000000000", BookStatus.LOST, null, null);

    private final Identifier identifier;
    private final Patron changedPatron;
    private final Book changedBook;
    private final Action action;
    private final Instant timestamp;

    /**
     * Used to create an instance of a Transaction object. Should be used for creating one for testing, or from user input
     * Use {@link #Transaction(String[], Library)} for restoring from a saved instance of a Transaction
     *
     * @param identifier    The identifier of this transaction
     * @param changedPatron The {@link Patron} affected by this transaction
     * @param changedBook   The {@link Book} affected by this transaction
     * @param action        The {@link Action} that took place in this transaction
     * @param timestamp     The {@link Instant} that this transaction took place
     */
    public Transaction(Identifier identifier, Patron changedPatron, Book changedBook, Action action, Instant timestamp) {
        if (changedBook == null) {
            changedBook = deletedBook;
        }
        if (changedPatron == null) {
            changedPatron = deletedPatron;
        }
        this.identifier = identifier;
        this.changedPatron = changedPatron;
        this.changedBook = changedBook;
        this.action = action;
        this.timestamp = timestamp;
    }

    /**
     * Creates a transaction instance from the given data
     *
     * @param data    The data object returned from {@link #asData()}
     * @param library The library to resolve references to {@link Patron}s and {@link Book}s
     */
    public Transaction(String[] data, Library library) {
        if (data.length != 5) {
            System.out.println(Arrays.toString(data));
            throw new RuntimeException("Invalid data array passed to create a Transaction object.");
        }
        identifier = new Identifier(data[0]);
        Patron patron = library.getPatronFromID(new Identifier(data[1]));
        Book book = library.getBookFromID(new Identifier(data[2]));
        if (patron == null) {
            changedBook = deletedBook;
        } else {
            changedBook = book;
        }
        if (book == null) {
            changedPatron = deletedPatron;
        } else {
            changedPatron = patron;
        }
        action = Action.valueOf(data[3]);
        timestamp = Instant.parse(data[4]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] asData() {
        return new String[]{identifier.getId(),
                changedPatron == null ? "DELETED" : changedPatron.getIdentifier().getId(),
                changedBook == null ? "DELETED" : changedBook.getIdentifier().getId(),
                action.name(),
                timestamp.toString()};
    }

    /**
     * Getter for the action performed by this transaction
     *
     * @return The action the took place during this transaction
     */
    public Action getAction() {
        return action;
    }

    /**
     * Getter for the book affected by this transaction
     *
     * @return The book affected by this transaction
     */
    public Book getChangedBook() {
        return changedBook;
    }

    /**
     * Getter for the patron affected by this transaction
     *
     * @return The patron affected by this transaction
     */
    public Patron getChangedPatron() {
        return changedPatron;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Getter for the {@link Instant} that this transaction took place
     *
     * @return An {@linkplain Instant} containing the date and time this transaction occurred
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * An enum containing all possible actions that a transaction can do.
     * This includes checking out and returning books.
     */
    public enum Action {
        /**
         * Indicates that a book was checked out
         */
        CHECKOUT,
        /**
         * Indicates that a book was returned
         */
        RETURN
    }
}