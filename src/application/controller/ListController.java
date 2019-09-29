package application.controller;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class ListController {

    @FXML
    private ListView listViewCreations;

    @FXML
    private static String _selectedCreation;

    @FXML
    private Button playButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Text selectPrompt;

    @FXML
    private Button newCreationButton;


    public void initialize(){
        ListCurrentFiles();
        playButton.setDisable(true);
        deleteButton.setDisable(true);
    }


    @FXML
    private void handlePlayButton(ActionEvent event) throws IOException {

        if (!(_selectedCreation == null )) {
            //change scene to playerScene
            Parent listViewParent = FXMLLoader.load(Main.class.getResource("resources/PlayerScene.fxml"));
            Scene listViewScene = new Scene(listViewParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(listViewScene);
            window.show();
        }
    }

    @FXML
    private void handleDeleteButton(){
        if (!(_selectedCreation == null )) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete");
            alert.setHeaderText(" delete " + _selectedCreation);
            alert.setContentText("Are you sure?.");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                getSelectedFile().delete();
                ListCurrentFiles();
            }
        }
    }

    @FXML
    private void handleRefreshButton() {
        ListCurrentFiles();
    }

    @FXML
    private void handleNewCreationButton(ActionEvent event) throws IOException {
        Parent creationViewParent = FXMLLoader.load(Main.class.getResource("resources/newCreationScene.fxml"));
        Scene creationViewScene = new Scene(creationViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(creationViewScene);
        window.show();
    }


        @FXML
    private void handleListReturnButton(ActionEvent event) throws IOException {

        Parent listViewParent = FXMLLoader.load(Main.class.getResource("resources/home.fxml"));
        Scene listViewScene = new Scene(listViewParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(listViewScene);
        window.show();
    }


    @FXML
    private void handleSelectedCreation(){
        _selectedCreation =  (String) listViewCreations.getSelectionModel().getSelectedItem();
        playButton.setDisable(false);
        deleteButton.setDisable(false);
        selectPrompt.setVisible(false);
    }




    // This will return a list of all current creations in the creations directory.
    // This list will be displayed to the user in the view interface.
    private void ListCurrentFiles(){
        // The creations directory where all creations are stored.
        final File folder = new File(System.getProperty("user.dir")+"/creations/");
        ArrayList<String> listFilesNames = new ArrayList<String>();
        ArrayList<String> listCreationNames = new ArrayList<String>();

        for (final File fileName : folder.listFiles()) {
            if (fileName.getName().endsWith(".mp4")) {
                listFilesNames.add(fileName.getName());
            }
        }
        // Sort the files by creation name in alphabetical order.
        Collections.sort(listFilesNames);

        // Will get every file in the creations directory and create an indexed
        // list of file names.
        int indexCounter = 1;
        for (final String creations : listFilesNames) {
            if (creations.endsWith(".mp4")) {
                listCreationNames.add("" + indexCounter + ". " + creations.replace(".mp4", ""));
                indexCounter++;

            }
        }
        // Turning the list of creation names into an listView<String> for the GUI.
        ObservableList<String> listViewFiles = FXCollections.observableArrayList(listCreationNames);
        //ListView CreationsListView = new ListView();
        listViewCreations.setItems(listViewFiles);
    }


    public static File getSelectedFile(){


        // Removal of the index on the creation name
        // and creating it as a file to be played or deleted.
        String fileName = ( "" + _selectedCreation.substring(_selectedCreation.indexOf(".")+2) );
        File _selectedfile = new File(System.getProperty("user.dir")+"/creations/"+ fileName +".mp4");

        return _selectedfile;

    }





}
