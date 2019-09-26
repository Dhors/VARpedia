package application.controller;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CreationController {

    @FXML
    private TextField _creationNameTextField;


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
        if (!_creationNameTextField.getText().matches("[a-zA-Z0-9_-]*") || _creationNameTextField.getText().isEmpty()) {
            // throw alerts
        } else if (!validCreationName(_creationNameTextField.getText())) {
            // throw alerts
            //override existing file name
            String creationName = _creationNameTextField.getText();
            File _existingfile = new File(System.getProperty("user.dir")+"/creations/"+ creationName +".mp4");
            _existingfile.delete();

            //CreationVideoTask makeVid = new CreationVideoTask(_term, creationName, splitWikiSearchOutput, selectedLineNum);
            //threadWorker.submit(makeVid);




        } else {

            String creationName = _creationNameTextField.getText();
            File _existingfile = new File(System.getProperty("user.dir")+"/creations/"+ creationName +".mp4");

            //CreationVideoTask makeVid = new CreationVideoTask(_term, creationName, splitWikiSearchOutput, selectedLineNum);
            //threadWorker.submit(makeVid);


        }
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

}
