package library.data;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;

/**
 * Generates reports from a Library data source. These reports are dynamically generated with the method calls.
 *
 * @author Srikavin Ramkumar
 */
public class ReportGenerator {
    private static final String BOOK_HEADER_FORMAT = "%-8s%-30.30s%-30.30s%-12s%-8s%n";
    private static final String BOOK_CONTENT_FORMAT = "%-8s%-30.30s%-30.30s%-12s%-8s%n";
    private static final String SEPARATOR = "---------------------------------------------------------------------------------------------\n";
    private final Library library;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault());
    private double fineRate = 1.5;
    private double fineLimit = 60;

    /**
     * Creates an instance of a ReportGenerator using the given {@link Library} object as a data source.
     *
     * @param library The library to gather data from
     */
    public ReportGenerator(Library library) {
        this.library = library;
    }

    /**
     * Calculates the total number of {@link Book}s with each {@link BookStatus} and returns it as a map.
     * The map is defined as follows:
     * AVAILABLE  {@literal ->} 15
     * CHECKED_OUT {@literal ->} 25
     * That is, one key for each BookStatus and an Integer object as the value.
     *
     * @return A map with a BookStatus as the key, with Integer values. Using any given BookStatus will return a Integer of the number of items with that status.
     */
    public Map<BookStatus, Integer> getBookStatusTotals() {
        Map<BookStatus, Integer> bookStatusTotals = new HashMap<>();

        for (Book e : library.getBooks()) {
            //Set the value to 1 if it does not currently exist; else increment it by 1
            bookStatusTotals.merge(e.getStatus(), 1, Integer::sum);
        }
        return bookStatusTotals;
    }

    /**
     * Returns a string representation of the books and their status in this library.
     * Groups the books by the patron who has them checked out. This is helpful when creating printable reports that can
     * be handed out to groups of patrons easily.
     *
     * @param books The list of books to format
     *
     * @return A string representation of the books in this library grouped by patron
     */
    public String formatByPatron(List<Book> books) {
        if (books.isEmpty()) {
            return "No Books.";
        }

        //Use stringbuilder for efficiency; reduces creation of strings
        StringBuilder report = new StringBuilder();
        Formatter formatter = new Formatter(report);

        Map<Patron, List<Book>> patronBookMap = new HashMap<>();
        //Get all books checked out by a patron
        for (Book e : books) {
            Patron patron = e.getCurrentPatron();

            patronBookMap.computeIfAbsent(patron, (s) -> new ArrayList<>());
            patronBookMap.get(patron).add(e);
        }

        //Print out the generated map of books checked out by each patron
        for (Map.Entry<Patron, List<Book>> e : patronBookMap.entrySet()) {
            List<Book> booksOwned = e.getValue();
            Patron patron = e.getKey();
            report.append(patron.getLastName()).append(", ").append(patron.getFirstName()).append(" - ").append(patron.getIdentifier()).append('\n');
            formatter.format(BOOK_HEADER_FORMAT, "Item ID", "Title", "Author", "Due Date", "Days Till Due");
            report.append(SEPARATOR);

            PatronType patronType = patron.getPatronType();

            for (Book book : booksOwned) {
                TemporalAmount maxCheckoutTime = Duration.ofDays(patronType.getMaxCheckoutDays());

                int daysTillDue = getDayTillDue(book);

                Instant dueDate = book.getCheckOutDate().plus(maxCheckoutTime);
                formatter.format(BOOK_CONTENT_FORMAT, book.getIdentifier().getId(), book.getTitle(), book.getAuthor(),
                        dateTimeFormatter.format(dueDate), getDaysTillDue(daysTillDue));
            }
            report.append('\n').append('\n');
        }

        return formatter.out().toString();
    }

    /**
     * Returns a string representation of the books and their status in this library.
     * Groups the books by the patron who has them checked out.
     *
     * @param books The list of books to format
     *
     * @return A string representation of the books in this library with no grouping
     */
    public String formatByItems(List<Book> books) {
        if (books.isEmpty()) {
            return "No Books.";
        }

        //Use stringbuilder for efficiency; reduces creation of strings
        StringBuilder report = new StringBuilder();
        Formatter formatter = new Formatter(report);

        formatter.format(BOOK_HEADER_FORMAT, "Item ID", "Title", "Author", "Due Date", "Days Till Due");
        report.append(SEPARATOR);

        for (Book e : books) {
            Patron patron = e.getCurrentPatron();

            PatronType patronType = patron.getPatronType();
            TemporalAmount maxCheckoutTime = Duration.ofDays(patronType.getMaxCheckoutDays());

            int daysLeft = getDayTillDue(e);

            Instant dueDate = e.getCheckOutDate().plus(maxCheckoutTime);
            formatter.format(BOOK_CONTENT_FORMAT, e.getIdentifier().getId(), e.getTitle(), e.getAuthor(),
                    dateTimeFormatter.format(dueDate), getDaysTillDue(daysLeft));

        }
        return formatter.out().toString();
    }

    /**
     * Returns a map containing a date and the number of times a given book (or all books) were checked out on that date.
     * For example, if 5 books were checked out on May 1st; and 8 books were checked out on May 2nd, calling this method
     * will return a map with the following entries:
     * May 1 => 5
     * May 2 => 8
     *
     * @param start  The minimum date of transactions to consider
     * @param end    The maximum date of transactions to consider
     * @param book   The book to return data for; null be used to indicate all books to be counted
     * @param action The transaction action to filter for
     *
     * @return A map containing dates and the number of times books were checked out on that date.
     */
    public Map<String, Integer> getCheckoutsBetweenDates(LocalDate start, LocalDate end, Book book, Transaction.Action action) {
        boolean allBooks = book == null;

        List<Transaction> transactions = library.getTransactions();
        Map<String, Integer> toRet = new LinkedHashMap<>();

        int daysBetween = (int) (end.toEpochDay() - start.toEpochDay());

        for (int i = 0; i < daysBetween; i++) {
            toRet.put(dateFormatter.format(start.plusDays(i)), 0);
        }

        for (Transaction e : transactions) {
            LocalDate timestamp = instantToLocalDate(e.getTimestamp());

            //Verify the transaction took place at the correct time
            if ((allBooks || book.isCopyOf(e.getChangedBook())) && e.getAction() == action && timestamp.isBefore(end) && timestamp.isAfter(start)) {
                String key = dateFormatter.format(timestamp);
                toRet.put(key, toRet.get(key) + 1);
            }
        }
        return toRet;
    }

    private LocalDate instantToLocalDate(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }

    private int getDayTillDue(Book e) {
        Patron patron = e.getCurrentPatron();
        PatronType patronType = patron.getPatronType();
        TemporalAmount maxCheckoutTime = Duration.ofDays(patronType.getMaxCheckoutDays());

        Instant dueDate = e.getCheckOutDate().plus(maxCheckoutTime);
        return (int) ChronoUnit.DAYS.between(Instant.now(), dueDate);
    }

    /**
     * Formats the number of days left as a string
     *
     * @param daysLeft An integer number of days until the book is due. A negative number indicates that it is overdue.
     *
     * @return A string containing the number of days left or the string "overdue".
     */
    private String getDaysTillDue(int daysLeft) {
        if (daysLeft > 0) {
            return String.format("%-8.2s", daysLeft);
        } else {
            return "Overdue";
        }
    }

    /**
     * Returns the fines formatted as a String.
     * Uses the fee rate and fee limit set using {@link #setFineRate(double)} and {@link #setFineLimit(double)} respectively
     *
     * @return A string representation of the fines of each patron.
     */
    public String getFines() {
        List<Book> books = getOverdueBooks();
        if (books.isEmpty()) {
            return "No fines.";
        }

        //Use stringbuilder for efficiency; reduces creation of strings
        StringBuilder report = new StringBuilder();
        Formatter formatter = new Formatter(report);

        Map<Patron, List<Book>> patronBookMap = new HashMap<>();

        for (Book e : books) {
            Patron patron = e.getCurrentPatron();

            patronBookMap.computeIfAbsent(patron, (s) -> new ArrayList<>());
            patronBookMap.get(patron).add(e);
        }

        for (Map.Entry<Patron, List<Book>> bookValue : patronBookMap.entrySet()) {
            List<Book> booksOwned = bookValue.getValue();
            Patron patron = bookValue.getKey();
            report.append(patron.getLastName()).append(", ").append(patron.getFirstName()).append(" - ").append(patron.getIdentifier()).append('\n');
            formatter.format(BOOK_HEADER_FORMAT, "Item ID", "Title", "Author", "Due Date", "Fine");
            report.append(SEPARATOR);

            PatronType patronType = patron.getPatronType();

            for (Book book : booksOwned) {
                TemporalAmount maxCheckoutTime = Duration.ofDays(patronType.getMaxCheckoutDays());

                int daysLeft = -getDayTillDue(book);
                double fine = daysLeft * fineRate;
                if (fine > fineLimit) {
                    fine = fineLimit;
                }

                Instant dueDate = book.getCheckOutDate().plus(maxCheckoutTime);

                formatter.format(BOOK_CONTENT_FORMAT, book.getIdentifier().getId(), book.getTitle(), book.getAuthor(),
                        dateTimeFormatter.format(dueDate), fine);
            }
            formatter.format("\n\n");
        }

        return formatter.out().toString();
    }

    /**
     * Find all books that are currently checked out
     *
     * @return A list of all {@linkplain Book}s that are checked out in the library
     */
    public List<Book> getCheckedOutBooks() {
        List<Book> checkedOutBooks = new ArrayList<>();
        for (Book e : library.getBooks()) {
            if (bookIsCheckedOut(e)) {
                checkedOutBooks.add(e);
            }
        }
        return checkedOutBooks;
    }

    /**
     * Gets all overdue books in this library
     *
     * @return A list of overdue books currently in this library
     */
    public List<Book> getOverdueBooks() {
        List<Book> overDueBooks = new ArrayList<>();
        for (Book e : getCheckedOutBooks()) {
            Patron patron = e.getCurrentPatron();
            PatronType patronType = patron.getPatronType();
            TemporalAmount maxCheckoutTime = Duration.ofDays(patronType.getMaxCheckoutDays());

            Instant dueDate = e.getCheckOutDate().plus(maxCheckoutTime);
            //Book is overdue; the due date is before now
            if (dueDate.isBefore(Instant.now())) {
                overDueBooks.add(e);
            }
        }
        return overDueBooks;
    }

    private boolean bookIsCheckedOut(Book e) {
        return e.getStatus() == BookStatus.CHECKED_OUT && e.getCurrentPatron() != null &&
                !e.getCurrentPatron().getIdentifier().getId().equals("null")
                && e.getCheckOutDate() != null;
    }

    /**
     * Get the currently set fine rate in dollars per day overdue
     *
     * @return The current fine rate ($/day)
     */
    public double getFineRate() {
        return fineRate;
    }

    /**
     * Sets the current fine rate in dollars per day overdue
     *
     * @param fineRate The new fine rate ($/day)
     */
    public void setFineRate(double fineRate) {
        this.fineRate = fineRate;
    }

    /**
     * Gets the currently set maximum fine in dollars
     *
     * @return The current fine limit ($)
     */
    public double getFineLimit() {
        return fineLimit;
    }

    /**
     * Sets the current maximum fine in dollars
     *
     * @param fineLimit The new fine limit ($)
     */
    public void setFineLimit(double fineLimit) {
        this.fineLimit = fineLimit;
    }
}
