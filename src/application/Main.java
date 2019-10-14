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

  static private Stage _primaryStage;
  
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		File creationsfolder = new File(System.getProperty("user.dir")+"/creations");
		if (!creationsfolder.exists()) {
			creationsfolder.mkdirs();
		}
		
    File quizfolder = new File(System.getProperty("user.dir")+"/quiz");
    if (!quizfolder.exists()) {
      quizfolder.mkdirs();
    }
  
    _primaryStage =  primaryStage;
    
  // Main.setScene("resources/MainScreenScene.fxml");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(this.getClass().getResource("resources/MainScreenScene.fxml"));
		Parent layout = loader.load();
		Scene scene = new Scene(layout);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
  
	// This method is used throughout this application to change between scenes.
    // Upon the correct button actions by the user, the scene will switch to the next scene indicated by
    // the parameter String fxmlFileName.
//	public static void changeScene(String sceneDir, ActionEvent event) throws IOException {
//		Parent sceneParent = FXMLLoader.load(Main.class.getResource(sceneDir));
//		Scene newScene = new Scene(sceneParent);
//
//		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
//
//		window.setScene(newScene);
//		window.show();
//	}
  
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
  
  // This method is used in this application to open a new window to modify customer information
    // or to add a new customers information.
    // Upon the correct button actions by the user, the application will open a new window displaying a new scene
    // indicated by the parameter String fxmlFileName.
    static public void newWindow(String fxmlFileName) throws IOException {

        FXMLLoader fXMLLoader = new FXMLLoader();
        fXMLLoader.setLocation(Main.class.getResource(fxmlFileName));
        Parent newLayout = fXMLLoader.load();
        Stage newStage = new Stage();
        Scene newScene = new Scene(newLayout);
        newStage.setScene(newScene);
        newStage.show();

    }
}
