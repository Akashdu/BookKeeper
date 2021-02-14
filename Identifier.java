package library.data;

import java.util.Objects;

/**
 * An unique identifier that is used to identify all {@link LibraryData} objects.
 * This class does not enforce uniqueness; it is up to the client class to maintain uniqueness
 *
 * @author Srikavin Ramkumar
 */
public class Identifier implements Comparable<Identifier> {
    private final String id;

    /**
     * Creates an identifier using a String identifier
     *
     * @param id This identifier should be a digit zero-padded number; however, it can be anything as long as it is uniform
     */
    public Identifier(String id) {
        this.id = id;
    }

    /**
     * Creates an identifier using a integer identifier
     *
     * @param id This identifier is converted to a string identifier as a 6 digit zero-padded number
     */
    public Identifier(int id) {
        this.id = String.format("%06d", id);
    }

    /**
     * Returns the identifier as a String
     *
     * @return The identifier as a String
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Identifier && Objects.equals(id, ((Identifier) o).id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Identifier o) {
        return id.compareTo(o.id);
    }
}
