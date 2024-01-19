import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;


// JPanel is extended to create a custom task component
class Task extends JPanel {

    JLabel index; // Displays the task index
    JTextField taskName; // Input field for the task name
    JButton done; // Button to mark the task as done

    Color pink = new Color(255, 161, 161); // Color for the task
    Color green = new Color(188, 226, 158); // Color for completed tasks
    Color doneColor = new Color(233, 119, 119); // Color for the "Done" button

    private boolean checked; // Indicates if the task is completed

    Task() {
        // Set properties for the task panel
        this.setPreferredSize(new Dimension(400, 20));
        this.setBackground(pink);
        this.setLayout(new BorderLayout());

        // Initialize task components
        checked = false;
        index = new JLabel("");
        index.setPreferredSize(new Dimension(20, 20));
        index.setHorizontalAlignment(JLabel.CENTER);
        this.add(index, BorderLayout.WEST);

        taskName = new JTextField("Write something..");
        taskName.setBorder(BorderFactory.createEmptyBorder());
        taskName.setBackground(pink);
        this.add(taskName, BorderLayout.CENTER);

        done = new JButton("Done");
        done.setPreferredSize(new Dimension(80, 20));
        done.setBorder(BorderFactory.createEmptyBorder());
        done.setBackground(doneColor);
        done.setFocusPainted(false);
        this.add(done, BorderLayout.EAST);

        // Attach listeners to the "Done" button
        addListeners();
    }

