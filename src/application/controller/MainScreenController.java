package application.controller;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.IOException;

public class MainScreenController {
	@FXML
	private CheckBox backgroundMusicCheckBox;
	
	public void initialize() {
        Main.setCurrentScene("MainScreenScene");
	    backgroundMusicCheckBox.setSelected(Main.backgroundMusicPlayer().checkBoxesAreSelected());
	}
	
    @FXML
    private void handleListButton() throws IOException {
        Main.changeScene("resources/listCreationsScene.fxml");
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
    	Main.backgroundMusicPlayer().handleBackgroundMusic(backgroundMusicCheckBox.isSelected());
    }
}
