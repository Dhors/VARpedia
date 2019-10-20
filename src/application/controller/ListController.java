package application.controller;

import application.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class ListController {

	@FXML
	private ListView<String> listViewCreations;

	@FXML
	private static String _selectedCreation;

	@FXML
	private Button playButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Text selectPrompt;

	@FXML
	private Button newCreationButton;


	public void initialize(){
		ListCurrentFiles();
		playButton.setDisable(true);
		deleteButton.setDisable(true);
		
		// Disable the buttons until a creation is selected
		BooleanBinding noCreationSelected = listViewCreations.getSelectionModel().selectedItemProperty().isNull();
		playButton.disableProperty().bind(noCreationSelected);
		deleteButton.disableProperty().bind(noCreationSelected);
		selectPrompt.visibleProperty().bind(noCreationSelected);
	}


	@FXML
	private void handlePlayButton(ActionEvent event) throws IOException {
			//change scene to playerScene
			Main.changeScene("resources/PlayerScene.fxml");
	}

	@FXML
	private void handleDeleteButton(){
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Delete");
			alert.setHeaderText(" delete " + _selectedCreation);
			alert.setContentText("Are you sure?.");
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == ButtonType.OK) {
				//System.out.println("" +getSelectedFile());
				//System.out.println("" +getSelectedFile());
				getSelectedFile().delete();
				ListCurrentFiles();
			}
	}

	@FXML
	private void handleRefreshButton() {
		ListCurrentFiles();
	}

	@FXML
	private void handleNewCreationButton(ActionEvent event) throws IOException {
		Main.changeScene("resources/newCreationScene.fxml");
	}

	// This will return a list of all current creations in the creations directory.
	// This list will be displayed to the user in the view interface.
	private void ListCurrentFiles(){
		// The creations directory where all creations are stored.
		final File folder = new File(System.getProperty("user.dir")+"/creations/");
		ArrayList<String> listFilesNames = new ArrayList<String>();
		ArrayList<String> listCreationNames = new ArrayList<String>();

		for (final File fileName : folder.listFiles()) {
			if (fileName.getName().endsWith(".mp4")) {
				listFilesNames.add(fileName.getName());
			}
		}
		// Sort the files by creation name in alphabetical order.
		Collections.sort(listFilesNames);

		// Will get every file in the creations directory and create an indexed
		// list of file names.
		int indexCounter = 1;
		for (final String creations : listFilesNames) {
			if (creations.endsWith(".mp4")) {
				listCreationNames.add("" + indexCounter + ". " + creations.replace(".mp4", ""));
				indexCounter++;

			}
		}
		// Turning the list of creation names into an listView<String> for the GUI.
		ObservableList<String> listViewFiles = FXCollections.observableArrayList(listCreationNames);
		//ListView CreationsListView = new ListView();
		listViewCreations.setItems(listViewFiles);
	}

	@FXML
	public void handleSelectedCreation() {
		_selectedCreation = listViewCreations.getSelectionModel().getSelectedItem();
	}

	public static File getSelectedFile(){


		// Removal of the index on the creation name
		// and creating it as a file to be played or deleted.
		String fileName = ( "" + _selectedCreation.substring(_selectedCreation.indexOf(".")+2) );
		File _selectedfile = new File(System.getProperty("user.dir")+"/creations/"+ fileName +".mp4");

		return _selectedfile;

	}

	public static String getSelectedCreationName(){
		// Removal of the index on the creation name
		String fileName = ( "" + _selectedCreation.substring(_selectedCreation.indexOf(".")+2) );
		return fileName;
	}
  
  // Return to main menu
  @FXML
  private void handleReturnButton() throws IOException {
    Main.changeScene("resources/MainScreenScene.fxml");
  }
}
