package application.controller;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;

import java.io.File;
import java.io.IOException;

public class MainScreenController {
	@FXML
	private ToggleButton backgroundMusicButton;
	
	public void initialize() {
        Main.setCurrentScene("MainScreenScene");
        backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
        backgroundMusicButton.setSelected(Main.backgroundMusicPlayer().getButtonIsSelected());
	}
	
    @FXML
    private void handleListButton() throws IOException {
        Main.changeScene("resources/ListCreationsScene.fxml");
    }

    @FXML
    private void handleNewCreationButton() throws IOException {
        Main.changeScene("resources/NewCreationScene.fxml");
    }
    
    @FXML
    private void handleQuizButton() throws IOException {
        File quizFolder = new File(System.getProperty("user.dir") + "/quiz/");
        if (!(quizFolder.listFiles().length== 0)){
            Main.changeScene("resources/QuizScene.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No quiz videos");
            alert.setContentText("Please create a creation first and then start the quiz.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleBackgroundMusic() throws IOException {
    	Main.backgroundMusicPlayer().handleBackgroundMusic(backgroundMusicButton.isSelected());
    	backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
    }

    @FXML
    private void handleCreateInformation() throws IOException {
        Alert creationInProgressPopup = new Alert(Alert.AlertType.INFORMATION);
        creationInProgressPopup.setTitle("Make a new creation");
        creationInProgressPopup.setHeaderText("Make a brand cew creation to watch or learn from");
        creationInProgressPopup.setContentText("YOu will be guided through the creation process step by step." +
                "You can make a creation from anything that you want");
        creationInProgressPopup.show();
    }
    @FXML
    private void handlePlayInformation() throws IOException {
        Alert creationInProgressPopup = new Alert(Alert.AlertType.INFORMATION);
        creationInProgressPopup.setTitle("Watch a creation");
        creationInProgressPopup.setHeaderText("Watch any creation you have made.");
        creationInProgressPopup.setContentText("You will see a list of creations that you have made in the past" +
                                                " You can play any creation that you want to.");
        creationInProgressPopup.show();
    }
    @FXML
    private void handleLearnInformation() throws IOException {
        Alert creationInProgressPopup = new Alert(Alert.AlertType.INFORMATION);
        creationInProgressPopup.setTitle("Play a game");
        creationInProgressPopup.setHeaderText("A fun quiz game begins when you press the button.");
        creationInProgressPopup.setContentText("You will be quizzed based on all past creations you have made." +
                "Good luck going for the highest score.");
        creationInProgressPopup.show();

    }

}
