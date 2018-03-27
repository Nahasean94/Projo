package controllers;

import database.DatabaseOperations;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
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
    private Button addDescription, editDescription, saveNewTask;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab projectsTab,notesTab;
    @FXML
    private Label viewProjectTitle;
    @FXML
    private Accordion projectAccordion;
    @FXML
    private TitledPane tasksPane;
    @FXML
    private TextFlow projectDescription;
    @FXML
    private TextField newTaskTextField;
    @FXML
    private TableColumn<Task, String> taskName, taskCount, taskDate, taskComplete, taskPriority;
    @FXML
    private TableView tasksTable;
    @FXML
    private ChoiceBox priorityBox;

    private String projectDescriptionText = "";

    private String itemName = "";
    private int itemId = 0;


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
        ArrayList arrayList = databaseOperations.loadProjectTitles();
        if (!arrayList.isEmpty()) {
            try {
                ObservableList observableList = FXCollections.observableArrayList(reverse(arrayList));
                projectTitles.setEditable(true);
                projectsTab.setText("Projects ("+arrayList.size()+")");
                projectTitles.setCellFactory(TextFieldListCell.forListView());

                projectTitles.setOnEditCommit((EventHandler<ListView.EditEvent<String>>) t -> {
                    String oldValue = projectTitles.getSelectionModel().getSelectedItem().toString();
                    projectTitles.getItems().set(t.getIndex(), t.getNewValue());
                    databaseOperations.editProjectName(oldValue, t.getNewValue().toString());
                });
                projectTitles.setItems(observableList);
                projectTitles.getSelectionModel().selectFirst();
                onProjectClicked();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fetch project titles
     */

    private void fetchNoteTitles() {
        ArrayList arrayList = databaseOperations.loadNoteTitles();
        try {
            ObservableList observableList = FXCollections.observableArrayList(reverse(arrayList));
            noteTitles.setCellFactory(TextFieldListCell.forListView());
            notesTab.setText("Notes ("+arrayList.size()+")");
            noteTitles.setOnEditCommit((EventHandler<ListView.EditEvent<String>>) t -> {
                String oldValue = noteTitles.getSelectionModel().getSelectedItem().toString();
                noteTitles.getItems().set(t.getIndex(), t.getNewValue());
                databaseOperations.editNoteTitle(oldValue, t.getNewValue().toString());
            });
            noteTitles.setItems(observableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    //populate the view pane with details of the selected project.
    public void onProjectClicked() {
        viewProjectTitle.setText(projectTitles.getSelectionModel().getSelectedItem().toString());
        projectAccordion.setExpandedPane(tasksPane);
        itemName = projectTitles.getSelectionModel().getSelectedItem().toString();
        itemId = databaseOperations.getItemId(itemName);
        priorityBox.getItems().setAll("Low", "Medium", "High");
        priorityBox.setValue("Low");
        fetchProjectTasks();
        fetchProjectDescription();

    }

    private void fetchProjectDescription() {
        String description = databaseOperations.getProjectDescription(itemName);
        if (description.isEmpty()) {
            description = "Add a description for this project";
            addDescription.setDisable(false);
            editDescription.setDisable(true);
        } else {
            addDescription.setDisable(true);
            editDescription.setDisable(false);
            projectDescriptionText = description;
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
        ArrayList<ArrayList> arrayLists = (databaseOperations.fetchTasks(itemId));
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
                }
                //add event listener to mark a task as complete or not
                complete.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!!newValue) {
                        databaseOperations.markTaskAsComplete(id);
                    } else {
                        databaseOperations.markTaskAsIncomplete(id);
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
                            t.getTableView().getItems().get(
                                    t.getTablePosition().getRow()).setTaskName(t.getNewValue());
                            databaseOperations.editTaskName(id, t.getNewValue());
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String formatTime(String date) {
        String dateTime[] = date.split(" ");
        String day[] = dateTime[0].split("-");
        String time[] = dateTime[1].split(":");
        return day[0] + "-" + day[1] + "-" + day[2] + " " + time[0] + ":" + time[1];
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

}
