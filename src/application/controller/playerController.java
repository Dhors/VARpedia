package application.controller;

import application.Main;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class playerController {

    @FXML
    private Pane _player;


    MediaPlayer _mediaPlayer;
    MediaView _mediaView;

    public void initialize(){
        _player.getChildren().removeAll();

        Media video = new Media(ListController.getSelectedFile().toURI().toString());
        _mediaPlayer = new MediaPlayer(video);
        _mediaPlayer.setAutoPlay(true);
        _mediaView = new MediaView(_mediaPlayer);
        _mediaView.setFitHeight(400);
        _mediaView.setFitWidth(400);



        _mediaPlayer.setOnReady(new Runnable() {
            @Override public void run() {
                //
            }
        });
        _player.getChildren().add(_mediaView);

    }




    @FXML
    private void handlePausePlayButton(ActionEvent event) throws IOException {
        if (_mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            _mediaPlayer.pause();
        } else {
            _mediaPlayer.play();
        }

    }

    @FXML
    private void handleMuteButton(ActionEvent event) throws IOException {
        _mediaPlayer.setMute( !_mediaPlayer.isMute() );



    }

    @FXML
    private void handleForwardButton(ActionEvent event) throws IOException {

        _mediaPlayer.seek( _mediaPlayer.getCurrentTime().add( Duration.seconds(3)) );


    }

    @FXML
    private void handleBackwardsButton(ActionEvent event) throws IOException {

        _mediaPlayer.seek( _mediaPlayer.getCurrentTime().add( Duration.seconds(-3)) );


    }




    @FXML
    private void handleReturnButton(ActionEvent event) throws IOException {
        _mediaPlayer.stop();

        Parent listViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
        Scene listViewScene = new Scene(listViewParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(listViewScene);
        window.show();
    }



}
