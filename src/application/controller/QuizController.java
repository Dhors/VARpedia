package application.controller;

import application.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizController {
	private final int MEDIA_VIEW_WIDTH = 600;
	private final int MEDIA_VIEW_HEIGHT = 400;
	
    private String _quizTerm;
    private File _quizVideo;

    @FXML
    private Pane _quizPlayer;
    @FXML
    private TextField _playerAnswerTextField;
    @FXML
    private Button _startButton;
    @FXML
    private Button _skipButton;
    @FXML
    private Button _checkButton;
    @FXML
    private Button _pauseButton;

    MediaPlayer _mediaPlayer;
    MediaView _mediaView;

    public void initialize(){
        _startButton.setVisible(true);

        _pauseButton.setVisible(false);
        _checkButton.setVisible(false);
        _skipButton.setVisible(false);
        _playerAnswerTextField.setVisible(false);

        // Don't let the user check their answer until they enter an answer
        BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() ->
        	_playerAnswerTextField.getText().trim().isEmpty(),
        	_playerAnswerTextField.textProperty());
        _checkButton.disableProperty().bind(textIsEmpty);
    }


    @FXML
    private void handleStartButton() throws IOException {
        _startButton.setVisible(false);

        _pauseButton.setVisible(true);
        _checkButton.setVisible(true);
        _skipButton.setVisible(true);
        _playerAnswerTextField.setVisible(true);

        _quizPlayer.getChildren().removeAll();
        _quizPlayer.getChildren().clear();

        getRandomQuiz();
        
        Media video = new Media(_quizVideo.toURI().toString());
        _mediaPlayer = new MediaPlayer(video);
        _mediaPlayer.setAutoPlay(true);
        
        _mediaView = new MediaView(_mediaPlayer);
        _mediaView.setFitHeight(MEDIA_VIEW_HEIGHT);
        _mediaView.setFitWidth(MEDIA_VIEW_WIDTH);

        _quizPlayer.getChildren().add(_mediaView);

        //Once the video is finished the video will replay from the start
        _mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                _mediaPlayer.seek(Duration.ZERO);
                _mediaPlayer.play();
            }
        });
    }

    @FXML
    private void handleCheckButton() throws IOException {
        _mediaPlayer.pause();
        
        boolean answerIsCorrect = _playerAnswerTextField.getText().equalsIgnoreCase(_quizTerm);
        if (answerIsCorrect){
            Alert correctAnswerPopup = new Alert(Alert.AlertType.INFORMATION);
            correctAnswerPopup.setTitle("Correct!");
            correctAnswerPopup.setHeaderText(null);
            correctAnswerPopup.setContentText("Now try the next one");
            correctAnswerPopup.showAndWait();
            _startButton.fire();
        } else {
            Alert incorrectAnswerPopup = new Alert(Alert.AlertType.INFORMATION);
            incorrectAnswerPopup.setTitle("Incorrect");
            incorrectAnswerPopup.setHeaderText(null);
            incorrectAnswerPopup.setContentText("Please try again");
            incorrectAnswerPopup.showAndWait();
            _mediaPlayer.play();
        }
    }

    @FXML
    private void handlePauseButton() throws IOException {
        if (_mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            _mediaPlayer.pause();
        } else {
            _mediaPlayer.play();
        }
    }

    // Return to main menu
    @FXML
    private void handleReturnButton() throws IOException {
        if (_mediaPlayer != null) {
        	_mediaPlayer.stop();
        }
        Main.changeScene("resources/MainScreenScene.fxml");
    }

    @FXML
    private void handleSkipButton() throws IOException {
        _startButton.fire();
    }

    private void getRandomQuiz() {
        File quizFolder = new File(System.getProperty("user.dir") + "/quiz/");
        File[] quizVideosArray = quizFolder.listFiles();

        List<File> quizVideosList = new ArrayList<File>(Arrays.asList(quizVideosArray));

        Collections.shuffle(quizVideosList);

        Random rand = new Random();
        _quizVideo = quizVideosList.get(rand.nextInt(quizVideosList.size()));

        _quizTerm = _quizVideo.getName().replace(".mp4", "");
    }
}
