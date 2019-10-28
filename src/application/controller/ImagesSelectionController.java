package application.controller;

import application.tasks.FlickrImagesTask;
import application.Main;
import application.tasks.ImageVideoTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagesSelectionController {
    @FXML private ImageView _ImageView0;
    @FXML private ImageView _ImageView1;
    @FXML private ImageView _ImageView2;
    @FXML private ImageView _ImageView3;
    @FXML private ImageView _ImageView4;
    @FXML private ImageView _ImageView5;
    @FXML private ImageView _ImageView6;
    @FXML private ImageView _ImageView7;
    @FXML private ImageView _ImageView8;
    @FXML private ImageView _ImageView9;
    @FXML private CheckBox _checkBox0;
    @FXML private CheckBox _checkBox1;
    @FXML private CheckBox _checkBox2;
    @FXML private CheckBox _checkBox3;
    @FXML private CheckBox _checkBox4;
    @FXML private CheckBox _checkBox5;
    @FXML private CheckBox _checkBox6;
    @FXML private CheckBox _checkBox7;
    @FXML private CheckBox _checkBox8;
    @FXML private CheckBox _checkBox9;

    @FXML
    private ToggleButton backgroundMusicButton;

    @FXML
    private ProgressBar _imagesProgressBar;
    @FXML
    private Text _imageDownloadInProgress;

    @FXML
    private Text _instructions;
    @FXML
    private TextField _creationNameTextField;
    @FXML
    private Button _submitButton;
    @FXML
    private Pane _progressPane;
    @FXML
    private ImageView _clockImage;
    @FXML
    private Pane _imagePane;

    private List<ImageView> _flickrImageViewList;
    private List<CheckBox> _checkBoxIncludeImageList;
    private List<Image> _imageList = new ArrayList<Image>();

    private ExecutorService team = Executors.newSingleThreadExecutor();

    private String _searchTerm;
    private int numberOfImages;

    private File imagesFolder;
    private final String CREATIONS_DIR = System.getProperty("user.dir") + "/creations/";

    @FXML
    private void initialize() {
        _clockImage.setVisible(true);
        _progressPane.setVisible(true);
        Main.setCurrentScene("ImageSelectionScene");
        backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
        backgroundMusicButton.setSelected(Main.backgroundMusicPlayer().getButtonIsSelected());

        _searchTerm = CreationController.getSearchTerm();
        imagesFolder= new File(CREATIONS_DIR + _searchTerm);

        _flickrImageViewList = new ArrayList<ImageView>(Arrays.asList(_ImageView0,_ImageView1,_ImageView2,_ImageView3,_ImageView4,_ImageView5
                ,_ImageView6,_ImageView7,_ImageView8,_ImageView9));
        _checkBoxIncludeImageList = new ArrayList<CheckBox>(Arrays.asList(_checkBox0,_checkBox1,_checkBox2,_checkBox3,_checkBox4,_checkBox5
                ,_checkBox6,_checkBox7,_checkBox8,_checkBox9));

        downloadFlickrImages();
    }

    @FXML
    private void handleSubmitButton() throws IOException {
        int numImagesDeleted = 0;
        for (int i = 0; i < _checkBoxIncludeImageList.size(); i++) {
        	if (!_checkBoxIncludeImageList.get(i).isSelected()) {
                File imageFile = new File(imagesFolder + "/" + i + ".jpg");
                imageFile.delete();
                numImagesDeleted++;
            }
        }

        numberOfImages = 10 - numImagesDeleted;
        if (numberOfImages==0) {
            Alert noImagesSelectedError = new Alert(Alert.AlertType.WARNING);
            noImagesSelectedError.setTitle("No images selected");
            noImagesSelectedError.setContentText("Please select at last one image.");
            noImagesSelectedError.showAndWait();
            return;
        }

        // checking that the creation name is valid set of inputs
        if (_creationNameTextField.getText().isEmpty()) {
        	// do nothing
        } else if (!_creationNameTextField.getText().matches("[a-zA-Z0-9_-]*")) {
            Alert invalidCreationNameError = new Alert(Alert.AlertType.WARNING);
            invalidCreationNameError.setTitle("Invalid Creation name");
            invalidCreationNameError.setContentText("Please enter a valid creation name consisting of alphabet letters, digits, underscores, and hyphens only.");
            invalidCreationNameError.showAndWait();
        } else if (!isUniqueCreationName(_creationNameTextField.getText())) {
            // throw alerts
            Alert overrideExistingCreationPopup = new Alert(Alert.AlertType.CONFIRMATION);
            overrideExistingCreationPopup.setTitle("Override");
            overrideExistingCreationPopup.setHeaderText("Creation name already exists");
            overrideExistingCreationPopup.setContentText("Would you like to override the existing creation?");
            Optional<ButtonType> buttonClicked = overrideExistingCreationPopup.showAndWait();

            // Override existing file name
            // This is the same as deleting the current file and creating a new file.
            if (buttonClicked.get() == ButtonType.OK) {
                String creationName = _creationNameTextField.getText();
                File _existingFile = new File(CREATIONS_DIR + creationName + ".mp4");
                _existingFile.delete();

                createVideo(creationName);
            }
        } else {
            //No problems with any inputs will create the creation normally.
            String creationName = _creationNameTextField.getText();
            createVideo(creationName);
        }
    }

    /*The method to create the creation
      This method will pull images from flickr based on user input.
      Using these images a video will be created with the search term added as text.*/
    private void createVideo(String creationName){
    	Alert creationInProgressPopup = new Alert(Alert.AlertType.INFORMATION);
        creationInProgressPopup.setTitle("Creation in progress");
        creationInProgressPopup.setHeaderText("Creation is being made, please wait...");
        creationInProgressPopup.setContentText("You will be informed when the creation is complete.");
        creationInProgressPopup.show();

    	// Thread to ensure that GUI remains concurrent while the video is being created
        ImageVideoTask flickrImagesTask = new ImageVideoTask (_searchTerm, creationName, numberOfImages );
        team.submit(flickrImagesTask);

        // return to main menu
        try {
            Main.changeScene("resources/MainScreenScene.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        flickrImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                // close the 'in progress' popup
            	Button cancelButton = ( Button ) creationInProgressPopup.getDialogPane().lookupButton( ButtonType.OK );
                cancelButton.fire();

                if (Main.getCurrentScene().equals("ListCreationScene")){
                    try {
                        Main.changeScene("resources/listCreationsScene.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Alert creationFinishedPopup = new Alert(Alert.AlertType.INFORMATION);
                creationFinishedPopup.setTitle("Creation completed");
                creationFinishedPopup.setHeaderText("Creation completed: "+ _creationNameTextField.getText() +" is finished");
                creationFinishedPopup.setContentText("You can find it in the Creations or Quiz sections");
                creationFinishedPopup.showAndWait();
            }
        });
    }

    // This method will check if the given name is already associated with
    // an existing creation. Returns false if the creation name is already used.
    // Returns true otherwise.
    private boolean isUniqueCreationName(String creationName){
        File creationsFolder = new File(CREATIONS_DIR);
        for (final File creationFile : creationsFolder.listFiles()) {
            if (creationFile.getName().equals("" + creationName + ".mp4")) {
                // An already existing creation name is invalid.
                return false;
            }
        }
        return true;
    }

    private String getDefaultCreationName(){
        int creationNumber = 1;
        String defaultCreationName;
        File quizFileName;
        do {
        	defaultCreationName = _searchTerm + "-" + creationNumber;
        	quizFileName = new File(CREATIONS_DIR + defaultCreationName + ".mp4");
        	creationNumber++;
        } while (quizFileName.exists());

        return defaultCreationName;
    }

    private void downloadFlickrImages() {
    	FlickrImagesTask imagesTask = new FlickrImagesTask(_searchTerm);

        _imagesProgressBar.progressProperty().bind(imagesTask.progressProperty());

        team.submit(imagesTask);
        imagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                _progressPane.setVisible(false);
            	_imageDownloadInProgress.setVisible(false);
                _imagesProgressBar.setVisible(false);
                _clockImage.setVisible(false);

                populateFlickrImageViews();
            }
        });
    }

    private void populateFlickrImageViews() {
    	File[] imageFileArray = imagesFolder.listFiles();
    	Arrays.sort(imageFileArray);
    	for (File imageFile : imageFileArray) {
            if (imageFile.getName().endsWith(".jpg")) {
            	_imageList.add(new Image(imageFile.toURI().toString()));
            }
        }

        for (int i = 0; i < _flickrImageViewList.size(); i++) {
        	ImageView flickrImageView = _flickrImageViewList.get(i);
        	flickrImageView.setImage(_imageList.get(i));
        }

        for (ImageView flickrImageView: _flickrImageViewList) {
            flickrImageView.setVisible(true);
        }
        for (CheckBox checkBoxIcludeImage: _checkBoxIncludeImageList) {
            checkBoxIcludeImage.setVisible(true);
        }

        // By default first checkbox is ticked.
        _checkBox0.setSelected(true);

        // By default creation name is search term. If the search
        // term is already associated with another creation it is serialized.
        _creationNameTextField.setText(getDefaultCreationName());


        _creationNameTextField.setVisible(true);
        _submitButton.setVisible(true);
        _imagePane.setVisible(true);

    }

    @FXML
    private void handleCreationCancelButton() throws IOException {
        // Return to main menu
        Main.changeScene("resources/MainScreenScene.fxml");
        cleanFolder();
    }


    // This method will clean the temporary fold that stored the audio chunks, the flikr images
    // the no audio .mp4 file the .wav file as well as the folders themselves.
    private void cleanFolder() {
        // The creations directory where all creations are stored.
        File creationFolder = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm+ "/" );
        for (final File creationFileName : creationFolder.listFiles()) {
            creationFileName.delete();
        }
        creationFolder.delete();

        // The chunks directory where all audio chunks are stored.
        File chunksFolder = new File(System.getProperty("user.dir") + "/chunks/" );
        for (final File chunkFileName : chunksFolder.listFiles()) {
            chunkFileName.delete();
        }
        chunksFolder.delete();
    }


    @FXML
    private void handleBackgroundMusic() throws IOException {
    	Main.backgroundMusicPlayer().handleBackgroundMusic(backgroundMusicButton.isSelected());
    	backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
    }
}