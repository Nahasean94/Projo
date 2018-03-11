package controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class Controller {

    @FXML
    public Button saveProject;
    @FXML
    public Button saveNote;
    @FXML
    private TextField projectTitle;
    @FXML
    private TextField noteTitle;
    @FXML
    private DatePicker due;

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
        System.out.println(projectTitle.getText() + due.getValue());
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
        }else{
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
        System.out.println(noteTitle.getText());
        final Node source = (Node) e.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Enable save button
     */

    public void enableSaveNoteButton() {
        if (!noteTitle.getText().trim().equals("")) {
            saveNote.setDisable(false);
        }else{
            saveNote.setDisable(true);
        }
    }
}
