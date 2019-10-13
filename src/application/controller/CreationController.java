package application.controller;

import application.BashCommand;
import application.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import application.ImageVideoTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreationController {
	private ExecutorService team = Executors.newFixedThreadPool(5);

	private String _searchTerm;

	int numberOfImages;

	Alert alertLocal;

	@FXML
	private Text enterSearchTerm;
	@FXML
	private TextField enterSearchTermTextInput;
	@FXML
	private Button searchWikipediaButton;
	@FXML
	private Text searchInProgress;
	@FXML
	private Text termNotFound;

	@FXML
	private TextArea searchResultTextArea;
	@FXML
	private Button previewChunk;
	@FXML
	private Button saveChunk;
	@FXML
	private Text voiceSelectDescription;
	@FXML
	private Text textSelectDescription;
	@FXML
	private Text chunkSelectDescription;
	@FXML
	private ChoiceBox<String> voiceDropDownMenu;
	@FXML
	private ListView<String> chunkList;
	@FXML
	private Slider numImagesSlider;
	@FXML
	private TextField creationNameTextField;
	@FXML
	private Button finalCreate;
	@FXML
	private Button selectButton;

	@FXML
	private TextField _NumberOfImagesTextField;
	@FXML
	private Button _numberImagesButton;

	@FXML
	private Text numberOfImagesPrompt;
	@FXML
	private Text creationNamePrompt;

	@FXML
	private void initialize() {
		searchWikipediaButton.setDisable(true);

		// Don't let the user search until they put in a search term
//		BooleanBinding textIsEmpty = enterSearchTermTextInput.textProperty().isEmpty();	
		//========================== ADD REFERENCE, COPY-PASTED CODE =====================================================
		BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() -> 
				enterSearchTermTextInput.getText().trim().isEmpty(),
				enterSearchTermTextInput.textProperty()
			);
		searchWikipediaButton.disableProperty().bind(textIsEmpty);
		//==========================

		// Don't let the user create a chunk until requirements are met
//		BooleanBinding noTextSelected = searchResultTextArea.selectedTextProperty().isEmpty();
		BooleanBinding noTextSelected = Bindings.createBooleanBinding(() -> 
				!(numberOfWords(searchResultTextArea.getSelectedText().trim()) > 0),
				searchResultTextArea.selectedTextProperty()
			);
		previewChunk.disableProperty().bind(noTextSelected);
		saveChunk.disableProperty().bind(noTextSelected);

		// Don't let the user confirm the selected chunks until they select at least one
		BooleanBinding noChunkSelected = chunkList.getSelectionModel().selectedItemProperty().isNull();
		selectButton.disableProperty().bind(noChunkSelected);

		// Don't let the user confirm the number of images until they put in a valid number
		// TODO

		// Don't let the user confirm the name of the creation until it is valid
		// TODO
	}

	@FXML
	private void handleCreationCancelButton(ActionEvent event) throws IOException {
		// Return to main menu
		Main.changeScene("resources/listCreationsScene.fxml", event);

		// Cleaning the chunks folder if the creation is cancelled.
		File folderChunk = new File(System.getProperty("user.dir") + "/chunks/" );
		if (folderChunk.exists()) {
			for (final File fileNameChunk : folderChunk.listFiles()) {
				fileNameChunk.delete();
			}
			folderChunk.delete();
		}
	}

	@FXML
	private void handleSearchWikipedia(ActionEvent event) throws IOException {
		_searchTerm = enterSearchTermTextInput.getText().trim();

		termNotFound.setVisible(false);
		searchInProgress.setVisible(true);

		// Run bash script that uses wikit and returns the the result of the search
		String[] command = new String[]{"/bin/bash", "-c", "./script.sh search " + _searchTerm};
		BashCommand bashCommand = new BashCommand(command);
		team.submit(bashCommand);

		// Using concurrency allows the user to cancel the creation if the search takes too long
		bashCommand.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					/**
					 * Returns a list with only one element
					 * If the term was not found, the element is "(Term not found)"
					 * Otherwise the element is the search result
					 */
					List<String> result = bashCommand.get();
					String searchResult = result.get(0);

					if (searchResult.equals("(Term not found)")) {
						searchInProgress.setVisible(false);
						termNotFound.setVisible(true);
					} else {
						searchResultTextArea.setText(searchResult);
						displayChunkSelection();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}



	@FXML
	private void handlePreviewChunk(ActionEvent event) throws IOException {
		String chunk = searchResultTextArea.getSelectedText();

		if (numberOfWords(chunk) >= 30 && !lengthConfirmed()) {
			return;
		}
		// If reached here then the chunk is valid

		// Remove brackets from the chunk, some voices can't speak with brackets
		chunk = chunk.replace("(", "").replace(")", "");

		// Run bash script using festival tts to speak the selected text to the user
		String[] command = new String[]{"/bin/bash", "-c", "./script.sh preview " + chunk};
		BashCommand bashCommand = new BashCommand(command);
		team.submit(bashCommand);
	}

	private int numberOfWords(String chunk) {
		if (chunk.isEmpty()) {
			return 0;
		}

		// Splits the input at any instance of one or more whitespace character
		// The number of splits is the number of words
		String[] words = chunk.split("\\s+");
		return words.length;
	}

	private boolean lengthConfirmed() {
		// Let the user confirm if they want a longer chunk

		String warningMessage = "Chunks longer than 30 words can result in a lower sound quality. Are you sure you want to create this chunk?";
		Alert alert = new Alert(AlertType.WARNING, warningMessage, ButtonType.CANCEL, ButtonType.YES);

		// Display the confirmation alert and store the button pressed
		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.YES) {
			return true;
		} else {
			return false;
		}
	}
	
	@FXML
	private void handleSaveChunk(ActionEvent event) throws IOException {
		String chunk = searchResultTextArea.getSelectedText();
		if (numberOfWords(chunk) >= 30 && !lengthConfirmed()) {
			return;
		}
		// If reached here then the chunk is valid

		String voiceChoice = voiceDropDownMenu.getValue();

		// Remove brackets from the chunk, some voices can't speak with brackets
		chunk = chunk.replace("(", "").replace(")", "");

		// Run bash script using festival to save a .wav file containing the spoken selected text
		String[] command = new String[]{"/bin/bash", "-c", "./script.sh save " + voiceChoice + " " + chunk};
		BashCommand bashCommand = new BashCommand(command);
		team.submit(bashCommand);

		bashCommand.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				// Make the new chunk visible to the user
				updateChunkList();
			}
		});

	}

	private void updateChunkList() {
		// The chunks directory where all chunks are stored.
		final File chunksFolder = new File(System.getProperty("user.dir")+"/chunks/");
		if (!chunksFolder.exists()) {
			chunksFolder.mkdirs();
		}

		List<String> chunkNamesList = new ArrayList<String>();

		/**
		 * Checks every file in the chunks directory. If a file is .wav format,
		 * the file name without .wav extension to the list of chunkNames, and keeps count
		 */
		int numChunks = 0;
		for (File fileName : chunksFolder.listFiles()) {
			if (fileName.getName().endsWith(".wav")) {
				chunkNamesList.add(fileName.getName().replace(".wav", ""));
				numChunks++;
			}
		}

		// Sort the files by chunk name in alphabetical order.
		Collections.sort(chunkNamesList);

		if (numChunks == 0) {
			chunkNamesList.add("No Chunks Found.");
			// Prevent the user from selecting "No Chunks Found" as a chunk
			chunkList.setDisable(true);
		} else {
			chunkList.setDisable(false);
		}

		// Turns the list of chunk names into an ObservableList<String> and displays to the GUI.
		ObservableList<String> observableListChunkNames = FXCollections.observableArrayList(chunkNamesList);
		chunkList.setItems(observableListChunkNames);
	}

	private void displayChunkSelection() {		
		// Hide search elements
		enterSearchTerm.setVisible(false);
		enterSearchTermTextInput.setVisible(false);
		searchWikipediaButton.setVisible(false);
		searchInProgress.setVisible(false);
		termNotFound.setVisible(false);

		// Show chunk elements
		searchResultTextArea.setVisible(true); 
		previewChunk.setVisible(true);
		saveChunk.setVisible(true);
		voiceSelectDescription.setVisible(true);
		textSelectDescription.setVisible(true);
		chunkSelectDescription.setVisible(true);
		voiceDropDownMenu.setVisible(true);
		chunkList.setVisible(true);
		selectButton.setVisible(true);

		// Show the options for chunk voices, and set the default choice
		voiceDropDownMenu.getItems().addAll("Default", "NZ-Man", "NZ-Woman");
		voiceDropDownMenu.setValue("Default");

		// Show the currently stored chunks, and allow the user to select multiple with ctrl+click or shift+click
		updateChunkList();
		chunkList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	@FXML
	private void handleSelectButton(){
		// Hide chunk elements
		searchResultTextArea.setVisible(false);
		previewChunk.setVisible(false);
		saveChunk.setVisible(false);
		voiceSelectDescription.setVisible(false);
		textSelectDescription.setVisible(false);
		chunkSelectDescription.setVisible(false);
		voiceDropDownMenu.setVisible(false);
		chunkList.setVisible(false);
		selectButton.setVisible(false);

		creationNameTextField.setVisible(true);
		creationNameTextField.setDisable(true);

		finalCreate.setVisible(true);
		finalCreate.setDisable(true);

		// show flickr creation options
		_NumberOfImagesTextField.setVisible(true);
		_numberImagesButton.setVisible(true);
		numberOfImagesPrompt.setVisible(true);
	}

	@FXML
	private void handleCheckCreationButton(ActionEvent event) throws IOException  {
		// checking that the creation name is valid set of inputs
		if (!creationNameTextField.getText().matches("[a-zA-Z0-9_-]*") || creationNameTextField.getText().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Creation name");
			alert.setContentText("Please enter a valid creation name consisting of alphabet letters and digits only.");
			alert.showAndWait();
			return;
		} else if (!validCreationName(creationNameTextField.getText())) {
			// throw alerts
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Override");
			alert.setHeaderText("Creation name already exists");
			alert.setContentText("Would you like to override the existing creation?");
			Optional<ButtonType> result = alert.showAndWait();

			// Override existing file name
			// This is the same as deleting the current file and creating a new file.
			if (result.get() == ButtonType.OK) {
				String creationName = creationNameTextField.getText();
				File _existingFile = new File(System.getProperty("user.dir")+"/creations/"+ creationName +".mp4");
				_existingFile.delete();

				File creationFolder = new File(System.getProperty("user.dir")+"/creations/"+ creationName +"/");
				if (!creationFolder.exists()) {
					creationFolder.mkdirs();
				}
				combineAudioChunks(creationName);

				// return to main menu
				Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
				Scene creationViewScene = new Scene(creationViewParent);
				Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
				window.setScene(creationViewScene);
				window.show();

				Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
				alert2.setTitle("Creation in progress");
				alert2.setHeaderText("Creation is being made, please wait...");
				alert2.setContentText("You will be informed when the creation is complete.");
				alert2.show();
			}

		} else {
			//No problems with any inputs will create the creation normally.
			String creationName = creationNameTextField.getText();
			File creationFolder = new File(System.getProperty("user.dir")+"/creations/"+ creationName +"/");
			if (!creationFolder.exists()) {
				creationFolder.mkdirs();
			}
			combineAudioChunks(creationName);
			// return to main menu
			Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
			Scene creationViewScene = new Scene(creationViewParent);
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(creationViewScene);
			window.show();

			alertLocal = new Alert(Alert.AlertType.INFORMATION);
			alertLocal.setTitle("Creation in progress");
			alertLocal.setHeaderText("Creation is being made, please wait...");
			alertLocal.setContentText("You will be informed when the creation is complete.");
			alertLocal.show();

		}
	}

	private void combineAudioChunks(String creationName) {
		ObservableList<String> selectedChunks = chunkList.getSelectionModel().getSelectedItems();

		// Convert the ObservableList of chunks into a single string, with each element separated by a space
		String chunksAsString = "";
		int numChunksSelected = selectedChunks.size();
		for (int i = 0; i < numChunksSelected - 1; i++) {
			chunksAsString += selectedChunks.get(i) + " ";
		}
		// Last element should not have a space after it
		chunksAsString += selectedChunks.get(numChunksSelected - 1);

		// Run bash script to create a combined audio of each selected chunk
		String[] command = new String[]{"/bin/bash", "-c", "./script.sh create " + creationName + " " + chunksAsString};		
		BashCommand bashCommand = new BashCommand(command);
		team.submit(bashCommand);

		bashCommand.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				createVideo();
			}
		});
	}

	/*The method to create the creation
	  This method will pull images from flickr based on user input.
	  Using these images a video will be created with the search term added as text.*/
	private void createVideo() {
		// Thread to ensure that GUI remains concurrent while the video is being created
		ImageVideoTask flickrImagesTask = new ImageVideoTask (enterSearchTermTextInput.getText(), creationNameTextField.getText(), numberOfImages );
		team.submit(flickrImagesTask);

		flickrImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {

				Button cancelButton = ( Button ) alertLocal.getDialogPane().lookupButton( ButtonType.OK );
				cancelButton.fire();


				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Creation completed");
				alert.setHeaderText("Creation completed: "+ creationNameTextField.getText() +" is finished");
				alert.setContentText("Please refresh the list of creations");
				alert.showAndWait();
			}
		});
	}

	@FXML
	private void handleNumberOfImagesButton() {
		if (!(_NumberOfImagesTextField.getText().isEmpty())) {

			////
			if (!_NumberOfImagesTextField.getText().matches("[0-9]*")){ 
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Creation name");
				alert.setContentText("Please enter a valid creation name consisting of alphabet letters and digits only.");
				alert.showAndWait();
				return;
			} else {


				/////
				int num = Integer.parseInt(_NumberOfImagesTextField.getText());
				if (num <= 0 || num > 10) {
					Alert alert = new Alert(Alert.AlertType.WARNING);
					alert.setTitle("Invalid number of images");
					alert.setContentText("Please enter a valid number between 1 and 10");
					alert.showAndWait();
					return;
				}

				// If the input number of images is valid
				// The user will be prompted to continue with the creation.
				// The creation name box/button will be shown and the number of
				// images box/ button will be disabled
				numberOfImages = Integer.parseInt(_NumberOfImagesTextField.getText());

				// Number of images box/button will disabled
				_NumberOfImagesTextField.setDisable(true);
				_numberImagesButton.setDisable(true);
				numberOfImagesPrompt.setVisible(false);

				// Creation name box/button will be shown
				creationNamePrompt.setVisible(true);
				creationNameTextField.setDisable(false);
				finalCreate.setDisable(false);
			}
		}
	}


	// This method will check if the given name is already associated with
	// an existing creation. Returns false if the creation name is already used.
	// Returns true otherwise.
	private boolean validCreationName(String creationName){
		File folder = new File(System.getProperty("user.dir")+"/creations/");
		for (final File fileName : folder.listFiles()) {
			if (fileName.getName().equals("" + creationName + ".mp4")) {
				// An already existing creation name is invalid.
				return false;
			}
		}
		return true;
	}
}
