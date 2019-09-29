package application.controller;

import application.BashCommand;
import application.Main;
import javafx.beans.property.ListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import application.ImageVideoTask;
import application.Main;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


import java.awt.*;
import java.io.File;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.util.Optional;
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
    private Text voiceLabel;
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
	private TextField _NumberOfImagesTextField;
	@FXML
	private Button _numberImagesButton;

	@FXML
	private void initialize() {
		
	}
	
    @FXML
    private TextField _creationNameTextField;




    @FXML
    private void handleCreationCancelButton(ActionEvent event) throws IOException {

        Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
        Scene creationViewScene = new Scene(creationViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(creationViewScene);
        window.show();
    }

	@FXML
    private void handleSearchWikipedia(ActionEvent event) throws IOException {
		getSearchResult();
	}

	@FXML
    private void handlePreviewChunk(ActionEvent event) throws IOException {
		String chunk = searchResultTextArea.getSelectedText();
		
		boolean isValidChunk = checkForValidChunk(chunk);
		if (isValidChunk) {
			// Run bash script
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
			String voice = voiceDropDownMenu.getValue();
			
			// Run bash script
    		String[] command = new String[]{"/bin/bash", "-c", "./script.sh save " + voice + " " + chunk};
			BashCommand bashCommand = new BashCommand(command);
			team.submit(bashCommand);
			
			bashCommand.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					updateChunkList();
				}
			});
		}
	}
	
	@FXML
    private void combineAudioChunks(String creationName) {
		ObservableList<String> selectedChunks = chunkList.getSelectionModel().getSelectedItems();
		
		String args = "";
		int numChunksSelected = selectedChunks.size();
		for (int i = 0; i < numChunksSelected - 1; i++) {
			args += selectedChunks.get(i) + " ";
		}
		args += selectedChunks.get(numChunksSelected-1);
		
		// Run bash script to create a combined audio of each selected chunk
		String[] command = new String[]{"/bin/bash", "-c", "./script.sh create " + creationName + " " + args};		
		BashCommand bashCommand = new BashCommand(command);
		team.submit(bashCommand);
		
		bashCommand.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				createVideo();
			}
		});
	}
	
	@FXML
    private void handleSliderDragged(ActionEvent event) throws IOException {

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
    	voiceLabel.setVisible(true);
    	voiceDropDownMenu.setVisible(true);
    	chunkList.setVisible(true);

        creationNameTextField.setVisible(true);
        finalCreate.setVisible(true);

        // show flickr creation options

		_NumberOfImagesTextField.setVisible(true);
		_numberImagesButton.setVisible(true);

        
        voiceDropDownMenu.getItems().addAll("Default", "NZ-Man", "NZ-Woman");
		voiceDropDownMenu.setValue("Default");
		updateChunkList();
		chunkList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    @FXML
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
			Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
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
					alert.showAndWait();
                }
                });
  }
    @FXML
    private void handleNumberOfImagesButton() {
        if (_NumberOfImagesTextField.getText().isEmpty()){
            return;
        }
        int num = Integer.parseInt(_NumberOfImagesTextField.getText());
        if (num <=0||num>10){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid number of images");
            //alert.setHeaderText(" delete " + _selectedCreation);
            alert.setContentText("Please enter a valid number between 1 nd 10");
            alert.showAndWait();
            return;

        }
        //possibly let the user continue on from this point
        // if successful let them see the create button
        // and set transparency of number of items to lower.

        numberOfImages =  Integer.parseInt(_NumberOfImagesTextField.getText());
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


    private void getSearchResult() {
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
        				List<String> searchResult = bashCommand.get();
        				
        				if (searchResult.get(0).equals("(Term not found)")) {
        					searchInProgress.setVisible(false);
        					termNotFound.setVisible(true);
        				} else {
        					displayChunkSelection();
        			        searchResultTextArea.setText(searchResult.get(0));
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
    
    /**
     * @return whether or not the chunk is valid
     * valid if 0 < number of words <= 30, or the chunk is too long but the user confirms anyway
     */
    private boolean checkForValidChunk(String chunk) {
    	int numberOfWords = countWords(chunk);
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
    
    private int countWords(String input) {
    	if (input == null || input.isEmpty()) {
    		return 0;
    	}

    	// Splits the input at any instance of one or more whitespace character, then counts the number of splits
    	String[] words = input.split("\\s+");
    	return words.length;
    }
    
    private void updateChunkList() {
    	// The chunks directory where all chunks are stored.
        final File folder = new File(System.getProperty("user.dir")+"/chunks/");
        
        List<String> listFileNames = new ArrayList<String>();
        for (final File fileName : folder.listFiles()) {
            if (fileName.getName().endsWith(".wav")) {
                listFileNames.add(fileName.getName());
            }
        }
        // Sort the files by chunk name in alphabetical order.
        Collections.sort(listFileNames);

        List<String> listChunkNames = new ArrayList<String>();
        // Will get every file in the creations directory and create an indexed
        // list of file names.
        for (final String chunk : listFileNames) {
            if (chunk.endsWith(".wav")) {
                listChunkNames.add(chunk.replace(".wav", ""));
            }
        }
        // Turning the list of chunk names into an ObservableList<String> for the GUI.
        ObservableList<String> observableListChunkNames = FXCollections.observableArrayList(listChunkNames);
        
        chunkList.setItems(observableListChunkNames);
    }
}
