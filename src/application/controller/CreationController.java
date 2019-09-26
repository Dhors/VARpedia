package application.controller;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;

public class CreationController {

	@FXML
    private Text enterSearchTerm;
	
	@FXML
    private TextField enterSearchTermTextInput;
	
	@FXML
    private Button searchWikipediaButton;
	
	@FXML
    private Text searchProgress;
	
    @FXML
    private void handleCreationCancelButton(ActionEvent event) throws IOException {

        Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
        Scene creationViewScene = new Scene(creationViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(creationViewScene);
        window.show();
    }



}
