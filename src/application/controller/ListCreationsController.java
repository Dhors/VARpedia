package application.controller;

import application.Main;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class ListCreationsController {
	@FXML
	private ToggleButton _backgroundMusicButton;
	
	@FXML
	private ListView<String> _listViewCreations;

	private static String _selectedCreation;
	@FXML
	private Button _playButton;
	@FXML
	private Button _deleteButton;
	@FXML
	private Label _selectPrompt;


	@FXML
	public void initialize(){

		Main.setCurrentScene("ListCreationScene");
		_backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
        _backgroundMusicButton.setSelected(Main.backgroundMusicPlayer().getButtonIsSelected());

		ListCurrentFiles();

		// Disable the buttons whenever there is no creation selected
		BooleanBinding noCreationSelected = _listViewCreations.getSelectionModel().selectedItemProperty().isNull();
		_playButton.disableProperty().bind(noCreationSelected);
		_deleteButton.disableProperty().bind(noCreationSelected);

	}

	@FXML
	private void handleNewCreationButton() throws IOException {
		Main.changeScene("resources/NewCreationScene.fxml");
	}


	@FXML
	private void handlePlayButton() throws IOException {
		Main.changeScene("resources/PlayerScene.fxml");
	}

	@FXML
	private void handleReturnButton() throws IOException {
		Main.changeScene("resources/MainScreenScene.fxml");
	}
	
	@FXML
	public void handleSelectedCreation() {
		_selectedCreation = _listViewCreations.getSelectionModel().getSelectedItem();
		if (!(_selectedCreation==null)) {
			_selectPrompt.setText("");
		}
	}
	
	@FXML
	private void handleDeleteButton(){
		Alert deleteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
		deleteConfirmation.getDialogPane().getStylesheets().add(("Alert.css"));
		deleteConfirmation.setTitle("Confirm Deletion");
		deleteConfirmation.setHeaderText("Delete " + getSelectedCreationName() + "?");
		deleteConfirmation.setContentText("Are you sure you want to delete this creation?");
		Optional<ButtonType> buttonClicked = deleteConfirmation.showAndWait();

		if (buttonClicked.get() == ButtonType.OK) {
			getSelectedFile().delete();
			ListCurrentFiles();
			_selectedCreation = null;
			_selectPrompt.setText("                  Please select a creation to continue.");
		}
	}

	// This will return a list of all current creations in the creations directory.
	// This list will be displayed to the user in the view interface.
	public void ListCurrentFiles(){

		// The creations directory where all creations are stored.
		final File creationsFolder = new File(System.getProperty("user.dir")+"/creations/");
		ArrayList<String> creationNamesList = new ArrayList<String>();

		// Will get every file in the creations directory and create an indexed
		// list of file names.
		int indexCounter = 1;
		for (final File file : creationsFolder.listFiles()) {
			String fileName = file.getName();
			if (fileName.endsWith(".mp4")) {
				creationNamesList.add("" + indexCounter + ". " + fileName.replace(".mp4", ""));
				indexCounter++;
			}
		}
		
		// Turning the list of creation names into an listView<String> for the GUI.
		ObservableList<String> observableCreationNamesList = FXCollections.observableArrayList(creationNamesList);
		_listViewCreations.setItems(observableCreationNamesList);
	}

	public static File getSelectedFile(){
		// Turning a string of the file name into a file to be played or deleted.
		String fileName = getSelectedCreationName();
		File selectedfile = new File(System.getProperty("user.dir")+"/creations/"+ fileName +".mp4");

		return selectedfile;
	}

	public static String getSelectedCreationName(){
		// Removal of the index on the creation name
		String fileName = ( "" + _selectedCreation.substring(_selectedCreation.indexOf(".")+2) );
		return fileName;
	}


	@FXML
	private void handleBackgroundMusic()  {
		Main.backgroundMusicPlayer().handleBackgroundMusic(_backgroundMusicButton.isSelected());
    	_backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
	}
}
