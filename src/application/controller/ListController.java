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
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ListController {

    @FXML
    private ListView listViewCreations;

    @FXML
    private String _selectedCreation;

    public void initialize(){

        //ListView listViewCreations =
        ListCurrentFiles();

        System.out.println( "fuck initialze"+  listViewCreations );
    }


    @FXML
    private void handlePlayButton(ActionEvent event) throws IOException {

        if (_selectedCreation == null ) {
        //do nothing
        }  else {


            //change scene to playerScene
            Parent listViewParent = FXMLLoader.load(Main.class.getResource("resources/PlayerScene.fxml"));
            Scene listViewScene = new Scene(listViewParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(listViewScene);
            window.show();
        }
    }

    /*@FXML
    private void handleDeleteButton(){


    }*/


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
        System.out.println( ""+  _selectedCreation );
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
                System.out.println( "fuck fileloop" +fileName.getName()  );
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
                System.out.println( "fuck fileloop" +indexCounter );
                indexCounter++;

            }
        }
        // Turning the list of creation names into an listView<String> for the GUI.
        ObservableList<String> listViewFiles = FXCollections.observableArrayList(listCreationNames);
        //ListView CreationsListView = new ListView();
        listViewCreations.setItems(listViewFiles);
       // CreationsListView.setPrefHeight(300);

        //return CreationsListView;
    }




}
