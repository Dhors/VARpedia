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
import java.util.ArrayList;


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



        getFlickrImages();
        videoCreation();
        mergeAudioAndVideo();
        //cleanFolder();
        return null;


    }

    private void videoCreation() throws IOException, InterruptedException {

        String audioLengthCommand = ("soxi -D " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav");

        ProcessBuilder audioLengthBuilder = new ProcessBuilder("bash", "-c", audioLengthCommand);
        Process audioLengthProcess = audioLengthBuilder.start();
        audioLengthProcess.waitFor();

        // Reading the Process output and to determine the
        // Audio length to determine the length of creation.
        BufferedReader stdout = new BufferedReader(new InputStreamReader(audioLengthProcess.getInputStream()));
        String audioLengthDouble = stdout.readLine();

        double rateOfImages = _numberOfImages/(Double.parseDouble(audioLengthDouble) + 1);


        // ffmpeg is buggy I didnt want to deal with it either.
        if (_numberOfImages==1){
            Path source = Paths.get( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "1.jpg");

            File file2 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");
            File file3 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            File file4 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "4.jpg");
            file2.createNewFile();
            file3.createNewFile();
            file4.createNewFile();

            Path source2 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");
            Path source3 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            Path source4 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "4.jpg");
            Files.copy(source, source2, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source, source3, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source, source4, StandardCopyOption.REPLACE_EXISTING);
            rateOfImages = rateOfImages*4;
        }else if (_numberOfImages==2){
            Path source = Paths.get( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "1.jpg");
            Path source2 = Paths.get( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "2.jpg");

            File file3 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            File file4 = new File( System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "4.jpg");
            file3.createNewFile();
            file4.createNewFile();

            Path source3 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "3.jpg");
            Path source4 = Paths.get(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "4.jpg");
            Files.copy(source2, source3, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(source2, source4, StandardCopyOption.REPLACE_EXISTING);

            Files.copy(source, source2, StandardCopyOption.REPLACE_EXISTING);
            rateOfImages=  rateOfImages*2;
        }


       String imagesCommand = "ffmpeg -y -framerate " + rateOfImages + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "%01d.jpg " +
                "-pix_fmt yuv420p -r 25 -vf \"scale=trunc(iw/2)*2:trunc(ih/2)*2\" " + "-vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" "
                + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4";

        ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash","-c",imagesCommand);

        Process videoBuilderProcess = videoBuilder.start();
        int exit = videoBuilderProcess.waitFor();

        return;

    }

    private void mergeAudioAndVideo() {
        /** time to merge and put in creations folder as completed */
        try {
            String createCommand = "ffmpeg -y -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4" + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav " + System.getProperty("user.dir") + "/creations/" + _creationName + ".mp4";
            // String cleanCommand = "rm text audio.wav video.mp4";
            // String combinedCommand = (""+ videoCommand+";"+createCommand+";"+cleanCommand);

            ProcessBuilder finalVideoBuilder = new ProcessBuilder("/bin/bash", "-c", createCommand);
            Process finalVideoBuilderProcess = finalVideoBuilder.start();
            int exitFinal = finalVideoBuilderProcess.waitFor();
            System.out.println("exit code of final: " + exitFinal);
            System.out.println("merge audio and video");
            return;


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void cleanFolder() {
        // The creations directory where all creations are stored.
        final File folder = new File(System.getProperty("user.dir") + "/creations/" + _creationName + "/" );
        //ArrayList<String> listFilesNames = new ArrayList<String>();
        //ArrayList<String> listCreationNames = new ArrayList<String>();

        for (final File fileName : folder.listFiles()) {
            fileName.delete();
        }
        folder.delete();

        }





    private void getFlickrImages() {
        try {
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
            System.out.println("Retrieving " + results.size() + " results");

            int count = 1;
            for (Photo photo : results) {
                try {
                    BufferedImage image = photos.getImage(photo, Size.LARGE);

                    //String filename = query.trim().replace(' ', '-') + "-" + System.currentTimeMillis() + "-" + photo.getId() + ".jpg";
                    String filename = (""+ count +".jpg");

                    // fix this somehow
                    File outputfile = new File(System.getProperty("user.dir") + "/creations/" + _creationName);
                    if (!outputfile.exists()) {
                        outputfile.mkdirs();
                    }

                    File imagefile = new File(System.getProperty("user.dir") + "/creations/" + _creationName + "/" + filename);
                    // File outputfile = new File("downloads", filename);
                    ImageIO.write(image, "jpg", imagefile);
                    // System.out.println("Downloaded " + filename);
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
