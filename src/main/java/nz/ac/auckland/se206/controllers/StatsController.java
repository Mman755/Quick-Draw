package nz.ac.auckland.se206.controllers;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.net.URISyntaxException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.filereader.CategorySelector;
import nz.ac.auckland.se206.userutils.Database;
import nz.ac.auckland.se206.userutils.User;

public class StatsController {
  private String userName;
  @FXML private Label wordList;
  @FXML private Label userLabel;
  @FXML private Label gamesPlayedLabel;
  @FXML private Label wordsPlayedLabel;
  @FXML private Label fastestTimeLabel;
  @FXML private Label avgTimeLabel;
  @FXML private PieChart gamesPlayedChart;
  @FXML private ComboBox<String> wordDifficultyFilter;

  public void initialize() {
    wordDifficultyFilter.getItems().addAll("All Words", "Easy", "Medium", "Hard");
    wordDifficultyFilter.setValue("All Words");
  }

  protected void setName(String userId) {
    this.userName = userId;
    this.userLabel.setText(userId);
  }

  protected void setStats() throws IOException {
    // Create database instance and obtain the current user for which we set stats
    User currentUser = Database.read(userName);
    int numWins = currentUser.getWins();
    int numLosses = currentUser.getLosses();
    int totalGames = numLosses + numWins;
    gamesPlayedLabel.setText(totalGames + " Games Played");

    // preparing observable list object to insert into pie chart
    ObservableList<PieChart.Data> pieChartData =
        FXCollections.observableArrayList(
            new PieChart.Data(numWins + " WINS", numWins),
            new PieChart.Data(numLosses + " LOSSES", numLosses));
    gamesPlayedChart.setData(pieChartData);

    fastestTimeLabel.setText(currentUser.getFastestTime() + ".0 seconds");
    avgTimeLabel.setText(currentUser.getAverageSolveTime() + " seconds");
  }

  @FXML
  protected void setWordsPlayed() throws IOException, URISyntaxException, CsvException {
    // Create database instance and obtain the current user for which we set stats
    User currentUser = Database.read(userName);
    String difficultyFilter = wordDifficultyFilter.getValue();
    switch (difficultyFilter) {
      case "Easy" -> updateWordsPlayedLabel("E", 142, currentUser);
      case "Medium" -> updateWordsPlayedLabel("M", 130, currentUser);
      case "Hard" -> updateWordsPlayedLabel("H", 68, currentUser);
      default -> updateWordsPlayedLabel(currentUser);
    }
  }

  @FXML
  private void onUserMenuSwitch(ActionEvent event) {
    Scene scene = ((Node) event.getSource()).getScene();
    scene.setRoot(SceneManager.getUiRoot(SceneManager.AppUi.USER_MENU));
  }

  private void updateWordsPlayedLabel(String difficulty, int numWords, User currentUser)
      throws IOException, URISyntaxException, CsvException {
    new CategorySelector();
    StringBuilder sb = new StringBuilder();
    for (String word : currentUser.getWordList()) {
      if (CategorySelector.getWordDifficulty(word).equals(difficulty)) {
        sb.append(word).append(System.getProperty("line.separator"));
      }
    }
    switch (difficulty) {
      case "E" -> wordsPlayedLabel.setText(currentUser.getNumEasyWords() + "/" + numWords);
      case "M" -> wordsPlayedLabel.setText(currentUser.getNumMediumWords() + "/" + numWords);
      case "H" -> wordsPlayedLabel.setText(currentUser.getNumHardWords() + "/" + numWords);
    }
    wordList.setText(sb.toString());
  }

  private void updateWordsPlayedLabel(User currentUser)
      throws IOException, URISyntaxException, CsvException {
    new CategorySelector();
    StringBuilder sb = new StringBuilder();
    for (String word : currentUser.getWordList()) {
      sb.append(word).append(System.getProperty("line.separator"));
    }
    wordsPlayedLabel.setText(currentUser.getNumWordsPlayed() + "/340");
    wordList.setText(sb.toString());
  }
}
