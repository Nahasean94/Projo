package controllers;

import database.DatabaseOperations;
import database.PasswordAuthentication;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    @FXML
    private AnchorPane mainAnchor;
    @FXML
    private MenuItem addPasswordMenu, changePassword, hideLockedItems, viewLockedProjects, viewLockedNotes;

    private String projectDescriptionText = "";
    private ArrayList projectsArrayList;
    private ArrayList notesArrayList;
    private Tooltip tooltip=new Tooltip("A task with such name already exists ");

    private String itemName = "";
    private int itemId = 0;


    private DatabaseOperations databaseOperations = new DatabaseOperations();

    public Controller() {
    }

    @FXML
    public void initialize() {

        /*
        add shortcuts to the app
         */
        //new project
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN).match(event)) {
                onCreateNewProject();
            }
        });
        //about app
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN).match(event)) {
                aboutApp();
            }
        });
        //keyboard shortcuts
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN).match(event)) {
                appShortcuts();
            }
        });
        //new note
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN).match(event)) {
                onCreateNewNote();
            }
        });
        //projects trash
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN).match(event)) {
                viewTrashedProjects();
            }
        });
        //notes trash
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN).match(event)) {
                viewTrashedNotes();
            }
        });
        //tasks trash
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN).match(event)) {
                viewTrashedTasks();
            }
        });
        //request focus on search textfield
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                searchTitles.requestFocus();
            }
        });
        //switch tabs
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN).match(event)) {
                if (tabPane.getSelectionModel().isSelected(1))
                    tabPane.getSelectionModel().selectFirst();
                else
                    tabPane.getSelectionModel().selectLast();

            }
        });
        //Quit app
        mainAnchor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN).match(event)) {
                System.exit(0);
            }
        });
        //permanently delete a project
        projectTitles.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN).match(event)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + projectTitles.getSelectionModel().getSelectedItems().size() + " projects");
                } else {
                    alert.setHeaderText("Are you sure you want to permanently delete '" + projectTitles.getSelectionModel().getSelectedItem() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = projectTitles.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            databaseOperations.eraseProject(item.toString());
                        }
                    } else {
                        databaseOperations.eraseProject(projectTitles.getSelectionModel().getSelectedItem().toString());
                    }
                    fetchUnlockedProjectTitles();
                    projectTitles.getSelectionModel().selectFirst();
                }


            } else if (event.getCode() == KeyCode.DELETE) {
                confirmTrashingProject();
            }
        });
        //permanently delete a note
        noteTitles.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN).match(event)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + noteTitles.getSelectionModel().getSelectedItems().size() + " notes");
                } else {
                    alert.setHeaderText("Are you sure you want to permanently delete '" + noteTitles.getSelectionModel().getSelectedItem() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = noteTitles.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            databaseOperations.eraseNote(item.toString());
                        }
                    } else {
                        databaseOperations.eraseNote(noteTitles.getSelectionModel().getSelectedItem().toString());
                    }
                    fetchUnlockedNoteTitles();
                    noteTitles.getSelectionModel().selectFirst();
                }


            } else if (event.getCode() == KeyCode.DELETE) {
                confirmTrashingNote();
            }
        });
        //lock a project
        projectTitles.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN).match(event)) {
                lockItems();
            }
        });
        //Lock a note
        noteTitles.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN).match(event)) {
                lockItems();
            }
        });
        //permanently delete a task
        tasksTable.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN).match(event)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (tasksTable.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + tasksTable.getSelectionModel().getSelectedItems().size() + " tasks");
                } else {
                    Task selectedTask = (Task) tasksTable.getSelectionModel().getSelectedItem();
                    alert.setHeaderText("Are you sure you want to permanently delete '" + selectedTask.getTaskName() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (tasksTable.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = tasksTable.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            Task selectedTask = (Task) item;
                            databaseOperations.eraseTask(selectedTask.getTaskName());
                        }
                    } else {
                        Task selectedTask = (Task) tasksTable.getSelectionModel().getSelectedItem();
                        databaseOperations.eraseTask(selectedTask.getTaskName());
                    }
                    fetchProjectTasks();
                    tasksTable.getSelectionModel().selectFirst();
                }

            } else if (event.getCode() == KeyCode.DELETE) {
                confirmTrashingTask();
            }
        });
        //Save note
        noteBody.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
                saveNote();
            }
        });
        //view locked projects
        noteBody.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN).match(event)) {
                fetchAllProjectTitles();
            }
        });
        //view locked notes
        noteBody.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN).match(event)) {
                fetchAllNoteTitles();
            }
        });
        //hide locked notes
        noteBody.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN).match(event)) {
                onHideLockedItems();
            }
        });


        //disable the add button menu item if a password exists in the db
        if (databaseOperations.isPassword()) {
            addPasswordMenu.setDisable(true);
            noteBody.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN).match(event)) {
                    changePassword();
                }
            });

        } else {
            changePassword.setDisable(true);

        }
        newTaskTextField.setTooltip(tooltip);
        fetchUnlockedNoteTitles();
        fetchUnlockedProjectTitles();

    }

    /**
     * event handler for new project menu item
     */
    public void onCreateNewProject() {
        Dialog dialog = new Dialog<>();
        dialog.setTitle("New Project");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12, 10, 10, 10));
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
        Label errors = new Label();
        errors.setPrefWidth(340);
        errors.setTextFill(javafx.scene.paint.Color.RED);
        grid.add(errors, 1, 2);
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

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        btOk.addEventFilter(ActionEvent.ACTION, (event) -> {
//            result.ifPresent(titleDue -> {
            if (databaseOperations.isProjectExists(title.getText().trim())) {
                event.consume();
                saveButton.setDisable(true);
                errors.setText("A project with that name already exists");
            } else {
                saveNewProject(title.getText().trim(), datePicker.getValue());

            }
//            });
        });
        dialog.showAndWait();
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
        grid.setPadding(new Insets(12, 10, 10, 10));
        TextField title = new TextField();
        title.setPromptText("Title");
        title.setPrefWidth(340);
        grid.add(new Label("Title:"), 0, 0);
        grid.add(title, 1, 0);
        Label errors = new Label();
        errors.setPrefWidth(340);
        errors.setTextFill(javafx.scene.paint.Color.RED);
        grid.add(errors, 1, 2);

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

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        btOk.addEventFilter(ActionEvent.ACTION, (event) -> {
//            result.ifPresent(titleDue -> {
            if (databaseOperations.isNoteExists(title.getText().trim())) {
                event.consume();
                saveButton.setDisable(true);
                errors.setText("A note with that name already exists");
            } else {
                databaseOperations.createNote(title.getText().trim());
                tabPane.getSelectionModel().selectLast();
                fetchUnlockedNoteTitles();

            }
//            });
        });
