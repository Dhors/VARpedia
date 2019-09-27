package application;

import javafx.concurrent.Task;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class FlickrImagesTask extends Task<Void> {


    private String _searchTerm;
    private String _creationName;
    private int _numberOfImages;


    FlickrImagesTask(String searchTerm, String creationName, int numberOfImages) {
        _searchTerm =searchTerm;
        _creationName =  creationName;
        _numberOfImages =numberOfImages;
    }


    @Override
    protected Void call() throws Exception {
        try {

            String apiKey = "17ba392cb9876b838e944b76316c2f89";
            String sharedSecret = "f91dad2d72061fa7";

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            String query = "bicycle";
            int resultsPerPage = 5;
            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, resultsPerPage, page);
            System.out.println("Retrieving " + results.size() + " results");

            int count =1;
            for (Photo photo : results) {
                try {
                    BufferedImage image = photos.getImage(photo, Size.LARGE);

                    //String filename = query.trim().replace(' ', '-') + "-" + System.currentTimeMillis() + "-" + photo.getId() + ".jpg";
                    String filename = ("" + count+".jpeg");
                    File outputfile = new File(System.getProperty("user.dir")+"/creations/"+ _creationName+ "/"+filename );
                   // File outputfile = new File("downloads", filename);
                    ImageIO.write(image, "jpg", outputfile);
                   // System.out.println("Downloaded " + filename);
                    count++;

                } catch (FlickrException fe) {
                    System.err.println("Ignoring image " + photo.getId() + ": " + fe.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nDone");

        return null;


    }




}
