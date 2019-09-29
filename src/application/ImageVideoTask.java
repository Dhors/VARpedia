package application;

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
        getFlickrImages();
        videoCreation();
        mergeAudioAndVideo();
        cleanFolder();
        return null;

    }


    // This method will create the no audio .mp4 file
    // It will use the images from flickr to create a slideshow
    // THis slideshow will have the search term as centered text.
    private void videoCreation() throws IOException, InterruptedException {
        String audioFileName = (System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav");

        // To determine the length of the .wav file for generation of the video.
        String audioLengthCommand = ("soxi -D " + audioFileName);
        ProcessBuilder audioLengthBuilder = new ProcessBuilder("bash", "-c", audioLengthCommand);
        Process audioLengthProcess = audioLengthBuilder.start();
        audioLengthProcess.waitFor();

        // Reading the Process output and to determine the
        // Audio length to determine the length of creation.
        BufferedReader stdout = new BufferedReader(new InputStreamReader(audioLengthProcess.getInputStream()));
        String audioLengthDouble = stdout.readLine();
        int audioLength = ((int) Double.parseDouble(audioLengthDouble) + 1);
        double frameRate = _numberOfImages /(Double.parseDouble(audioLengthDouble) + 1);


        // ffmpeg slideshow behaviours is inconsistent for 1 and 2 image size slideshows.
        // As such, I have manually made them into 4 image slides of repeated images
        if (_numberOfImages==1){
            Path source = Paths.get( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "0.jpg");

            File file2 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "1.jpg");
            File file3 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");
            File file4 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            file2.createNewFile();
            file3.createNewFile();
            file4.createNewFile();

            Path source2 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "1.jpg");
            Path source3 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");
            Path source4 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            Files.copy(source, source2, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source, source3, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source, source4, StandardCopyOption.REPLACE_EXISTING);


            frameRate =  frameRate*4;
        }else if (_numberOfImages==2){
            Path source = Paths.get( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "0.jpg");
            Path source2 = Paths.get( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "1.jpg");

            File file3 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");
            File file4 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            file3.createNewFile();
            file4.createNewFile();

            Path source3 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");
            Path source4 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            Files.copy(source2, source3, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source2, source4, StandardCopyOption.REPLACE_EXISTING);

            Files.copy(source, source2, StandardCopyOption.REPLACE_EXISTING);
            frameRate =  frameRate*2;
        }

        String imagesCommand = "ffmpeg -y -framerate " + frameRate + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "%01d.jpg " + "-r 25 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+System.getProperty("user.dir") + "/creations/" + _creationName + "/tempVideo.mp4";
        String textCommand = "ffmpeg -y -i " +System.getProperty("user.dir") + "/creations/" + _creationName + "/tempVideo.mp4" + " -vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4";

        ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash","-c",(imagesCommand+";"+textCommand));

        Process videoBuilderProcess = videoBuilder.start();
        videoBuilderProcess.waitFor();

        return;

    }

    // This method will merge the audio .wav and video .mp4 file to create
    // the creation.
    private void mergeAudioAndVideo() {
        try {
            String createCommand = "ffmpeg -y -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4" + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav " + System.getProperty("user.dir") + "/creations/" + _creationName + ".mp4";

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
        File folder = new File(System.getProperty("user.dir") + "/creations/" + _creationName + "/" );
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




    // This method will use the provided api key to retrieve images related to the user search term
    // A number between 1 and 10 images will be retrieved .
    private void getFlickrImages() {
        try {
            // retrieved api key
            String apiKey = "17ba392cb9876b838e944b76316c2f89";
            String sharedSecret = "f91dad2d72061fa7";

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            String query = _searchTerm;
            int resultsPerPage = _numberOfImages;
            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, resultsPerPage, page);

            int count = 0;
            for (Photo photo : results) {
                try {
                    BufferedImage image = photos.getImage(photo, Size.LARGE);

                    String filename = (""+ count +".jpg");

                    File outputfile = new File(System.getProperty("user.dir") + "/creations/" + _creationName);
                    if (!outputfile.exists()) {
                        outputfile.mkdirs();
                    }

                    File imagefile = new File(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + filename);
                    ImageIO.write(image, "jpg", imagefile);
                    count++;

                } catch (FlickrException fe) {
                    System.err.println("Ignoring image " + photo.getId() + ": " + fe.getMessage());
                }
            }
        } catch(Exception e)

        {
            e.printStackTrace();
        }
    }

}
