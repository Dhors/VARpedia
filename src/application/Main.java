package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

	static private Stage _primaryStage;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		String userDir = System.getProperty("user.dir");
		
		File creationsFolder = new File(userDir + "/creations");
		if (!creationsFolder.exists()) {
			creationsFolder.mkdirs();
		}

		File quizFolder = new File(userDir + "/quiz");
		if (!quizFolder.exists()) {
			quizFolder.mkdirs();
		}

		_primaryStage =  primaryStage;
		Main.changeScene("resources/MainScreenScene.fxml");
	}

	// This method is used throughout this application to change between scenes.
	// Upon the correct button actions by the user, the scene will switch to the next scene indicated by
	// the parameter String fxmlFileName.
	static public void changeScene(String fxmlFileName) throws IOException {
		FXMLLoader fMXLLoader = new FXMLLoader();
		fMXLLoader.setLocation(Main.class.getResource(fxmlFileName));
		Parent newLayout = fMXLLoader.load();
		Scene newScene = new Scene(newLayout);
		_primaryStage.setScene(newScene);
		_primaryStage.show();
	}
}
