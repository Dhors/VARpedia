package application.controller;

import application.tasks.CreateCreationTask;
import application.Main;
import application.tasks.WikiSearchTask;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
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
		//need this for move button
		_selectedChunk=null;
		searchWikipediaButton.setDisable(true);
		
		updateChunkList();
		setUpButtonBooleanBindings();
	}

	@FXML
	private void handleMoveUpButton() throws IOException {

		int chunkIndex = chunkList.getSelectionModel().getSelectedIndex();
		String selectedChunk = chunkList.getSelectionModel().getSelectedItem();
		// Only move if the chunk is not the first in the list
		if (chunkIndex > 0){
			chunkList.getItems().remove(chunkIndex);
			chunkList.getItems().add(chunkIndex-1,selectedChunk);
			chunkList.getSelectionModel().select(chunkIndex-1);
		}
	}
	@FXML
	private void handleMoveDownButton() throws IOException {
		int chunkIndex = chunkList.getSelectionModel().getSelectedIndex();
		String selectedChunk = chunkList.getSelectionModel().getSelectedItem();
		// Only move if the chunk is not the last in the list
		if (chunkIndex < chunkList.getItems().size() - 1) {
			chunkList.getItems().remove(chunkIndex);
			chunkList.getItems().add(chunkIndex + 1, selectedChunk);
			chunkList.getSelectionModel().select(chunkIndex + 1);
		}
	}

	@FXML
	private void handleCreationCancelButton(ActionEvent event) throws IOException {
		// Return to list of creations
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

		// If the number of words is 30 or more, the user is asked whether to continue.
		// If they don't click "yes", stop previewing chunk
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

		// Return whether or not the user clicked yes
		if (result.isPresent() && result.get() == ButtonType.YES) {
			return true;
		} else {
			return false;
		}
	}
	
	@FXML
	private void handleSaveChunk(ActionEvent event) throws IOException {
		String chunk = searchResultTextArea.getSelectedText().trim();
		
		// If the number of words is 30 or more, the user is asked whether to continue.
		// If they don't click "yes", stop saving chunk
		if (numberOfWords(chunk) >= 30 && !lengthConfirmed()) {
			return;
		}
		// If reached here then the chunk is valid

		// Remove brackets from the chunk, some voices can't speak with brackets
		chunk = chunk.replace("(", "").replace(")", "");
				
		String voiceChoice = voiceDropDownMenu.getValue();

		// Run bash script using festival to save a .wav file containing the spoken selected text
		SaveTextTask saveTextTask = new SaveTextTask(voiceChoice, chunk);
		team.submit(saveTextTask);
		
		saveTextTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				// Make the new chunk visible to the user
				//chunkList.getItems().add() =================dont update it, rather just add the new chunck in amnually with name==========================================



				chunkList.getItems().add(saveTextTask.getValue().replace(".wav", ""));


				chunkList.setDisable(false);
				if (chunkList.getItems().get(0).equals("No Chunks Found.")){
					chunkList.getItems().remove(0);
				}

				if ((chunkList.getItems().size()>=2)&&(!(_selectedChunk==null))  ) {
					_moveUpButton.setDisable(false);
//					_moveDownButton.setDisable(false);
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
		//chunkList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
		//temp testing this out===========================================================================
		//ObservableList<String> selectedChunks = chunkList.getSelectionModel().getSelectedItems();
		ObservableList<String> selectedChunks = chunkList.getItems();
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
				//_deleteButton.setDisable(false);

				if (chunkList.getItems().size()>=2) {
					_moveUpButton.setDisable(false);
//					_moveDownButton.setDisable(false);
				} else {
					_moveUpButton.setDisable(true);
//					_moveDownButton.setDisable(true);
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
//				_moveDownButton.setDisable(true);
			}

        }
	}

	private void setUpButtonBooleanBindings() {
		// Don't let the user search until they put in a search term
//		BooleanBinding textIsEmpty = enterSearchTermTextInput.textProperty().isEmpty();	

		BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() -> 
				enterSearchTermTextInput.getText().trim().isEmpty(),
				enterSearchTermTextInput.textProperty()
			);
		searchWikipediaButton.disableProperty().bind(textIsEmpty);


		// Don't let the user preview or save a chunk until they have selected some non-space text
//		BooleanBinding noTextSelected = searchResultTextArea.selectedTextProperty().isEmpty();

		BooleanBinding noTextSelected = Bindings.createBooleanBinding(() -> 
				!(numberOfWords(searchResultTextArea.getSelectedText().trim()) > 0),
				searchResultTextArea.selectedTextProperty()
			);
		previewChunk.disableProperty().bind(noTextSelected);
		saveChunk.disableProperty().bind(noTextSelected);

		// Don't let the user confirm the selected chunks or delete a chunk until they select at least one
		BooleanBinding noChunkSelected = chunkList.getSelectionModel().selectedItemProperty().isNull();
		selectButton.disableProperty().bind(noChunkSelected);
		_deleteButton.disableProperty().bind(noChunkSelected);

		
		// TODO don't let user moveup unless valid chunk selected
		
		// TODO don't let user movedown unless there are at least two chunks and a chunk is selected
		
//		BooleanBinding upDownButtonBinding = Bindings.size(chunkList.getItems()).lessThan(2).or(chunkList.getSelectionModel().selectedItemProperty().isNull());
		
//		BooleanBinding cantMoveDown = Bindings.createBooleanBinding(() -> 
//		(chunkList.itemsProperty().get().size() < 2),
//		chunkList.itemsProperty());
//				.or(noChunkSelected);
		
//		IntegerBinding listSizeProperty = Bindings.size(chunkList.getItems());
//		BooleanBinding sizeGoodSize = listSizeProperty.greaterThanOrEqualTo(2);
		
//		BooleanBinding cantMoveDown = Bindings.size(chunkList.itemsProperty().get()).lessThan(2).or(noChunkSelected);
		
//		_moveDownButton.disableProperty().bind(sizeGoodSize.not());
	}


}