//        result.ifPresent(titleDue -> {

//        });
        dialog.showAndWait();
    }

    private void confirmTrashingProject(ListCell<String> cell) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
            alert.setHeaderText("Are you sure you want to move " + projectTitles.getSelectionModel().getSelectedItems().size() + " projects to trash?");
        } else {
            alert.setHeaderText("Are you sure you want to move '" + cell.getItem() + "' to trash?");
        }
        alert.setContentText("This project will trashed");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            deleteProject(cell);
            fetchUnlockedProjectTitles();
            projectTitles.getSelectionModel().selectFirst();
        }
    }

    private void confirmTrashingProject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
            alert.setHeaderText("Are you sure you want to move " + projectTitles.getSelectionModel().getSelectedItems().size() + " projects to trash?");
        } else {
            alert.setHeaderText("Are you sure you want to move '" + projectTitles.getSelectionModel().getSelectedItem() + "' to trash?");
        }
        alert.setContentText("This project will trashed");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
                ObservableList items = projectTitles.getSelectionModel().getSelectedItems();
                for (Object item : items) {
                    databaseOperations.trashProject(item.toString());
                }
            } else {
                databaseOperations.trashProject(projectTitles.getSelectionModel().getSelectedItem().toString());
            }
            fetchUnlockedProjectTitles();
            projectTitles.getSelectionModel().selectFirst();
        }
    }

    private void confirmTrashingNote() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
            alert.setHeaderText("Are you sure you want to move " + noteTitles.getSelectionModel().getSelectedItems().size() + " notes to trash?");
        } else {
            alert.setHeaderText("Are you sure you want to move '" + noteTitles.getSelectionModel().getSelectedItem() + "' to trash?");
        }
        alert.setContentText("This note will trashed");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
                ObservableList items = noteTitles.getSelectionModel().getSelectedItems();
                for (Object item : items) {
                    databaseOperations.trashNote(item.toString());
                }
            } else {
                databaseOperations.trashNote(noteTitles.getSelectionModel().getSelectedItem().toString());
            }
            fetchUnlockedNoteTitles();
            noteTitles.getSelectionModel().selectFirst();
        }
    }

    private void confirmTrashingTask() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setContentText("This operation cannot be undone");
        if (tasksTable.getSelectionModel().getSelectedItems().size() > 1) {
            alert.setHeaderText("Are you sure you want to move " + tasksTable.getSelectionModel().getSelectedItems().size() + " tasks to trash");
        } else {
            Task selectedTask = (Task) tasksTable.getSelectionModel().getSelectedItem();
            alert.setHeaderText("Are you sure you want to move '" + selectedTask.getTaskName() + "' to trash?");
        }
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (tasksTable.getSelectionModel().getSelectedItems().size() > 1) {
                ObservableList items = tasksTable.getSelectionModel().getSelectedItems();
                for (Object item : items) {
                    Task selectedTask = (Task) item;
                    databaseOperations.trashTask(selectedTask.getTaskName());
                }
            } else {
                Task selectedTask = (Task) tasksTable.getSelectionModel().getSelectedItem();
                databaseOperations.trashTask(selectedTask.getTaskName());
            }
            fetchProjectTasks();
            noteTitles.getSelectionModel().selectFirst();

        }
    }

    private void deleteProject(ListCell<String> cell) {
        if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
            ObservableList items = projectTitles.getSelectionModel().getSelectedItems();
            for (Object item : items) {
                databaseOperations.trashProject(item.toString());
            }
        } else {
            databaseOperations.trashProject(cell.getItem());
        }
    }

    /**
     * Fetch project titles
     */

    private void fetchUnlockedProjectTitles() {
        processProjectTitles(databaseOperations.loadUnlockedProjectTitles());

    }

    public void fetchAllProjectTitles() {
        if (!viewLockedProjects.isDisable()) {
            Dialog dialog = new Dialog();
            dialog.setTitle("Enter password to view locked items");

// Set the button types.
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(12, 10, 10, 10));
            PasswordField password = new PasswordField();
            password.setPromptText("Password");
            password.setPrefWidth(340);
            grid.add(new Label("Password:"), 0, 0);
            grid.add(password, 1, 0);
            Label errors = new Label();
            errors.setPrefWidth(340);
            errors.setTextFill(javafx.scene.paint.Color.RED);
            grid.add(errors, 1, 1);

// Enable/Disable login button depending on whether a title was entered.
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
            password.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty());
            });
            dialog.getDialogPane().setContent(grid);

