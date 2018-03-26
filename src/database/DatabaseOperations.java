package database;


import java.sql.*;
import java.util.ArrayList;

public class DatabaseOperations {
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
     * @param id
     */
    public void deleteProject(int id) {
        String sql = "DELETE FROM PROJECTS WHERE ID=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Empty trash
     *
     * @param id
     */
    public void emptyProjectTrash(int id) {
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
     * @param id
     * @param comment
     */
    public void trashProject(int id, String comment) {
        String sql = "UPDATE PROJECTS SET TRASH=? WHERE ID=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            String sql2 = "INSERT INTO TRASH_PROJECTS(ID,PROJECT_ID,DATE_DELETED,COMMENT) VALUES(DEFAULT,?,DEFAULT,?)";
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(sql2)) {
                preparedStatement2.setInt(1, id);
                preparedStatement2.setString(2, comment);
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
     * @param id
     * @param projectId
     */
    public void restoreProject(int id, int projectId) {
        String sql = "UPDATE PROJECTS SET TRASH=? WHERE ID=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, projectId);
            preparedStatement.executeUpdate();
            String sql2 = "DELETE FROM TRASH_PROJECTS WHERE ID=?";
            try (PreparedStatement preparedStatement2 = conn.prepareStatement(sql2)) {
                preparedStatement2.setInt(1, id);
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
    public void createTask(String name, int projectID) {
        String sql = "INSERT INTO TASKS (ID,NAME,COMPLETE,PROJECT_ID,DATE_CREATED,TRASH) VALUES (DEFAULT,? ,DEFAULT,?,DEFAULT,DEFAULT)";
        Query(projectID, name, sql);
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
    public void editNoteTitle(int noteID, String title) {
        String sql = "UPDATE NOTES SET TITLE=? WHERE ID=?";
        edit(noteID, title, sql);
    }

    /**
     * Edit note title
     *
     * @param noteID
     * @param body
     */
    public void editNoteBody(int noteID, String body) {
        String sql = "UPDATE NOTES SET BODY=? WHERE ID=?";
        edit(noteID, body, sql);
    }

    /**
     * @param id
     * @param body
     * @param sql
     */
    private void edit(int id, String body, String sql) {
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, body);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param id
     */
    public void trashNote(int id) {
        String sql = "UPDATE NOTES SET TRASHED=? WHERE ID=?";
        trash(id, sql);
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
     * @param id
     */
    public void restoreNote(int id) {
        String sql = "UPDATE NOTES SET TRASHED=? WHERE ID=?";
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            String sql2 = "DELETE FROM TRASH_NOTES WHERE ID=?";
            try (Connection conn2 = this.connect();
                 PreparedStatement preparedStatement2 = conn2.prepareStatement(sql2)) {
                preparedStatement2.setInt(1, id);
                preparedStatement2.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
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
     * Fetch all project tiles
     */
    public ArrayList loadProjectTitles() {
        String sql = "SELECT * FROM PROJECTS";
        ResultSet resultSet;
        ArrayList arrayList = new ArrayList();

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
        return arrayList;
    }

    public ArrayList loadNoteTitles() {
        String sql = "SELECT * FROM NOTES";
        ResultSet resultSet = null;
        ArrayList arrayList = new ArrayList();

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
        return arrayList;
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
        String sql = "SELECT * FROM TASKS WHERE PROJECT_ID=?";
        ResultSet resultSet = null;
        ArrayList<ArrayList> arrayLists=new ArrayList<>();
        try (Connection conn = this.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ArrayList arrayList=new ArrayList();
                arrayList.add(resultSet.getInt("ID"));
                arrayList.add(resultSet.getString("NAME"));
                arrayList.add(resultSet.getInt("COMPLETE"));
                arrayList.add(resultSet.getString("DATE_CREATED"));
                arrayLists.add(arrayList);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return arrayLists;
    }


}
