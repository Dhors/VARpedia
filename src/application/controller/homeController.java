package application.controller;


import application.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class homeController {

        @FXML
        private void handleCreationButton() throws IOException {
            titleText.setText("the change to commit");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("resources/CreationStage.fxml"));
            Parent layout = loader.load();
            Scene scene = new Scene(layout);
            Stage creationStage = new Stage();
            creationStage.setScene(scene);
            creationStage.show();

           // Parent CreationViewParent = FXMLLoader.load(getClass().getResource("resources/CreationStage.fxml"));
           // Scene CreationViewScene = new Scene(CreationViewParent )



        }



        @FXML
        private Text titleText;

}
