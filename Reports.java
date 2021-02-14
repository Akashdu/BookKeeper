package library.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import library.data.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class serves as a controller of the Reports view defined in the FXML.
 * This handles the display, graphing, and communication between the {@link ReportGenerator} class and the view.
 *
 * @author Srikavin Ramkumar
 */
public class Reports extends BaseController {
    @FXML
    private RadioButton specificCheckedOutButton;
    @FXML
    private RadioButton allCheckedOutButton;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private CategoryAxis dateAxis;
    @FXML
    private BarChart<String, Number> checkouts;
    @FXML
    private TextField currentBook;
    @FXML
    private TextField bookTitle;
    @FXML
    private TextField bookAuthor;
    @FXML
    private ChoiceBox<BookStatus> bookStatus;
    @FXML
    private NumberAxis checkoutAxis;
    @FXML
    private Spinner<Double> fineLimit;
    @FXML
    private Spinner<Double> fineRate;
    @FXML
    private TextArea reportView;
    @FXML
    private RadioButton itemSort;
    @FXML
    private RadioButton patronSort;
    @FXML
    private PieChart bookStatusChart;

    private boolean sortByItem = false;
    private boolean allBooksCharted = true;
    private Views currentView = Views.CHECKED_OUT;
    private Book selectedBook;

    private void setReportContent() {
        Library library = getLibrary();
        ReportGenerator reportGenerator = library.getReportGenerator();

        List<Book> bookList = new ArrayList<>();
        switch (currentView) {
            case CHECKED_OUT:
                bookList = reportGenerator.getCheckedOutBooks();
                break;
            case OVERDUE:
                bookList = reportGenerator.getOverdueBooks();
                break;
            case FINES:
                reportView.setText(reportGenerator.getFines());
                reportGenerator.setFineRate(fineRate.getValue());
                reportGenerator.setFineLimit(fineLimit.getValue());
                sortByItem = false;
                itemSort.setSelected(false);
                itemSort.setDisable(true);
                patronSort.setSelected(true);
                return;
        }
        itemSort.setDisable(false);

        if (sortByItem) {
            reportView.setText(reportGenerator.formatByItems(bookList));
        } else {
            reportView.setText(reportGenerator.formatByPatron(bookList));
        }

        Map<BookStatus, Integer> bookStatusTotals = reportGenerator.getBookStatusTotals();
        ObservableList<PieChart.Data> bookStatusData = FXCollections.observableArrayList();

        //For each entry in the map, create a new data entry and add it to the list
        bookStatusTotals.forEach((bookStatus, amount) -> {
            PieChart.Data data = new PieChart.Data(bookStatus.toString(), amount);
            data.nameProperty().bind(Bindings.concat(data.getName(), " (", (int) data.getPieValue(), ")"));
            bookStatusData.add(data);
        });
        //Set the data into the PieChart
        bookStatusChart.setData(bookStatusData);

        checkouts.getData().clear();

        ObservableList<XYChart.Data<String, Number>> returnSeries = FXCollections.observableArrayList();
        ObservableList<XYChart.Data<String, Number>> checkoutSeries = FXCollections.observableArrayList();
        Book book = allBooksCharted ? null : selectedBook;
        Map<String, Integer> checkoutData = reportGenerator.getCheckoutsBetweenDates(startDate.getValue(), endDate.getValue(), book, Transaction.Action.CHECKOUT);
        Map<String, Integer> returnData = reportGenerator.getCheckoutsBetweenDates(startDate.getValue(), endDate.getValue(), book, Transaction.Action.RETURN);
        int max = -1;
        for (Map.Entry<String, Integer> e : checkoutData.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
            }
            checkoutSeries.add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        for (Map.Entry<String, Integer> e : returnData.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
            }
            returnSeries.add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        checkouts.getData().add(new XYChart.Series<>("Checked Out", checkoutSeries));
        checkouts.getData().add(new XYChart.Series<>("Returned", returnSeries));
        checkoutAxis.setUpperBound(Math.max(1.2 * max, 3));
        checkoutAxis.setLowerBound(0);
        checkoutAxis.setTickUnit(Math.max((max) / 20, 1));
    }

    @FXML
    private void viewOverdueItems(ActionEvent event) {
        currentView = Views.OVERDUE;
        setReportContent();
    }

    @FXML
    private void sortByItem(ActionEvent event) {
        sortByItem = true;
        setReportContent();
    }

    @FXML
    private void sortByPatron(ActionEvent event) {
        sortByItem = false;
        setReportContent();
    }

    @FXML
    private void checkoutView(ActionEvent event) {
        currentView = Views.CHECKED_OUT;
        setReportContent();
    }