// Request focus on the title field by default.
            Platform.runLater(password::requestFocus);

            final Button btOk = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            btOk.addEventFilter(ActionEvent.ACTION, (event) -> {
                if (databaseOperations.isCurrentPassword(password.getText().trim())) {
                    processProjectTitles(databaseOperations.loadAllProjectTitles());
                    tabPane.getSelectionModel().selectFirst();
                    errors.setText("");
                    hideLockedItems.setDisable(false);
                    viewLockedProjects.setDisable(true);
                } else {
                    event.consume();
                    saveButton.setDisable(true);
                    errors.setText("Current Password is incorrrect");
                }

            });
            dialog.showAndWait();
        }

    }

    private void processProjectTitles(ArrayList projectsArrayList) {
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
                projectTitles.getSelectionModel().clearSelection();
                projectTitles.getSelectionModel().selectFirst();
                onProjectClicked();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        projectTitles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        projectTitles.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                projectTitles.setEditable(false);
                onProjectClicked();
            }
        });
        projectTitles.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem lockItem = new MenuItem();
            lockItem.textProperty().bind(Bindings.format("Lock", cell.itemProperty()));
            lockItem.setOnAction(event -> {
                lockItems();

            });
            lockItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                confirmTrashingProject();

            });
            deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
            contextMenu.getItems().addAll(lockItem, deleteItem);
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
    private void fetchUnlockedNoteTitles() {
        processNoteTitles(databaseOperations.loadNoteTitles());
    }

    public void fetchAllNoteTitles() {
        if (!viewLockedNotes.isDisable()) {


            Dialog dialog = new Dialog();
            dialog.setTitle("Enter password to view locked items");

// Set the button types.
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(12, 10, 10, 10));
            PasswordField password = new PasswordField();
            password.setPromptText("Password");
            password.setPrefWidth(340);
            grid.add(new Label("Password:"), 0, 0);
            grid.add(password, 1, 0);
            Label errors = new Label();
            errors.setPrefWidth(340);
            errors.setTextFill(javafx.scene.paint.Color.RED);
            grid.add(errors, 1, 1);

// Enable/Disable login button depending on whether a title was entered.
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
            password.textProperty().addListener((observable, oldValue, newValue) -> {
                saveButton.setDisable(newValue.trim().isEmpty());
            });
            dialog.getDialogPane().setContent(grid);

// Request focus on the title field by default.
            Platform.runLater(password::requestFocus);

            final Button btOk = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            btOk.addEventFilter(ActionEvent.ACTION, (event) -> {
                if (databaseOperations.isCurrentPassword(password.getText().trim())) {
                    processNoteTitles(databaseOperations.loadAllNoteTitles());
                    tabPane.getSelectionModel().selectLast();
                    errors.setText("");
                    hideLockedItems.setDisable(false);
                    viewLockedNotes.setDisable(true);
                } else {
                    event.consume();
                    saveButton.setDisable(true);
                    errors.setText("Current Password is incorrrect");
                }
            });
            dialog.showAndWait();
        }
    }

    public void onHideLockedItems() {
        if (!hideLockedItems.isDisable()) {
            fetchUnlockedProjectTitles();
            fetchUnlockedNoteTitles();
            viewLockedNotes.setDisable(false);
            viewLockedProjects.setDisable(false);
            hideLockedItems.setDisable(true);
        }
    }

    private void processNoteTitles(ArrayList notesArrayList) {
        noteTitles.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                noteTitles.setEditable(false);
                onNoteClicked();
            }
        });
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
            noteTitles.getSelectionModel().clearSelection();
            noteTitles.getSelectionModel().selectFirst();
            onNoteClicked();
        } catch (Exception e) {
            e.printStackTrace();
        }
        noteTitles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        noteTitles.setCellFactory(lv -> {

            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem lockItem = new MenuItem();
            lockItem.textProperty().bind(Bindings.format("Lock", cell.itemProperty()));
            lockItem.setOnAction(event -> {
                lockItems();

            });
            lockItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to move " + noteTitles.getSelectionModel().getSelectedItems().size() + " notes to trash?");
                } else {
                    alert.setHeaderText("Are you sure you want to move '" + cell.getItem() + "' to trash?");
                }
                alert.setContentText("This note will trashed");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = noteTitles.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            databaseOperations.trashNote(item.toString());
                        }
                    } else {
                        databaseOperations.trashNote(cell.getItem());
                    }
                    noteTitles.getSelectionModel().selectFirst();
                    fetchUnlockedNoteTitles();
                }

            });
            deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
            contextMenu.getItems().addAll(lockItem, deleteItem);
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
        fetchUnlockedProjectTitles();
        tabPane.getSelectionModel().selectFirst();

