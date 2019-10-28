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
        Alert createInfo = new Alert(Alert.AlertType.INFORMATION);
        createInfo.getDialogPane().getStylesheets().add(("Alert.css"));

        createInfo.setTitle("Make a new creation");
        createInfo.setHeaderText("Make a brand cew creation to watch or learn from.");
        createInfo.setContentText("You will be guided through the creation process step by step. " +
                "You can make a creation for anything that you want to know about.");
        createInfo.show();
    }
    @FXML
    private void handlePlayInformation() throws IOException {
        Alert playInfo = new Alert(Alert.AlertType.INFORMATION);
        playInfo.getDialogPane().getStylesheets().add(("Alert.css"));
        playInfo.setTitle("Watch a creation");
        playInfo.setHeaderText("Watch any creation that you have made.");
        playInfo.setContentText("You will see a list of creations that you have made in the past. " +
                                                "You can play any creation that you want to.");
        playInfo.show();
    }
    @FXML
    private void handleLearnInformation() throws IOException {
        Alert learnInfo = new Alert(Alert.AlertType.INFORMATION);
        learnInfo.setTitle("Play a game");
        learnInfo.getDialogPane().getStylesheets().add(("Alert.css"));
        learnInfo.setHeaderText("A fun quiz game begins when you press the button.");
        learnInfo.setContentText("You will be quizzed based on all past creations you have made. " +
                "Good luck going for the highest score.");
        learnInfo.show();

    }

    @FXML
    private void handleVarpediaInformation() throws IOException {
        Alert varpediaInfo = new Alert(Alert.AlertType.INFORMATION);
        varpediaInfo.setTitle("Welcome to VARpedia");
        varpediaInfo.getDialogPane().getStylesheets().add(("Alert.css"));
        varpediaInfo.setHeaderText("A Visual, Aural and Reading encyclopedia.");
        varpediaInfo.setContentText("For the feature overview of this application please " +
                "refer to the User Manual where each feature is explained fully.");
        varpediaInfo.show();

    }



}
