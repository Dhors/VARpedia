package application.controller;

import application.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuizController {
	private final int MEDIA_VIEW_WIDTH = 600;
	private final int MEDIA_VIEW_HEIGHT = 400;
	
    private String _quizTerm;
    private File _quizVideo;

    @FXML
	private CheckBox backgroundMusicCheckBox;
    @FXML
    private Pane _quizPlayer;
    @FXML
    private TextField _playerAnswerTextField;
    @FXML
    private Button _startButton;
    @FXML
    private Button _skipButton;
    @FXML
    private Button _checkButton;
    @FXML
    private Button _pauseButton;
    @FXML
    private Button _manageQuizButton;
    @FXML
    private ListView<String> _listOfQuiz;

    private static String _selectedQuiz;
    @FXML
    private Button _deleteButton;
    @FXML
    private Button _returnButton;

    MediaPlayer _mediaPlayer;
    MediaView _mediaView;

    public void initialize(){
        backgroundMusicCheckBox.setSelected(Main.backgroundMusicPlayer().checkBoxesAreSelected());
        
        _startButton.setVisible(true);
        _manageQuizButton.setVisible(true);


        _listOfQuiz.setVisible(false);
        _returnButton.setVisible(false);
        _pauseButton.setVisible(false);
        _checkButton.setVisible(false);
        _skipButton.setVisible(false);
        _deleteButton.setVisible(false);
        _playerAnswerTextField.setVisible(false);

        // Don't let the user check their answer until they enter an answer
        BooleanBinding textIsEmpty = Bindings.createBooleanBinding(() ->
        	_playerAnswerTextField.getText().trim().isEmpty(),
        	_playerAnswerTextField.textProperty());
        _checkButton.disableProperty().bind(textIsEmpty);
    }


    @FXML
    private void handleStartButton() throws IOException {

        _startButton.setVisible(false);
        _manageQuizButton.setVisible(false);
        _pauseButton.setVisible(true);
        _checkButton.setVisible(true);
        _skipButton.setVisible(true);
        _playerAnswerTextField.setVisible(true);

        _quizPlayer.getChildren().removeAll();
        _quizPlayer.getChildren().clear();

        getRandomQuiz();
        
        Media video = new Media(_quizVideo.toURI().toString());
        _mediaPlayer = new MediaPlayer(video);
        _mediaPlayer.setAutoPlay(true);
        
        _mediaView = new MediaView(_mediaPlayer);
        _mediaView.setFitHeight(MEDIA_VIEW_HEIGHT);
        _mediaView.setFitWidth(MEDIA_VIEW_WIDTH);

        _quizPlayer.getChildren().add(_mediaView);

        //Once the video is finished the video will replay from the start
        _mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                _mediaPlayer.seek(Duration.ZERO);
                _mediaPlayer.play();
            }
        });
    }

    @FXML
    private void handleCheckButton() throws IOException {
        _mediaPlayer.pause();
        
        boolean answerIsCorrect = _playerAnswerTextField.getText().equalsIgnoreCase(_quizTerm);
        if (answerIsCorrect){
            Alert correctAnswerPopup = new Alert(Alert.AlertType.INFORMATION);
            correctAnswerPopup.setTitle("Correct!");
            correctAnswerPopup.setHeaderText(null);
            correctAnswerPopup.setContentText("Now try the next one");
            correctAnswerPopup.showAndWait();
            _startButton.fire();
        } else {
            Alert incorrectAnswerPopup = new Alert(Alert.AlertType.INFORMATION);
            incorrectAnswerPopup.setTitle("Incorrect");
            incorrectAnswerPopup.setHeaderText(null);
            incorrectAnswerPopup.setContentText("Please try again");
            incorrectAnswerPopup.showAndWait();
            _mediaPlayer.play();
        }
    }

    @FXML
    private void handlePauseButton() throws IOException {
        if (_mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            _mediaPlayer.pause();
        } else {
            _mediaPlayer.play();
        }
    }

    // Return to main menu
    @FXML
    private void handleBackButton() throws IOException {
        if (_mediaPlayer != null) {
        	_mediaPlayer.stop();
        }
        Main.changeScene("resources/MainScreenScene.fxml");
    }

    @FXML
    private void handleSkipButton() throws IOException {
        _startButton.fire();
    }

    private void getRandomQuiz() {
        File quizFolder = new File(System.getProperty("user.dir") + "/quiz/");
        File[] quizVideosArray = quizFolder.listFiles();

        List<File> quizVideosList = new ArrayList<File>(Arrays.asList(quizVideosArray));

        Collections.shuffle(quizVideosList);

        Random rand = new Random();
        _quizVideo = quizVideosList.get(rand.nextInt(quizVideosList.size()));

        _quizTerm = _quizVideo.getName().replace(".mp4", "");
    }

    @FXML
    public void handleManageQuizButton(){
        ListCurrentQuiz();
        _listOfQuiz.setVisible(true);
        _manageQuizButton.setVisible(false);
        _deleteButton.setVisible(true);
        _startButton.setVisible(false);
        _returnButton.setVisible(true);
    }

    @FXML
    public void handleSelectedQuiz() {
        _selectedQuiz = _listOfQuiz.getSelectionModel().getSelectedItem();
    }

    private void ListCurrentQuiz(){
        // The quiz directory where all creations are stored.
        final File quizFolder = new File(System.getProperty("user.dir")+"/quiz/");
        ArrayList<String> creationNamesList = new ArrayList<String>();

        // Will get every file in the creations directory and create an indexed
        // list of file names.
        int indexCounter = 1;
        for (final File quiz : quizFolder.listFiles()) {
            String fileName = quiz.getName();
            if (fileName.endsWith(".mp4")) {
                creationNamesList.add("" + indexCounter + ". " + fileName.replace(".mp4", ""));
                indexCounter++;
            }
        }

        // Turning the list of creation names into an listView<String> for the GUI.
        ObservableList<String> observableCreationNamesList = FXCollections.observableArrayList(creationNamesList);
        _listOfQuiz.setItems(observableCreationNamesList);
    }

    public static File getSelectedFile(){
        // Removal of the index on the creation name
        // and creating it as a file to be played or deleted.
        String fileName = getSelectedQuizName();
        File selectedQuiz = new File(System.getProperty("user.dir")+"/quiz/"+ fileName +".mp4");

        return selectedQuiz;
    }

    public static String getSelectedQuizName(){
        // Removal of the index on the creation name
        String fileName = ( "" + _selectedQuiz.substring(_selectedQuiz.indexOf(".")+2) );
        return fileName;
    }

    @FXML
    private void handleDeleteButton(){
        Alert deleteConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
        deleteConfirmation.setTitle("Confirm Deletion");
        deleteConfirmation.setHeaderText("Delete " + getSelectedQuizName() + "?");
        deleteConfirmation.setContentText("Are you sure you want to delete this quiz?");
        Optional<ButtonType> buttonClicked = deleteConfirmation.showAndWait();

        if (buttonClicked.get() == ButtonType.OK) {
            getSelectedFile().delete();
            ListCurrentQuiz();
        }
    }

    //this one exists out of managing
    @FXML
    private void handleReturnButton(){
        initialize();

    }

    @FXML
    private void handleBackgroundMusic() throws IOException {
        Main.backgroundMusicPlayer().handleBackgroundMusic(backgroundMusicCheckBox.isSelected());
    }
}