//        fetchUnlockedNoteTitles();
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
            projectTitles.getSelectionModel().clearSelection();
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
            projectTitles.getSelectionModel().clearSelection();
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

        Optional result = dialog.showAndWait();
        result.ifPresent(desc -> {
            databaseOperations.updateProjectDescription(itemName, desc.toString());
            fetchProjectDescription();
        });
    }

    public void typingTask() {
        tooltip.hide();
        if (!newTaskTextField.getText().trim().isEmpty()) {
            saveNewTask.setDisable(false);
        } else {
            saveNewTask.setDisable(true);
        }
    }

    //add a new task
    public void addProjectTask() {
        if (!newTaskTextField.getText().trim().isEmpty()) {
            if (databaseOperations.isTaskExists(newTaskTextField.getText().trim())) {
                System.out.println("task exists");
                Point2D p = newTaskTextField.localToScene(0.0, 0.0);
                tooltip.show(newTaskTextField,p.getX()
                        + newTaskTextField.getScene().getX() + newTaskTextField.getScene().getWindow().getX(), p.getY()
                        + newTaskTextField.getScene().getY() + newTaskTextField.getScene().getWindow().getY()+newTaskTextField.getHeight()+2);
                saveNewTask.setDisable(true);
//                tooltip.hide();
            } else {
                databaseOperations.createTask(newTaskTextField.getText().trim(), itemId, priorityBox.getValue().toString());
                newTaskTextField.setText("");
                saveNewTask.setDisable(true);
                priorityBox.setValue("Low");
                fetchProjectTasks();
            }
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
            tasksTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
                if (tasksTable.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to move " + tasksTable.getSelectionModel().getSelectedItems().size() + " tasks to trash?");
                } else {
                    alert.setHeaderText("Are you sure you want to move '" + cell.getItem() + "' to trash?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (tasksTable.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = tasksTable.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            Task selectedTask = (Task) item;
                            databaseOperations.trashTask(selectedTask.getTaskName());
                        }
                    } else {
                        databaseOperations.trashTask(cell.getItem());
                    }
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

    private void viewSourceCode() {
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
        projectsTrash.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//permanently delete a task
        projectsTrash.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN).match(event)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (projectsTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + projectsTrash.getSelectionModel().getSelectedItems().size() + " projects?");
                } else {
                    TrashedProjects selectedProject = (TrashedProjects) projectsTrash.getSelectionModel().getSelectedItem();
                    alert.setHeaderText("Are you sure you want to permanently delete '" + selectedProject.getProjectName() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (projectsTrash.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = projectsTrash.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            TrashedProjects selectedProject = (TrashedProjects) item;
                            databaseOperations.eraseProject(selectedProject.getProjectName());
                        }
                    } else {
                        TrashedProjects selectedProject = (TrashedProjects) projectsTrash.getSelectionModel().getSelectedItem();
                        databaseOperations.eraseProject(selectedProject.getProjectName());
                    }
                    projectsTrashTable(stage);
                }
            } else if (new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN).match(event)) {
                if (projectsTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    ObservableList items = projectsTrash.getSelectionModel().getSelectedItems();
                    for (Object item : items) {
                        TrashedProjects selectedProject = (TrashedProjects) item;
                        databaseOperations.restoreProject(selectedProject.getProjectName());
                    }
                } else {
                    TrashedProjects selectedProject = (TrashedProjects) projectsTrash.getSelectionModel().getSelectedItem();
                    databaseOperations.restoreProject(selectedProject.getProjectName());
                }

                projectsTrashTable(stage);
                tabPane.getSelectionModel().selectFirst();
                fetchUnlockedProjectTitles();
                if (projectsTrash.getSelectionModel().getSelectedItem() != null) {
                    TrashedProjects selectedTask = (TrashedProjects) projectsTrash.getSelectionModel().getSelectedItem();
                    projectTitles.getSelectionModel().select(selectedTask.getProjectName());
                    onProjectClicked();
                }
            }
        });
        trashProjectName.setCellFactory(lv -> {
            TableCell<TrashedProjects, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem restoreItem = new MenuItem();
            restoreItem.textProperty().bind(Bindings.format("Restore", cell.itemProperty()));
            restoreItem.setOnAction(event -> {
                if (projectsTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    ObservableList items = projectsTrash.getSelectionModel().getSelectedItems();
                    for (Object item : items) {
                        TrashedProjects selectedProject = (TrashedProjects) item;
                        databaseOperations.restoreProject(selectedProject.getProjectName());
                    }
                } else {
                    databaseOperations.restoreProject(cell.getItem());
                }
                projectsTrashTable(stage);
                tabPane.getSelectionModel().selectFirst();
                fetchUnlockedProjectTitles();
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Permanently", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (projectsTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + projectsTrash.getSelectionModel().getSelectedItems().size() + " projects");
                } else {
                    alert.setHeaderText("Are you sure you want to permanently delete '" + cell.getItem() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (projectsTrash.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = projectsTrash.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            TrashedProjects selectedProject = (TrashedProjects) item;
                            databaseOperations.eraseProject(selectedProject.getProjectName());
                        }
                    } else {
                        databaseOperations.eraseProject(cell.getItem());
                    }
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
        //permanently delete a task
        notesTrash.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN).match(event)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (notesTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + notesTrash.getSelectionModel().getSelectedItems().size() + " notes?");
                } else {
                    TrashedNotes selectedProject = (TrashedNotes) notesTrash.getSelectionModel().getSelectedItem();
                    alert.setHeaderText("Are you sure you want to permanently delete '" + selectedProject.getNoteName() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (notesTrash.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = notesTrash.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            TrashedNotes selectedProNote = (TrashedNotes) item;
                            databaseOperations.eraseNote(selectedProNote.getNoteName());
                        }
                    } else {
                        TrashedNotes selectedProNote = (TrashedNotes) notesTrash.getSelectionModel().getSelectedItem();
                        databaseOperations.eraseNote(selectedProNote.getNoteName());
                    }
                    notesTrashTable(stage);
                }
            } else if (new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN).match(event)) {
                if (notesTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    ObservableList items = notesTrash.getSelectionModel().getSelectedItems();
                    for (Object item : items) {
                        TrashedNotes selectedNote = (TrashedNotes) item;
                        databaseOperations.restoreNote(selectedNote.getNoteName());
                    }
                } else {
                    TrashedNotes selectedNote = (TrashedNotes) notesTrash.getSelectionModel().getSelectedItem();
                    databaseOperations.restoreNote(selectedNote.getNoteName());
                }

                tabPane.getSelectionModel().selectLast();
                notesTrashTable(stage);
                fetchUnlockedNoteTitles();
                if (notesTrash.getSelectionModel().getSelectedItem() != null) {
                    TrashedNotes selectedNote = (TrashedNotes) notesTrash.getSelectionModel().getSelectedItem();
                    projectTitles.getSelectionModel().select(selectedNote.getNoteName());
                    onNoteClicked();
                }
            }
        });
        notesTrash.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        trashNoteName.setCellFactory(lv -> {
            TableCell<TrashedNotes, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem restoreItem = new MenuItem();
            restoreItem.textProperty().bind(Bindings.format("Restore", cell.itemProperty()));
            restoreItem.setOnAction(event -> {
                if (notesTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    ObservableList items = notesTrash.getSelectionModel().getSelectedItems();
                    for (Object item : items) {
                        TrashedNotes selectedNote = (TrashedNotes) item;
                        databaseOperations.restoreNote(selectedNote.getNoteName());
                    }
                } else {
                    databaseOperations.restoreNote(cell.getItem());
                }

                tabPane.getSelectionModel().selectLast();
                notesTrashTable(stage);
                fetchUnlockedNoteTitles();
                if (notesTrash.getSelectionModel().getSelectedItem() != null) {
                    TrashedNotes selectedNote = (TrashedNotes) notesTrash.getSelectionModel().getSelectedItem();
                    noteTitles.getSelectionModel().select(selectedNote.getNoteName());
                    onNoteClicked();
                }
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Permanently", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (notesTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + notesTrash.getSelectionModel().getSelectedItems().size() + " notes");
                } else {
                    alert.setHeaderText("Are you sure you want to permanently delete '" + cell.getItem() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (notesTrash.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = notesTrash.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            TrashedNotes selectedNote = (TrashedNotes) item;
                            databaseOperations.eraseNote(selectedNote.getNoteName());
                        }
                    } else {
                        databaseOperations.eraseNote(cell.getItem());
                    }

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
        tasksTrash.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //permanently delete a task
        tasksTrash.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN).match(event)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion");
                alert.setContentText("This operation cannot be undone");
                if (tasksTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + tasksTrash.getSelectionModel().getSelectedItems().size() + " tasks");
                } else {
                    TrashedTasks selectedTask = (TrashedTasks) tasksTrash.getSelectionModel().getSelectedItem();
                    alert.setHeaderText("Are you sure you want to permanently delete '" + selectedTask.getTaskName() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (tasksTrash.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = tasksTrash.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            TrashedTasks selectedTask = (TrashedTasks) item;
                            databaseOperations.eraseTask(selectedTask.getTaskName());
                        }
                    } else {
                        TrashedTasks selectedTask = (TrashedTasks) tasksTrash.getSelectionModel().getSelectedItem();
                        databaseOperations.eraseTask(selectedTask.getTaskName());
                    }
                    trashTasksTable(stage);
                }
            } else if (new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN).match(event)) {
                if (tasksTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    ObservableList items = tasksTrash.getSelectionModel().getSelectedItems();
                    for (Object item : items) {
                        TrashedTasks selectedTask = (TrashedTasks) item;
                        databaseOperations.restoreTask(selectedTask.getTaskName());
                    }
                } else {
                    TrashedTasks selectedTask = (TrashedTasks) tasksTrash.getSelectionModel().getSelectedItem();
                    databaseOperations.restoreTask(selectedTask.getTaskName());
                }

                tabPane.getSelectionModel().selectFirst();
                trashTasksTable(stage);
                fetchUnlockedProjectTitles();
                if (tasksTrash.getSelectionModel().getSelectedItem() != null) {
                    TrashedTasks selectedTask = (TrashedTasks) tasksTrash.getSelectionModel().getSelectedItem();
                    projectTitles.getSelectionModel().select(selectedTask.getProjectName());
                    onProjectClicked();
                }
            }
        });
        trashTaskName.setCellFactory(lv -> {
            TableCell<TrashedTasks, String> cell = new TableCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem restoreItem = new MenuItem();
            restoreItem.textProperty().bind(Bindings.format("Restore", cell.itemProperty()));
            restoreItem.setOnAction(event -> {
                if (tasksTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    ObservableList items = tasksTrash.getSelectionModel().getSelectedItems();
                    for (Object item : items) {
                        TrashedTasks selectedTask = (TrashedTasks) item;
                        databaseOperations.restoreTask(selectedTask.getTaskName());
                    }
                } else {
                    databaseOperations.restoreTask(cell.getItem());
                }

                tabPane.getSelectionModel().selectFirst();
                trashTasksTable(stage);
                fetchUnlockedProjectTitles();
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
                if (tasksTrash.getSelectionModel().getSelectedItems().size() > 1) {
                    alert.setHeaderText("Are you sure you want to permanently delete " + tasksTrash.getSelectionModel().getSelectedItems().size() + " tasks");
                } else {
                    alert.setHeaderText("Are you sure you want to permanently delete '" + cell.getItem() + "'?");
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if (tasksTrash.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList items = tasksTrash.getSelectionModel().getSelectedItems();
                        for (Object item : items) {
                            TrashedTasks selectedTask = (TrashedTasks) item;
                            databaseOperations.eraseTask(selectedTask.getTaskName());
                        }
                    } else {
                        databaseOperations.eraseTask(cell.getItem());
                    }
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

    public void aboutApp() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Text name = new Text("App name:");
        name.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(name, 0, 0);
        Text projo = new Text("Projo");
        grid.add(projo, 1, 0, 2, 1);
        Text use = new Text("Use:");
        use.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(use, 0, 1);
        Text projects = new Text("Manage personal projects and take notes");
        grid.add(projects, 1, 1, 2, 1);
        Text language = new Text("Language:");
        language.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(language, 0, 2);
        Text java = new Text("Java");
        grid.add(java, 1, 2, 2, 1);
        Text developer = new Text("Developer:");
        developer.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(developer, 0, 3);
        Text me = new Text("Nahashon Njenga");
        grid.add(me, 1, 3, 2, 1);
        Text email = new Text("Email:");
        email.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(email, 0, 4);
        Text ncubed = new Text("ncubed940@gmail.com");
        grid.add(ncubed, 1, 4, 2, 1);
        Text sourceCode = new Text("Source Code:");
        grid.add(sourceCode, 0, 5);
        Hyperlink source = new Hyperlink("GitHub");
        source.setOnAction(e -> {
            viewSourceCode();
        });
        grid.add(source, 1, 5, 2, 1);
        sourceCode.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        Button close = new Button("Close");
        grid.add(close, 1, 6, 2, 1);

        Stage stage = new Stage();
        close.setOnAction(e -> {
            stage.close();
        });
        stage.setTitle("About Projo");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(new Scene(grid));
        stage.showAndWait();
    }

    public void appShortcuts() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Text project = new Text("Ctrl+P:");
        project.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(project, 0, 0);
        Text newProject = new Text("Create new Project");
        grid.add(newProject, 1, 0, 2, 1);
        Text note = new Text("Ctrl+N:");
        note.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(note, 0, 1);
        Text newNote = new Text("Create new Note");
        grid.add(newNote, 1, 1, 2, 1);
        Text trashedProjects = new Text("Ctrl+Shift+P:");
        trashedProjects.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(trashedProjects, 0, 2);
        Text projectsTrash = new Text("View trashed projects");
        grid.add(projectsTrash, 1, 2, 2, 1);
        Text trashedNotes = new Text("Ctrl+Shift+N:");
        trashedNotes.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(trashedNotes, 0, 3);
        Text notesTrash = new Text("View trashed Notes");
        grid.add(notesTrash, 1, 3, 2, 1);
        Text trashedTasks = new Text("Ctrl+Shift+T:");
        trashedTasks.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(trashedTasks, 0, 4);
        Text tasksTrash = new Text("View Trashed Tasks");
        grid.add(tasksTrash, 1, 4, 2, 1);
        Text find = new Text("Ctrl+F:");
        grid.add(find, 0, 5);
        find.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        Text search = new Text("Request focus on the search field");
        grid.add(search, 1, 5, 2, 1);
        Text tab = new Text("Ctrl+T:");
        tab.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(tab, 0, 6);
        Text tabbing = new Text("Switch between project and notes tab");
        grid.add(tabbing, 1, 6, 2, 1);
        Text quit = new Text("Ctrl+Q:");
        quit.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(quit, 0, 7);
        Text quitting = new Text("Quit app");
        grid.add(quitting, 1, 7, 2, 1);
        Text save = new Text("Ctrl+S:");
        save.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(save, 0, 8);
        Text saving = new Text("Save note while editing");
        grid.add(saving, 1, 8, 2, 1);
        Text delete = new Text("Delete:");
        delete.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(delete, 0, 9);
        Text deleting = new Text("Trash the selected item(s)");
        grid.add(deleting, 1, 9, 2, 1);
        Text erase = new Text("Shift+Delete:");
        erase.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(erase, 0, 10);
        Text erasing = new Text("Permanently delete selected item(s)");
        grid.add(erasing, 1, 10, 2, 1);
        Text restore = new Text("Ctrl+R:");
        restore.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(restore, 0, 11);
        Text restoring = new Text("Restore selected item(s)");
        grid.add(restoring, 1, 11, 2, 1);
        Text info = new Text("Ctrl+I:");
        info.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(info, 0, 12);
        Text about = new Text("View information about the app");
        grid.add(about, 1, 12, 2, 1);

        Text lockedProjects = new Text("Ctrl+Alt+P:");
        lockedProjects.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(lockedProjects, 0, 13);
        Text lockProjects = new Text("View locked projects");
        grid.add(lockProjects, 1, 13, 2, 1);
        Text lockedNotes = new Text("Ctrl+Alt+N:");
        lockedNotes.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(lockedNotes, 0, 14);
        Text lockNotes = new Text("View locked notes");
        grid.add(lockNotes, 1, 14, 2, 1);
        Text changePassword = new Text("Shift+Alt+P:");
        changePassword.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(changePassword, 0, 14);
        Text changingPassword = new Text("Change Password");
        grid.add(changingPassword, 1, 14, 2, 1);

        Text shortcut = new Text("Ctrl+K:");
        shortcut.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(shortcut, 0, 13);
        Text keyboardShortcuts = new Text("View keyboard shortcuts");
        grid.add(keyboardShortcuts, 1, 13, 2, 1);
        Text etc = new Text("Others:");
        etc.setFont(javafx.scene.text.Font.font("Helvetica", FontWeight.BOLD, 12));
        grid.add(etc, 0, 13);
        Text others = new Text("Other Operating Systems Shortcuts may work");
        grid.add(others, 1, 13, 2, 1);

        Button close = new Button("Close");
        grid.add(close, 1, 14, 2, 1);

        Stage stage = new Stage();
        close.setOnAction(e -> {
            stage.close();
        });
        stage.setTitle("About Projo");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(new Scene(grid));
        stage.showAndWait();
    }

    public void addPassword() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("New Password");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12, 10, 10, 10));
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setPrefWidth(340);
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        confirmPassword.setPrefWidth(340);
        grid.add(new Label("Password:"), 0, 0);
        grid.add(password, 1, 0);
        grid.add(new Label("Confirm password:"), 0, 1);
        grid.add(confirmPassword, 1, 1);

// Enable/Disable login button depending on whether a title was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(!newValue.trim().equals(password.getText().trim()));
        });
        dialog.getDialogPane().setContent(grid);

