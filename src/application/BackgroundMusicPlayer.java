package application;

import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class BackgroundMusicPlayer {
	private final File BACKGROUND_MUSIC_DIR;
	private boolean isPlaying;
	private MediaPlayer _mediaPlayer;
	
	public BackgroundMusicPlayer() {
		BACKGROUND_MUSIC_DIR = new File(System.getProperty("user.dir")+"/blue.wav");
		isPlaying = false;
		createMediaPlayer();
	}
	
	public void createMediaPlayer() {
		Media video = new Media(BACKGROUND_MUSIC_DIR.toURI().toString());
		_mediaPlayer = new MediaPlayer(video);
		
		//Once the video is finished the video will replay from the start
        _mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                _mediaPlayer.seek(Duration.ZERO);
            }
        });
	}
	
	public void handleBackgroundMusic(boolean checkBoxIsSelected) {
		if (checkBoxIsSelected) {
			_mediaPlayer.play();
			isPlaying = true;
    	} else {
    		_mediaPlayer.pause();
    		isPlaying = false;
    	}
	}
	
	public boolean checkBoxesAreSelected() {
		return isPlaying;
	}
}
