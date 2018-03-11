package controllers;

import database.DatabaseOperations;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Date;

public class NewProjectController {
    @FXML
    private Button saveProject;
    @FXML
    private Button saveNote;
    @FXML
    private TextField projectTitle;
    @FXML
    private TextField noteTitle;
    @FXML
    private DatePicker due;

    private DatabaseOperations databaseOperations = new DatabaseOperations();


    /**
     * Event handler for cancel new Project dialog
     */

    public void onCancelNewProject(ActionEvent e) {
        final Node source = (Node) e.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Event handler for save new Project dialog
     */

    public void onSaveNewProject(ActionEvent e) {
        if (due.getValue() != null) {
            databaseOperations.createNewProject(projectTitle.getText().trim(), Date.valueOf(due.getValue()));
        } else {
            databaseOperations.createNewProject(projectTitle.getText().trim());
        }
        final Node source = (Node) e.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Enable save button
     */

    public void enableSaveProjectButton() {
        if (!projectTitle.getText().trim().equals("")) {
            saveProject.setDisable(false);
        } else {
            saveProject.setDisable(true);
        }
    }

    /**
     * Event handler for cancel new Note dialog
     */

    public void onCancelNewNote(ActionEvent e) {
        final Node source = (Node) e.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Event handler for save new Project dialog
     */

    public void onSaveNewNote(ActionEvent e) {
        if (!noteTitle.getText().trim().equals("")) {
            databaseOperations.createNote(noteTitle.getText().trim());
            final Node source = (Node) e.getSource();
            final Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Enable save button
     */

    public void enableSaveNoteButton() {
        if (!noteTitle.getText().trim().equals("")) {
            saveNote.setDisable(false);
        } else {
            saveNote.setDisable(true);
        }
    }
}
