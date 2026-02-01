import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddTopic extends JFrame {
    private JPanel CoursePanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel CoursesLogo;
    private JLabel UserLabel;
    private JPanel MainPanel;
    private JLabel TppicNL;
    private JTextField NameTF;
    private JLabel LevelL;
    private JLabel ContentL;
    private JTextArea LectureArea;
    private JComboBox<String> DiffcultCombo;
    private JButton Feedbackbtn;
    private JButton Menubtn;
    private JLabel TopicL;
    private JButton LogoutBtn;
    private JPanel topicPanel;
    private JLabel SelectCourseL;
    private JComboBox<String> CourseCombo;

    public AddTopic(){
        setTitle("Add Topic");
        setContentPane(CoursePanel);
        setSize(700, 500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Display username from session
        System.out.println("=== Add Topic Form Initialized ===");
        System.out.println("Current User: " + SessionSaving.getDisplayName());
        UserLabel.setText(SessionSaving.getDisplayName());

        // Configure text area to prevent horizontal expansion
        LectureArea.setLineWrap(true);
        LectureArea.setWrapStyleWord(true);
        System.out.println("Text area configured with word wrap.");

        // Load courses into ComboBox
        loadCoursesIntoComboBox();

        // Setup button actions
        setupButtonActions();

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Window closing event triggered.");
                int choice = JOptionPane.showConfirmDialog(
                        AddTopic.this,
                        "Go back to Manage Course?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    System.out.println("User chose to go back to Manage Course.");
                    dispose();
                    ManageCourse.showManageCourse();
                } else {
                    System.out.println("User cancelled exit.");
                }
            }
        });

        System.out.println("=== Add Topic Form Ready ===\n");
    }

    // Load unique course names from database into CourseCombo
    private void loadCoursesIntoComboBox() {
        System.out.println("--- Loading Courses into ComboBox ---");
        CourseCombo.removeAllItems();
        CourseCombo.addItem("Select one...");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            System.out.println("Database connection established.");

            // Get distinct course names from database
            String sql = "SELECT DISTINCT course_name FROM courses ORDER BY course_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                CourseCombo.addItem(courseName);
                count++;
                System.out.println("  Loaded course: " + courseName);
            }

            System.out.println("Total courses loaded: " + count);

            if (count == 0) {
                System.out.println("WARNING: No courses found in database!");
                JOptionPane.showMessageDialog(this,
                        "No courses available!\nPlease add a course first.",
                        "No Courses",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            System.err.println("ERROR: Failed to load courses from database.");
            System.err.println("SQL Exception: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading courses from database:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    System.out.println("ResultSet closed.");
                }
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("PreparedStatement closed.");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException ex) {
                System.err.println("ERROR: Failed to close database resources.");
                ex.printStackTrace();
            }
        }
        System.out.println("--- Course Loading Complete ---\n");
    }

    // Setup button action listeners
    private void setupButtonActions() {
        System.out.println("--- Setting up Button Actions ---");

        // Submit button - Add new topic to selected course
        Feedbackbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("\n=== Submit Button Clicked ===");
                submitNewTopic();
            }
        });

        // Menu button - Go back to Manage Course
        Menubtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("\n=== Menu Button Clicked ===");
                System.out.println("Navigating back to Manage Course...");
                dispose();
                ManageCourse.showManageCourse();
            }
        });

        // Logout button - Return to Welcome Form
        LogoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("\n=== Logout Button Clicked ===");
                System.out.println("User " + SessionSaving.getDisplayName() + " is logging out.");

                int choice = JOptionPane.showConfirmDialog(
                        AddTopic.this,
                        "Are you sure you want to logout?",
                        "Logout Confirmation",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    System.out.println("Logout confirmed. Clearing session...");
                    SessionSaving.clearSession();
                    dispose();
                    WelcomeForm.showWelcomeForm();
                    System.out.println("Navigated to Welcome Form.");
                } else {
                    System.out.println("Logout cancelled.");
                }
            }
        });

        System.out.println("Button actions configured successfully.\n");
    }

    // Submit new topic to database
    private void submitNewTopic() {
        // Get input values
        String selectedCourse = (String) CourseCombo.getSelectedItem();
        String topicName = NameTF.getText().trim();
        String difficulty = (String) DiffcultCombo.getSelectedItem();
        String content = LectureArea.getText().trim();

        System.out.println("--- Validating Input ---");
        System.out.println("Selected Course: " + selectedCourse);
        System.out.println("Topic Name: " + topicName);
        System.out.println("Difficulty: " + difficulty);
        System.out.println("Content Length: " + content.length() + " characters");

        // Validation
        if (selectedCourse == null || selectedCourse.equals("Select one...")) {
            System.out.println("VALIDATION FAILED: No course selected.");
            JOptionPane.showMessageDialog(this,
                    "Please select a course!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (topicName.isEmpty()) {
            System.out.println("VALIDATION FAILED: Topic name is empty.");
            JOptionPane.showMessageDialog(this,
                    "Please enter a topic name!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (difficulty == null || difficulty.equals("Select one...")) {
            System.out.println("VALIDATION FAILED: No difficulty selected.");
            JOptionPane.showMessageDialog(this,
                    "Please select difficulty level!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (content.isEmpty()) {
            System.out.println("VALIDATION FAILED: Content is empty.");
            JOptionPane.showMessageDialog(this,
                    "Please enter topic content!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Validation passed. Proceeding to insert into database...");

        // Insert into database
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DB.getConnection();
            System.out.println("Database connection established.");

            String sql = "INSERT INTO courses (course_name, topic_name, difficulty, description) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, selectedCourse);
            pstmt.setString(2, topicName);
            pstmt.setString(3, difficulty);
            pstmt.setString(4, content);

            System.out.println("Executing INSERT query...");
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("SUCCESS: Topic added successfully!");
                System.out.println("  Course: " + selectedCourse);
                System.out.println("  Topic: " + topicName);
                System.out.println("  Difficulty: " + difficulty);
                System.out.println("  Rows Affected: " + rowsAffected);

                JOptionPane.showMessageDialog(this,
                        "Topic added successfully!\n\n" +
                        "Course: " + selectedCourse + "\n" +
                        "Topic: " + topicName + "\n" +
                        "Difficulty: " + difficulty,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Clear fields after successful insertion
                System.out.println("Clearing input fields...");
                NameTF.setText("");
                LectureArea.setText("");
                DiffcultCombo.setSelectedIndex(0);
                CourseCombo.setSelectedIndex(0);
                System.out.println("Fields cleared successfully.");
            } else {
                System.out.println("WARNING: No rows were affected. Insert may have failed.");
            }

        } catch (SQLException ex) {
            System.err.println("ERROR: Failed to add topic to database.");
            System.err.println("SQL Exception: " + ex.getMessage());
            System.err.println("SQL State: " + ex.getSQLState());
            System.err.println("Error Code: " + ex.getErrorCode());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Error adding topic to database:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("PreparedStatement closed.");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException ex) {
                System.err.println("ERROR: Failed to close database resources.");
                ex.printStackTrace();
            }
        }
        System.out.println("=== Submit Process Complete ===\n");
    }

    public static void showAddTopic(){
        System.out.println("\n>>> Opening Add Topic Form <<<");
        SwingUtilities.invokeLater(() -> {
            AddTopic addTopic = new AddTopic();
            addTopic.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showAddTopic();
        });
    }
}
