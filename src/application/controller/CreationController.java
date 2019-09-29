package application.controller;

import application.ImageVideoTask;
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
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


import java.awt.*;
import java.io.File;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;


import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreationController {


    private ExecutorService threadWorker = Executors.newSingleThreadExecutor();
    int numberOfImages;


    @FXML
    private Text enterSearchTerm;
	@FXML
    private TextField enterSearchTermTextInput;
	@FXML
    private Button searchWikipediaButton;
	@FXML
    private Text searchProgress;



	@FXML
    private TextArea searchResultTextArea;
	@FXML
    private Button previewChunk;
	@FXML
    private Button saveChunk;
	@FXML
    private Text voiceLabel;
	@FXML
    private ChoiceBox voiceDropDownMenu;
	@FXML
    private ListView chunkList;
	@FXML
    private Slider numImagesSlider;
	@FXML
    private TextField creationNameTextField;
	@FXML
    private Button finalCreate;
	
    @FXML
    private TextField _creationNameTextField;


    @FXML
    private TextField _NumberOfImagesTextField;

    @FXML
    private void handleCreationCancelButton(ActionEvent event) throws IOException {

        Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
        Scene creationViewScene = new Scene(creationViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(creationViewScene);
        window.show();
    }


    @FXML
    private void handleCheckCreationButton() {
        System.out.println("got to here at least");
        if (!creationNameTextField.getText().matches("[a-zA-Z0-9_-]*") || creationNameTextField.getText().isEmpty()) {
            // throw alerts
        } else if (!validCreationName(creationNameTextField.getText())) {
            // throw alerts


            //override existing file name
            String creationName = creationNameTextField.getText();
            File _existingfile = new File(System.getProperty("user.dir")+"/creations/"+ creationName +".mp4");
           // _existingfile.delete();

            //CreationVideoTask makeVid = new CreationVideoTask(_term, creationName, splitWikiSearchOutput, selectedLineNum);
            //threadWorker.submit(makeVid);




        } else { //on success
           // FlickrImagesTask
            // need to check valid number and search term
            String creationName = creationNameTextField.getText();



            File creationFolder = new File(System.getProperty("user.dir")+"/creations/"+ creationName +"/");

            if (!creationFolder.exists()) {
                creationFolder.mkdirs();
            }


            System.out.println(""+ enterSearchTermTextInput.getText() + creationNameTextField.getText() + numberOfImages );

            ImageVideoTask flickrImagesTask = new ImageVideoTask (enterSearchTermTextInput.getText(), creationNameTextField.getText(), numberOfImages );
            threadWorker.submit(flickrImagesTask);


            flickrImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    //yay
                }
                });


        }
    }


    @FXML
    private void handleNumberOfImagesButton() {
        if (_NumberOfImagesTextField.getText().isEmpty()){
            return;
        }
        int num = Integer.parseInt(_NumberOfImagesTextField.getText());
        if (num <=0||num>10){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid number of images");
            //alert.setHeaderText(" delete " + _selectedCreation);
            alert.setContentText("Please enter a valid number between 1 nd 10");
            alert.showAndWait();
            return;

        }
        //possibly let the user continue on from this point
        // if successful let them see the create button
        // and set transparency of number of items to lower.

        numberOfImages =  Integer.parseInt(_NumberOfImagesTextField.getText());
    }



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




    @FXML
    private void handleCheckCreationNameButton() {
    // this will just check the name of the creation


    }







}
