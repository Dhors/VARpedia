package application.controller;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;



/** Media video creation will play as soon as the scene is loaded included
    is a pause/play button, skip forwards, skip backwards and mute button */
public class playerController {
	@FXML
	private CheckBox backgroundMusicCheckBox;
	
	@FXML
    private BorderPane _player;
    @FXML
    private Button _returnButton;
    @FXML
    private Text _videoTitle;

    @FXML
    private MediaView _mediaView;
    private MediaPlayer _mediaPlayer;
    
    public void initialize(){
        Main.setCurrentScene("PlayerScene");
        backgroundMusicCheckBox.setSelected(Main.backgroundMusicPlayer().checkBoxesAreSelected());
        
        _player.getChildren().removeAll();

        _videoTitle.setText("Now Playing: " + ListController.getSelectedCreationName());
        Media video = new Media(ListController.getSelectedFile().toURI().toString());
        _mediaPlayer = new MediaPlayer(video);
        _mediaPlayer.setAutoPlay(true);

        _mediaView.setMediaPlayer(_mediaPlayer);
        _player.setCenter(_mediaView);

        //Once the video is finished the user will return to the main menu.
        _mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                _returnButton.fire();
            }
        });
    }

    @FXML
    private void handlePausePlayButton() throws IOException {
        if (_mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            _mediaPlayer.pause();
        } else {
            _mediaPlayer.play();
        }
    }

    // for muting the audio of the video
    // Video will continue to play
    @FXML
    private void handleMuteButton() throws IOException {
        _mediaPlayer.setMute( !_mediaPlayer.isMute() );
    }

    // Will skip forwards 3 seconds in the video time.
    @FXML
    private void handleForwardButton() throws IOException {
    	changeVideoTime(3);
    }

    // Will skip back 3 seconds in the video time.
    @FXML
    private void handleBackwardsButton() throws IOException {
    	changeVideoTime(-3);
    }

    private void changeVideoTime(int secondsToAdd) {
    	_mediaPlayer.seek( _mediaPlayer.getCurrentTime().add( Duration.seconds(secondsToAdd)) );
    }

    // Return to main menu
    @FXML
    private void handleReturnButton() throws IOException {
        _mediaPlayer.stop();
        _mediaPlayer.dispose();
        Main.changeScene("resources/listCreationsScene.fxml");
    }

    @FXML
    private void handleBackgroundMusic() throws IOException {
    	Main.backgroundMusicPlayer().handleBackgroundMusic(backgroundMusicCheckBox.isSelected());
    }
}
