package controllers;


import database.DatabaseOperations;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Date;
import java.util.ArrayList;


public class Controller {
    @FXML
    private ListView projectTitles;
    @FXML
    private ListView noteTitles;
    @FXML


    private DatabaseOperations databaseOperations = new DatabaseOperations();

    public Controller() {
    }

    @FXML
    public void initialize() {
        fetchProjectTitles();
        fetchNoteTitles();
    }

    /**
     * event handler for new project menu item
     */
    public void onCreateNewProject() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/new_project.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Create a new Project");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * event handler for new note menu item
     */
    public void onCreateNewNote() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/new_note.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Create a new Note");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch project titles
     */
    @FXML
    private void fetchProjectTitles() {
        ArrayList arrayList = databaseOperations.loadProjectTitles();
            try {
                ObservableList observableList = FXCollections.observableArrayList(arrayList);
                projectTitles.setItems(observableList);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * Fetch project titles
     */
    @FXML
    private void fetchNoteTitles() {
        ArrayList arrayList = databaseOperations.loadNoteTitles();
            try {
                ObservableList observableList = FXCollections.observableArrayList(arrayList);
                noteTitles.setItems(observableList);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    /**
     * Fetch project titles
     */

    public void addProjectTitleToList(String title) {
       projectTitles.getItems().add(title);
    }
}
