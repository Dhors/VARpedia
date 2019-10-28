package application.controller;

import application.tasks.CombineChunksTask;
import application.Main;
import application.tasks.WikiSearchTask;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import application.tasks.PreviewTextTask;
import application.tasks.SaveTextTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewCreationController {
	private ExecutorService team = Executors.newFixedThreadPool(5);

	private static String _searchTerm;

	@FXML
	private ToggleButton backgroundMusicButton;

	@FXML
	private Label enterSearchTerm;
	@FXML
	private TextField enterSearchTermTextInput;
	@FXML
	private Button searchWikipediaButton;
	@FXML
	private Text searchInProgress;


	@FXML
	private TextArea searchResultTextArea;
	@FXML
	private Button previewChunk;
	@FXML
	private Button saveChunk;
	@FXML
	private Label voiceSelectDescription;
	@FXML
	private Label textSelectDescription;
	@FXML
	private Label chunkSelectDescription;
	@FXML
	private ChoiceBox<String> voiceDropDownMenu;
	@FXML
	private ListView<String> chunkList;



	@FXML
	private Button selectButton;

	// buttons to move audio===================================
	@FXML
	private Button _moveUpButton;
	@FXML
	private Button _moveDownButton;
	@FXML
	private Button _deleteButton;
	@FXML
	private Pane _progressPane;

	@FXML
	private ImageView _searchImage;


	private static String _selectedChunk;
	@FXML
	private ProgressBar _wikiProgressBar;

	@FXML
	private void initialize() {

		cleanChunks();

		_searchImage.setVisible(true);

		Main.setCurrentScene("CreationScene");
		backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
        backgroundMusicButton.setSelected(Main.backgroundMusicPlayer().getButtonIsSelected());
		
		_selectedChunk=null;

		setUpBooleanBindings();
		
		// Add the options for chunk voices, and set the default choice
		voiceDropDownMenu.getItems().addAll("Default", "NZ-Man", "NZ-Woman");
		voiceDropDownMenu.setValue("Default");
	}

	@FXML
	private void handleCreationCancelButton() throws IOException {
		// Return to main menu
		Main.changeScene("resources/MainScreenScene.fxml");
		cleanChunks();
	}
	
	@FXML
	private void handleSearchWikipedia() throws IOException {
		_searchTerm = enterSearchTermTextInput.getText().trim();
		_searchImage.setVisible(false);
		_progressPane.setVisible(true);
		_wikiProgressBar.setVisible(true);

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

					_searchImage.setVisible(false);
					_progressPane.setVisible(false);
					_wikiProgressBar.setVisible(false);
					
					// Returns a list with only one element.
					// If the term was not found, the element is "(Term not found)"
					// Otherwise the element is the search result
					String searchResult = wikiSearchTask.get();

					if (searchResult.contains("not found :^")) {
						searchInProgress.setVisible(false);

						Alert invalidSearchAlert = new Alert(Alert.AlertType.ERROR);
						invalidSearchAlert.setTitle("That term cannot be searched");
						invalidSearchAlert.setHeaderText(null);
						invalidSearchAlert.setContentText("Please enter a different search term");
						invalidSearchAlert.showAndWait();

						//termNotFound.setVisible(true);
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
	private void handlePreviewChunk() throws IOException {
		String chunk = searchResultTextArea.getSelectedText().trim();

		if (isValidChunk(chunk)) {
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

			// Run bash script using festival to save a .wav file containing the spoken selected text
			SaveTextTask saveTextTask = new SaveTextTask(voiceChoice, chunk);
			team.submit(saveTextTask);

			saveTextTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					// Make the new chunk visible to the user
					chunkList.getItems().add(saveTextTask.getValue().replace(".wav", ""));
					chunkList.setDisable(false);
					selectButton.setDisable(false);

					if (chunkList.getItems().get(0).equals("No Chunks Found.")){
						chunkList.getItems().remove(0);
					}

					if (chunkList.getItems().size() > 1 && _selectedChunk != null)   {
						_moveUpButton.setDisable(false);
						_moveDownButton.setDisable(false);
					}
				}
			});
		}
	}
	
	
	
	@FXML
	private void handleDeleteChunkButton(){
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
	
	@FXML
	private void handleMoveUpButton() throws IOException {
		if (_selectedChunk != null) {
			int selectedChunkIndex = chunkList.getSelectionModel().getSelectedIndex();

			// only move up if they do not select the top-most chunk
			if (selectedChunkIndex > 0){
				moveSelectedChunk(selectedChunkIndex, selectedChunkIndex - 1);
			}
		}
	}

	@FXML
	private void handleMoveDownButton() throws IOException {
		if (_selectedChunk != null) {
			int selectedChunkIndex = chunkList.getSelectionModel().getSelectedIndex();

			// only move down if they do not select the bottom-most chunk
			if (selectedChunkIndex < chunkList.getItems().size() - 1) {
				moveSelectedChunk(selectedChunkIndex, selectedChunkIndex + 1);
			}
		}
	}

	private void moveSelectedChunk(int oldIndex, int newIndex) {
		chunkList.getItems().remove(oldIndex);
		chunkList.getItems().add(newIndex, _selectedChunk);
		chunkList.getSelectionModel().select(newIndex);
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
		ObservableList<String> chunksList = chunkList.getItems();
		
		// Run bash script to create a combined audio of each selected chunk
		CombineChunksTask combineChunksTask = new CombineChunksTask(chunksList, searchTerm);
		team.submit(combineChunksTask);

		combineChunksTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					Main.changeScene("resources/ImagesSelectionScene.fxml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@FXML
	public void handleSelectedChunk() {
		_selectedChunk = chunkList.getSelectionModel().getSelectedItem();

		if (_selectedChunk != null) {
			selectButton.setDisable(false);
			
			if (chunkList.getItems().size() > 1) {
				_moveUpButton.setDisable(false);
				_moveDownButton.setDisable(false);
			}
		}
	}

	private void displayChunkSelection() {
		// Hide search elements
		enterSearchTerm.setVisible(false);
		enterSearchTermTextInput.setVisible(false);
		searchWikipediaButton.setVisible(false);
		searchInProgress.setVisible(false);
		//termNotFound.setVisible(false);

		// Show chunk elements
		searchResultTextArea.setVisible(true);
		previewChunk.setVisible(true);
		saveChunk.setVisible(true);
		voiceSelectDescription.setVisible(true);
		textSelectDescription.setVisible(true);
		voiceDropDownMenu.setVisible(true);
		
		chunkSelectDescription.setVisible(true);
		chunkList.setVisible(true);
		selectButton.setVisible(true);
		_deleteButton.setVisible(true);
		_moveUpButton.setVisible(true);
		_moveDownButton.setVisible(true);

		// Show the currently stored chunks
		updateChunkList();
	}

	private void updateChunkList() {
		// The chunks directory where all chunks are stored.
		final File chunksFolder = new File(System.getProperty("user.dir")+"/chunks/");
		if (!chunksFolder.exists()) {
			chunksFolder.mkdirs();
		}
		
		List<String> chunkNamesList = new ArrayList<String>();
		// Checks every file in the chunks directory. If a file is .wav format,
		// the file name without .wav extension to the list of chunkNames, and keeps count
		for (File fileName : chunksFolder.listFiles()) {
			if (fileName.getName().endsWith(".wav")) {
				chunkNamesList.add(fileName.getName().replace(".wav", ""));
			}
		}

		if (chunkNamesList.size() == 0) {
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
	
	private boolean isValidChunk(String chunk) {
		if (numberOfWords(chunk) < 30) {
			return true;
		} 
		
		// If the user selects 30 or more words, they must confirm that they want to continue
		String warningMessage = "Chunks longer than 30 words can result in a lower sound quality. Are you sure you want to create this chunk?";
		Alert alert = new Alert(AlertType.WARNING, warningMessage, ButtonType.CANCEL, ButtonType.YES);
		// Display the confirmation alert and store the button pressed
		Optional<ButtonType> buttonClicked = alert.showAndWait();

		if (buttonClicked.isPresent() && buttonClicked.get() == ButtonType.YES) {
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
	
	private void setUpBooleanBindings() {
		// Don't let the user search until they enter a search term
		BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() ->
				enterSearchTermTextInput.getText().trim().isEmpty(),
				enterSearchTermTextInput.textProperty());
		searchWikipediaButton.disableProperty().bind(textIsEmpty);

		// Don't let the user create a chunk until they select some text
		BooleanBinding noTextSelected = Bindings.createBooleanBinding(() ->
				!(numberOfWords(searchResultTextArea.getSelectedText().trim()) > 0),
				searchResultTextArea.selectedTextProperty());
		previewChunk.disableProperty().bind(noTextSelected);
		saveChunk.disableProperty().bind(noTextSelected);

		// Don't let the user delete the selected chunk until they select at least one
		BooleanBinding noChunkSelected = chunkList.getSelectionModel().selectedItemProperty().isNull();
		_deleteButton.disableProperty().bind(noChunkSelected);
	}
	
	public static String getSearchTerm(){
		return 	_searchTerm;
	}



	// Cleaning the chunks folder if the creation is cancelled.
	private void cleanChunks(){
		File chunksFolder = new File(System.getProperty("user.dir") + "/chunks/");
		if (chunksFolder.exists()) {
			for (final File chunkFileName : chunksFolder.listFiles()) {
				chunkFileName.delete();
			}
			chunksFolder.delete();
		}
	}



	@FXML
    private void handleBackgroundMusic() throws IOException {
		Main.backgroundMusicPlayer().handleBackgroundMusic(backgroundMusicButton.isSelected());
    	backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
    }






}