// Request focus on the title field by default.
        Platform.runLater(password::requestFocus);

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(passwordConfirm -> {
            databaseOperations.addPassword(passwordConfirm.getKey());
            if (databaseOperations.isPassword()) {
                addPasswordMenu.setDisable(true);
                changePassword.setDisable(false);
                noteBody.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                    if (new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN).match(event)) {
                        changePassword();
                    }
                });

            } else {
                changePassword.setDisable(true);
                addPasswordMenu.setDisable(false);

            }
        });

    }


    public void changePassword() {
        Dialog dialog = new Dialog();
        dialog.setTitle("Change Password");

// Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

// Create the title and datePicker labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12, 10, 10, 10));
        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        grid.add(currentPassword, 1, 0);
        currentPassword.setPrefWidth(340);
        grid.add(new Label("Current Password:"), 0, 0);
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        newPassword.setPrefWidth(340);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("New Password:"), 0, 1);
        PasswordField confirmNewPassword = new PasswordField();
        confirmNewPassword.setPromptText("Confirm New Password");
        confirmNewPassword.setPrefWidth(340);
        grid.add(confirmNewPassword, 1, 2);
        grid.add(new Label("Confirm New Password:"), 0, 2);
        Hyperlink forgotPassword = new Hyperlink("Forgot Password");
        forgotPassword.setPrefWidth(340);
        grid.add(forgotPassword, 1, 3);
        Label errors = new Label();
        errors.setPrefWidth(340);
        errors.setTextFill(javafx.scene.paint.Color.RED);
        grid.add(errors, 1, 4);


