package controllers;

import database.DatabaseOperations;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Controller {
    @FXML
    private ListView projectTitles, noteTitles;
    @FXML
    private Button addDescription, editDescription, saveNewTask, saveNoteBody;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab projectsTab, notesTab;
    @FXML
    private Label viewProjectTitle, percentage;
    @FXML
    private Accordion projectAccordion;
    @FXML
    private TitledPane tasksPane, notesPane, descriptionPane;
    @FXML
    private TextFlow projectDescription;
    @FXML
    private TextField newTaskTextField, searchTitles;
    @FXML
    private TableColumn<Task, String> taskName, taskCount, taskDate, taskComplete, taskPriority;
    @FXML
    private TableView tasksTable;
    @FXML
    private ChoiceBox priorityBox;
    @FXML
    private HTMLEditor noteBody;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox metaBox;

    private String projectDescriptionText = "";
    private ArrayList projectsArrayList;
    private ArrayList notesArrayList;

    private String itemName = "";
    private int itemId = 0;


    private DatabaseOperations databaseOperations = new DatabaseOperations();

    public Controller() {
    }

    @FXML
    public void initialize() {
        fetchNoteTitles();
        fetchProjectTitles();
    }

    /**
     * event handler for new project menu item
     */
    public void onCreateNewProject() {
        Dialog<Pair<String, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("New Project");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        TextField title = new TextField();
        title.setPromptText("Title");
        title.setPrefWidth(340);
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Due");
        datePicker.setPrefWidth(340);
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
        Platform.runLater(title::requestFocus);

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
        Dialog dialog = new Dialog();
        dialog.setTitle("New Note");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        TextField title = new TextField();
        title.setPromptText("Title");
        title.setPrefWidth(340);
        grid.add(new Label("Title:"), 0, 0);
        grid.add(title, 1, 0);


// Enable/Disable login button depending on whether a title was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        title.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);

// Request focus on the title field by default.
        Platform.runLater(title::requestFocus);

// Convert the result to a title-datePicker-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return title.getText();
            }
            return null;
        });

        Optional result = dialog.showAndWait();
        result.ifPresent(titleDue -> {
            databaseOperations.createNote(titleDue.toString());
            tabPane.getSelectionModel().selectLast();
            fetchNoteTitles();
        });
    }

    /**
     * Fetch project titles
     */

    private void fetchProjectTitles() {
        projectsArrayList = databaseOperations.loadProjectTitles();
        if (projectsArrayList.isEmpty()) {
            tasksPane.setDisable(true);
            descriptionPane.setDisable(true);
        }
        if (!projectsArrayList.isEmpty()) {
            try {
                ObservableList observableList = FXCollections.observableArrayList(reverse(projectsArrayList));
                projectTitles.setEditable(true);
                projectsTab.setText("Projects (" + projectsArrayList.size() + ")");
                projectTitles.setCellFactory(TextFieldListCell.forListView());
                projectTitles.setOnEditCommit((EventHandler<ListView.EditEvent<String>>) t -> {
                    if (!t.getNewValue().isEmpty()) {

                        String oldValue = projectTitles.getSelectionModel().getSelectedItem().toString();
                        projectTitles.getItems().set(t.getIndex(), t.getNewValue());
                        databaseOperations.editProjectName(oldValue, t.getNewValue().toString());
                    }
                });
                projectTitles.setItems(observableList);
                projectTitles.getSelectionModel().selectFirst();
                onProjectClicked();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        projectTitles.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                projectTitles.setEditable(false);
                onProjectClicked();
            }
        });
        projectTitles.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem viewDetailsItem = new MenuItem();
            viewDetailsItem.textProperty().bind(Bindings.format("View Details", cell.itemProperty()));
            viewDetailsItem.setOnAction(event -> {
                String item = cell.getItem();
                // code to edit item...
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This project will trashed");
                alert.setHeaderText("Are you sure you want to move '" + cell.getItem() + "' to trash?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    databaseOperations.trashProject(cell.getItem());
                    projectTitles.getSelectionModel().selectFirst();
                    projectTitles.getItems().remove(cell.getItem());
                    fetchProjectTitles();
                }

            });
            contextMenu.getItems().addAll(viewDetailsItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }

    /**
     * Fetch project titles
     */

    private void fetchNoteTitles() {
        noteTitles.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                noteTitles.setEditable(false);
                onNoteClicked();
            }
        });
        notesArrayList = databaseOperations.loadNoteTitles();
        if (notesArrayList.isEmpty()) {
            notesPane.setDisable(true);
        }
        try {
            ObservableList observableList = FXCollections.observableArrayList(reverse(notesArrayList));
            noteTitles.setCellFactory(TextFieldListCell.forListView());
            notesTab.setText("Notes (" + notesArrayList.size() + ")");
            noteTitles.setOnEditCommit((EventHandler<ListView.EditEvent<String>>) t -> {
                if (!t.getNewValue().isEmpty()) {

                    String oldValue = noteTitles.getSelectionModel().getSelectedItem().toString();
                    noteTitles.getItems().set(t.getIndex(), t.getNewValue());
                    databaseOperations.editNoteTitle(oldValue, t.getNewValue().toString());
                }
            });
            noteTitles.setItems(observableList);
            noteTitles.getSelectionModel().selectFirst();
            onNoteClicked();
        } catch (Exception e) {
            e.printStackTrace();
        }
        noteTitles.setCellFactory(lv -> {

            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem viewDetailsItem = new MenuItem();
            viewDetailsItem.textProperty().bind(Bindings.format("View Details", cell.itemProperty()));
            viewDetailsItem.setOnAction(event -> {
                String item = cell.getItem();
                // code to edit item...
            });

            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This note will trashed");
                alert.setHeaderText("Are you sure you want to move '" + cell.getItem() + "' to trash?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    databaseOperations.trashNote(cell.getItem());
                    noteTitles.getSelectionModel().selectFirst();
                    noteTitles.getItems().remove(cell.getItem());
                    fetchNoteTitles();
                }

            });
            contextMenu.getItems().addAll(viewDetailsItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

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

    //Reverse the elements of an arraylist to bring the most recent title on top
    private static <T> List<T> reverse(final List<T> list) {
        final int last = list.size() - 1;
        return IntStream.rangeClosed(0, last).map(i -> (last - i)).mapToObj(list::get).collect(Collectors.toList());
    }

    public void onProjectsTab() {
        if (descriptionPane != null) {
            descriptionPane.setDisable(false);
            tasksPane.setDisable(false);
            notesPane.setDisable(true);
            metaBox.setVisible(true);
            projectAccordion.setExpandedPane(tasksPane);
            projectTitles.getSelectionModel().selectFirst();
//            if (!projectTitles.getSelectionModel().getSelectedItem().toString().equals("0 Results found")){
            onProjectClicked();
//            }
//            else viewProjectTitle.setText("");

        }
    }

    public void onNotesTab() {
        if (descriptionPane != null) {
            descriptionPane.setDisable(true);
            tasksPane.setDisable(true);
            notesPane.setDisable(false);
            metaBox.setVisible(false);
            noteTitles.getSelectionModel().selectFirst();

            onNoteClicked();
//            }
//            else viewProjectTitle.setText("");
        }
    }

    //populate the view pane with details of the selected project.
    public void onProjectClicked() {
        viewProjectTitle.setText(projectTitles.getSelectionModel().getSelectedItem().toString());
        projectAccordion.setExpandedPane(tasksPane);
        itemName = projectTitles.getSelectionModel().getSelectedItem().toString();
        itemId = databaseOperations.getItemId(itemName);
        if (itemId != 0) {
            priorityBox.getItems().setAll("Low", "Medium", "High");
            priorityBox.setValue("Low");
            fetchProjectTasks();
            fetchProjectDescription();
            newTaskTextField.setOnKeyPressed(keyEvent ->

            {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    addProjectTask();
                }
            });
        }

    }

    //populate the view pane with details of the selected project.
    public void onNoteClicked() {
        viewProjectTitle.setText(noteTitles.getSelectionModel().getSelectedItem().toString());
        itemName = noteTitles.getSelectionModel().getSelectedItem().toString();
        String body = databaseOperations.getNoteBody(itemName);
        String created = databaseOperations.getNoteDate(itemName);
        if (body != null) {
            noteBody.setHtmlText(body);
        } else {
            noteBody.setHtmlText("");
        }
        tasksPane.setText("Tasks");
        descriptionPane.setText("Description");
        notesPane.setText("Take notes      Created: " + created);
        projectAccordion.setExpandedPane(notesPane);
        saveNoteBody.setDisable(true);

    }

    private void fetchProjectDescription() {
        String description = databaseOperations.getProjectDescription(itemName);
        ArrayList arrayList = databaseOperations.getProjectDates(itemName);
        String created = arrayList.get(0).toString();
        String due = arrayList.get(1).toString();

        if (description.isEmpty()) {
            description = "Add a description for this project";
            addDescription.setDisable(false);
            editDescription.setDisable(true);
        } else {
            addDescription.setDisable(true);
            editDescription.setDisable(false);
            projectDescriptionText = description;
        }
        if (due.equals(created.split(" ")[0])) {
            descriptionPane.setText("Description         Created: " + created);
        } else {
            descriptionPane.setText("Description         Created: " + created + "  Due: " + due);
        }
        projectDescription.getChildren().setAll(new Text(description));
//        projectDescription.getChildren().add();

    }

    //add a project description
    public void addProjectDescription() {
        Dialog dialog = new Dialog();
        dialog.setTitle("Add project description");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextArea description = new TextArea();

        description.setWrapText(true);

        grid.setPadding(new Insets(10));
        grid.add(description, 0, 0);


// Enable/Disable login button depending on whether a title was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        description.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);


// Request focus on the title field by default.
        Platform.runLater(description::requestFocus);

// Convert the result to a title-datePicker-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return description.getText();
            }
            return null;
        });

        Optional result = dialog.showAndWait();
        result.ifPresent(desc -> {
            databaseOperations.updateProjectDescription(itemName, desc.toString());
            fetchProjectDescription();
        });
    }

    //edit a project description
    public void editProjectDescription() {
        Dialog dialog = new Dialog();
        dialog.setTitle("Edit project description");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextArea description = new TextArea();
        description.setText(projectDescriptionText);
        description.setWrapText(true);

        grid.setPadding(new Insets(10));
        grid.add(description, 0, 0);


// Enable/Disable login button depending on whether a title was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        description.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);


// Request focus on the title field by default.
        Platform.runLater(description::requestFocus);

// Convert the result to a title-datePicker-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return description.getText();
            }
            return null;
        });

        Optional result = dialog.showAndWait();
        result.ifPresent(desc -> {
            databaseOperations.updateProjectDescription(itemName, desc.toString());
            fetchProjectDescription();
        });
    }

    public void typingTask() {
        if (!newTaskTextField.getText().trim().isEmpty()) {
            saveNewTask.setDisable(false);
        } else {
            saveNewTask.setDisable(true);
        }
    }

    //add a new task
    public void addProjectTask() {
        if (!newTaskTextField.getText().trim().isEmpty()) {
            databaseOperations.createTask(newTaskTextField.getText().trim(), itemId, priorityBox.getValue().toString());
            newTaskTextField.setText("");
            saveNewTask.setDisable(true);
            priorityBox.setValue("Low");
            fetchProjectTasks();

        }
    }

    //fetch Project Tasks
    private void fetchProjectTasks() {
        ArrayList<ArrayList> arrayLists = databaseOperations.fetchTasks(itemId);
        int completeTasks = 0;
        int incompleteTasks = 0;
        try {
            ObservableList<Task> data = FXCollections.observableArrayList();
//iterate through each element of the arraylist
            for (int i = arrayLists.size() - 1; i >= 0; i--) {
                int id = (Integer) arrayLists.get(i).get(0);
                SimpleStringProperty name = new SimpleStringProperty(arrayLists.get(i).get(1).toString());
                SimpleStringProperty date = new SimpleStringProperty(formatTime(arrayLists.get(i).get(3).toString()));
                CheckBox complete = new CheckBox();
                //mark checkbox if task is complete
                if (Integer.parseInt(arrayLists.get(i).get(2).toString()) == 1) {
                    complete.setSelected(true);
                    completeTasks++;
                } else {
                    incompleteTasks++;
                }
                //add event listener to mark a task as complete or not
                complete.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!!newValue) {
                        databaseOperations.markTaskAsComplete(id);
                        fetchProjectTasks();
                    } else {
                        databaseOperations.markTaskAsIncomplete(id);
                        fetchProjectTasks();
                    }
                });
                ChoiceBox priority = new ChoiceBox();
                priority.getItems().setAll("Low", "Medium", "High");
                priorityBox.setTooltip(new Tooltip("Priority"));
