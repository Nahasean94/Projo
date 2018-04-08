package database;


import java.sql.*;
import java.util.ArrayList;

public class DatabaseOperations {
    PasswordAuthentication passwordAuthentication = new PasswordAuthentication();

    /**
     * Connect to the prac.db database
     *
     * @return the Connection object
     */
    private Connection connect() {
        // SQLite connection string
        Connection conn = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.
                    getConnection("jdbc:h2:~/projo", "sa", "");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Create new project
     *
     * @param name
     * @param due
     */
    public String createNewProject(String name, Date due) {
        String projectName = "";
        String sql = "INSERT INTO PROJECTS(DUE,NAME) VALUES (?,?)";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setDate(1, due);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
//                System.out.println(rs.getInt(1));
                int id = rs.getInt(1);
                System.out.println(id);
                String sql2 = "SELECT * FROM PROJECTS WHERE ID=?";
                try (Connection conn2 = this.connect();
                     PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
                    preparedStatement2.setInt(1, id);
                    ResultSet rs2 = preparedStatement2.executeQuery();
                    if (rs2.next()) {
                        projectName = rs2.getString("NAME");
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return projectName;
    }

    /**
     * Create a new project
     *
     * @param name
     */
    public void createNewProject(String name) {
        String sql = "INSERT INTO PROJECTS(NAME,INDEFINITE) VALUES (?,?)";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, 1);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
//            if (rs.next()) {
//                System.out.println(rs.getInt(1));
//            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Update the name of the project
     *
     * @param name
     * @param id
     */
    public void updateProjectName(int id, String name) {
        String sql = "UPDATE PROJECTS SET NAME=? WHERE ID=?";
        Query(id, name, sql);
    }

    /**
     * Update the due date of the project
     *
     * @param due
     * @param id
     */
    public void updateProjectDueDate(int id, Date due) {
        String sql = "UPDATE PROJECTS SET DUE=? WHERE ID=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setDate(1, new java.sql.Date(due.getTime()));
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Update the project description
     *
     * @param description
     * @param title
     */
    public void updateProjectDescription(String title, String description) {
        String sql = "SELECT ID FROM PROJECTS WHERE NAME=?";
        ResultSet resultSet;
        int id = 0;
        id = getId(title, sql, id);
        String sql2 = "UPDATE PROJECTS SET DESCRIPTION=? WHERE ID=?";
        Query(id, description, sql2);
    }

    public String getProjectDescription(String name) {
        String sql = "SELECT DESCRIPTION FROM PROJECTS WHERE NAME=?";
        String description = "";
        ResultSet resultSet;
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while ((resultSet.next())) {
                description = resultSet.getString("DESCRIPTION");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return description;

    }

    public ArrayList getProjectDates(String name) {
        String sql = "SELECT DUE, DATE_CREATED FROM PROJECTS WHERE NAME=?";
        ArrayList arrayList = new ArrayList();
        ResultSet resultSet;
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while ((resultSet.next())) {
                arrayList.add(resultSet.getDate("DATE_CREATED"));
                arrayList.add(resultSet.getDate("DUE"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return arrayList;

    }

    /**
     * Mark project as complete
     *
     * @param id
     */
    public void markProjectAsComplete(int id) {
        String sql = "UPDATE PROJECTS SET COMPLETE=? WHERE ID=?";
        markComplete(id, sql);
    }

    /**
     * Mark project as incomplete
     *
     * @param projectId
     */
    public void markProjectAsIncomplete(int projectId) {
        String sql = "UPDATE PROJECTS SET COMPLETE=? WHERE ID=?";
        markIncomplete(projectId, sql);
    }

    /**
     * @param projectId
     * @param sql
     */
    private void markIncomplete(int projectId, String sql) {
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, projectId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * delete a project completely
     *
     * @param name
     */
    public void eraseProject(String name) {
        String sql = "DELETE FROM PROJECTS WHERE NAME=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        String sql2 = "DELETE FROM TRASH_PROJECTS WHERE PROJECT_NAME=?";
        try (Connection conn2 = this.connect();
             PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
            preparedStatement2.setString(1, name);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * delete a note completely
     *
     * @param title
     */
    public void eraseNote(String title) {
        String sql = "DELETE FROM NOTES WHERE TITLE=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        String sql2 = "DELETE FROM TRASH_NOTES WHERE NOTE_TITLE=?";
        try (Connection conn2 = this.connect();
             PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
            preparedStatement2.setString(1, title);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * delete a note completely
     *
     * @param name
     */
    public void eraseTask(String name) {
        String sql2 = "DELETE FROM TRASH_TASKS WHERE TASK_NAME=?";
        try (Connection conn2 = this.connect();
             PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
            preparedStatement2.setString(1, name);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        String sql = "DELETE FROM TASKS WHERE NAME=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Empty trash
     */
    public void emptyProjectTrash() {
        String sql = "DELETE FROM TRASH_PROJECTS";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Trash a project
     *
     * @param name
     */
    public void trashProject(String name) {
        String sql = "UPDATE PROJECTS SET TRASH=? WHERE NAME=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
            String sql2 = "INSERT INTO TRASH_PROJECTS(ID,PROJECT_NAME,DATE_DELETED) VALUES(DEFAULT,?,DEFAULT)";
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(sql2)) {
                preparedStatement2.setString(1, name);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Trash a task
     *
     * @param name
     */
    public void trashTask(String name) {
        String sql = "UPDATE TASKS SET TRASHED=? WHERE NAME=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
            String getProjectID = "SELECT PROJECT_ID FROM TASKS WHERE NAME=?";
            int id = 0;
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(getProjectID)) {
                preparedStatement2.setString(1, name);
                ResultSet resultSet = preparedStatement2.executeQuery();
                if (resultSet.next()) {
                    id = resultSet.getInt("PROJECT_ID");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            String sql2 = "INSERT INTO TRASH_TASKS(ID,TASK_NAME,PROJECT_ID,DATE_DELETED) VALUES(DEFAULT,?,?,DEFAULT)";
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(sql2)) {
                preparedStatement2.setString(1, name);
                preparedStatement2.setInt(2, id);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Restore a trashed  project
     *
     * @param name
     */
    public void restoreProject(String name) {
        String sql = "UPDATE PROJECTS SET TRASH=? WHERE NAME=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
            String sql2 = "DELETE FROM TRASH_PROJECTS WHERE PROJECT_NAME=?";
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(sql2)) {
                preparedStatement2.setString(1, name);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Create a new task
     *
     * @param name
     * @param due
     * @param projectID
     */
//    public void createTask(String name, Date due, int projectID) {
//        String sql = "INSERT INTO TASKS (ID,NAME,DUE,COMPLETE,PROJECT_ID,DATE_CREATED,TRASH) VALUES (DEFAULT,?,?,DEFAULT,?,DEFAULT,DEFAULT)";
//        try (Connection conn = this.connect();
//             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
//            preparedStatement.setString(1, name);
//            preparedStatement.setDate(2, new java.sql.Date(due.getTime()));
//            preparedStatement.setInt(3, projectID);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//    }

    /**
     * Create a new task
     *
     * @param name
     * @param projectID
     */
    public void createTask(String name, int projectID, String priority) {
        String sql = "INSERT INTO TASKS (ID,NAME,COMPLETE,PROJECT_ID,DATE_CREATED,TRASHED,PRIORITY) VALUES (DEFAULT,? ,DEFAULT,?,DEFAULT,DEFAULT,?)";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, projectID);
            preparedStatement.setString(3, priority);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Update a task
     *
     * @param name   holds the name of the task
     * @param taskID
     * @param due
     */
    public void updateTask(int taskID, String name, Date due) {
        String sql = "UPDATE TASKS SET NAME=?, DUE=? WHERE ID=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setDate(2, due);
            preparedStatement.setInt(3, taskID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Update a task
     *
     * @param name
     * @param taskID
     */
    public void updateTask(int taskID, String name) {
        String sql = "UPDATE TASKS SET NAME=?, DUE=? WHERE ID=?";
        Query(taskID, name, sql);
    }

    /**
     * @param taskID
     * @param name
     * @param sql
     */
    private void Query(int taskID, String name, String sql) {
        edit(taskID, name, sql);
    }

    /**
     * Mark task as complete
     *
     * @param taskID
     */
    public void markTaskAsComplete(int taskID) {
        String sql = "UPDATE TASKS SET COMPLETE=? WHERE ID=?";
        markComplete(taskID, sql);
    }

    /**
     * Mark task/project as complete
     *
     * @param taskID
     * @param sql
     */
    private void markComplete(int taskID, String sql) {
        trash(taskID, sql);
    }

    /**
     * Mark task as incomplete
     *
     * @param taskID
     */
    public void markTaskAsIncomplete(int taskID) {
        String sql = "UPDATE TASKS SET COMPLETE=? WHERE ID=?";
        markIncomplete(taskID, sql);
    }

    /**
     * Create a new note
     *
     * @param title
     */
    public void createNote(String title) {
        String sql = "INSERT INTO NOTES(ID,TITLE,DATE_CREATED) VALUES (DEFAULT,?,DEFAULT )";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Edit note title
     *
     * @param noteID
     * @param title
     */
//  //  public void editNoteTitle(int noteID, String title) {
//        String sql = "UPDATE NOTES SET TITLE=? WHERE ID=?";
//        edit(noteID, title, sql);
//    }

    /**
     * Edit note title
     *
     * @param title
     * @param body
     */
    public void editNoteBody(String title, String body) {
        String sql = "UPDATE NOTES SET BODY=? WHERE TITLE=?";
        query5(title, body, sql);
    }

    private void query5(String title, String body, String sql) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, body);
            preparedStatement.setString(2, title);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * get note body
     *
     * @param title
     */
    public String getNoteBody(String title) {
        String sql = "SELECT BODY FROM  NOTES WHERE TITLE=?";
        String body = "";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                body = resultSet.getString("BODY");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return body;
    }

    public String getNoteDate(String title) {
        String sql = "SELECT DATE_CREATED FROM  NOTES WHERE TITLE=?";
        Date date = null;
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                date = resultSet.getDate("DATE_CREATED");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        assert date != null;
        return date.toString();
    }

    /**
     * @param id
     * @param body
     * @param sql
     */
    private void edit(int id, String body, String sql) {
        anotherQuery(id, body, sql);
    }

    /**
     * @param title
     */
    public void trashNote(String title) {
        String sql = "UPDATE NOTES SET TRASHED=? WHERE TITLE=?";
        ;
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, title);
            preparedStatement.executeUpdate();
            int id = 0;
            String sql2 = "INSERT INTO TRASH_NOTES(ID,NOTE_TITLE,DATE_DELETED) VALUES(DEFAULT,?,DEFAULT)";
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(sql2)) {
                preparedStatement2.setString(1, title);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private void trash(int id, String sql) {
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Restore a deleted note
     *
     * @param title
     */
    public void restoreNote(String title) {
        String sql = "UPDATE NOTES SET TRASHED=? WHERE TITLE=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, title);
            preparedStatement.executeUpdate();
            String sql2 = "DELETE FROM TRASH_NOTES WHERE NOTE_TITLE=?";
            try (Connection conn2 = this.connect();
                 PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
                preparedStatement2.setString(1, title);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void restoreTask(String name) {
        String sql = "UPDATE TASKS SET TRASHED=? WHERE NAME=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
            String sql2 = "DELETE FROM TRASH_TASKS WHERE TASK_NAME=?";
            try (Connection conn2 = this.connect();
                 PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
                preparedStatement2.setString(1, name);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
//            String sql3 = "SELECT PROJECT_NAME FROM TRASH_TASKS WHERE TASK_NAME=?";
//            try (Connection conn3 = this.connect();
//                 PreparedStatement preparedStatement3 = conn3.prepareStatement(sql3)) {
//                preparedStatement3.setString(1, name);
//                preparedStatement3.executeUpdate();
//            } catch (SQLException e) {
//                System.out.println(e.getMessage());
//            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Empty notes trash
     */
    public void emptyNotesTrash() {
        String sql = "DELETE FROM TRASH_NOTES";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Empty tasks trash
     */
    public void emptyTasksTrash() {
        String sql = "DELETE FROM TRASH_TASKS";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Fetch all project tiles
     */
    public ArrayList loadUnlockedProjectTitles() {
        String sql = "SELECT * FROM PROJECTS WHERE TRASH=0 AND LOCKED=0";
        ResultSet resultSet;
        ArrayList arrayList = new ArrayList();
        projectTitles(sql, arrayList);
        return arrayList;
    }

    private void projectTitles(String sql, ArrayList arrayList) {
        ResultSet resultSet;
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("NAME");
                int id = resultSet.getInt("ID");
                arrayList.add(name);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList loadAllProjectTitles() {
        String sql = "SELECT * FROM PROJECTS WHERE TRASH=0 ";
        ResultSet resultSet;
        ArrayList arrayList = new ArrayList();
        projectTitles(sql, arrayList);
        return arrayList;
    }

    public ArrayList loadNoteTitles() {
        String sql = "SELECT * FROM NOTES WHERE TRASHED=0 AND LOCKED=0";
        ResultSet resultSet;
        ArrayList arrayList = new ArrayList();
        noteTitles(sql, arrayList);
        return arrayList;
    }

    public ArrayList loadAllNoteTitles() {
        String sql = "SELECT * FROM NOTES WHERE TRASHED=0";
        ResultSet resultSet;
        ArrayList arrayList = new ArrayList();
        noteTitles(sql, arrayList);
        return arrayList;
    }

    private void noteTitles(String sql, ArrayList arrayList) {
        ResultSet resultSet;
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String title = resultSet.getString("TITLE");
                int id = resultSet.getInt("ID");
                arrayList.add(title);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int getItemId(String name) {
        String sql = "SELECT ID FROM PROJECTS WHERE NAME=?";
        ResultSet resultSet;
        int id = 0;

        id = getId(name, sql, id);
        return id;
    }

    private int getId(String name, String sql, int id) {
        ResultSet resultSet;
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                id = resultSet.getInt("ID");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return id;
    }

    //fetch the tasks of a project
    public ArrayList<ArrayList> fetchTasks(int id) {
        String sql = "SELECT * FROM TASKS WHERE PROJECT_ID=? AND TRASHED=0";

        ArrayList<ArrayList> arrayLists = new ArrayList<>();
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(resultSet.getInt("ID"));
                arrayList.add(resultSet.getString("NAME"));
                arrayList.add(resultSet.getInt("COMPLETE"));
                arrayList.add(resultSet.getString("DATE_CREATED"));
                arrayList.add(resultSet.getString("PRIORITY"));
                arrayLists.add(arrayList);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return arrayLists;
    }

    //change Priority of a taks
    public void changePriority(int id, String priority) {
        String sql = "UPDATE TASKS SET PRIORITY=? WHERE ID=?";
        anotherQuery(id, priority, sql);
    }

    private void anotherQuery(int id, String priority, String sql) {
        query3(id, priority, sql);
    }

    public void editTaskName(int id, String name) {
        String sql = "UPDATE TASKS SET NAME=? WHERE ID=?";
        query3(id, name, sql);
    }

    private void query3(int id, String name, String sql) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void editProjectName(String oldName, String newName) {
        String sql = "UPDATE PROJECTS SET NAME=? WHERE NAME=?";
        query4(oldName, newName, sql);
    }

    public void editNoteTitle(String oldName, String newName) {
        String sql = "UPDATE NOTES SET TITLE=? WHERE TITLE=?";
        query4(oldName, newName, sql);
    }

    private void query4(String oldName, String newName, String sql) {
        query5(oldName, newName, sql);
    }

    public ArrayList<ArrayList> fetchTrashedProjects() {
        String sql = "SELECT * FROM TRASH_PROJECTS";
        ArrayList arrayList = new ArrayList();
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 1;
            while (resultSet.next()) {
                ArrayList arrayList1 = new ArrayList();
                arrayList1.add(count++);
                arrayList1.add(resultSet.getString("PROJECT_NAME"));
                arrayList1.add(resultSet.getString("DATE_DELETED"));
                arrayList.add(arrayList1);
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return arrayList;
    }

    public ArrayList<ArrayList> fetchTrashedNotes() {
        String sql = "SELECT * FROM TRASH_NOTES";
        ArrayList arrayList = new ArrayList();
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 1;
            while (resultSet.next()) {
                ArrayList arrayList1 = new ArrayList();
                arrayList1.add(count++);
                arrayList1.add(resultSet.getString("NOTE_TITLE"));
                arrayList1.add(resultSet.getString("DATE_DELETED"));
                arrayList.add(arrayList1);
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return arrayList;
    }

    public ArrayList<ArrayList> fetchTrashedTasks() {
        String sql = "SELECT * FROM TRASH_TASKS";
        ArrayList arrayList = new ArrayList();
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 1;
            while (resultSet.next()) {
                ArrayList arrayList1 = new ArrayList();
                arrayList1.add(count++);
                int id = resultSet.getInt("PROJECT_ID");
                arrayList1.add(resultSet.getString("TASK_NAME"));
                arrayList1.add(resultSet.getString("DATE_DELETED"));
                String getProjectName = "SELECT NAME FROM PROJECTS WHERE ID=?";
                try (Connection connection1 = this.connect();
                     PreparedStatement preparedStatement1 = connection1.prepareStatement(getProjectName)) {
                    preparedStatement1.setInt(1, id);
                    ResultSet resultSet1 = preparedStatement1.executeQuery();
                    while (resultSet1.next()) {
                        arrayList1.add(resultSet1.getString("NAME"));
                    }
                }
                arrayList.add(arrayList1);
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return arrayList;
    }

    /**
     * Add a new password
     *
     * @param password
     */
    public void addPassword(String password) {
        String sql = "INSERT INTO PASSWORD(SALT) VALUES(?)";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, passwordAuthentication.hash(password.toCharArray()));
            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.getMessage();
        }
    }

    /**
     * Determine if a password exists
     *
     * @return boolean
     */
    public boolean isPassword() {
        String sql = "SELECT SALT FROM PASSWORD";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException exception) {
            exception.getMessage();
        }
        return false;
    }

    /**
     * Confirm if input password is current password
     *
     * @return boolean
     */
    public boolean isCurrentPassword(String password) {
        String sql = "SELECT SALT FROM PASSWORD";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String currentPassword = resultSet.getString("SALT");
                if (passwordAuthentication.authenticate(password.toCharArray(), currentPassword))
                    return true;
            }

        } catch (SQLException exception) {
            exception.getMessage();
        }
        return false;
    }

    /**
     * Change password
     *
     * @param password
     */
    public void changePassword(String password) {
        String sql = "UPDATE PASSWORD  SET SALT=?";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, passwordAuthentication.hash(password.toCharArray()));
            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.getMessage();
        }
    }

    public void lockProject(String name) {
        String sql = "UPDATE PROJECTS SET LOCKED=1 WHERE NAME=?";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.getMessage();
        }

    }

    public void lockNote(String title) {
        String sql = "UPDATE NOTES SET LOCKED=1 WHERE TITLE=?";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.getMessage();
        }

    }

    public boolean isProjectExists(String name) {
        String sql = "SELECT NAME FROM PROJECTS WHERE NAME=?";
        if (isExists(name, sql)) return true;
        return false;
    }
    public boolean isNoteExists(String title) {
        String sql = "SELECT TITLE FROM NOTES WHERE TITLE=?";
        if (isExists(title, sql)) return true;
        return false;
    }

    private boolean isExists(String title, String sql) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return true;
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return false;
    }

    public boolean isTaskExists(String name) {
        String sql = "SELECT NAME FROM TASKS WHERE NAME=? ";
        if (isExists(name, sql)) return true;
        return false;
    }
}
