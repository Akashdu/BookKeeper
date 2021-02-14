package library.data;

/**
 * This enum represents the possible states of a library book.
 *
 * @author Srikavin Ramkumar
 */
public enum BookStatus {
    /**
     * Indicates that the book is available and able to be checked out
     */
    AVAILABLE("Available"),
    /**
     * Indicates that the book is not available and is checked out
     */
    CHECKED_OUT("Checked out"),
    /**
     * Indicates that the book is not available and it is lost
     */
    LOST("Lost");

    private final String message;

    BookStatus(String message) {
        this.message = message;
    }

    /**
     * Get a friendly message of this enum value
     *
     * @return A user-friendly message to display regarding this enum value
     */
    @Override
    public String toString() {
        return message;
    }
}
