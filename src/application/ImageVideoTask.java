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



        // System.out.println("ffmpeg -y -framerate "+0.5+" -i "+System.getProperty("user.dir")+"/creations/" + _creationName +"/"+"image{1..10}.jpg -r 25 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" "+System.getProperty("user.dir")+"/creations/" + _creationName +"/"+"tempVideo.mp4");
        getFlickrImages();
        System.out.println("got flickr");
        videoCreation();
        System.out.println("video creation isnt working fuck me");

        mergeAudioAndVideo();
        return null;


    }

    private void videoCreation() throws IOException, InterruptedException {
        // Audio creation using bash commands
        //String audioCommand = "cat text | text2wave -o audio.wav";
        String audioFileName = (System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav");
        String videoFileName = (System.getProperty("user.dir") + "/creations/" + _creationName + ".mp4");


        String audioLengthCommand = ("soxi -D " + audioFileName);

        // ProcessBuilder audioBuilder = new ProcessBuilder("bash", "-c", audioCommand);
        // Process audioProcess = audioBuilder.start();
        // audioProcess.waitFor();
        ProcessBuilder audioLengthBuilder = new ProcessBuilder("bash", "-c", audioLengthCommand);
        Process audioLengthProcess = audioLengthBuilder.start();
        audioLengthProcess.waitFor();

        // Reading the Process output and to determine the
        // Audio length to determine the length of creation.
        BufferedReader stdout = new BufferedReader(new InputStreamReader(audioLengthProcess.getInputStream()));
        String audioLengthDouble = stdout.readLine();
        int audioLength = ((int) Double.parseDouble(audioLengthDouble) /*+ 1*/);
        double length = audioLength;

        length = (_numberOfImages / length);
        //double lengthOfOneImage = (double)((audioLength -1)/_numberOfImages);

        System.out.println("fucking end me");


        String imagesCommand = "ffmpeg -y -framerate " + length + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "%01d.jpg " +
                "-r 25 " + "-vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" "
                + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4";

     //   String imagesCommand2 = ""+ System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "%01d.jpg | ffmpeg -f image2pipe -framerate "+ length + "-r 25 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" " + "-vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" "
        //                + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4";
       /* safecopy
       String imagesCommand = "ffmpeg -y -framerate " + length + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "%01d.jpg " +
                "-r 25 -vf \"pad=ceil(iw/2)*2:ceil(ih/2)*2\" " + "-vf \"drawtext=fontfile=myfont.ttf:fontsize=30:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" "
                + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4"; */


        //ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash", "-c", (imagesCommand + ";" + addTextCommand));
        ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash","-c",imagesCommand);
        System.out.println(imagesCommand);
        //ProcessBuilder videoBuilder = new ProcessBuilder("/bin/bash","-c",imagesCommand);
        Process videoBuilderProcess = videoBuilder.start();
        int exit = videoBuilderProcess.waitFor();
        System.out.println("exit code: " + exit);

        // Video creation followed by creation completion by merging audio and video using bash commands

        /** time to merge and put in creations folder as completed */
      /*  String createCommand = "ffmpeg -y -i " +System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4" +" -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav " + System.getProperty("user.dir") + "/creations/" + _creationName+".mp4";
        // String cleanCommand = "rm text audio.wav video.mp4";
        // String combinedCommand = (""+ videoCommand+";"+createCommand+";"+cleanCommand);

        ProcessBuilder finalVideoBuilder = new ProcessBuilder("/bin/bash","-c",createCommand);
        Process finalVideoBuilderProcess = finalVideoBuilder.start();
        int exitFinal = finalVideoBuilderProcess.waitFor();
        System.out.println("exit code of final: " + exitFinal);
        System.out.println("merge audio and video");*/
        return;

    }

    private void mergeAudioAndVideo() {
        /** time to merge and put in creations folder as completed */
        try {
            String createCommand = "ffmpeg -y -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + "noSoundVideo.mp4" + " -i " + System.getProperty("user.dir") + "/creations/" + _creationName + "/" + _creationName + ".wav " + System.getProperty("user.dir") + "/creations/" + _creationName + "/"+ _creationName + ".mp4";
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







    private void getFlickrImages() {
        try {
            String apiKey = "17ba392cb9876b838e944b76316c2f89";
            String sharedSecret = "f91dad2d72061fa7";

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            String query = _searchTerm;
            int resultsPerPage = _numberOfImages;
            //int resultsPerPage = 3;
            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, resultsPerPage, page);
            System.out.println("Retrieving " + results.size() + " results");

            int count = 0;
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
