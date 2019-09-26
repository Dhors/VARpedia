package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
	
	public static void main(String[] args) {
		launch(args);
	}

    @Override
    public void start(Stage stage) throws Exception {

        File creationsfolder = new File(System.getProperty("user.dir")+"/creations");
        if (!creationsfolder.exists()) {
            creationsfolder.mkdirs();
        }



        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("resources/home.fxml"));
        Parent layout = loader.load();
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();



    }






















}
