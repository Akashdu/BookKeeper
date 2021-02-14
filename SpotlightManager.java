package library.ui;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates "spotlights", interactive help menus.
 * Allows for registering of multiple spotlights.
 * When {@link #trigger()} is called, all spotlights are displayed, in the order they were registered in.
 * {@link #disable()} should be called before disposing of this object
 *
 * @author Srikavin Ramkumar
 */
public class SpotlightManager {
    private final List<Spotlight> spotlights = new ArrayList<>();
    private final FXInitializer initializer;
    private int currentSpotlightIndex = -1;
    private Pane spotlightContainer;
    private Shape curShape;
    private boolean isActive = false;
    private final ChangeListener<? super Transform> changeListener = (observable, oldValue, newValue) -> draw();

    /**
     * Creates a spotlight manager that creates "spotlight" interactive help menus.
     *
     * @param initializer        An instance of {@link FXInitializer} to load fxml files from
     * @param spotlightContainer The container the backdrop should be limited to
     */
    public SpotlightManager(FXInitializer initializer, Pane spotlightContainer) {
        this.spotlightContainer = spotlightContainer;
        this.initializer = initializer;

        //Force redraws when the spotlight container is resized in order to keep the overlay over the entire bounding box
        spotlightContainer.heightProperty().addListener((observable, oldValue, newValue) -> draw());
        spotlightContainer.widthProperty().addListener((observable, oldValue, newValue) -> draw());
    }

    /**
     * Registers a spotlight with this manager. Spotlights will be shown in the order of registration when
     * {@link #trigger()} is called. If node is null, the request is ignored without throwing an exception.
     *
     * @param node        The node to highlight when this spotlight is displayed
     * @param title       The title to show on the title pane of this spotlight
     * @param description The description of the node highlighted and any important, helpful information
     */
    public void registerSpotlight(Node node, String title, String description) {
        //Ignore request if node is null
        if (node == null) {
            return;
        }
        spotlights.add(new Spotlight(node, title, description, initializer));
    }

    /**
     * Goes to the previous spotlight and updates the listeners
     * Updating the listeners improves performance as the overlay will only be drawn when the scene changes
     */
    public void previous() {
        if (currentSpotlightIndex - 1 >= 0) {
            removeListenerFromCurrentSpotlight();
            //Increment the spotlight index
            currentSpotlightIndex--;
            addListenerToCurrentSpotlight();
            //Draw the current state to the screen
            draw();
        }
    }

    /**
     * Resets the status of this spotlight to the starting position and removes the current overlay.
     * Registered spotlights will not be removed nor cleared.
     */
    public void reset() {
        if (currentSpotlightIndex != -1) {
            removeListenerFromCurrentSpotlight();
        }
        currentSpotlightIndex = 0;
        isActive = false;
        draw();
    }

    /**
     * Trigger the spotlight help UI
     * All of the registered spotlights will be shown in the order of registration.
     */
    public void trigger() {
        //Ignore if no spotlights are registered
        if (spotlights.isEmpty()) {
            return;
        }
        currentSpotlightIndex = 0;
        addListenerToCurrentSpotlight();
        isActive = true;
        draw();
    }

    /**
     * Disables the spotlight by immediately removing the overlay and resetting to the default state.
     * Registered spotlights will be removed and cleared.
     * <p>
     * Use {@link #reset()} to simply remove the overlay and reset to the starting positions.
     */
    public void disable() {
        reset();
        spotlights.clear();
    }

    /**
     * Removes the listener from the current node
     * Should be done before going to the next spotlight in the current scene
     */
    private void removeListenerFromCurrentSpotlight() {
        Spotlight current = spotlights.get(currentSpotlightIndex);
        if (current.node != null) {
            spotlightContainer.getChildren().remove(current.titledPane);
            spotlightContainer.getChildren().remove(curShape);
            current.node.localToSceneTransformProperty().removeListener(changeListener);
        }
    }

    /**
     * Adds the listener to the current node
     * Allows the draw function to only be called when needed
     */
    private void addListenerToCurrentSpotlight() {
        Spotlight current = spotlights.get(currentSpotlightIndex);
        if (current.node != null) {
            current.node.localToSceneTransformProperty().addListener(changeListener);
        }
    }

    /**
     * Goes to the next spotlight and updates the listeners
     * Updating the listeners improves performance as the overlay will only be drawn when the scene changes
     */
    public void next() {
        removeListenerFromCurrentSpotlight();
        if (spotlights.size() - 1 > currentSpotlightIndex) {
            //Increment the spotlight index
            currentSpotlightIndex++;
            addListenerToCurrentSpotlight();
        } else {
            //Set the current status to inactive
            currentSpotlightIndex = 0;
            isActive = false;
        }
        //Draw the current state to the screen
        draw();
    }

    /**
     * Calls {@link #draw(Spotlight)} with the currently active spotlight
     */
    private void draw() {
        if (!spotlights.isEmpty() && currentSpotlightIndex != -1) {
            draw(spotlights.get(currentSpotlightIndex));
        }
    }

    /**
     * Draws the current state to the spotlight container. If {@link #isActive} is false, it will clear the screen of the
     * spotlight overly. If it is true, it will draw the overlay with the information given in the specified spotlight.
     *
     * @param spotlight The spotlight information to be used when drawing the overlay.
     */
    private void draw(Spotlight spotlight) {
        TitledPane tooltipContainer = spotlight.titledPane;
        ContainerController containerController = spotlight.containerController;

        spotlightContainer.getChildren().remove(curShape);
        spotlightContainer.getChildren().remove(tooltipContainer);
        if (!isActive) {
            return;
        }

        //Calculate bounds of object
        Bounds currentBounds = spotlight.node.getBoundsInParent();
        final Point2D pt = spotlightContainer.sceneToLocal(spotlight.node.getParent().localToScene(currentBounds.getMinX(), currentBounds.getMinY()));
        Bounds bounds = new BoundingBox(pt.getX(), pt.getY(), currentBounds.getWidth(), currentBounds.getHeight());

        //Create rectangle for entire container
        Rectangle base = new Rectangle(spotlightContainer.getWidth(), spotlightContainer.getHeight());
        //Create rectangle and set properties to only cover the currentTarget
        Rectangle rect = new Rectangle();
        rect.setLayoutX(bounds.getMinX());
        rect.setLayoutY(bounds.getMinY());
        rect.setWidth(bounds.getMaxX() - bounds.getMinX());
        rect.setHeight(bounds.getMaxY() - bounds.getMinY());
        //Get the difference between the two shapes; this is everything but the current target
        Shape shape = Shape.subtract(base, rect);
        //Make the shape ignore clicks
        shape.setMouseTransparent(false);
        //Set the color and opacity
        shape.setFill(Color.BLACK);
        shape.setOpacity(0.7);
        //Remove the shape if it exists and add the newly calculated shape to the container
        spotlightContainer.getChildren().add(shape);
        //Set the current shape
        this.curShape = shape;

        //When clicking on the overlay, make it act like a next button to make it easy to go through items
        curShape.setOnMouseClicked((e) -> this.next());

        //Used for calculating the position to place the tooltip container
        final double CONTAINER_MARGIN = 16;


        double layoutX = bounds.getMaxX() + CONTAINER_MARGIN;
        double layoutY = bounds.getMinY();

        double tooltipWidth = spotlight.getWidth();
        double tooltipHeight = spotlight.getHeight();

        double containerWidth = spotlightContainer.getWidth();
        double containerHeight = spotlightContainer.getHeight();

        //Put the tooltip on the left if the tooltip is too big to fit on the right
        if (layoutX + tooltipWidth + CONTAINER_MARGIN > containerWidth) {
            layoutX = bounds.getMinX() - (CONTAINER_MARGIN + tooltipWidth);
            if (layoutX < 5 || layoutX > containerWidth - tooltipWidth) {
                //Fallbacks in case a proper layout cannot be found
                layoutX = CONTAINER_MARGIN;
            }
        }

        if (layoutY + tooltipHeight > containerHeight - 15) {
            //Fallback incase the calculate layout is out of bounds; prevent the buttons from being offscreen
            layoutY = containerHeight - (15 + tooltipHeight);
        }

        //Set the layout to the layout calculated above
        tooltipContainer.relocate(layoutX, layoutY);

        //Sets the title pane and spotlight information to be in front of the backdrop
        spotlightContainer.getChildren().add(tooltipContainer);

        //If we are on the first item in the spotlight, we can disable the previous button
        containerController.setDisablePrevious(false);
        if (currentSpotlightIndex == 0) {
            containerController.setDisablePrevious(true);
        }
    }

    /**
     * Used to store the spotlights registered with the spotlight manager
     */
    private class Spotlight {
        final Node node;
        final ContainerController containerController;
        final TitledPane titledPane;
        private double height = -1;
        private double width = -1;

        Spotlight(Node node, String title, String description, FXInitializer initializer) {
            this.node = node;
            containerController = new ContainerController();
            titledPane = (TitledPane) initializer.loadNode("SpotlightContainer.fxml", containerController);
            containerController.setDescription(description);
            containerController.setTitle(title);
        }

        private void calculateSize() {
            //Make sure the calculations have not been done before
            if (height != -1 && width != -1) {
                return;
            }
            //Calculate the size of the spotlight information pane
            final Pane pane = new Pane();
            pane.getChildren().add(titledPane);
            Scene scene = new Scene(pane);
            scene.snapshot(null);
            width = titledPane.getWidth();
            height = titledPane.getHeight();
            pane.getChildren().clear();
        }

        public double getHeight() {
            calculateSize();
            return height;
        }

        public double getWidth() {
            calculateSize();
            return width;
        }
    }

    /**
     * The controller of the spotlight titled pane fxml. Delegates events to the parent class, {@link SpotlightManager},
     * which is then able to call {@link #setDescription(String)}, {@link #setTitle(String)}, and {@link #setDisablePrevious(boolean)}
     * on this instance. These method calls update the contents of the view.
     */
    protected class ContainerController {
        @FXML
        private Button previous;
        @FXML
        private Text description;
        @FXML
        private TitledPane titledPane;

        /**
         * Event handler; called when the next button is clicked
         */
        @FXML
        private void onNext(ActionEvent event) {
            next();
        }

        /**
         * Event handler; called when the previous button is clicked
         */
        @FXML
        private void onPrevious(ActionEvent event) {
            previous();
        }

        /**
         * Event handler; called when the exit button is clicked
         */
        @FXML
        private void onExit(ActionEvent event) {
            reset();
        }

        /**
         * Sets the description in the view to the given description
         *
         * @param descriptionString The new description to show
         */
        void setDescription(String descriptionString) {
            description.setText(descriptionString);
        }

        /**
         * Sets the state of the previous button
         *
         * @param disable True if the button should be disabled; false otherwise
         */
        void setDisablePrevious(boolean disable) {
            previous.setDisable(disable);
        }

        /**
         * Sets the title of the TitledPane to the specified title
         *
         * @param title The new title to set
         */
        void setTitle(String title) {
            titledPane.setText(title);
        }
    }
}
