package application.controller;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ListController {


    @FXML
    private void handleListReturnButton(ActionEvent event) throws IOException {

        Parent listViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
        Scene listViewScene = new Scene(listViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(listViewScene);
        window.show();
    }


}
