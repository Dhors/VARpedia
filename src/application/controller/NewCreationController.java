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

	private ExecutorService _team = Executors.newFixedThreadPool(5);

	@FXML
	private ToggleButton _backgroundMusicButton;
	@FXML
	private Label _enterSearchTerm;
	@FXML
	private TextField _enterSearchTermTextInput;
	@FXML
	private Button _searchWikipediaButton;
	@FXML
	private Text _searchInProgress;
	@FXML
	private TextArea _searchResultTextArea;
	@FXML
	private Button _previewChunk;
	@FXML
	private Button _saveChunk;
	@FXML
	private Label _voiceSelectDescription;
	@FXML
	private Label _textSelectDescription;
	@FXML
	private Label _chunkSelectDescription;
	@FXML
	private ChoiceBox<String> _voiceDropDownMenu;
	@FXML
	private ListView<String> _chunkList;

	@FXML
	private Button _selectButton;
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

	@FXML
	private ProgressBar _wikiProgressBar;

	private static String _searchTerm;
	private static String _selectedChunk;

    @FXML
	private void initialize() {

		cleanChunks();

		_searchImage.setVisible(true);

		Main.setCurrentScene("CreationScene");
		_backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
        _backgroundMusicButton.setSelected(Main.backgroundMusicPlayer().getButtonIsSelected());
		
		_selectedChunk=null;

		setUpBooleanBindings();
		
		// Add the options for chunk voices, and set the default choice
		_voiceDropDownMenu.getItems().addAll("Default", "NZ-Man", "NZ-Woman");
		_voiceDropDownMenu.setValue("Default");
	}

	@FXML
	private void handleCreationCancelButton() throws IOException {
		// Return to main menu
		Main.changeScene("resources/MainScreenScene.fxml");
		cleanChunks();
	}
	
	@FXML
	private void handleSearchWikipedia() throws IOException {
		_searchTerm = _enterSearchTermTextInput.getText().trim();
		_searchImage.setVisible(false);
		_progressPane.setVisible(true);
		_wikiProgressBar.setVisible(true);

		_searchInProgress.setVisible(true);

		// Run bash script that uses wikit and returns the result of the search
		WikiSearchTask wikiSearchTask = new WikiSearchTask(_searchTerm);
		_wikiProgressBar.progressProperty().bind(wikiSearchTask.progressProperty());

		_team.submit(wikiSearchTask);

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
						_searchInProgress.setVisible(false);

						Alert invalidSearchAlert = new Alert(Alert.AlertType.ERROR);
						invalidSearchAlert.getDialogPane().getStylesheets().add(("Alert.css"));
						invalidSearchAlert.setTitle("That term cannot be searched");
						invalidSearchAlert.setHeaderText(null);
						invalidSearchAlert.setContentText("Please enter a different search term");
						invalidSearchAlert.showAndWait();

					} else {
						_searchResultTextArea.setText(searchResult);
						displayChunkSelection();
					}
					
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	
	@FXML
	private void handlePreviewChunk() {
		String chunk = _searchResultTextArea.getSelectedText().trim();
		chunk = chunk.replaceAll("[^a-zA-Z0-9-_ ]*", "");
		
		if (isValidChunk(chunk)) {
			// Run bash script using festival tts to speak the selected text to the user
			PreviewTextTask previewTextTask = new PreviewTextTask(chunk);
			_team.submit(previewTextTask);
		}
	}

	@FXML
	private void handleSaveChunk() {
		String chunk = _searchResultTextArea.getSelectedText().trim();
		chunk = chunk.replaceAll("[^a-zA-Z0-9-_ ]*", "");

		if (isValidChunk(chunk)) {
			String voiceChoice = _voiceDropDownMenu.getValue();

			// Run bash script using festival to save a .wav file containing the spoken selected text

			SaveTextTask saveTextTask = new SaveTextTask(voiceChoice, chunk);
			_team.submit(saveTextTask);

			saveTextTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					// Make the new chunk visible to the user
					_chunkList.getItems().add(saveTextTask.getValue().replace(".wav", ""));
					_chunkList.setDisable(false);
					_selectButton.setDisable(false);

					if (_chunkList.getItems().get(0).equals("No Chunks Found.")){
						_chunkList.getItems().remove(0);
					}

					if (_chunkList.getItems().size() > 1 && _selectedChunk != null)   {
						_moveUpButton.setDisable(false);
						_moveDownButton.setDisable(false);
					}
				}
			});
		}
	}
	
	
	// This method will give a confirmation of deletion. If the user confirms,
	// the selected chunk will be deleted.
	@FXML
	private void handleDeleteChunkButton(){
		int chunkIndex = _chunkList.getSelectionModel().getSelectedIndex();
		_chunkList.getItems().remove(chunkIndex);
		File _selectedfile = new File(System.getProperty("user.dir") + "/chunks/" + _selectedChunk + ".wav");
		_selectedfile.delete();

		_chunkList.getSelectionModel().clearSelection();
		_selectedChunk=null;

		_moveUpButton.setDisable(true);
		_moveDownButton.setDisable(true);

		if (_chunkList.getItems().size()==0) {
			_chunkList.getItems().add("No Chunks Found.");
			_chunkList.setDisable(true);
			_selectButton.setDisable(true);
		}
	}
	
	@FXML
	private void handleMoveUpButton() {
		if (_selectedChunk != null) {
			int selectedChunkIndex = _chunkList.getSelectionModel().getSelectedIndex();

			// only move up if they do not select the top-most chunk
			if (selectedChunkIndex > 0){
				moveSelectedChunk(selectedChunkIndex, selectedChunkIndex - 1);
			}
		}
	}

	@FXML
	private void handleMoveDownButton() {
		if (_selectedChunk != null) {
			int selectedChunkIndex = _chunkList.getSelectionModel().getSelectedIndex();

			// only move down if they do not select the bottom-most chunk
			if (selectedChunkIndex < _chunkList.getItems().size() - 1) {
				moveSelectedChunk(selectedChunkIndex, selectedChunkIndex + 1);
			}
		}
	}

	private void moveSelectedChunk(int oldIndex, int newIndex) {
		_chunkList.getItems().remove(oldIndex);
		_chunkList.getItems().add(newIndex, _selectedChunk);
		_chunkList.getSelectionModel().select(newIndex);
	}

	@FXML
	private void handleSelectButton() {
		File outputFolder = new File(System.getProperty("user.dir") + "/creations/" + _searchTerm);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		combineAudioChunks(_searchTerm);
	}

	private void combineAudioChunks(String searchTerm) {
		ObservableList<String> chunksList = _chunkList.getItems();
		
		// Run bash script to create a combined audio of each selected chunk
		CombineChunksTask combineChunksTask = new CombineChunksTask(chunksList, searchTerm);
		_team.submit(combineChunksTask);

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
		_selectedChunk = _chunkList.getSelectionModel().getSelectedItem();

		if (_selectedChunk != null) {
			_selectButton.setDisable(false);
			
			if (_chunkList.getItems().size() > 1) {
				_moveUpButton.setDisable(false);
				_moveDownButton.setDisable(false);
			}
		}
	}

	private void displayChunkSelection() {
		// Hide search elements
		_enterSearchTerm.setVisible(false);
		_enterSearchTermTextInput.setVisible(false);
		_searchWikipediaButton.setVisible(false);
		_searchInProgress.setVisible(false);
		//termNotFound.setVisible(false);

		// Show chunk elements
		_searchResultTextArea.setVisible(true);
		_previewChunk.setVisible(true);
		_saveChunk.setVisible(true);
		_voiceSelectDescription.setVisible(true);
		_textSelectDescription.setVisible(true);
		_voiceDropDownMenu.setVisible(true);
		
		_chunkSelectDescription.setVisible(true);
		_chunkList.setVisible(true);
		_selectButton.setVisible(true);
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
			_chunkList.setDisable(true);
			_selectButton.setDisable(true);
		} else {
			_chunkList.setDisable(false);
			_selectButton.setDisable(false);
		}
		
		// Turns the list of chunk names into an ObservableList<String> and displays to the GUI.
		ObservableList<String> observableChunkNamesList = FXCollections.observableArrayList(chunkNamesList);
		_chunkList.setItems(observableChunkNamesList);
	}
	
	private boolean isValidChunk(String chunk) {
		if (numberOfWords(chunk) < 30) {
			return true;
		} 
		
		// If the user selects 30 or more words, they must confirm that they want to continue
		String warningMessage = "Chunks longer than 30 words can result in a lower sound quality. Are you sure you want to create this chunk?";
		Alert alert = new Alert(AlertType.WARNING, warningMessage, ButtonType.CANCEL, ButtonType.YES);
		alert.getDialogPane().getStylesheets().add(("Alert.css"));
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
		/**
		 * Credit to user DVarga
		 * https://stackoverflow.com/questions/37853634/javafx-textfield-onkeytyped-not-working-properly
		 * "BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() -> textField.getText().trim().isEmpty(), textField.textProperty());
		 * button.disableProperty().bind(textIsEmpty);"
		 */
		// Don't let the user search until they enter a search term
		BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() ->
				_enterSearchTermTextInput.getText().trim().isEmpty(),
				_enterSearchTermTextInput.textProperty());
		_searchWikipediaButton.disableProperty().bind(textIsEmpty);

		// Don't let the user create a chunk until they select some text
		BooleanBinding noTextSelected = Bindings.createBooleanBinding(() ->
				!(numberOfWords(_searchResultTextArea.getSelectedText().trim()) > 0),
				_searchResultTextArea.selectedTextProperty());
		_previewChunk.disableProperty().bind(noTextSelected);
		_saveChunk.disableProperty().bind(noTextSelected);

		// Don't let the user delete the selected chunk until they select at least one
		BooleanBinding noChunkSelected = _chunkList.getSelectionModel().selectedItemProperty().isNull();
		_deleteButton.disableProperty().bind(noChunkSelected);
	}
	
	public static String getSearchTerm(){
		return 	_searchTerm;
	}



	// This method will clean the chunks folder if the creation is cancelled.
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
		Main.backgroundMusicPlayer().handleBackgroundMusic(_backgroundMusicButton.isSelected());
    	_backgroundMusicButton.setText(Main.backgroundMusicPlayer().getButtonText());
    }
}