    private void addListeners() {
        done.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                changeState(); // Mark the task as done
                saveToDatabase(); // Save the task to the database
            }
        });
    }

    // Update the displayed index of the task
    public void changeIndex(int num) {
        this.index.setText(num + ""); // Convert num to String
        this.revalidate(); // Refresh the UI
    }

    // Getter for the "Done" button
    public JButton getDone() {
        return done;
    }

    // Getter for the completion state of the task
    public boolean getState() {
        return checked;
    }

    // Visually indicate that the task is completed
    public void changeState() {
        this.setBackground(green);
        taskName.setBackground(green);
        checked = true;
        revalidate(); // Refresh the UI
    }

    // Save the task to the database
    //The saveToDatabase() method connects to a MySQL database, inserts a new task with its name and completion status. 
    //If the task is marked as completed, it retrieves the generated task ID, then updates the database to reflect the completion status.
    //It manages potential SQL exceptions by printing stack traces for troubleshooting.
    public void saveToDatabase() {
        String jdbcurl="jdbc:mysql://localhost:3306/mytodolist";
        String username="root";
        String password="pass123";
        try (Connection connection = DriverManager.getConnection(jdbcurl, username, password)) {
            String sql = "INSERT INTO tasks (task_name, is_completed) VALUES (?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(sql)) {
                insertStatement.setString(1, taskName.getText());
                insertStatement.setBoolean(2, checked);
                insertStatement.executeUpdate();
                if (checked) {
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int taskId = generatedKeys.getInt(1);
                        updateIsCompleted(connection, taskId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    // Update the completion state in the database

    // The updateIsCompleted method updates the completion status of a task in the database to true based on its unique identifier (task ID). 
    // It utilizes a prepared statement to execute an SQL update, 
    // setting the completion status to true for the specified task ID. 
    // Any potential SQL exceptions are handled by throwing a SQLException

    private void updateIsCompleted(Connection connection, int taskId) throws SQLException {
        String updateSql = "UPDATE tasks SET is_completed = true WHERE id = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setInt(1, taskId);
            updateStatement.executeUpdate();
        }
    }
}

class List extends JPanel {

    Color lightColor = new Color(252, 221, 176);

    List() {

        GridLayout layout = new GridLayout(10, 1);
        layout.setVgap(5); // Vertical gap

        this.setLayout(layout); 
        this.setPreferredSize(new Dimension(400, 560));
        this.setBackground(lightColor);
    }

    // The updateNumbers method iterates through the components of the List class. 
    // For each component that is an instance of the Task class, it calls the changeIndex method on that Task,
    //  updating its index based on its position in the list. 
    //  This method ensures that the displayed index of each task reflects its current order within the list.
    public void updateNumbers() {
        Component[] listItems = this.getComponents();

        for (int i = 0; i < listItems.length; i++) {
            if (listItems[i] instanceof Task) {
                ((Task) listItems[i]).changeIndex(i + 1);
            }
        }

    }


    public void removeCompletedTasks() {

        for (Component c : getComponents()) {
            if (c instanceof Task) {
                if (((Task) c).getState()) {
                    remove(c); // remove the component
                    updateNumbers(); // update the indexing of all items
                }
            }
        }

    }
}

class Footer extends JPanel {

    JButton addTask;
    JButton clear;

    Color orange = new Color(233, 133, 128);
    Color lightColor = new Color(252, 221, 176);
    Border emptyBorder = BorderFactory.createEmptyBorder();

    Footer() {
        this.setPreferredSize(new Dimension(400, 60));
        this.setBackground(lightColor);

        addTask = new JButton("Add Task"); // add task button
        addTask.setBorder(emptyBorder); // remove border
        addTask.setFont(new Font("Sans-serif", Font.ITALIC, 20)); // set font
        addTask.setVerticalAlignment(JButton.BOTTOM); // align text to bottom
        addTask.setBackground(orange); // set background color
        this.add(addTask); // add to footer

        this.add(Box.createHorizontalStrut(20)); // Space between buttons

        clear = new JButton("Clear finished tasks"); // clear button
        clear.setFont(new Font("Sans-serif", Font.ITALIC, 20)); // set font
        clear.setBorder(emptyBorder); // remove border
        clear.setBackground(orange); // set background color
        this.add(clear); // add to footer
    }

    public JButton getNewTask() {
        return addTask;
    }

    public JButton getClear() {
        return clear;
    }
}

class TitleBar extends JPanel {

    Color lightColor = new Color(252, 221, 176);

    TitleBar() {
        this.setPreferredSize(new Dimension(400, 80)); // Size of the title bar
        this.setBackground(lightColor); // Color of the title bar
        JLabel titleText = new JLabel("To Do List"); // Text of the title bar
        titleText.setPreferredSize(new Dimension(200, 60)); // Size of the text
        titleText.setFont(new Font("Sans-serif", Font.BOLD, 20)); // Font of the text
        titleText.setHorizontalAlignment(JLabel.CENTER); // Align the text to the center
        this.add(titleText); // Add the text to the title bar
    }
}

class AppFrame extends JFrame {

    private TitleBar title;
    private Footer footer;
    private List list;

    private JButton newTask;
    private JButton clear;

    AppFrame() {
        this.setSize(400, 600); // 400 width and 600 height
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit
        this.setVisible(true); // Make visible

        title = new TitleBar();
        footer = new Footer();
        list = new List();

        this.add(title, BorderLayout.NORTH); // Add title bar on top of the screen
        this.add(footer, BorderLayout.SOUTH); // Add footer on bottom of the screen
        this.add(list, BorderLayout.CENTER); // Add list in middle of footer and title

        newTask = footer.getNewTask();
        clear = footer.getClear();

        addListeners();
    }

    // The addListeners method sets up mouse press listeners for the "Add Task" and "Clear finished tasks" buttons. 
    // When the "Add Task" button is pressed, a new Task is created, added to the list, and its state is updated. 
    // The "Done" button within each task is also configured to change the task's state, update the list numbers,
    // and revalidate the frame. The "Clear finished tasks" button triggers the removal of completed tasks from the
    // list and updates the display, while also clearing completed tasks from the database.

    public void addListeners() {
        newTask.addMouseListener(new MouseAdapter() {
            @override
            public void mousePressed(MouseEvent e) {
                Task task = new Task();
                list.add(task); // Add new task to list
                list.updateNumbers(); // Updates the numbers of the tasks

                task.getDone().addMouseListener(new MouseAdapter() {
                    @override
                    public void mousePressed(MouseEvent e) {

                        task.changeState(); // Change color of task
                        list.updateNumbers(); // Updates the numbers of the tasks
                        revalidate(); // Updates the frame

                    }
                });
                saveTaskToDatabase(task);
            }

        });

        clear.addMouseListener(new MouseAdapter() {
            @override
            public void mousePressed(MouseEvent e) {
                list.removeCompletedTasks(); // Removes all tasks that are done
                clearCompletedTasksFromDatabase();
                repaint(); // Repaints the list
            }
        });
    }
    private void saveTaskToDatabase(Task task) {
        // Call the method to save the task to the database
        task.saveToDatabase();
    }

    // The clearCompletedTasksFromDatabase method establishes a connection to a MySQL database named "mytodolist" and 
    // deletes all rows from the "tasks" table where the column "is_completed" has a value of 1 (indicating completed tasks). 
    // The method utilizes JDBC (Java Database Connectivity) to interact with the database. 
    // If any SQLException occurs during the execution, the method prints the stack trace for error handling. Overall, 
    // this method clears completed tasks from the associated MySQL database.

    private void clearCompletedTasksFromDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mytodolist", "root", "pass123")) {
            String deleteSql = "DELETE FROM tasks WHERE is_completed = 1";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

public class ToDoList {

    public static void main(String args[]) {
        AppFrame frame = new AppFrame(); // Create the frame
    }
}

@interface override {

}
