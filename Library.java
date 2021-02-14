package library.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class that contains all of the {@link LibraryData} objects including {@link Patron}s, {@link PatronType}s, and
 * {@link Book}s. This class also manages saving and loading library data from a data file. {@link ReportGenerator}s can
 * be created using an instance of this class.
 *
 * @author Srikavin Ramkumar
 */
public class Library {
    /**
     * Used to separate different data types (patrons, books, etc.) in the data file.
     */
    private final static String dataTypeSeparator = "--------";
    private List<Patron> patrons = new ArrayList<>();
    private List<PatronType> patronTypes = new ArrayList<>();
    private List<Book> books = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private ReportGenerator reportGenerator;
    /**
     * Used to identify when changes are made to this library that are not saved.
     */
    private boolean modified = false;

    /**
     * Creates a library object from the saved data in the provided file path. The file will be parsed and loaded into
     * this Library instance. Path can be null to create an in-memory library instance that will not be saved to disk.
     *
     * @param dataFilePath The file to load library data from; can be null to create an in-memory instance
     *
     * @throws IOException If an error occurs while reading the file, an IOException will be thrown
     */
    public Library(Path dataFilePath) throws IOException {
        if (dataFilePath == null) {
            PatronType patronType = new PatronType(new Identifier(1), "default", 25, 3);
            patronTypes.add(patronType);
            return;
        }
        //Load the data file
        Path dataFile = dataFilePath.resolve("data.txt");

        //Check if the file exists
        boolean fileExists = Files.isRegularFile(dataFile);
        if (fileExists) {
            AtomicReference<String> current = new AtomicReference<>("");
            Files.lines(dataFile).forEach((line) -> {
                if (line.startsWith(dataTypeSeparator)) {
                    current.set(line.substring(dataTypeSeparator.length()));
                    return;
                }

                //Split only on commas not inside of quotes
                //Matches -> |  author, name, string
                //No Match-> |  "Book title, also part of book title"
                //-1 makes sure all elements are returned
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                //Remove unnecessary quotes from saved fields and empty fields
                for (int i = 0; i < data.length; i++) {
                    String s = data[i];
                    if (s == null || s.isEmpty()) {
                        data[i] = "";
                        continue;
                    }
                    if (s.contains(",") && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                        data[i] = s.substring(1, s.length() - 1);
                    }
                }

                switch (current.get()) {
                    case "TYPES":
                        PatronType patronType = new PatronType(data);
                        patronTypes.add(patronType);
                        break;
                    case "PATRONS":
                        Patron patron = new Patron(data, this);
                        patrons.add(patron);
                        break;
                    case "BOOKS":
                        Book book = new Book(data, this);
                        books.add(book);
                        break;
                    case "TRANSACTIONS":
                        Transaction transaction = new Transaction(data, this);
                        transactions.add(transaction);
                        break;
                }
            });
        } else {
            try {
                //If the file doesn't exist, create it
                Files.createFile(dataFile);
            } catch (IOException e) {
                //Throw an unchecked exception with the same contents as the exception
                throw new RuntimeException(e);
            }
        }
        //Create a report generator using this as its data source
        reportGenerator = new ReportGenerator(this);
    }

    /**
     * Gets all the transaction stored in this library instance
     *
     * @return A list of all {@link Transaction}s in this library
     */
    public List<Transaction> getTransactions() {
        for (int i = 0; i < transactions.size(); i++) {
            Transaction e = transactions.get(i);
            boolean removed = false;
            if (e.getChangedBook() == null || getBookFromID(e.getChangedBook().getIdentifier()) == null) {
                transactions.remove(e);
                removed = true;
            }
            if (e.getChangedPatron() == null || getPatronFromID(e.getChangedPatron().getIdentifier()) == null) {
                transactions.remove(e);
                removed = true;
            }
            if (removed) {
                transactions.add(new Transaction(e.asData(), this));
            }
        }
        transactions.sort(Comparator.comparing(Transaction::getIdentifier));
        return transactions;
    }

    /**
     * Gets all the patrons stored in this library instance
     *
     * @return A list of all {@link Patron}s in this library
     */
    public List<Patron> getPatrons() {
        return patrons;
    }

