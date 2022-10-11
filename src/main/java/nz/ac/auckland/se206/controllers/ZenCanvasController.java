package nz.ac.auckland.se206.controllers;

import ai.djl.ModelException;
import com.opencsv.exceptions.CsvException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.filereader.CategorySelector;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.userutils.Database;
import nz.ac.auckland.se206.userutils.User;

/**
 * This is the controller of the canvas. You are free to modify this class and the corresponding
 * FXML file as you see fit. For example, you might no longer need the "Predict" button because the
 * DL model should be automatically queried in the background every second.
 *
 * <p>!! IMPORTANT !!
 *
 * <p>Although we added the scale of the image, you need to be careful when changing the size of the
 * drawable canvas and the brush size. If you make the brush too big or too small with respect to
 * the canvas size, the ML model will not work correctly. So be careful. If you make some changes in
 * the canvas and brush sizes, make sure that the prediction works fine.
 */
public class ZenCanvasController implements Initializable {

    private String userName;

    @FXML private Canvas canvas;
    @FXML private Label wordLabel;
    @FXML private Button readyButton;
    private GraphicsContext graphic;
    protected DoodlePrediction model;
    @FXML private Label predLabel;
    @FXML private Button eraseBtn;
    @FXML private Button onInk;
    @FXML private Label timerCount;
    private int counter;
    private String wordChosen;
    @FXML private Button clearButton;
    protected FileChooser fileChooser;
    private Boolean isContent;
    private Color color;
    @FXML private Button newGameBtn;
    @FXML private Button saveImage;
    @FXML private Button mainMenuBtn;
    private User user;
    @FXML private Button speakWord;
    @FXML private ColorPicker colourSwitcher;

    // mouse coordinates
    private double currentX;
    private double currentY;

