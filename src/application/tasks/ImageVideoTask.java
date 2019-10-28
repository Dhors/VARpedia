package application.tasks;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import application.Main;

public class ImageVideoTask extends Task<Void> {
    private String _searchTerm;
    private String _creationName;
    private int _numberOfImages;

    private final String USER_DIR;
    private final String AUDIO_FILE_DIR;
    private final String TEMP_VIDEO_DIR;
    private final String NO_SOUND_VIDEO_DIR;


    public ImageVideoTask(String searchTerm, String creationName, int numberOfImages) {
        _searchTerm = searchTerm;
        _creationName = creationName;
        _numberOfImages = numberOfImages;

        USER_DIR = System.getProperty("user.dir");
        AUDIO_FILE_DIR = USER_DIR + "/creations/" + _searchTerm + "/" + _searchTerm + ".wav";
        TEMP_VIDEO_DIR = USER_DIR + "/creations/" + _searchTerm + "/tempVideo.mp4";
        NO_SOUND_VIDEO_DIR = USER_DIR + "/creations/" + _searchTerm + "/" + "noSoundVideo.mp4";
    }

    @Override
    protected Void call() throws Exception {
        // All video creation methods are completed in this task
        videoCreation();
        mergeAudioAndVideo();

        // need to create the quiz vids before cleanning
        quizVideoCreation();

        Main.cleanFolders();
        return null;
    }

    // This method will create the no audio .mp4 file
    // It will use the images from flickr to create a slideshow
    // THis slideshow will have the search term as centered text.
    private void videoCreation() throws IOException, InterruptedException {
        // To determine the length of the .wav file for generation of the video.
        String audioLengthCommand = ("soxi -D " + AUDIO_FILE_DIR);
        ProcessBuilder audioLengthBuilder = new ProcessBuilder("bash", "-c", audioLengthCommand);
        Process audioLengthProcess = audioLengthBuilder.start();
        audioLengthProcess.waitFor();

        // Reading the Process output and to determine the
        // Audio length to determine the length of creation.
        BufferedReader stdout = new BufferedReader(new InputStreamReader(audioLengthProcess.getInputStream()));
        String audioLengthDouble = stdout.readLine();

        double frameRate = _numberOfImages / (Double.parseDouble(audioLengthDouble) + 1);

        String imagesDirs = USER_DIR + "/creations/" + _searchTerm + "/*.jpg";
        String imageCommand = "cat " + imagesDirs + " | ffmpeg -f image2pipe -framerate " + frameRate + " -i - -c:v libx264 -pix_fmt yuv420p -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" -r 25 -y " + TEMP_VIDEO_DIR;
        String textCommand = "ffmpeg -y -i " + TEMP_VIDEO_DIR + " -vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" " + NO_SOUND_VIDEO_DIR;

        ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash", "-c", (imageCommand + ";" + textCommand));

        Process videoBuilderProcess = videoBuilder.start();
        videoBuilderProcess.waitFor();
    }

    // This method will merge the audio .wav and video .mp4 file to create
    // the creation.
    private void mergeAudioAndVideo() {
        try {
            String videoOutputDir = USER_DIR + "/creations/" + _creationName + ".mp4";
            String command = "ffmpeg -y -i " + NO_SOUND_VIDEO_DIR + " -i " + AUDIO_FILE_DIR + " " + videoOutputDir;

            ProcessBuilder finalVideoBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            Process finalVideoBuilderProcess = finalVideoBuilder.start();
            finalVideoBuilderProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void quizVideoCreation() {
        //the search term is the quiz video name so there is no need to repeat.
        File quizVideo = new File(USER_DIR + "/quiz/" + _searchTerm + ".mp4");
        if (!quizVideo.exists()) {
            try {
                String quizVideoDir = USER_DIR + "/quiz/" + _searchTerm + ".mp4";
                String command = "ffmpeg -y -i " + TEMP_VIDEO_DIR + " -i " + AUDIO_FILE_DIR + " " + quizVideoDir;

                ProcessBuilder quizVideoBuilder = new ProcessBuilder("/bin/bash", "-c", command);
                Process quizVideoBuilderProcess = quizVideoBuilder.start();
                quizVideoBuilderProcess.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
