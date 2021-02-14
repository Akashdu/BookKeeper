package library.data;

/**
 * Can be applied to {@link Patron}s to set checkout days and checkout limits. The same PatronType can be applied to
 * multiple Patrons. This object contains a checkout time limit, checkout day limit, a name, and an {@link Identifier}.
 * @author Srikavin Ramkumar
 */
public class PatronType implements LibraryData {
    private Identifier identifier;
    private String name;
    private int maxCheckoutDays;
    private int maxCheckedOutBooks;

    /**
     * Creates an PatronType instance using the specified parameters.
     *
     * @param identifier         An {@link Identifier} object containing the unique desired identifier for this patron type.
     * @param name               A {@linkplain String} object containing the desired name of this patron.
     * @param maxCheckoutDays    An integer indicating the maximum number of days a book can be checked out.
     * @param maxCheckedOutBooks An integer indicating the maximum number of checked out books.
     */
    public PatronType(Identifier identifier, String name, int maxCheckoutDays, int maxCheckedOutBooks) {
        this.identifier = identifier;
        this.name = name;
        this.maxCheckoutDays = maxCheckoutDays;
        this.maxCheckedOutBooks = maxCheckedOutBooks;
    }

    /**
     * Creates a {@linkplain Patron} object from a String Array, arr, in the format:
     * <pre>
     *  arr[0] {@literal ->} identifier
     *  arr[1] {@literal ->} name
     *  arr[2] {@literal ->} maximum days a book can be checked out
     *  arr[3] {@literal ->} maximum checked-out books
     * </pre>
     *
     * @param data A string array in the format above, or given from {@link #asData()}
     * @throws IllegalArgumentException if the data format given is invalid.
     */
    public PatronType(String[] data) {
        this(new Identifier(data[0]),
                data[1],
                Integer.parseInt(data[2]),
                Integer.parseInt(data[3]));
        if (data.length != 4) {
            throw new IllegalArgumentException("Invalid data used!");
        }
    }

    /**
     * Gets the name of this instance as a String.
     *
     * @return A string containing the name given to this instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this PatronType
     * @param name A string containing the desired name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the maximum days a {@link Patron} with this instance attached can have a book checked-out
     * @return The maximum number of days a book can be checked out
     */
    public int getMaxCheckoutDays() {
        return maxCheckoutDays;
    }

    /**
     * Sets the maximum days a {@link Patron} with this instance attached can have a book checked-out
     * @param maxCheckoutDays The maximum number of days a book can be checked out
     */
    public void setMaxCheckoutDays(int maxCheckoutDays) {
        this.maxCheckoutDays = maxCheckoutDays;
    }

    /**
     * Gets the maximum number of books a {@link Patron} with this instance attached can hold at one time
     * @return The maximum number of books a Patron can hold at one time
     */
    public int getMaxCheckedOutBooks() {
        return maxCheckedOutBooks;
    }

    /**
     * Sets the maximum number of books a {@link Patron} with this instance attached can hold at one time
     * @param maxCheckedOutBooks The maximum number of books a Patron can hold at one time
     */
    public void setMaxCheckedOutBooks(int maxCheckedOutBooks) {
        this.maxCheckedOutBooks = maxCheckedOutBooks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     *
     * The return value can be used with {@link #PatronType(String[])} to create a clone of this object.
     */
    @Override
    public String[] asData() {
        return new String[]{
                identifier.toString(),
                name,
                Integer.toString(maxCheckoutDays),
                Integer.toString(maxCheckedOutBooks)
        };
    }
}
