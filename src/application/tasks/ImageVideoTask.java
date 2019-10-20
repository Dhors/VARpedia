package application.tasks;

import javafx.concurrent.Task;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImageVideoTask extends Task<Void> {


    private String _searchTerm;
    private String _creationName;
    private int _numberOfImages;


    public ImageVideoTask(String searchTerm, String creationName, int numberOfImages) {
        _searchTerm = searchTerm;
        _creationName = creationName;
        _numberOfImages = numberOfImages;
    }


    @Override
    protected Void call() throws Exception {
        // All video creation methods are completed in this task
        videoCreation();
        mergeAudioAndVideo();

        // need to create the quiz vids before cleanning
        quizVideoCreation();

        cleanFolder();
        return null;

    }


    // This method will create the no audio .mp4 file
    // It will use the images from flickr to create a slideshow
    // THis slideshow will have the search term as centered text.
    private void videoCreation() throws IOException, InterruptedException {
        String audioFileName = (System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + _searchTerm+ ".wav");

        // To determine the length of the .wav file for generation of the video.
        String audioLengthCommand = ("soxi -D " + audioFileName);
        ProcessBuilder audioLengthBuilder = new ProcessBuilder("bash", "-c", audioLengthCommand);
        Process audioLengthProcess = audioLengthBuilder.start();
        audioLengthProcess.waitFor();

        // Reading the Process output and to determine the
        // Audio length to determine the length of creation.
        BufferedReader stdout = new BufferedReader(new InputStreamReader(audioLengthProcess.getInputStream()));
        String audioLengthDouble = stdout.readLine();
        //int audioLength = ((int) Double.parseDouble(audioLengthDouble));

        double frameRate = _numberOfImages /(Double.parseDouble(audioLengthDouble) +1);
        System.out.println("Framerate: "+frameRate+ "number images: "+ _numberOfImages );


        String imageCommand = "cat " + System.getProperty("user.dir") + "/creations/" +_searchTerm+ "/*.jpg | ffmpeg -f image2pipe -framerate " + frameRate + " -i - -c:v libx264 -pix_fmt yuv420p -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" -r 25 -y " +System.getProperty("user.dir") + "/creations/" + _searchTerm + "/tempVideo.mp4";


        String imagesCommand = "ffmpeg -y -framerate " + frameRate + " -i " + System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + "%01d.jpg " + "-r 25 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+System.getProperty("user.dir") + "/creations/" + _searchTerm + "/tempVideo.mp4";
        String textCommand = "ffmpeg -y -i " +System.getProperty("user.dir") + "/creations/" + _searchTerm + "/tempVideo.mp4" + " -vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" " + System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + "noSoundVideo.mp4";

        ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash","-c",(imageCommand+";"+textCommand));

        Process videoBuilderProcess = videoBuilder.start();
        videoBuilderProcess.waitFor();

        return;

    }

    // This method will merge the audio .wav and video .mp4 file to create
    // the creation.
    private void mergeAudioAndVideo() {
        try {
            String createCommand = "ffmpeg -y -i " + System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + "noSoundVideo.mp4" + " -i " + System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + _searchTerm + ".wav " + System.getProperty("user.dir") + "/creations/" + _creationName + ".mp4";

            ProcessBuilder finalVideoBuilder = new ProcessBuilder("/bin/bash", "-c", createCommand);
            Process finalVideoBuilderProcess = finalVideoBuilder.start();
            finalVideoBuilderProcess.waitFor();

            return;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    // This method will clean the temporary fold that stored the audio chunks, the flikr images
    // the no audio .mp4 file the .wav file as well as the folders themselves.
    private void cleanFolder() {
        // The creations directory where all creations are stored.
        File folder = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm+ "/" );
        for (final File fileName : folder.listFiles()) {
            fileName.delete();
        }
        folder.delete();

        // The chunks directory where all audio chunks are stored.
        File folderChunk = new File(System.getProperty("user.dir") + "/chunks/" );
        for (final File fileNameChunk : folderChunk.listFiles()) {
            fileNameChunk.delete();
        }
        folderChunk.delete();
        }





    private void quizVideoCreation(){
            //the search term is the quiz video name so there is no need to repeat.
             File quizVideo = new File(System.getProperty("user.dir") + "/quiz/" + _searchTerm + ".mp4");
            if (!quizVideo.exists()) {
             try {
                String createCommand = "ffmpeg -y -i " + System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + "tempVideo.mp4" + " -i " + System.getProperty("user.dir") + "/creations/" + _searchTerm+ "/" + _searchTerm + ".wav " + System.getProperty("user.dir") + "/quiz/" + _searchTerm + ".mp4";

                ProcessBuilder finalVideoBuilder = new ProcessBuilder("/bin/bash", "-c", createCommand);
                Process finalVideoBuilderProcess = finalVideoBuilder.start();
                finalVideoBuilderProcess.waitFor();

                return;

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


    }



}
