package controllers;


import database.DatabaseOperations;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Controller {
    @FXML
    private ListView projectTitles;
    @FXML
    private ListView noteTitles;
    @FXML
    private TabPane tabPane;

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
        Dialog<Pair<String, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("New Project");
        dialog.setHeaderText("Create a new project");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField title = new TextField();
        title.setPromptText("Title");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Due");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(title, 1, 0);
        grid.add(new Label("Due (optional):"), 0, 1);
        grid.add(datePicker, 1, 1);

// Enable/Disable login button depending on whether a title was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        title.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the title field by default.
        Platform.runLater(() -> title.requestFocus());

// Convert the result to a title-datePicker-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<String, LocalDate>(title.getText(), datePicker.getValue());
            }
            return null;
        });

        Optional<Pair<String, LocalDate>> result = dialog.showAndWait();

        result.ifPresent(titleDue -> {
            saveNewProject(titleDue.getKey(), titleDue.getValue());
        });
    }

    /**
     * event handler for new note menu item
     */
    public void onCreateNewNote() {
        TextInputDialog dialog = new TextInputDialog("Title");
        dialog.setTitle("Title");
        dialog.setHeaderText("New Note");
        dialog.setContentText("Add a new note");

// Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();

// The Java 8 way to get the response value (with lambda expression).
        result.ifPresent(name -> {
            databaseOperations.createNote(name);
            tabPane.getSelectionModel().selectLast();
            fetchNoteTitles();
        });
    }

    /**
     * Fetch project titles
     */

    private void fetchProjectTitles() {
        ArrayList arrayList = databaseOperations.loadProjectTitles();
        try {
            ObservableList observableList = FXCollections.observableArrayList(reverse( arrayList));
            projectTitles.setItems(observableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch project titles
     */

    private void fetchNoteTitles() {
        ArrayList arrayList = databaseOperations.loadNoteTitles();
        try {
            ObservableList observableList = FXCollections.observableArrayList(reverse( arrayList));
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

    private void saveNewProject(String title, LocalDate due) {
        if (due != null) {
            databaseOperations.createNewProject(title.trim(), Date.valueOf(due));
        } else {
            databaseOperations.createNewProject(title.trim());
        }
        fetchProjectTitles();
        tabPane.getSelectionModel().selectFirst();

//        fetchNoteTitles();
    }
    static <T> List<T> reverse(final List<T> list){
        final int last=list.size()-1;
        return IntStream.rangeClosed(0,last).map(i->(last-i)).mapToObj(list::get).collect(Collectors.toList());
    }
}
