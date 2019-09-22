package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("resources/Home.fxml"));
        Parent layout = loader.load();
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();

    }
}
