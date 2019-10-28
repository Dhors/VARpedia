package application.controller;

import application.Main;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;

import java.awt.*;
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
        Main.changeScene("resources/listCreationsScene.fxml");
    }

    @FXML
    private void handleNewCreationButton() throws IOException {
        Main.changeScene("resources/newCreationScene.fxml");
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
          //  File myFile = new File(System.getProperty("user.dir") + "/VARpediaUserManual.pdf");
          //  Desktop.getDesktop().open(myFile);
    }
    @FXML
    private void handlePlayInformation() throws IOException {

    }
    @FXML
    private void handleLearnInformation() throws IOException {


    }

}
