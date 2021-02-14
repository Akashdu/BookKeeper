package library.data;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single library book. Includes a title, author, and ISBN.
 * Also stores the patron currently holding this book, the current status of this book,
 * and the checkout date (if this book is checked out).
 *
 * @author Srikavin Ramkumar
 */
public class Book implements LibraryData {
    private Identifier identifier;
    private String isbn;
    private String title;
    private String author;
    private BookStatus status;
    private Patron currentPatron;
    private Instant checkOutDate;

    /**
     * Creates an instance of this class.
     *
     * @param identifier    Identifier representing this object
     * @param title         The title of the book
     * @param author        The author's name
     * @param isbn          The ISBN of this book
     * @param status        The current status of this book
     * @param checkOutDate  The checked out date of this book, can be null if it is not currently checked out
     * @param currentPatron The current patron holding this book
     */
    public Book(Identifier identifier, String title, String author, String isbn, BookStatus status, Patron currentPatron, Instant checkOutDate) {
        this.identifier = identifier;
        this.isbn = isbn;
        this.status = status;
        this.title = title;
        this.author = author;
        this.currentPatron = currentPatron;
        this.checkOutDate = checkOutDate;
    }

    /**
     * Initialize this object using saved data from {@link #asData()}.
     *
     * @param library A library to resolve the PatronType {@link Identifier} to a {@link PatronType}
     * @param data    A string array in the same format as returned by {@link #asData()}
     */
    public Book(String[] data, Library library) {
        this(new Identifier(data[0]), data[1], data[2], data[3], BookStatus.valueOf(data[4]),
                data[5].equals("null") || data[5].isEmpty() ? null : library.getPatronFromID(new Identifier(data[5])),
                data[6].equals("null") || data[6].isEmpty() ? null : Instant.parse(data[6]));
        if (data.length != 7) {
            throw new RuntimeException("Invalid data type!");
        }
    }

    /**
     * Checks if the given book is a copy of another book. This is true if and only if the ISBN, the author, and the
     * title of both books match. None of the other fields (Book status, current patron) are taken into consideration.
     *
     * @param book The book to compare with
     *
     * @return True if and only if the ISBN, the author, and the title of both books match; otherwise false
     */
    public boolean isCopyOf(Book book) {
        if (book == null) {
            return false;
        }
        if (this == book) {
            return true;
        }
        return Objects.equals(isbn, book.isbn) &&
                Objects.equals(title, book.title) &&
                Objects.equals(author, book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, title, author, status);
    }

    /**
     * Gets the {@link Patron} currently holding this book, or null if no patron has this book currently checked out
     *
     * @return The patron that currently has this book checked out; will be null if it is not checked out
     */
    public Patron getCurrentPatron() {
        return currentPatron;
    }

    /**
     * Sets the patron that has this book checked out.
     * Can be set to null to indicate that nobody has this book checked out
     *
     * @param currentPatron The patron this book should be checked out to
     */
    public void setCurrentPatron(Patron currentPatron) {
        this.currentPatron = currentPatron;
    }

    /**
     * Get the time and date this book was checked out.
     * This value may be null.
     *
     * @return An {@link Instant} object indicating the time and date this book was checked out; may be null
     */
    public Instant getCheckOutDate() {
        return checkOutDate;
    }

    /**
     * Get the time and date this book was checked out.
     * This value may be null.
     *
     * @param checkOutDate An {@link Instant} object indicating the time and date this book was checked out; may be null
     */
    public void setCheckOutDate(Instant checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    /**
     * {@inheritDoc}
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of this book.
     *
     * @param identifier The new identifier to set
     */
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * The output from this can be used in {@link #Book(String[], Library)} to create a book instance
     * {@inheritDoc}
     */
    @Override
    public String[] asData() {
        return new String[]{
                identifier.getId(),
                title,
                author,
                isbn,
                status.name(),
                currentPatron != null ? currentPatron.getIdentifier().getId() : "null",
                checkOutDate != null ? checkOutDate.toString() : "null"
        };
    }

    /**
     * Get the ISBN that represents this book instance
     *
     * @return An Valid 10 or 13 digit ISBN number matching this book
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Update the ISBN of this book.
     *
     * @param isbn A valid 10 or 13 digit ISBN.
     */
    public void setIsbn(String isbn) {
        int isbnChars = 0;
        for (char e : isbn.toCharArray()) {
            if (Character.isDigit(e) || Character.isAlphabetic(e)) {
                isbnChars++;
            }
        }
        if (isbnChars != 10 && isbnChars != 13) {
            throw new RuntimeException("Invalid ISBN!");
        }
        this.isbn = isbn;
    }

    /**
     * Get the current status of this book
     *
     * @return A {@link BookStatus} representing the current status of this book
     */
    public BookStatus getStatus() {
        return status;
    }

    /**
     * Set the current status of this book
     *
     * @param status A {@link BookStatus} value indicating the new state of this book
     */
    public void setStatus(BookStatus status) {
        this.status = status;
    }

    /**
     * Get the title of this book
     *
     * @return The title of this book
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the title of this book
     *
     * @param title The title of the book this object represents
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the author of the book
     *
     * @return The name of the author of the book represented by this object
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author's name of this book
     *
     * @param author The author's name as a {@link String}
     */
    public void setAuthor(String author) {
        this.author = author;
    }
}
