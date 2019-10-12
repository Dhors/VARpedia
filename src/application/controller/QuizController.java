package application.controller;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class QuizController {

    private String _quizTerm;
    private File _quizVideo;

    @FXML
    private Pane _quizPlayer;
    @FXML
    private TextField _playerAnswerTextField;
    @FXML
    private Button _startButton;

    MediaPlayer _mediaPlayer;
    MediaView _mediaView;


    @FXML
    private void handleStartButton() throws IOException {
        _quizPlayer.getChildren().removeAll();
        _quizPlayer.getChildren().clear();

        playRandomQuiz();
        //_videoTitle.setText("Now Playing: " + ListController.getSelectedCreationName());
        Media video = new Media(_quizVideo.toURI().toString());
        _mediaPlayer = new MediaPlayer(video);
        _mediaPlayer.setAutoPlay(true);
        _mediaView = new MediaView(_mediaPlayer);
        _mediaView.setFitHeight(250);
        _mediaView.setFitWidth(300);



        _mediaPlayer.setOnReady(new Runnable() {
            @Override public void run() {
                //
            }
        });


        _quizPlayer.getChildren().add(_mediaView);

        //Once the video is finished the user will return to the main menu.
        _mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                _mediaPlayer.seek(Duration.ZERO);
                _mediaPlayer.play();
            }
        });
    }




    @FXML
    private void handleCheckButton() throws IOException {

        if (checkAnswer(_playerAnswerTextField.getText())){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Correct");
            alert.setHeaderText(null);
            alert.setContentText("next one");
            alert.showAndWait();
            _startButton.fire();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Wrong");
            alert.setHeaderText(null);
            alert.setContentText("try again");
            alert.showAndWait();
        }
    }









    // Return to main menu
    @FXML
    private void handleReturnButton() throws IOException {
        Main.setScene("resources/MainScreenScene.fxml");

    }




    private boolean checkAnswer(String answer){
        //null checker?
        if (answer==null){
            return false;
        }
        return answer.equalsIgnoreCase(_quizTerm);
    }

    private void playRandomQuiz() {
        File folder = new File(System.getProperty("user.dir") + "/quiz/");
        File[] arrayQuizVideos = folder.listFiles();

        ArrayList<File> listQuizVideos = new ArrayList<File>(Arrays.asList(arrayQuizVideos));


        Collections.shuffle(listQuizVideos);

        Random rand = new Random();
        _quizVideo = listQuizVideos.get(rand.nextInt(listQuizVideos.size()));

        _quizTerm = _quizVideo.getName().replace(".mp4", "");


    }


}
