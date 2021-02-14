package library.data;

/**
 * Used to indicate that the object is LibraryData and can be saved to a file and loaded from a file.
 *
 * @author Srikavin Ramkumar
 */
public interface LibraryData {
    /**
     * Gets the unique identifier for all types (Patron, Book, PatronType, etc.) of this object.
     *
     * @return An unique identifier representing this object
     */
    Identifier getIdentifier();

    /**
     * Returns the data stored in this object as a String array. Can be used with the constructor to load an instance
     * with the same data.
     *
     * @return A string array containing the data of this object
     */
    String[] asData();
}