// Enable/Disable login button depending on whether a title was entered.
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        confirmNewPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(!newValue.trim().equals(newPassword.getText().trim()));
        });
        currentPassword.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {
                if (!databaseOperations.isCurrentPassword(currentPassword.getText())) {
                    saveButton.setDisable(true);
                    errors.setText("Current Password is incorrrect");
//                currentPassword.requestFocus();
                } else {
                    errors.setText("");
                }
            }


        });
        dialog.getDialogPane().setContent(grid);

// Request focus on the title field by default.
        Platform.runLater(currentPassword::requestFocus);

        Optional result = dialog.showAndWait();
        result.ifPresent(titleDue -> {
            databaseOperations.changePassword(newPassword.getText());
        });
    }

    private void lockItems() {
        if (tabPane.getSelectionModel().isSelected(0)) {
            lockProjects();
        } else {
            lockNotes();
        }
    }

    private void lockProjects() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Locking");
        alert.setContentText("You will require a password to view locked items");
        if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
            alert.setHeaderText("Are you sure you want to lock " + projectTitles.getSelectionModel().getSelectedItems().size() + " projects");
        } else {
            alert.setHeaderText("Are you sure you want to lock '" + projectTitles.getSelectionModel().getSelectedItem() + "'?");
        }
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (projectTitles.getSelectionModel().getSelectedItems().size() > 1) {
                ObservableList items = projectTitles.getSelectionModel().getSelectedItems();
                for (Object item : items) {
                    databaseOperations.lockProject(item.toString());
                }
            } else {
                databaseOperations.lockProject(projectTitles.getSelectionModel().getSelectedItem().toString());
            }
            fetchUnlockedProjectTitles();
            projectTitles.getSelectionModel().selectFirst();
        }

    }

    private void lockNotes() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Locking");
        alert.setContentText("You will require a password to view locked items");
        if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
            alert.setHeaderText("Are you sure you want to lock " + noteTitles.getSelectionModel().getSelectedItems().size() + " notes");
        } else {
            alert.setHeaderText("Are you sure you want to lock '" + noteTitles.getSelectionModel().getSelectedItem() + "'?");
        }
        Optional<ButtonType> result = alert.showAndWait();
        if (noteTitles.getSelectionModel().getSelectedItems().size() > 1) {
            ObservableList items = noteTitles.getSelectionModel().getSelectedItems();
            for (Object item : items) {
                databaseOperations.lockNote(item.toString());
            }
        } else {
            databaseOperations.lockNote(noteTitles.getSelectionModel().getSelectedItem().toString());
        }
        fetchUnlockedNoteTitles();
        noteTitles.getSelectionModel().selectFirst();

    }


    /**
     * Data classes to be used in various tables
     */
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
