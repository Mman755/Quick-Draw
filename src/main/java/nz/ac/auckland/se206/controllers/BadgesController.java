package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.userutils.Database;
import nz.ac.auckland.se206.userutils.User;
import nz.ac.auckland.se206.util.SceneManager;

public class BadgesController {
  @FXML private Label goldTime;
  @FXML private Label goldGames;
  @FXML private Label goldWins;
  @FXML private Label silverTime;
  @FXML private Label silverGames;
  @FXML private Label silverWins;
  @FXML private Label bronzeTime;
  @FXML private Label bronzeWins;
  @FXML private Label bronzeGames;

  /**
   * This method takes in a username as input and will display the badges earned for that user to
   * the GUI.
   *
   * @param userName the current user's name as a string.
   * @throws IOException if an I/O error occurs reading from the stream when reading user data from
   *     Database.
   */
  protected void setBadgesForUser(String userName) throws IOException {
    // Obtain the current user instance
    User currentUser = Database.read(userName);

    // Check if user has five consecutive wins and display badge
    if (currentUser.fiveConsecutiveWins()) {
      bronzeWins.getTooltip().setText("JUNIOR ARTIST! \n For winning 5 games in a row.");
      bronzeWins.setDisable(false);
    }
    // Check if user has ten consecutive wins and display badge
    if (currentUser.tenConsecutiveWins()) {
      silverWins.getTooltip().setText("INTERMEDIATE ARTIST! \n For winning 10 games in a row.");
      silverWins.setDisable(false);
    }
    // Check if user has twenty consecutive wins and display badge
    if (currentUser.twentyConsecutiveWins()) {
      goldWins.getTooltip().setText("MASTER ARTIST! \n For winning 20 games in a row.");
      goldWins.setDisable(false);
    }

    // Check if user has twenty-five games played and display badge
    if (currentUser.twentyFiveGamesPlayed()) {
      bronzeGames.getTooltip().setText("EXPERIENCED SKETCHER! \n For playing 25 games.");
      bronzeGames.setDisable(false);
    }
    // Check if user has fifty games played and display badge
    if (currentUser.fiftyGamesPlayed()) {
      silverGames.getTooltip().setText("DEDICATED SKETCHER! \n For playing 50 games.");
      silverGames.setDisable(false);
    }
    // Check if user has one hundred games played and display badge
    if (currentUser.hundredGamesPlayed()) {
      goldGames.getTooltip().setText("SUPERSTAR SKETCHER! \n For playing 100 games.");
      goldGames.setDisable(false);
    }

    // Check if user has won a game under 30 seconds and display badge
    if (currentUser.underThirtySeconds()) {
      bronzeTime.getTooltip().setText("QUICK DRAWER! \n For winning a game under 30 seconds.");
      bronzeTime.setDisable(false);
    }

    // Check if user has won a game under 20 seconds and display badge
    if (currentUser.underTwentySeconds()) {
      silverTime.getTooltip().setText("SPEEDY SKETCHER! \n For winning a game under 20 seconds.");
      silverTime.setDisable(false);
    }
    // Check if user has won a game under 10 seconds and display badge
    if (currentUser.underTenSeconds()) {
      goldTime
          .getTooltip()
          .setText("SUPER SPEEDY SKETCHER! \n For winning a game under 10 seconds.");
      goldTime.setDisable(false);
    }
  }

  /**
   * This method switches back to the statistics page of the chosen user via a button click.
   *
   * @param event The (button) event which invokes this method.
   */
  @FXML
  private void onStatsSwitch(ActionEvent event) {
    // Obtain scene and set the statistics scene root.
    Scene scene = ((Node) event.getSource()).getScene();
    scene.setRoot(SceneManager.getUiRoot(SceneManager.AppUi.STATS));
  }
}
