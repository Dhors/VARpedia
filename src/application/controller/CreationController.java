package application.controller;

import application.tasks.CreateCreationTask;
import application.Main;
import application.tasks.WikiSearchTask;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import application.tasks.ImageVideoTask;
import application.tasks.PreviewTextTask;
import application.tasks.SaveTextTask;
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

	private static String _searchTerm;



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

	// buttons to move audio===================================
	@FXML
	private Button _moveUpButton;
	@FXML
	private Button _moveDownButton;
	@FXML
	private Button _deleteButton;


	private static String _selectedChunk;


	@FXML
	private void initialize() {
		searchWikipediaButton.setDisable(true);

		// Don't let the user search until they put in a search term
//		BooleanBinding textIsEmpty = enterSearchTermTextInput.textProperty().isEmpty();	

		BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() -> 
				enterSearchTermTextInput.getText().trim().isEmpty(),
				enterSearchTermTextInput.textProperty()
			);
		searchWikipediaButton.disableProperty().bind(textIsEmpty);


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

		//selectButton.disableProperty().bind(noChunkSelected);

		// Don't let the user confirm the number of images until they put in a valid number
		// TODO

		// Don't let the user confirm the name of the creation until it is valid
		// TODO
	}

	@FXML
	private void handleMoveUpButton() throws IOException {

		int chunkIndex = chunkList.getSelectionModel().getSelectedIndex();
		String selectedChunk = chunkList.getSelectionModel().getSelectedItem();
		//inside the array
		if (chunkIndex >=1){
			chunkList.getItems().remove(chunkIndex);
			chunkList.getItems().add(chunkIndex-1,selectedChunk);
			chunkList.getSelectionModel().select(chunkIndex-1);
		}
	}
	@FXML
	private void handleMoveDownButton() throws IOException {
		int chunkIndex = chunkList.getSelectionModel().getSelectedIndex();
		String selectedChunk = chunkList.getSelectionModel().getSelectedItem();
		//inside the array
		if (chunkIndex <= chunkList.getItems().size()-2){
			chunkList.getItems().remove(chunkIndex);
			chunkList.getItems().add(chunkIndex+1,selectedChunk);
			chunkList.getSelectionModel().select(chunkIndex+1);
		}
	}

	@FXML
	private void handleCreationCancelButton(ActionEvent event) throws IOException {
		// Return to main menu
		Main.changeScene("resources/listCreationsScene.fxml");

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
		WikiSearchTask wikiSearchTask = new WikiSearchTask(_searchTerm);
		team.submit(wikiSearchTask);

		// Using concurrency allows the user to cancel the creation if the search takes too long
		wikiSearchTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					/**
					 * Returns a list with only one element
					 * If the term was not found, the element is "(Term not found)"
					 * Otherwise the element is the search result
					 */
					String searchResult = wikiSearchTask.get();

					if (searchResult.contains("not found :^")) {
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
		String chunk = searchResultTextArea.getSelectedText().trim();

		if (numberOfWords(chunk) >= 30 && !lengthConfirmed()) {
			return;
		}
		// If reached here then the chunk is valid

		// Remove brackets from the chunk, some voices can't speak with brackets
		chunk = chunk.replace("(", "").replace(")", "");

		// Run bash script using festival tts to speak the selected text to the user
		PreviewTextTask previewTextTask = new PreviewTextTask(chunk);
		team.submit(previewTextTask);
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
		String chunk = searchResultTextArea.getSelectedText().trim();
		if (numberOfWords(chunk) >= 30 && !lengthConfirmed()) {
			return;
		}
		// If reached here then the chunk is valid

		String voiceChoice = voiceDropDownMenu.getValue();

		// Remove brackets from the chunk, some voices can't speak with brackets
		chunk = chunk.replace("(", "").replace(")", "");

		// Run bash script using festival to save a .wav file containing the spoken selected text
		SaveTextTask saveTextTask = new SaveTextTask(voiceChoice, chunk);
		team.submit(saveTextTask);
		

		saveTextTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				// Make the new chunk visible to the user
				//chunkList.getItems().add() =================dont update it, rather just add the new chunck in amnually with name==========================================
				chunkList.getItems().add(saveTextTask.getValue().replace(".wav", ""));

				if (chunkList.getItems().size()>=2) {
					_moveUpButton.setDisable(false);
					_moveDownButton.setDisable(false);
				}
				//updateChunkList();
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

		_deleteButton.setVisible(true);
		_moveUpButton.setVisible(true);
		_moveDownButton.setVisible(true);

	}

	@FXML
	private void handleSelectButton() throws IOException {
		File outputfile = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm);
		if (!outputfile.exists()) {
			outputfile.mkdirs();
		}

		combineAudioChunks(_searchTerm);

	}



	private void combineAudioChunks(String creationName) {
		ObservableList<String> selectedChunks = chunkList.getSelectionModel().getSelectedItems();

		// Run bash script to create a combined audio of each selected chunk
//		String[] command = new String[]{"/bin/bash", "-c", "./script.sh create " + creationName + " " + chunksAsString};		
//		BashCommand bashCommand = new BashCommand(command);
		CreateCreationTask createCreationTask = new CreateCreationTask(selectedChunks, creationName);
		team.submit(createCreationTask);

		createCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				//createVideo();

				//its already changed scenes but it might be at risk is super long audio
				// I would lik for it to happen at the same time the images and the audio.
				try {
					Main.changeScene("resources/ImagesSelection.fxml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}


	//need this dont touch
	public static String getSearchTerm(){
		return 	_searchTerm;
	}

	@FXML
	public void handleSelectedChunk() {

			_selectedChunk = chunkList.getSelectionModel().getSelectedItem();

			if (!(_selectedChunk==null)) {
			_deleteButton.setDisable(false);

			if (chunkList.getItems().size()>=2) {
				_moveUpButton.setDisable(false);
				_moveDownButton.setDisable(false);
			} else {
				_moveUpButton.setDisable(true);
				_moveDownButton.setDisable(true);
			}
		}
	}



	@FXML
	private void handleDeleteChunkButton(){
	    if (!(_selectedChunk==null)) {
            int chunkIndex = chunkList.getSelectionModel().getSelectedIndex();
            chunkList.getItems().remove(chunkIndex);
            File _selectedfile = new File(System.getProperty("user.dir") + "/chunks/" + _selectedChunk + ".wav");
            _selectedfile.delete();
            //updateChunkList();

			if (chunkList.getItems().size()<2) {
				_moveUpButton.setDisable(true);
				_moveDownButton.setDisable(true);
			}

        }
	}




}
