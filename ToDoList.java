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
class Task extends JPanel {

    JLabel index;
    JTextField taskName;
    JButton done;

    Color pink = new Color(255, 161, 161);
    Color green = new Color(188, 226, 158);
    Color doneColor = new Color(233, 119, 119);

    private boolean checked;

    Task() {
        this.setPreferredSize(new Dimension(400, 20)); // set size of task
        this.setBackground(pink); // set background color of task

        this.setLayout(new BorderLayout()); // set layout of task

        checked = false;

        index = new JLabel(""); // create index label
        index.setPreferredSize(new Dimension(20, 20)); // set size of index label
        index.setHorizontalAlignment(JLabel.CENTER); // set alignment of index label
        this.add(index, BorderLayout.WEST); // add index label to task

        taskName = new JTextField("Write something.."); // create task name text field
        taskName.setBorder(BorderFactory.createEmptyBorder()); // remove border of text field
        taskName.setBackground(pink); // set background color of text field

        this.add(taskName, BorderLayout.CENTER);

        done = new JButton("Done");
        done.setPreferredSize(new Dimension(80, 20));
        done.setBorder(BorderFactory.createEmptyBorder());
        done.setBackground(doneColor);
        done.setFocusPainted(false);

        this.add(done, BorderLayout.EAST);
        
        addListeners();
    }
    private void addListeners() {
        done.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                changeState(); // Change state when "Done" is clicked
                saveToDatabase(); // Save the task to the database
            }
        });
    }

    public void changeIndex(int num) {
        this.index.setText(num + ""); // num to String
        this.revalidate(); // refresh
    }

    public JButton getDone() {
        return done;
    }

    public boolean getState() {
        return checked;
    }

    public void changeState() {
        this.setBackground(green);
        taskName.setBackground(green);
        checked = true;
        revalidate();
    }
    public void saveToDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mytodolist", "root", "pass123")) {
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

        this.setLayout(layout); // 10 tasks
        this.setPreferredSize(new Dimension(400, 560));
        this.setBackground(lightColor);
    }

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