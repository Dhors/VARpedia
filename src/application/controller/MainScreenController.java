package application.controller;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainScreenController {

    @FXML
    private void handleListButton() throws IOException {
        Main.setScene("resources/listCreationsScene.fxml");

    }
    @FXML
    private void handleQuizButton() throws IOException {
        Main.setScene("resources/QuizScene.fxml");

    }

}
