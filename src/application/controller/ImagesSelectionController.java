package application.controller;

import application.FlickrImagesTask;
import application.Main;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    private TextField _creationNameTextField;
    @FXML
    private Button _submitButton;

    private ArrayList<ImageView> _imageViews;
    private ArrayList<CheckBox> _checkBoxes;

    private ExecutorService team = Executors.newSingleThreadExecutor();

    private Alert alertLocal;

    private String _searchTerm;

    private File imageFolder;

    private ArrayList<Image> _imageList = new ArrayList<Image>();

    @FXML
    private void initialize() {
        _searchTerm=CreationController.getSearchTerm();
        imageFolder= new File(System.getProperty("user.dir") + "/creations/" + _searchTerm);


        _imageViews=new ArrayList<ImageView>(Arrays.asList(_ImageView0,_ImageView1,_ImageView2,_ImageView3,_ImageView4,_ImageView5
                ,_ImageView6,_ImageView7,_ImageView8,_ImageView9));
        _checkBoxes=new ArrayList<CheckBox>(Arrays.asList(_checkBox0,_checkBox1,_checkBox2,_checkBox3,_checkBox4,_checkBox5
                ,_checkBox6,_checkBox7,_checkBox8,_checkBox9));

        FlickrImagesTask imagesTask = new FlickrImagesTask(_searchTerm);
        team.submit(imagesTask );
        imagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {


                File[] imageFiles = imageFolder.listFiles();
                for (File file: imageFiles) {


                    _imageList.add(new Image(file.toURI().toString()));
                }
                int count = 0;
                for (ImageView i: _imageViews) {

                    i.setImage(_imageList.get(count));

                    count++;
                }

            }
        });

    }

    @FXML
    private void handleSubmitButton() throws IOException {
        int count = 0;
        for (CheckBox c: _checkBoxes){
            if (!c.isSelected()){
                File imageFile = new File(imageFolder+"/"+count+".jpg" );
                imageFile.delete();
            }
            count++;
        }


         /*   // checking that the creation name is valid set of inputs
            if (!_creationNameTextField.getText().matches("[a-zA-Z0-9_-]*") || _creationNameTextField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Creation name");
                alert.setContentText("Please enter a valid creation name consisting of alphabet letters and digits only.");
                alert.showAndWait();
                return;
            } else if (!validCreationName(_creationNameTextField.getText())) {
                // throw alerts
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Override");
                alert.setHeaderText("Creation name already exists");
                alert.setContentText("Would you like to override the existing creation?");
                Optional<ButtonType> result = alert.showAndWait();

                // Override existing file name
                // This is the same as deleting the current file and creating a new file.
                if (result.get() == ButtonType.OK) {
                    String creationName = _creationNameTextField.getText();
                    File _existingFile = new File(System.getProperty("user.dir")+"/creations/"+ creationName +".mp4");
                    _existingFile.delete();

                    File creationFolder = new File(System.getProperty("user.dir")+"/creations/"+ creationName +"/");
                    if (!creationFolder.exists()) {
                        creationFolder.mkdirs();
                    }
                    combineAudioChunks(creationName);

                    // return to main menu
                    Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
                    Scene creationViewScene = new Scene(creationViewParent);
                    Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
                    window.setScene(creationViewScene);
                    window.show();

                    alertLocal = new Alert(Alert.AlertType.INFORMATION);
                    alertLocal.setTitle("Creation in progress");
                    alertLocal.setHeaderText("Creation is being made, please wait...");
                    alertLocal.setContentText("You will be informed when the creation is complete.");
                    alertLocal.show();
                }

            } else {
                //No problems with any inputs will create the creation normally.
                String creationName = creationNameTextField.getText();
                File creationFolder = new File(System.getProperty("user.dir")+"/creations/"+ creationName +"/");
                if (!creationFolder.exists()) {
                    creationFolder.mkdirs();
                }
                combineAudioChunks(creationName);
                // return to main menu
                Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
                Scene creationViewScene = new Scene(creationViewParent);
                Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
                window.setScene(creationViewScene);
                window.show();

                alertLocal = new Alert(Alert.AlertType.INFORMATION);
                alertLocal.setTitle("Creation in progress");
                alertLocal.setHeaderText("Creation is being made, please wait...");
                alertLocal.setContentText("You will be informed when the creation is complete.");
                alertLocal.show();

            }*/
        }



    // This method will check if the given name is already associated with
    // an existing creation. Returns false if the creation name is already used.
    // Returns true otherwise.
    private boolean validCreationName(String creationName){
        File folder = new File(System.getProperty("user.dir")+"/creations/");
        for (final File fileName : folder.listFiles()) {
            if (fileName.getName().equals("" + creationName + ".mp4")) {
                // An already existing creation name is invalid.
                return false;
            }
        }
        return true;
    }


}