    /**
     * Gets the report generator associated with this library instance's data
     *
     * @return A {@link ReportGenerator} linked to this library instance's data
     */
    public ReportGenerator getReportGenerator() {
        //Lazily create a report generator if one hasn't been generated yet
        if (reportGenerator == null) {
            reportGenerator = new ReportGenerator(this);
        }
        return reportGenerator;
    }

    /**
     * Sets the status of the library to modified. The library has had changes made that have not been saved to disk yet.
     */
    public void modify() {
        this.modified = true;
    }

    /**
     * Gets all the patron types stored in this library instance
     *
     * @return A list of all {@link PatronType}s in this library
     */
    public List<PatronType> getPatronTypes() {
        return patronTypes;
    }

    /**
     * Resolves a {@link PatronType} from a specified identifier
     *
     * @param id The identifier to resolve
     *
     * @return The {@linkplain PatronType} object represented by the specified identifier or null, if not found
     */
    public PatronType getPatronTypeFromId(Identifier id) {
        for (PatronType e : patronTypes) {
            if (e.getIdentifier().equals(id)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Resolves a {@link PatronType} from a specified name
     *
     * @param name Name of the PatronType
     *
     * @return The {@linkplain PatronType} object represented by the specified name or null, if not found
     */
    public PatronType getPatronTypeFromName(String name) {
        for (PatronType e : patronTypes) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Resolves a {@link Patron} from a specified Identifier
     *
     * @param identifier The identifier to resolve
     *
     * @return The {@linkplain Patron} object represented by the specified identifier or null, if not found
     */
    public Patron getPatronFromID(Identifier identifier) {
        for (Patron e : patrons) {
            if (e.getIdentifier().equals(identifier)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Resolves a {@link Patron} from a specified Identifier
     *
     * @param identifier The identifier to resolve
     *
     * @return The {@linkplain Patron} object represented by the specified identifier or null, if not found
     */
    public Book getBookFromID(Identifier identifier) {
        for (Book e : books) {
            if (e.getIdentifier().equals(identifier)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Saves this library to the specified data file path
     *
     * @param path   The path at which to store the file; Must be a directory. A file "data.txt" is created inside of this directory
     * @param suffix the suffix to add to the end of the saved file
     *
     * @throws IOException If the file cannot be accessed or written to, an IOException will be thrown
     */
    public void saveTo(Path path, String suffix) throws IOException {
        Path dataFile;
        if (suffix != null) {
            dataFile = path.resolve("data-" + suffix + ".txt");
        } else {
            dataFile = path.resolve("data.txt");
        }
        BufferedWriter writer = Files.newBufferedWriter(dataFile);
        save(writer);
        writer.close();
        modified = false;
    }

    /**
     * Saves this library to the specified data file path
     *
     * @param path The path at which to store the file; Must be a directory. A file "data.txt" is created inside of this directory
     *
     * @throws IOException If the file cannot be accessed or written to, an IOException will be thrown
     */

    public void saveTo(Path path) throws IOException {
        saveTo(path, null);
    }

    private void save(Writer writer) throws IOException {
        appendToWriter("TYPES", writer, patronTypes);
        appendToWriter("PATRONS", writer, patrons);
        appendToWriter("BOOKS", writer, books);
        appendToWriter("TRANSACTIONS", writer, transactions);
    }

    private void appendToWriter(String dataType, Writer writer, List<? extends LibraryData> libraryObjects) throws IOException {
        writer.write(dataTypeSeparator + dataType + '\n');
        for (LibraryData e : libraryObjects) {
            String[] data = e.asData();
            for (int i = 0; i < data.length; i++) {
                String value = data[i];
                if (value.contains(",") && !(value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')) {
                    writer.write('"');
                    writer.write(value);
                    writer.write('"');
                } else {
                    writer.write(value);
                }
                if (data.length - 1 != i) {
                    writer.write(',');
                }
            }
            writer.write('\n');
        }
    }

    /**
     * Checks if the library has been modified since the last save
     *
     * @return True if the library has been modified; false if the library has not been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Gets all the books stored in this library instance
     *
     * @return A list of all {@link Book}s in this library
     */
    public List<Book> getBooks() {
        return books;
    }
}
