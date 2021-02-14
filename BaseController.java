package library.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import library.data.Library;

/**
 * This class serves as a base for other GUI's to implement.
 * The class is made to be extremely generic and should be extended to include custom functionality.
 * This class is abstract and cannot be instantiated.
 *
 * @author Srikavin Ramkumar
 */
public abstract class BaseController {
    /**
     * Should be used for animating the in animations. Used in {@link #animateIn(EventHandler)}
     */
    protected final Timeline animateInTimeline = new Timeline();
    /**
     * Should be used for animating the in animations. Used in {@link #animateOut(EventHandler)}
     */
    protected final Timeline animateOutTimeline = new Timeline();
    /**
     * Used for highlighting fields as incorrect or containing an error
     */
    protected final PseudoClass errorClass = PseudoClass.getPseudoClass("invalid-input");
    /**
     * The container holding all of the content of this controller
     * Excludes headers and other non-unique elements of this view
     */
    @FXML
    protected Node container;
    /**
     * The root pane of this view. Includes all nodes in the view, including headers.
     */
    @FXML
    protected Pane rootPane;
    @FXML
    private Pane header;
    private FXInitializer initializer;
    private Library library;
    private SpotlightManager spotlightManager;

    /**
     * Sets a {@link FXInitializer}. This is used to open new windows and change currently displayed content.
     * Must not be called with null.
     *
     * @param initializer The FXInitializer object to use.
     * @param library     The Library object to use.
     *
     * @throws IllegalArgumentException If the arguments passed are null
     */
    public void initialize(FXInitializer initializer, Library library) {
        //Check for null before setting
        if (initializer == null) {
            throw new IllegalArgumentException("Initializer cannot be null");
        }
        if (library == null) {
            throw new IllegalArgumentException("Library cannot be null");
        }
        this.initializer = initializer;
        this.library = library;
        //Make sure the javafx view file includes the root pane id; if not, this prevents null pointer exceptions
        if (rootPane != null) {
            this.spotlightManager = new SpotlightManager(initializer, rootPane);
            registerSpotlightItems(spotlightManager);
        }
    }

    /**
     * Returns to the main window.
     */
    @FXML
    protected void goHome(Event event) {
        getInitializer().setContent("MainWindow.fxml");
    }

    /**
     * Returns a {@link FXInitializer}. This is used to open new windows and change currently displayed content.
     *
     * @return a FXInitializer
     */
    protected FXInitializer getInitializer() {
        return initializer;
    }

    /**
     * Called after the window is visible and the root element has been loaded.
     * Should be used to implement any animations upon transitioning to this screen.
     *
     * @param callback Should be run as soon as the initial state of the animation is set.
     */
    public void animateIn(EventHandler<ActionEvent> callback) {
        final Timeline timeline = animateInTimeline;
        timeline.setCycleCount(1);

        //Set starting point of the animation - currently set to off-screen
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(0), callback,
                new KeyValue(container.opacityProperty(), 0),
                new KeyValue(header.translateYProperty(), -150)));

        //Run the animation
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(450),
                new KeyValue(container.opacityProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(container.translateYProperty(), 0),
                new KeyValue(header.translateYProperty(), 0, Interpolator.EASE_IN)));

        //Start the animation
        timeline.play();
    }

    @FXML
    private void onSpotlightHelp(MouseEvent event) {
        spotlightManager.trigger();
    }

    /**
     * Used to register any desired nodes in the scene with the current {@link SpotlightManager}
     *
     * @param spotlightManager The spotlight manager to register nodes with
     */
    protected void registerSpotlightItems(SpotlightManager spotlightManager) {

    }

    /**
     * Get the {@link Library} instance this controller is tied to
     *
     * @return The library instance this controller is tied to
     */
    protected Library getLibrary() {
        return library;
    }

    /**
     * Called when leaving this window.
     * Should be used to implement any animations before leaving this screen.
     *
     * @param callback Should be run as soon as the last animation is finished.
     */
    public void animateOut(EventHandler<ActionEvent> callback) {
        final Timeline timeline = animateOutTimeline;
        timeline.setCycleCount(1);

        //Set starting point of the animation - currently set to the current position of the items according to the layout
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(0),
                new KeyValue(container.opacityProperty(), 1, Interpolator.EASE_IN),
                new KeyValue(header.translateYProperty(), 0)));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(400),
                new KeyValue(container.opacityProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(container.translateYProperty(), 150, Interpolator.EASE_IN),
                new KeyValue(header.translateYProperty(), -150)));

        //Call the callback to insert the new content after the animation has finished
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(450), callback));

        //Start the animation
        timeline.play();
    }

    /**
     * Called to initialize data views in the view. Requires {@link #initialize(FXInitializer, Library)} to have been called.
     */
    public void initializeData() {

    }
}
