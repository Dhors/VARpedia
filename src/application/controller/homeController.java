package application.controller;


import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class homeController {

        @FXML
        private void handleCreationButton(ActionEvent event) throws IOException {


            Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/newCreationScene.fxml"));
            Scene creationViewScene = new Scene(creationViewParent);

            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(creationViewScene);
            window.show();
        }



    @FXML
    private void handlePlayCreationButton(ActionEvent event) throws IOException {

        Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/PlayerScene.fxml"));
        Scene creationViewScene = new Scene(creationViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(creationViewScene);
        window.show();
    }



    @FXML
    private void handleListButton(ActionEvent event) throws IOException {


        Parent listViewParent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
        Scene listViewScene = new Scene(listViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(listViewScene);
        window.show();
    }

        @FXML
        private Text titleText;

}
