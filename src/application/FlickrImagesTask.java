package application;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class FlickrImagesTask extends Task<Void> {

    private String _searchTerm;
    //private String _creationName;

    public FlickrImagesTask(String searchTerm){

        _searchTerm = searchTerm;
    }


    @Override
    protected Void call() throws Exception {
        getFlickrImages();
        updateProgress(0, 10);
        return null;
    }




    // This method will use the provided api key to retrieve images related to the user search term
    // A number between 1 and 10 images will be retrieved.
    private void getFlickrImages() {
        try {
            // retrieved api key
            String apiKey = "17ba392cb9876b838e944b76316c2f89";
            String sharedSecret = "f91dad2d72061fa7";

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            String query = _searchTerm;
            int resultsPerPage = 10;
            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, resultsPerPage, page);

            File outputfile = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm);
            if (!outputfile.exists()) {
                outputfile.mkdirs();
            }

            int count = 0;
            for (Photo photo : results) {
                try {
                    BufferedImage image = photos.getImage(photo, Size.LARGE);

                    String filename = (""+ count +".jpg");



                    File imagefile = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm + "/" + filename);
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