//get the priority of the task and set the choicebox
                if (arrayLists.get(i).get(4) != null) {
                    priority.setValue(arrayLists.get(i).get(4));
                } else {
                    priority.setValue("Low");
                }
                //add event listener to the priority choicebox
                priority.setOnAction(event -> databaseOperations.changePriority(id, priority.getSelectionModel().getSelectedItem().toString()));
                //save the new value when a task name is edited
                taskName.setOnEditCommit(
                        t -> {
                            if (!t.getNewValue().isEmpty()) {
                                t.getTableView().getItems().get(
                                        t.getTablePosition().getRow()).setTaskName(t.getNewValue());
                                databaseOperations.editTaskName(id, t.getNewValue());
                            }
                        }
                );
//initialize variable to help with the counter in table column
                int temp = arrayLists.size() - 1 - i;
                SimpleIntegerProperty c = new SimpleIntegerProperty(++temp);
                data.add(new Task(name, c, complete, date, priority));
//obtain the value of each column
                taskCount.setCellValueFactory(new PropertyValueFactory<>("taskId"));
                taskName.setCellValueFactory(new PropertyValueFactory<>("taskName"));
                taskName.setCellFactory(TextFieldTableCell.forTableColumn());
                taskDate.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
                taskComplete.setCellValueFactory(new PropertyValueFactory<>("complete"));
                taskPriority.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
            }
            tasksTable.getItems().setAll(data);
            tasksPane.setText("Tasks (" + arrayLists.size() + ")              Complete (" + completeTasks + "), Incomplete (" + incompleteTasks + ")");
            calculateCompletion(completeTasks, incompleteTasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        taskName.setCellFactory(lv -> {
            TableCell<Task, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This task will trashed");
                alert.setHeaderText("Are you sure you want to move '" + cell.getItem() + "' to trash?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    databaseOperations.trashTask(cell.getItem());
                    fetchProjectTasks();
                }

            });
            contextMenu.getItems().addAll(deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

    }

    //calculate percentage completion of a project
    private void calculateCompletion(int completeTasks, int incompleteTasks) {
        int total = completeTasks + incompleteTasks;
        long percent = Math.round(((double) completeTasks / total) * 100);
        progressBar.setProgress((double) completeTasks / total);
//        progressBar.setp;
        percentage.setText(percent + " % complete");

    }

    //format timestamp to human readable format
    private String formatTime(String date) {
        String dateTime[] = date.split(" ");
        String day[] = dateTime[0].split("-");
        String time[] = dateTime[1].split(":");
        return day[0] + "-" + day[1] + "-" + day[2] + " " + time[0] + ":" + time[1];
    }

    //save note
    public void saveNote() {
        databaseOperations.editNoteBody(itemName, noteBody.getHtmlText().trim());
        saveNoteBody.setDisable(true);
    }

    //disable and enable save button when typing
    public void onTypingNoteBody() {
        if (!noteBody.getHtmlText().trim().isEmpty()) {
            saveNoteBody.setDisable(false);
        } else {
            saveNoteBody.setDisable(true);
        }
    }

    public void searchTitles() {
        String me = "SDf";
        if (!searchTitles.getText().trim().isEmpty()) {
            ArrayList projectsResults = new ArrayList();
            ArrayList notesResults = new ArrayList();
            for (Object aProjectsArrayList : projectsArrayList) {
                if (aProjectsArrayList.toString().toLowerCase().contains(searchTitles.getText().trim().toLowerCase())) {
                    projectsResults.add(aProjectsArrayList);
                }
            }
            for (Object aProjectsArrayList : notesArrayList) {
                if (aProjectsArrayList.toString().toLowerCase().contains(searchTitles.getText().trim().toLowerCase())) {
                    notesResults.add(aProjectsArrayList);
                }
            }

            projectsTab.setText("Projects (" + projectsResults.size() + ")");
            notesTab.setText("Notes (" + notesResults.size() + ")");
            if (projectsResults.isEmpty()) {
                projectsResults.add("0 Results found");
                projectTitles.setEditable(false);
            }
            if (notesResults.isEmpty()) {
                notesResults.add("0 Results found");
                noteTitles.setEditable(false);
            }
            ObservableList projectsObservableList = FXCollections.observableArrayList(reverse(projectsResults));
            projectTitles.getItems().setAll(projectsObservableList);
//            if (!projectTitles.getSelectionModel().getSelectedItems().toString().equals("0 Results found"))
            projectTitles.getSelectionModel().selectFirst();
            ObservableList notesObservableList = FXCollections.observableArrayList(reverse(notesResults));
            noteTitles.getItems().setAll(notesObservableList);
//            if (!noteTitles.getSelectionModel().getSelectedItems().toString().equals("0 Results found"))
            noteTitles.getSelectionModel().selectFirst();


        } else {
            ObservableList projectsObservableList = FXCollections.observableArrayList(reverse(projectsArrayList));
            projectTitles.getItems().setAll(projectsObservableList);
            projectTitles.getSelectionModel().selectFirst();
            ObservableList notesObservableList = FXCollections.observableArrayList(reverse(notesArrayList));
            noteTitles.getItems().setAll(notesObservableList);
            noteTitles.getSelectionModel().selectFirst();
            projectsTab.setText("Projects (" + projectsArrayList.size() + ")");
            notesTab.setText("Notes (" + notesArrayList.size() + ")");
        }
    }

    public void viewSourceCode() {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/Nahasean94/Projo").toURI());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void viewTrashedProjects() {
        Stage stage = new Stage();
        projectsTrashTable(stage);
        stage.setTitle("Projects Trash");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void projectsTrashTable(Stage stage) {
        ArrayList<ArrayList> arrayLists = databaseOperations.fetchTrashedProjects();
        TableView projectsTrash = new TableView();
        TableColumn<TrashedProjects, String> trashProjectCount = new TableColumn<>("#");
        trashProjectCount.setPrefWidth(50);
        TableColumn<TrashedProjects, String> trashProjectName = new TableColumn<>("Name");
        trashProjectName.setPrefWidth(600);
        TableColumn<TrashedProjects, String> trashProjectDeleteDate = new TableColumn<>("Date Deleted");
        trashProjectDeleteDate.setPrefWidth(100);
        ObservableList<TrashedProjects> data = FXCollections.observableArrayList();
        try {
//iterate through each element of the arraylist
            for (ArrayList arrayList : arrayLists) {
                SimpleIntegerProperty id = new SimpleIntegerProperty((Integer) arrayList.get(0));
                SimpleStringProperty name = new SimpleStringProperty(arrayList.get(1).toString());
                SimpleStringProperty date = new SimpleStringProperty(formatTime(arrayList.get(2).toString()));
                data.add(new TrashedProjects(name, id, date));
//obtain the value of each column
                trashProjectCount.setCellValueFactory(new PropertyValueFactory<>("projectId"));
                trashProjectName.setCellValueFactory(new PropertyValueFactory<>("projectName"));
                trashProjectDeleteDate.setCellValueFactory(new PropertyValueFactory<>("dateDeleted"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        trashProjectName.setCellFactory(lv -> {
            TableCell<TrashedProjects, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem restoreItem = new MenuItem();
            restoreItem.textProperty().bind(Bindings.format("Restore", cell.itemProperty()));
            restoreItem.setOnAction(event -> {
                databaseOperations.restoreProject(cell.getItem());
                projectsTrashTable(stage);
                tabPane.getSelectionModel().selectFirst();
                fetchProjectTitles();
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Permanently", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                alert.setHeaderText("Are you sure you want to permanently delete '" + cell.getItem() + "' ?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    databaseOperations.eraseProject(cell.getItem());
                    projectsTrashTable(stage);
                }


            });
            contextMenu.getItems().addAll(restoreItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        projectsTrash.setItems(data);
        projectsTrash.getColumns().addAll(trashProjectCount, trashProjectName, trashProjectDeleteDate);
        projectsTrash.setPrefSize(750, 500);
        VBox root = new VBox();
        root.setPadding(new Insets(10, 10, 10, 10));
        ButtonBar buttonBar = new ButtonBar();
        Button emptyTrash = new Button("Empty Trash");
        buttonBar.setPadding(new Insets(10, 10, 10, 10));
        emptyTrash.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setContentText("This operation cannot be undone");
            alert.setHeaderText("Are you sure you want to empty trash' ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                databaseOperations.emptyProjectTrash();
                projectsTrashTable(stage);
            }
        });
        if (arrayLists.isEmpty()) {
            emptyTrash.setDisable(true);
        }
        buttonBar.getButtons().add(emptyTrash);
        root.getChildren().addAll(projectsTrash, buttonBar);
        stage.setScene(new Scene(root, 772, 521));


//    return projectsTrash;
    }

    public void viewTrashedNotes() {

        Stage stage = new Stage();
        notesTrashTable(stage);
        stage.setTitle("Notes Trash");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();


    }

    private void notesTrashTable(Stage stage) {
        ArrayList<ArrayList> arrayLists = databaseOperations.fetchTrashedNotes();
        TableView notesTrash = new TableView();
        TableColumn<TrashedNotes, String> trashNoteCount = new TableColumn<>("#");
        trashNoteCount.setPrefWidth(50);
        TableColumn<TrashedNotes, String> trashNoteName = new TableColumn<>("Name");
        trashNoteName.setPrefWidth(600);
        TableColumn<TrashedNotes, String> trashNoteDeleteDate = new TableColumn<>("Date Deleted");
        trashNoteDeleteDate.setPrefWidth(100);
        ObservableList<TrashedNotes> data = FXCollections.observableArrayList();
        try {
//iterate through each element of the arraylist
            for (ArrayList arrayList : arrayLists) {
                SimpleIntegerProperty id = new SimpleIntegerProperty((Integer) arrayList.get(0));
                SimpleStringProperty name = new SimpleStringProperty(arrayList.get(1).toString());
                SimpleStringProperty date = new SimpleStringProperty(formatTime(arrayList.get(2).toString()));
                data.add(new TrashedNotes(name, id, date));
//obtain the value of each column
                trashNoteCount.setCellValueFactory(new PropertyValueFactory<>("noteId"));
                trashNoteName.setCellValueFactory(new PropertyValueFactory<>("noteName"));
                trashNoteDeleteDate.setCellValueFactory(new PropertyValueFactory<>("dateDeleted"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        trashNoteName.setCellFactory(lv -> {
            TableCell<TrashedNotes, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem restoreItem = new MenuItem();
            restoreItem.textProperty().bind(Bindings.format("Restore", cell.itemProperty()));
            restoreItem.setOnAction(event -> {
                databaseOperations.restoreNote(cell.getItem());
                tabPane.getSelectionModel().selectLast();
                notesTrashTable(stage);
                fetchNoteTitles();
                if (notesTrash.getSelectionModel().getSelectedItem() != null) {
                    TrashedNotes selectedTask = (TrashedNotes) notesTrash.getSelectionModel().getSelectedItem();
                    noteTitles.getSelectionModel().select(selectedTask.getNoteName());
                    onNoteClicked();
                }
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Permanently", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                alert.setHeaderText("Are you sure you want to permanently delete '" + cell.getItem() + "' ?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    databaseOperations.eraseNote(cell.getItem());
                    notesTrashTable(stage);
                }

            });
            contextMenu.getItems().addAll(restoreItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        notesTrash.setItems(data);
        notesTrash.getColumns().addAll(trashNoteCount, trashNoteName, trashNoteDeleteDate);
        VBox root = new VBox();
        root.setPadding(new Insets(10, 10, 10, 10));
        notesTrash.setPrefSize(750, 500);
        ButtonBar buttonBar = new ButtonBar();
        Button emptyTrash = new Button("Empty Trash");
        buttonBar.setPadding(new Insets(10, 10, 10, 10));
        emptyTrash.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setContentText("This operation cannot be undone");
            alert.setHeaderText("Are you sure you want to empty trash' ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                databaseOperations.emptyNotesTrash();
                notesTrashTable(stage);
            }
        });
        if (arrayLists.isEmpty()) {
            emptyTrash.setDisable(true);
        }
        buttonBar.getButtons().add(emptyTrash);
        root.getChildren().addAll(notesTrash, buttonBar);
        stage.setScene(new Scene(root, 772, 521));
    }

    public void viewTrashedTasks() {
        Stage stage = new Stage();
        trashTasksTable(stage);
        stage.setTitle("Tasks Trash");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void trashTasksTable(Stage stage) {
        ArrayList<ArrayList> arrayLists = databaseOperations.fetchTrashedTasks();
        TableView tasksTrash = new TableView();
        TableColumn<TrashedTasks, String> trashTaskCount = new TableColumn<>("#");
        trashTaskCount.setPrefWidth(50);
        TableColumn<TrashedTasks, String> trashTaskName = new TableColumn<>("Name");
        trashTaskName.setPrefWidth(400);
        TableColumn<TrashedTasks, String> trashTaskProjectName = new TableColumn<>("Project");
        trashTaskProjectName.setPrefWidth(200);
        TableColumn<TrashedTasks, String> trashTaskDeleteDate = new TableColumn<>("Date Deleted");
        trashTaskDeleteDate.setPrefWidth(100);
        ObservableList<TrashedTasks> data = FXCollections.observableArrayList();
        try {
//iterate through each element of the arraylist
            for (ArrayList arrayList : arrayLists) {
                SimpleIntegerProperty id = new SimpleIntegerProperty((Integer) arrayList.get(0));
                SimpleStringProperty name = new SimpleStringProperty(arrayList.get(1).toString());
                SimpleStringProperty projectName = new SimpleStringProperty(arrayList.get(3).toString());
                SimpleStringProperty date = new SimpleStringProperty(formatTime(arrayList.get(2).toString()));
                data.add(new TrashedTasks(name, id, date, projectName));
//obtain the value of each column
                trashTaskCount.setCellValueFactory(new PropertyValueFactory<>("taskId"));
                trashTaskName.setCellValueFactory(new PropertyValueFactory<>("taskName"));
                trashTaskProjectName.setCellValueFactory(new PropertyValueFactory<>("projectName"));
                trashTaskDeleteDate.setCellValueFactory(new PropertyValueFactory<>("dateDeleted"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        trashTaskName.setCellFactory(lv -> {
            TableCell<TrashedTasks, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem restoreItem = new MenuItem();
            restoreItem.textProperty().bind(Bindings.format("Restore", cell.itemProperty()));
            restoreItem.setOnAction(event -> {
                databaseOperations.restoreTask(cell.getItem());
                tabPane.getSelectionModel().selectFirst();
                trashTasksTable(stage);
                fetchProjectTitles();
                if (tasksTrash.getSelectionModel().getSelectedItem() != null) {
                    TrashedTasks selectedTask = (TrashedTasks) tasksTrash.getSelectionModel().getSelectedItem();
                    projectTitles.getSelectionModel().select(selectedTask.getProjectName());
                    onProjectClicked();
                }
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Permanently", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                alert.setHeaderText("Are you sure you want to permanently delete '" + cell.getItem() + "' ?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    databaseOperations.eraseTask(cell.getItem());
                    trashTasksTable(stage);
                }
            });
            contextMenu.getItems().addAll(restoreItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
        tasksTrash.setItems(data);
        tasksTrash.getColumns().addAll(trashTaskCount, trashTaskName, trashTaskProjectName, trashTaskDeleteDate);
        VBox root = new VBox();
        root.setPadding(new Insets(10, 10, 10, 10));
        ButtonBar buttonBar = new ButtonBar();
        Button emptyTrash = new Button("Empty Trash");
        buttonBar.setPadding(new Insets(10, 10, 10, 10));
        emptyTrash.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setContentText("This operation cannot be undone");
            alert.setHeaderText("Are you sure you want to empty trash' ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                databaseOperations.emptyTasksTrash();
                trashTasksTable(stage);
            }
        });
        if (arrayLists.isEmpty()) {
            emptyTrash.setDisable(true);
        }
        buttonBar.getButtons().add(emptyTrash);
        tasksTrash.setPrefSize(750, 500);
        root.getChildren().addAll(tasksTrash, buttonBar);
        stage.setScene(new Scene(root, 772, 521));
    }


    public static class Task {
        private SimpleStringProperty taskName;
        private SimpleIntegerProperty taskId;
        private CheckBox complete;
        private ChoiceBox taskPriority;
        private SimpleStringProperty dateCreated;

        public Task(SimpleStringProperty taskName, SimpleIntegerProperty taskId, CheckBox complete, SimpleStringProperty dateCreated, ChoiceBox taskPriority) {
            this.taskName = taskName;
            this.taskId = taskId;
            this.complete = complete;
            this.dateCreated = dateCreated;
            this.taskPriority = taskPriority;
        }

        public String getTaskName() {
            return taskName.get();
        }

        public SimpleStringProperty taskNameProperty() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName.set(taskName);
        }

        public int getTaskId() {
            return taskId.get();
        }

        public SimpleIntegerProperty taskIdProperty() {
            return taskId;
        }

        public void setTaskId(int taskId) {
            this.taskId.set(taskId);
        }

        public CheckBox getComplete() {
            return complete;
        }

        public CheckBox completedProperty() {
            return complete;
        }

        public void setComplete(CheckBox complete) {
            this.complete = complete;
        }

        public ChoiceBox getTaskPriority() {
            return taskPriority;
        }

        public ChoiceBox taskpriorityProperty() {
            return taskPriority;
        }

        public void setTaskPriority(ChoiceBox taskPriority) {
            this.taskPriority = taskPriority;
        }

        public String getDateCreated() {
            return dateCreated.get();
        }

        public SimpleStringProperty dateCreatedProperty() {
            return dateCreated;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated.set(dateCreated);
        }


    }

    public static class TrashedProjects {
        private SimpleStringProperty projectName;
        private SimpleStringProperty dateDeleted;
        private SimpleIntegerProperty projectId;

        private TrashedProjects(SimpleStringProperty projectName, SimpleIntegerProperty projectId, SimpleStringProperty dateDeleted) {
            this.projectName = projectName;
            this.dateDeleted = dateDeleted;
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName.get();
        }

        public SimpleStringProperty projectNameProperty() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName.set(projectName);
        }

        public int getProjectId() {
            return projectId.get();
        }

        public SimpleIntegerProperty projectIdProperty() {
            return projectId;
        }

        public void setProjectId(int projectId) {
            this.projectId.set(projectId);
        }

        public String getDateDeleted() {
            return dateDeleted.get();
        }

        public SimpleStringProperty dateDeletedProperty() {
            return dateDeleted;
        }

        public void setDateDeleted(String dateDeleted) {
            this.dateDeleted.set(dateDeleted);
        }
    }

    public static class TrashedNotes {
        private SimpleStringProperty noteName;
        private SimpleStringProperty dateDeleted;
        private SimpleIntegerProperty noteId;

        public TrashedNotes(SimpleStringProperty noteName, SimpleIntegerProperty noteId, SimpleStringProperty dateDeleted) {
            this.noteName = noteName;
            this.dateDeleted = dateDeleted;
            this.noteId = noteId;

        }

        public String getNoteName() {
            return noteName.get();
        }

        public SimpleStringProperty noteNameProperty() {
            return noteName;
        }

        public void setNoteName(String noteName) {
            this.noteName.set(noteName);
        }

        public int getNoteId() {
            return noteId.get();
        }

        public SimpleIntegerProperty noteIdProperty() {
            return noteId;
        }

        public void setNoteId(int noteId) {
            this.noteId.set(noteId);
        }

        public String getDateDeleted() {
            return dateDeleted.get();
        }

        public SimpleStringProperty dateDeletedProperty() {
            return dateDeleted;
        }

        public void setDateDeleted(String dateDeleted) {
            this.dateDeleted.set(dateDeleted);
        }
    }

    public static class TrashedTasks {
        private SimpleStringProperty taskName;
        private SimpleStringProperty dateDeleted;
        private SimpleStringProperty projectName;
        private SimpleIntegerProperty taskId;

        public TrashedTasks(SimpleStringProperty taskName, SimpleIntegerProperty taskId, SimpleStringProperty dateDeleted, SimpleStringProperty projectName) {
            this.taskName = taskName;
            this.dateDeleted = dateDeleted;
            this.projectName = projectName;
            this.taskId = taskId;

        }

        public String getTaskName() {
            return taskName.get();
        }

        public SimpleStringProperty taskNameProperty() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName.set(taskName);
        }

        public String getProjectName() {
            return projectName.get();
        }

        public SimpleStringProperty projectNameProperty() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName.set(projectName);
        }

        public int getTaskId() {
            return taskId.get();
        }

        public SimpleIntegerProperty taskIdProperty() {
            return taskId;
        }

        public void setTaskId(int taskId) {
            this.taskId.set(taskId);
        }

        public String getDateDeleted() {
            return dateDeleted.get();
        }

        public SimpleStringProperty dateDeletedProperty() {
            return dateDeleted;
        }

        public void setDateDeleted(String dateDeleted) {
            this.dateDeleted.set(dateDeleted);
        }
    }

}
