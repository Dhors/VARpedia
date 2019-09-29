package application.controller;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;



/** Media video creation will play as soon as the scene is loaded included
    is a pause/play button, skip forwards, skip backwards and mute button*/
public class playerController {

    @FXML
    private Pane _player;
    @FXML
    private Button _returnButton;
    @FXML
    private Text _videoTitle;

    MediaPlayer _mediaPlayer;
    MediaView _mediaView;

    public void initialize(){

        _player.getChildren().removeAll();


        _videoTitle.setText("Now Playing: " + ListController.getSelectedCreationName());
        Media video = new Media(ListController.getSelectedFile().toURI().toString());
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
        _player.getChildren().add(_mediaView);

        //Once the video is finished the user will return to the main menu.
        _mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                _returnButton.fire();
            }
        });

    }




    @FXML
    private void handlePausePlayButton(ActionEvent event) throws IOException {
        if (_mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            _mediaPlayer.pause();
        } else {
            _mediaPlayer.play();
        }

    }

    // for muting the audio of the video
    // Video will continue to play
    @FXML
    private void handleMuteButton(ActionEvent event) throws IOException {
        _mediaPlayer.setMute( !_mediaPlayer.isMute() );
    }

    // Will skip forwards 3 seconds in the video time.
    @FXML
    private void handleForwardButton(ActionEvent event) throws IOException {

        _mediaPlayer.seek( _mediaPlayer.getCurrentTime().add( Duration.seconds(3)) );


    }

    // Will skip back 3 seconds in the video time.
    @FXML
    private void handleBackwardsButton(ActionEvent event) throws IOException {
        _mediaPlayer.seek( _mediaPlayer.getCurrentTime().add( Duration.seconds(-3)) );
    }



    // Return to main menu
    @FXML
    private void handleReturnButton(ActionEvent event) throws IOException {
        _mediaPlayer.stop();
        Parent _Parent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
        Scene _Scene = new Scene(_Parent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(_Scene);
        window.show();
    }

}
