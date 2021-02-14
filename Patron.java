package library.data;

/**
 * A simple class used to hold the data of a single library patron.
 *
 * @author Srikavin Ramkumar
 */
public class Patron implements LibraryData {
    private Identifier identifier;
    private String firstName;
    private String lastName;
    private PatronType patronType;

    /**
     * Creates a {@linkplain Patron} object using the specified parameters.
     *
     * @param identifier A {@link Identifier} object containing the desired identifier for this patron.
     * @param firstName  A {@linkplain String} object containing the desired first name of this patron.
     * @param lastName   A {@linkplain String} object containing the desired last name of this patron.
     * @param patronType A {@link PatronType} object containing the desired library use values of this patron.
     */
    public Patron(Identifier identifier, String firstName, String lastName, PatronType patronType) {
        if (identifier == null || patronType == null) {
            throw new IllegalArgumentException("Identifier and PatronType cannot be null!");
        }
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronType = patronType;
    }

    /**
     * Creates a {@linkplain Patron} object from a String Array, arr, in the format:
     * <pre>
     *  arr[0] {@literal ->} identifier
     *  arr[1] {@literal ->} firstName
     *  arr[2] {@literal ->} lastName
     *  arr[3] {@literal ->} birthday in ISO-8601 format
     *  arr[4] {@literal ->} {@link PatronType} id
     * </pre>
     *
     * @param patronData A string array with the format specified above.
     * @param library    The library instance this object belongs to. Used to resolve {@linkplain PatronType} from an id.
     * @throws IllegalArgumentException if the data format given is invalid.
     */
    public Patron(String[] patronData, Library library) {
        //Delegate object construction to other constructor
        this(new Identifier(patronData[0]),
                patronData[1],
                patronData[2],
                library.getPatronTypeFromId(new Identifier(patronData[3])));
        if (patronData.length != 4) {
            throw new RuntimeException("Invalid data type!");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Converts this patron's data into a format represented by a String Array. This is the inverse function of
     * {@link #Patron(String[], Library)}.
     *
     * @return This patron's data represented as a String array.
     */
    public String[] asData() {
        return new String[]{
                identifier.toString(),                       //Set identifier to [0]
                firstName,                                   //Set first name to [1]
                lastName,                                    //Set last name to [2]
                patronType.getIdentifier().getId()           //Set PatronType's id to [3]
        };
    }

    /**
     * @return The unique identifier representing this Patron.
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Sets the id of this patron. This id must not be duplicated among Patrons.
     * Must not be null.
     *
     * @param id An unique id for this Patron.
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    public void setIdentifier(Identifier id) {
        //Verify id is not null
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }
        this.identifier = id;
    }

    /**
     * Gets the first name of this patron object
     *
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of this patron object
     *
     * @param firstName The new first name; may not be null
     */
    public void setFirstName(String firstName) {
        if (firstName == null) {
            throw new IllegalArgumentException("First name cannot be null");
        }
        this.firstName = firstName;
    }

    /**
     * Gets the last name of this patron object
     *
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of this patron object
     *
     * @param lastName The new last name; may not be null
     */
    public void setLastName(String lastName) {
        if (lastName == null) {
            throw new IllegalArgumentException("Last name cannot be null");
        }
        this.lastName = lastName;
    }

    /**
     * This function relies on the unique nature of its identifier.
     *
     * @param obj The object to test equality with.
     * @return True if the objects are equal. Otherwise, it returns false.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Patron && identifier.equals(((Patron) obj).identifier);
    }

    /**
     * Gets the {@link PatronType} attached to this instance of this object
     *
     * @return The patron type with the checkout limits associated with this instance
     */
    public PatronType getPatronType() {
        return patronType;
    }

    /**
     * Sets the {@link PatronType} attached with this instance
     * @param patronType The new patron type to be associated with this instance
     */
    public void setPatronType(PatronType patronType) {
        this.patronType = patronType;
    }
}
