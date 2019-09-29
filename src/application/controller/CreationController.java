package application.controller;

import application.BashCommand;
import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import application.ImageVideoTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
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
	private ExecutorService team = Executors.newFixedThreadPool(3);

	private String _searchTerm;

	private ExecutorService threadWorker = Executors.newSingleThreadExecutor();
	int numberOfImages;

	@FXML
	private Text enterSearchTerm;
	@FXML
	private TextField enterSearchTermTextInput;
	@FXML
	private Button searchWikipediaButton;
	@FXML
	private Text 	searchInProgress;
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
	private void handleCreationCancelButton(ActionEvent event) throws IOException {

		Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/listCreationsScene.fxml"));
		Scene creationViewScene = new Scene(creationViewParent);

		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

		window.setScene(creationViewScene);
		window.show();
	}

	@FXML
	private void handleSearchWikipedia(ActionEvent event) throws IOException {
		_searchTerm = enterSearchTermTextInput.getText();

		if (_searchTerm.equals("") || _searchTerm.equals(null)) {
			termNotFound.setVisible(true);
		} else {
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
	}

	@FXML
	private void handlePreviewChunk(ActionEvent event) throws IOException {
		String chunk = searchResultTextArea.getSelectedText();

		boolean isValidChunk = checkForValidChunk(chunk);
		if (isValidChunk) {
			// Remove brackets from the chunk to prevent error
			chunk = chunk.replace("(", "").replace(")", "");

			// Run bash script using festival tts to speak the selected text to the user
			String[] command = new String[]{"/bin/bash", "-c", "./script.sh preview " + chunk};
			BashCommand bashCommand = new BashCommand(command);
			team.submit(bashCommand);
		}
	}

	@FXML
	private void handleSaveChunk(ActionEvent event) throws IOException {
		String chunk = searchResultTextArea.getSelectedText();

		boolean isValidChunk = checkForValidChunk(chunk);
		if (isValidChunk) {
			String voiceChoice = voiceDropDownMenu.getValue();

			// Remove brackets from the chunk to prevent error
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
	}

	/**
	 * @return whether or not the chunk is valid
	 * valid if 0 < number of words <= 30, or the chunk is too long but the user confirms anyway
	 * Also handles the error and warning messages for when a chunk is too short or long
	 */
	private boolean checkForValidChunk(String chunk) {
		int numberOfWords;

		if (chunk == null || chunk.isEmpty()) {
			numberOfWords = 0;
		}

		// Splits the input at any instance of one or more whitespace character
		// The number of splits is the number of words
		String[] words = chunk.split("\\s+");
		numberOfWords = words.length;

		if (numberOfWords == 0) {
			Alert alert = new Alert(AlertType.ERROR, "Please select a chunk by highlighting text.");
			alert.showAndWait();
			return false;

		} else if (numberOfWords > 30) {
			String warningMessage = "Chunks longer than 30 words can sound worse. Are you sure you want to create this chunk?";
			Alert alert = new Alert(AlertType.WARNING, warningMessage, ButtonType.CANCEL, ButtonType.YES);

			// Display the confirmation alert and store the button pressed
			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.YES) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	private void updateChunkList() {
		// The chunks directory where all chunks are stored.
		final File chunksFolder = new File(System.getProperty("user.dir")+"/chunks/");

		List<String> fileNamesList = new ArrayList<String>();
		List<String> chunkNamesList = new ArrayList<String>();

		if (!chunksFolder.exists()) {
			chunksFolder.mkdirs();
		}

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
		Collections.sort(fileNamesList);

		if (numChunks == 0) {
			chunkNamesList.add("No Chunks Found.");
			chunkList.setDisable(true);
		} else {
			chunkList.setDisable(false);
		}

		// Turns the list of chunk names into an ObservableList<String> and displays to the GUI.
		ObservableList<String> observableListChunkNames = FXCollections.observableArrayList(chunkNamesList);
		chunkList.setItems(observableListChunkNames);
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
		chunksAsString += selectedChunks.get(numChunksSelected-1);

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
		ObservableList<String> selectedChunks = chunkList.getSelectionModel().getSelectedItems();
		if (selectedChunks.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Invalid number of chunks");
			alert.setContentText("Please enter valid chunk(s) by clicking on them (or ctrl+click / shift+click)");
			alert.showAndWait();
			return;
		}
		
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
		//_NumberOfImagesTextField.setDisable(true);

		_numberImagesButton.setVisible(true);
		//_numberImagesButton.setDisable(true);


		//maybe add text
		numberOfImagesPrompt.setVisible(true);

		//_NumberOfImagesTextField.setDisable(false);
		//_numberImagesButton.setDisable(false);
	}

	@FXML	// this method actually starts the creation bad name
	private void handleCheckCreationButton(ActionEvent event) throws IOException  {
		System.out.println("got to here at least");
		if (!creationNameTextField.getText().matches("[a-zA-Z0-9_-]*") || creationNameTextField.getText().isEmpty()) {
			// throw alerts
		} else if (!validCreationName(creationNameTextField.getText())) {
			// throw alerts


			//override existing file name
			String creationName = creationNameTextField.getText();
			File _existingfile = new File(System.getProperty("user.dir")+"/creations/"+ creationName +".mp4");
			// _existingfile.delete();

		} else { //on success
			// FlickrImagesTask
			// need to check valid number and search term
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


		}
	}

	private void createVideo() {
		System.out.println(""+ enterSearchTermTextInput.getText() + creationNameTextField.getText() + numberOfImages );

		ImageVideoTask flickrImagesTask = new ImageVideoTask (enterSearchTermTextInput.getText(), creationNameTextField.getText(), numberOfImages );
		threadWorker.submit(flickrImagesTask);


		flickrImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Creation:  "+ creationNameTextField.getText() +"is finished");
				alert.setContentText("Creation:  "+ creationNameTextField.getText() +"is finished");
				alert.showAndWait();
			}
		});
	}

	@FXML
	private void handleNumberOfImagesButton() {
		if (!(_NumberOfImagesTextField.getText().isEmpty())) {

			int num = Integer.parseInt(_NumberOfImagesTextField.getText());
			if (num <= 0 || num > 10) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Invalid number of images");
				//alert.setHeaderText(" delete " + _selectedCreation);
				alert.setContentText("Please enter a valid number between 1 and 10");
				alert.showAndWait();
				return;
			}
			//possibly let the user continue on from this point
			// if successful let them see the create button
			// and set transparency of number of items to lower.

			numberOfImages = Integer.parseInt(_NumberOfImagesTextField.getText());

			_NumberOfImagesTextField.setDisable(true);
			_numberImagesButton.setDisable(true);
			numberOfImagesPrompt.setVisible(false);

			creationNamePrompt.setVisible(true);
			creationNameTextField.setDisable(false);
			finalCreate.setDisable(false);


		}
	}

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
