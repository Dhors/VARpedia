package application.controller;

import application.tasks.CreateCreationTask;
import application.Main;
import application.tasks.WikiSearchTask;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import application.tasks.PreviewTextTask;
import application.tasks.SaveTextTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
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
	private ProgressBar _wikiProgressBar;

	@FXML
	private void initialize() {
		_selectedChunk=null;

		setUpBooleanBindings();
		
		// Add the options for chunk voices, and set the default choice
		voiceDropDownMenu.getItems().addAll("Default", "NZ-Man", "NZ-Woman");
		voiceDropDownMenu.setValue("Default");
	}

	@FXML
	private void handleCreationCancelButton() throws IOException {
		// Return to main menu
		Main.changeScene("resources/listCreationsScene.fxml");

		// Cleaning the chunks folder if the creation is cancelled.
		File chunksFolder = new File(System.getProperty("user.dir") + "/chunks/" );
		if (chunksFolder.exists()) {
			for (final File chunkFileName : chunksFolder.listFiles()) {
				chunkFileName.delete();
			}
			chunksFolder.delete();
		}
	}
	
	@FXML
	private void handleSearchWikipedia() throws IOException {
		_searchTerm = enterSearchTermTextInput.getText().trim();
		
		_wikiProgressBar.setVisible(true);
		termNotFound.setVisible(false);
		searchInProgress.setVisible(true);

		// Run bash script that uses wikit and returns the result of the search
		WikiSearchTask wikiSearchTask = new WikiSearchTask(_searchTerm);
		_wikiProgressBar.progressProperty().bind(wikiSearchTask.progressProperty());

		team.submit(wikiSearchTask);

		// Using concurrency allows the user to cancel the creation if the search takes too long
		wikiSearchTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					_wikiProgressBar.setVisible(false);
					
					// Returns a list with only one element.
					// If the term was not found, the element is "(Term not found)"
					// Otherwise the element is the search result
					String searchResult = wikiSearchTask.get();

					if (searchResult.contains("not found :^")) {
						searchInProgress.setVisible(false);
						termNotFound.setVisible(true);
					} else {
						searchResultTextArea.setText(searchResult);
						displayChunkSelection();
					}
					
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	
	@FXML
	private void handleMoveUpButton() throws IOException {

		int selectedChunkIndex = chunkList.getSelectionModel().getSelectedIndex();
		
		// only move up if they do not select the top-most chunk
		if (selectedChunkIndex > 0){
			moveSelectedChunk(selectedChunkIndex, selectedChunkIndex - 1);
		}
	}
	
	@FXML
	private void handleMoveDownButton() throws IOException {
		
		int selectedChunkIndex = chunkList.getSelectionModel().getSelectedIndex();

		// only move down if they do not select the bottom-most chunk
		if (selectedChunkIndex < chunkList.getItems().size() - 1) {
			moveSelectedChunk(selectedChunkIndex, selectedChunkIndex + 1);
		}
	}

	private void moveSelectedChunk(int oldIndex, int newIndex) {
		chunkList.getItems().remove(oldIndex);
		chunkList.getItems().add(newIndex, _selectedChunk);
		chunkList.getSelectionModel().select(newIndex);
	}

	@FXML
	private void handlePreviewChunk() throws IOException {
		String chunk = searchResultTextArea.getSelectedText().trim();

		if (isValidChunk(chunk)) {
			// Remove brackets from the chunk, some voices can't speak with brackets
			chunk = chunk.replace("(", "").replace(")", "");

			// Run bash script using festival tts to speak the selected text to the user
			PreviewTextTask previewTextTask = new PreviewTextTask(chunk);
			team.submit(previewTextTask);
		}
	}

	@FXML
	private void handleSaveChunk() throws IOException {
		String chunk = searchResultTextArea.getSelectedText().trim();

		if (isValidChunk(chunk)) {
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
					//chunkList.getItems().add() 
					//=================dont update it, rather just add the new chunck in amnually with name==========================================

					chunkList.getItems().add(saveTextTask.getValue().replace(".wav", ""));
					selectButton.setDisable(false);
					if (chunkList.getItems().size()>0) {
						chunkList.setDisable(false);
						if (chunkList.getItems().get(0).equals("No Chunks Found.")){
							chunkList.getItems().remove(0);

						}
					}
					System.out.println(""+_selectedChunk);
					if (   (chunkList.getItems().size()>1))   {
						_moveUpButton.setDisable(false);
						_moveDownButton.setDisable(false);

					}
					//updateChunkList();
				}
			});
		}
	}
	
	private boolean isValidChunk(String chunk) {
		if (numberOfWords(chunk) < 30) {
			return true;
		} 
		
		// If the user selects 30 or more words, they must confirm that they want to continue
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
	
	private int numberOfWords(String chunk) {
		if (chunk.isEmpty()) {
			return 0;
		}
		
		// Splits the input at any instance of one or more whitespace character
		// The number of splits is the number of words
		String[] words = chunk.split("\\s+");
		return words.length;
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
			selectButton.setDisable(true);
		} else {
			chunkList.setDisable(false);
			selectButton.setDisable(false);
		}

		// Turns the list of chunk names into an ObservableList<String> and displays to the GUI.
		ObservableList<String> observableChunkNamesList = FXCollections.observableArrayList(chunkNamesList);
		chunkList.setItems(observableChunkNamesList);
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
		_deleteButton.setVisible(true);
		_moveUpButton.setVisible(true);
		_moveDownButton.setVisible(true);

		// Show the currently stored chunks
		updateChunkList();
	}

	@FXML
	private void handleSelectButton() throws IOException {
		File outputFolder = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		combineAudioChunks(_searchTerm);
	}

	private void combineAudioChunks(String searchTerm) {
		ObservableList<String> selectedChunks = chunkList.getItems();
		
		// Run bash script to create a combined audio of each selected chunk
		CreateCreationTask createCreationTask = new CreateCreationTask(selectedChunks, searchTerm);
		team.submit(createCreationTask);

		createCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					Main.changeScene("resources/ImagesSelection.fxml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	

	@FXML
	public void handleSelectedChunk() {

		_selectedChunk = chunkList.getSelectionModel().getSelectedItem();

		if (!(_selectedChunk==null)) {
			selectButton.setDisable(false);
			if (chunkList.getItems().size()>1) {
				_moveUpButton.setDisable(false);
				_moveDownButton.setDisable(false);
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
			chunkList.getSelectionModel().clearSelection();
			_selectedChunk=null;

			_moveUpButton.setDisable(true);
			_moveDownButton.setDisable(true);

			if (chunkList.getItems().size()==0) {
				chunkList.getItems().add("No Chunks Found.");
				chunkList.setDisable(true);
				selectButton.setDisable(true);
			}

		}
	}


	private void setUpBooleanBindings() {
		// Don't let the user search until they enter a search term
//		BooleanBinding textIsEmpty = enterSearchTermTextInput.textProperty().isEmpty();	
		BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() ->
						enterSearchTermTextInput.getText().trim().isEmpty(),
				enterSearchTermTextInput.textProperty()
		);
		searchWikipediaButton.disableProperty().bind(textIsEmpty);


		// Don't let the user create a chunk until they select some text
//		BooleanBinding noTextSelected = searchResultTextArea.selectedTextProperty().isEmpty();
		BooleanBinding noTextSelected = Bindings.createBooleanBinding(() ->
						!(numberOfWords(searchResultTextArea.getSelectedText().trim()) > 0),
				searchResultTextArea.selectedTextProperty()
		);
		previewChunk.disableProperty().bind(noTextSelected);
		saveChunk.disableProperty().bind(noTextSelected);

		// Don't let the user delete the selected chunk until they select at least one
		BooleanBinding noChunkSelected = chunkList.getSelectionModel().selectedItemProperty().isNull();
		_deleteButton.disableProperty().bind(noChunkSelected);
		
		//BooleanBinding upDownButtonBinding = Bindings.size(chunkList.getItems()).lessThan(2).or(chunkList.getSelectionModel().selectedItemProperty().isNull());
	}
	public static String getSearchTerm(){
		return 	_searchTerm;
	}
}