package application.controller;

import application.tasks.FlickrImagesTask;
import application.Main;
import application.tasks.ImageVideoTask;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagesSelectionController {
    @FXML
    private ImageView _ImageView0;
    @FXML
    private ImageView _ImageView1;
    @FXML
    private ImageView _ImageView2;
    @FXML
    private ImageView _ImageView3;
    @FXML
    private ImageView _ImageView4;
    @FXML
    private ImageView _ImageView5;
    @FXML
    private ImageView _ImageView6;
    @FXML
    private ImageView _ImageView7;
    @FXML
    private ImageView _ImageView8;
    @FXML
    private ImageView _ImageView9;
    @FXML
    private CheckBox _checkBox0;
    @FXML
    private CheckBox _checkBox1;
    @FXML
    private CheckBox _checkBox2;
    @FXML
    private CheckBox _checkBox3;
    @FXML
    private CheckBox _checkBox4;
    @FXML
    private CheckBox _checkBox5;
    @FXML
    private CheckBox _checkBox6;
    @FXML
    private CheckBox _checkBox7;
    @FXML
    private CheckBox _checkBox8;
    @FXML
    private CheckBox _checkBox9;

    @FXML
    private ToggleButton _backgroundMusicButton;

    @FXML
    private ProgressBar _imagesProgressBar;
    @FXML
    private Text _imageDownloadInProgress;
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

    private ExecutorService _team = Executors.newSingleThreadExecutor();

    private String _searchTerm;
    private int _numberOfImages;

    private File _imagesFolder;
    private final String CREATIONS_DIR = System.getProperty("user.dir") + "/creations/";

    @FXML
    private void initialize() {
        _clockImage.setVisible(true);
        _progressPane.setVisible(true);
        Main.setCurrentScene("ImageSelectionScene");
        _backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
        _backgroundMusicButton.setSelected(Main.backgroundMusicPlayer().getButtonIsSelected());

        _searchTerm = NewCreationController.getSearchTerm();
        _imagesFolder = new File(CREATIONS_DIR + _searchTerm);

        _flickrImageViewList = new ArrayList<ImageView>(Arrays.asList(_ImageView0, _ImageView1, _ImageView2, _ImageView3, _ImageView4, _ImageView5
                , _ImageView6, _ImageView7, _ImageView8, _ImageView9));
        _checkBoxIncludeImageList = new ArrayList<CheckBox>(Arrays.asList(_checkBox0, _checkBox1, _checkBox2, _checkBox3, _checkBox4, _checkBox5
                , _checkBox6, _checkBox7, _checkBox8, _checkBox9));


        /**
         * Credit to user DVarga
         * Full credit in NewCreationController in method setUpBooleanBindings()
         */
        // Don't let the user create a creation if they remove the default creation name
        BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() ->
                        _creationNameTextField.getText().trim().isEmpty(),
                _creationNameTextField.textProperty());
        _submitButton.disableProperty().bind(textIsEmpty);

        downloadFlickrImages();
    }

    @FXML
    private void handleSubmitButton() {
        int numImagesDeleted = 0;
        for (int i = 0; i < _checkBoxIncludeImageList.size(); i++) {
            if (!_checkBoxIncludeImageList.get(i).isSelected()) {
                File imageFile = new File(_imagesFolder + "/" + i + ".jpg");
                imageFile.delete();
                numImagesDeleted++;
            }
        }

        _numberOfImages = 10 - numImagesDeleted;
        if (_numberOfImages == 0) {
            Alert noImagesSelectedError = new Alert(Alert.AlertType.WARNING);
            noImagesSelectedError.getDialogPane().getStylesheets().add(("Alert.css"));
            noImagesSelectedError.setTitle("No images selected");
            noImagesSelectedError.setContentText("Please select at least one image.");
            noImagesSelectedError.showAndWait();
            return;
        }

        // checking that the creation name is valid set of inputs
        if (_creationNameTextField.getText().isEmpty()) {

        } else if (!_creationNameTextField.getText().matches("[a-zA-Z0-9_-]*")) {
            Alert invalidCreationNameError = new Alert(Alert.AlertType.WARNING);
            invalidCreationNameError.getDialogPane().getStylesheets().add(("Alert.css"));
            invalidCreationNameError.setTitle("Invalid Creation name");
            invalidCreationNameError.setContentText("Please enter a valid creation name consisting of alphabet letters, digits, underscores, and hyphens only.");
            invalidCreationNameError.showAndWait();
        } else if (!isUniqueCreationName(_creationNameTextField.getText())) {

            Alert overrideExistingCreationPopup = new Alert(Alert.AlertType.CONFIRMATION);
            overrideExistingCreationPopup.getDialogPane().getStylesheets().add(("Alert.css"));
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
            // If there are no problems with any inputs will create the creation normally.
            String creationName = _creationNameTextField.getText();
            createVideo(creationName);
        }
    }

    /*The method to create the creation
      This method will pull images from flickr based on user input.
      Using these images a video will be created with the search term added as text.*/
    private void createVideo(String creationName) {
        Alert creationInProgressPopup = new Alert(Alert.AlertType.INFORMATION);
        creationInProgressPopup.getDialogPane().getStylesheets().add(("Alert.css"));
        creationInProgressPopup.setTitle("Creation in progress");
        creationInProgressPopup.setHeaderText("Creation is being made, please wait...");
        creationInProgressPopup.setContentText("You will be informed when the creation is complete.");
        creationInProgressPopup.show();

        // Thread to ensure that GUI remains concurrent while the video is being created
        ImageVideoTask flickrImagesTask = new ImageVideoTask(_searchTerm, creationName, _numberOfImages);
        _team.submit(flickrImagesTask);

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
                Button cancelButton = (Button) creationInProgressPopup.getDialogPane().lookupButton(ButtonType.OK);
                cancelButton.fire();

                // This will refresh the List of creations only if the user is currently on the List of creations scene
                // Otherwise when the user enters, the initialize() method of ListCreationsController will
                // refresh the list of creations.
                if (Main.getCurrentScene().equals("ListCreationScene")) {
                    try {
                        Main.changeScene("resources/ListCreationsScene.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Alert creationFinishedPopup = new Alert(Alert.AlertType.INFORMATION);
                creationFinishedPopup.getDialogPane().getStylesheets().add(("Alert.css"));
                creationFinishedPopup.setTitle("Creation completed");
                creationFinishedPopup.setHeaderText("Creation completed: " + _creationNameTextField.getText() + " is finished");
                creationFinishedPopup.setContentText("You can find it in the Creations or Quiz sections");
                creationFinishedPopup.showAndWait();
            }
        });
    }

    // This method will check if the given name is already associated with
    // an existing creation. Returns false if the creation name is already used.
    // Returns true otherwise.
    private boolean isUniqueCreationName(String creationName) {
        File creationsFolder = new File(CREATIONS_DIR);
        for (final File creationFile : creationsFolder.listFiles()) {
            if (creationFile.getName().equals("" + creationName + ".mp4")) {
                // An already existing creation name is invalid.
                return false;
            }
        }
        return true;
    }

    // This method is used to find a valid creation name to be used as the
    // predefined creation nae in the application.
    private String getDefaultCreationName() {
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

    // Method for retrieving Flickr images. The retrieval is done through a bash process
    // in the task class FlickrImagesTask.
    private void downloadFlickrImages() {
        FlickrImagesTask imagesTask = new FlickrImagesTask(_searchTerm);

        _imagesProgressBar.progressProperty().bind(imagesTask.progressProperty());

        _team.submit(imagesTask);
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
        File[] imageFileArray = _imagesFolder.listFiles();
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

        for (ImageView flickrImageView : _flickrImageViewList) {
            flickrImageView.setVisible(true);
        }
        for (CheckBox checkBoxIcludeImage : _checkBoxIncludeImageList) {
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
        Main.cleanFolders();
    }


    @FXML
    private void handleBackgroundMusic() {
        Main.backgroundMusicPlayer().handleBackgroundMusic(_backgroundMusicButton.isSelected());
        _backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
    }
}