package application;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

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
		loader.setLocation(this.getClass().getResource("resources/listCreationsScene.fxml"));
		Parent layout = loader.load();
		Scene scene = new Scene(layout);
		stage.setScene(scene);
		stage.show();
	}
	
	public static void changeScene(String sceneDir, ActionEvent event) throws IOException {
		Parent sceneParent = FXMLLoader.load(Main.class.getResource(sceneDir));
		Scene newScene = new Scene(sceneParent);

		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

		window.setScene(newScene);
		window.show();
	}
}