    /**
     * JavaFX calls this method once the GUI elements are loaded. In our case we create a listener for
     * the drawing, and we load the ML model.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startGame();
    }

    protected void startGame() {
        // Disable the buttons in the GUI as fit
        this.disableStartButtons();

        isContent = false;
        color = Color.WHITE;

        canvas.setOnMouseEntered(e -> canvas.setCursor(Cursor.HAND));

        graphic = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(
                e -> {
                    currentX = e.getX();
                    currentY = e.getY();
                    // Set content boolean to true when user has drawn, we will use this field as a guard
                    // for predictions
                    isContent = true;
                });

        canvas.setOnMouseDragged(
                e -> {
                    // Brush size (you can change this, it should not be too small or too large).
                    final double size = 6;

                    final double x = e.getX() - size / 2;
                    final double y = e.getY() - size / 2;

                    graphic.setStroke(color);
                    graphic.setLineWidth(size);
                    // Create a line that goes from the point (currentX, currentY) and (x,y)
                    graphic.strokeLine(currentX, currentY, x, y);

                    // update the coordinates
                    currentX = x;
                    currentY = y;
                });

        try {
            model = new DoodlePrediction();
        } catch (ModelException | IOException e) {
            e.printStackTrace();
        }
    }

    protected void setUserName(String userId) throws IOException {
        // Set the username of the current user playing, and also its corresponding stats to local
        // fields
        this.userName = userId;
        this.user = Database.read(this.userName);

        this.generateWord();
    }

    private void generateWord() {
        // Create an instance of category selector
        CategorySelector categorySelector = null;
        try {
            categorySelector = new CategorySelector();
        } catch (IOException | URISyntaxException | CsvException e) {
            e.printStackTrace();
        }
        // Get a random word with Easy difficulty and set the word to be displayed to the user in the
        // GUI
        String randomWord =
                categorySelector.getRandomDiffWord(this.user.getCurrentWordSetting(), this.user.getWordList());
        this.setWord(randomWord);
    }

    protected void disableStartButtons() {
        // This method when called well disable or enable the required buttons on input
        onInk.setDisable(true);
        eraseBtn.setDisable(true);
        speakWord.setDisable(false);
        clearButton.setDisable(true);
    }

    /** This method is called when the "Clear" button is pressed. */
    @FXML
    private void onClear() {
        graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Get the current snapshot of the canvas.
     *
     * @return The BufferedImage corresponding to the current canvas content.
     */
    public BufferedImage getCurrentSnapshot() {
        final Image snapshot = canvas.snapshot(null, null);
        final BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
        // Convert into a binary image.
        final BufferedImage imageBinary =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        final Graphics2D graphics = imageBinary.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        // To release memory we dispose.
        graphics.dispose();
        return imageBinary;
    }

    /**
     * Save the current snapshot on a bitmap file.
     *
     * @throws IOException If the image cannot be saved.
     */
    @FXML
    private void onSaveSnapshot() throws IOException {
        // Create a file chooser
        fileChooser = new FileChooser();
        // Set the title
        fileChooser.setTitle("Save Your Image");
        // Add all the extensions for saving the drawing
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter("All Images", "*.*"),
                        new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                        new FileChooser.ExtensionFilter("PNG", "*.png"));
        // Set the initial file name
        fileChooser.setInitialFileName(wordChosen);
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            ImageIO.write(getCurrentSnapshot(), "bmp", file);
        }
    }

    @FXML
    private void onReady() {
        // When player is ready we start the game by enabling canvas, starting the timer etc
        this.color = this.colourSwitcher.getValue();
        this.canvas.setDisable(false);
        this.onInk.setDisable(true);
        this.readyButton.setDisable(true);
        this.clearButton.setDisable(false);
        this.eraseBtn.setDisable(false);
        this.timerCount.setVisible(true);
        this.runTimer();
    }

    protected void setWord(String wordDraw) {
        // Obtain the word given to draw from filereader class
        wordLabel.setText("Draw: " + wordDraw);
        wordChosen = wordDraw;
    }

    private void runTimer() {
        counter = -1;
        Timer timer = new Timer();
        TimerTask task =
                new TimerTask() {
                    @Override
                    public void run() {
                        // Increment the counter each second the timer task runs
                        counter++;
                        // When possible we set the label to update the counter
                        Platform.runLater(() -> timerCount.setText(counter + " Seconds elapsed"));
                        // Here we call the predictions' method (onDraw()) only if user has actually begun
                        // drawing on the canvas
                        // Surrounded in try and catch for exception handling
                        if (isContent) {
                            Platform.runLater(
                                    () -> {
                                        try {
                                            onDraw();
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                        }
                    }
                };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    private void enableEndButtons() {
        // Enable the available buttons user can interact with when the game has ended
        newGameBtn.setDisable(false);
        saveImage.setDisable(false);
        mainMenuBtn.setDisable(false);
        speakWord.setDisable(true);
    }

    private void onDraw() {
        // Set the image to be the current snapshot which is called every second, image is final for
        // predictions
        final BufferedImage image = getCurrentSnapshot();
        // Begin the background thread so the GUI does not freeze when being used
        Task<Void> backgroundTask =
                new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        // Create a scene builder instance which is how the predictions will be formatted
                        StringBuilder sbf = new StringBuilder();
                        int k = 1;
                        // Loop to format the string, so it can be displayed to the label
                        for (int i = 0; i < 10; i++) {
                            // Append the required formatting to sbf
                            // The prediction number (10) being lowest (1) being the best prediction
                            sbf.append(k)
                                    .append(") ")
                                    .append(
                                            model
                                                    .getPredictions(image, 10)
                                                    .get(i)
                                                    .getClassName()
                                                    .replace("_", " ")); // Append the predictions themselves
                            k++;

                            sbf.append(System.getProperty("line.separator"));

                            // Check if the word given to the user to draw is within the top 3 predictions of the
                            // model, if it is
                            // We set the game won status to be true.
                            if (wordChosen.equals(
                                    model.getPredictions(image, 10).get(i).getClassName().replace("_", " "))
                                    && i < user.getCurrentAccuracySetting()
                                    && model.getPredictions(image, 10).get(i).getProbability() * 100
                                    >= (double) user.getCurrentConfidenceSetting()) {
                                enableEndButtons();
                            }
                        }

                        // Set the predictions label in the GUI to the string builder sbf
                        Platform.runLater(() -> predLabel.setText(sbf.toString()));

                        return null;
                    }
                };
        Thread backgroundThread = new Thread(backgroundTask);

        backgroundThread.setDaemon(true);
        backgroundThread.start(); // Begin the thread when called
    }

    @FXML
    private void onErase() { // If the user wants to erase something we set the pen color to white
        this.color = Color.WHITE;
        eraseBtn.setDisable(true);
        onInk.setDisable(false);
    }

    @FXML
    private void onPen() { // If the user wants to switch back to pen we change the pen color to black
        this.color = this.colourSwitcher.getValue();
        eraseBtn.setDisable(false);
        onInk.setDisable(true);
    }

    @FXML
    private void switchPenColour() {
        if (!this.eraseBtn.isDisabled()) { //only switch colour if we are currently on ink (not eraser)
            this.color = this.colourSwitcher.getValue();
        }
    }

    @FXML
    private void onSpeakWord() {
        // Create text to speech instance
        TextToSpeech speech = new TextToSpeech();
        // Create task for thread and put speak inside
        Task<Void> voiceThread =
                new Task<>() {
                    @Override
                    protected Void call() {
                        speech.speak(wordChosen);
                        return null;
                    }
                };
        // Create thread for bThread and start it when this method is called
        Thread speechThread = new Thread(voiceThread);
        speechThread.setDaemon(true);
        speechThread.start();
    }

    @FXML
    protected void onNewGame() throws IOException {
        // If the user wants to play a new game we clear the canvas and the user gets a new word to draw
        onClear();
        timerCount.setVisible(false);
        readyButton.setDisable(false);
        setUserName(userName);
        startGame();
        predLabel.setText(
                "Click the \"Ready!\" button to start drawing the word you see and view the predictions!");
        timerCount.setTextFill(Color.color(0.8, 0.6, 0.06));
    }

    @FXML
    private void onUserMenuSwitch(ActionEvent event) {
        MenuController menucontroller =
                (MenuController) SceneManager.getUiController(SceneManager.AppUi.USER_MENU);
        menucontroller.setName(userName);
        Image img = new Image("/images/profilepics/" + user.getImageName());
        menucontroller.setUserDetails(img);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(SceneManager.getUiRoot(SceneManager.AppUi.USER_MENU));
    }
}