    @FXML
    private void print(ActionEvent event) {
        //Copy the text to an offscreen print area
        Text reportText = new Text(reportView.getText());
        reportText.setFont(Font.font("monospaced"));
        TextFlow printArea = new TextFlow(reportText);
        printArea.setBackground(null);

        //Layout the page and format it with the correct size and orientation
        Printer printer = Printer.getDefaultPrinter();
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

        Scene s = new Scene(printArea);
        s.snapshot(new WritableImage(1, 1));


        //Calculate the scaling to match the paper size in terms of width
        double availHorizontal = printArea.getBoundsInParent().getWidth();
        double scaleRatio = pageLayout.getPrintableWidth() / availHorizontal;
        Scale scale = new Scale(scaleRatio, scaleRatio);
        printArea.getTransforms().add(scale);

        //Calculate the requires number of pages to print all content
        int pages = (int) ((printArea.getBoundsInParent().getHeight() / pageLayout.getPrintableHeight()) + 1);

        // Make sure user wants to print
        if (printerJob != null && printerJob.showPrintDialog(getInitializer().getPrimaryStage())) {
            // Set page layout and other settings
            // Print all pages in the node
            // Start at 1 instead of zero, as starting at 0 prints a page twice because it results in a translation of 0
            for (int page = 1; page <= pages; page++) {
                printerJob.printPage(pageLayout, printArea);
                printArea.setTranslateY(-1 * page * pageLayout.getPrintableHeight());
            }
            //End the job and send it to the print spooler
            printerJob.endJob();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeData() {
        super.initializeData();
        ReportGenerator reportGenerator = getLibrary().getReportGenerator();
        PreferenceManager preferenceManager = getInitializer().getPreferenceManager();

        double savedFineLimit = preferenceManager.getValueAsNumber("fine_limit", 60).doubleValue();
        double savedFineRate = preferenceManager.getValueAsNumber("fine_rate", 1.5).doubleValue();

        reportGenerator.setFineLimit(savedFineLimit);
        reportGenerator.setFineRate(savedFineRate);

        //Define how the spinners increment and decrement
        fineLimit.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, Double.MAX_VALUE, savedFineLimit));
        fineRate.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, savedFineRate));

        //Make the reports update when the value of the fee spinners change
        fineLimit.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            fineLimit.increment(0);
            getInitializer().getPreferenceManager().setValue("fine_limit", fineLimit.getValue());
            setReportContent();
        });
        fineRate.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            fineRate.increment(0);
            getInitializer().getPreferenceManager().setValue("fine_rate", fineRate.getValue());
            setReportContent();
        });

        startDate.setValue(LocalDate.of(2018, 3, 12));
        endDate.setValue(LocalDate.now());

        bookStatus.setItems(FXCollections.observableArrayList(BookStatus.values()));
        allCheckedOutButton.setSelected(true);
        allBooksCharted = true;

        setReportContent();
    }

    @FXML
    private void viewFines(ActionEvent event) {
        currentView = Views.FINES;
        setReportContent();
    }

    @FXML
    private void refreshReports(ActionEvent actionEvent) {
        reportView.clear();
        setReportContent();
    }

    @FXML
    private void findBook(MouseEvent mouseEvent) {
        Select.BookSelect bookSelect = new Select.BookSelect();
        Stage stage = getInitializer().getDialog("Select.fxml", bookSelect);
        bookSelect.init((e) -> {
            selectedBook = getLibrary().getBookFromID(e);
            updateCurrentBook(null);
        }, getLibrary().getBooks(), stage);
        stage.showAndWait();
    }

    @FXML
    private void updateCurrentBook(ActionEvent event) {
        currentBook.setText(selectedBook.getIdentifier().getId());
        bookTitle.setText(selectedBook.getTitle());
        bookAuthor.setText(selectedBook.getAuthor());
        bookStatus.setValue(selectedBook.getStatus());
        specificCheckedOutButton.setSelected(true);
        allBooksCharted = false;
        setReportContent();
    }

    @FXML
    private void viewSpecificCheckedOut(ActionEvent event) {
        if (allBooksCharted && selectedBook != null) {
            allBooksCharted = false;
            setReportContent();
        }
    }

    @FXML
    private void viewAllCheckedOut(ActionEvent event) {
        if (!allBooksCharted) {
            allBooksCharted = true;
            setReportContent();
        }
    }

    @FXML
    private void updateDate(ActionEvent event) {
        setReportContent();
    }

    /**
     * Used to maintain internal state about the current checked item
     */
    private enum Views {
        CHECKED_OUT,
        OVERDUE,
        FINES
    }
}
