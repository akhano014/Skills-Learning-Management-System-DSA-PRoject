import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DeleteTopic extends JFrame {
    private JPanel DeletePanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel CoursesLogo;
    private JLabel UserLabel;
    private JLabel TopicL;
    private JPanel MainPanel;
    private JLabel TppicNL;
    private JButton Deletetn;
    private JButton LogoutBtn;
    private JButton Menubtn;
    private JPanel DMainPanel;
    private JLabel SelectCourseL;
    private JComboBox<String> CourseCombo;
    private JComboBox<String> TopicCombo;

    // Map to store topic ID by topic display string (for deletion)
    private Map<String, Integer> topicIdMap;

    public DeleteTopic(){
        setTitle("Delete Topic");
        setContentPane(DeletePanel);
        setSize(700, 330);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Display username from session
        System.out.println("=== Delete Topic Form Initialized ===");
        System.out.println("Current User: " + SessionSaving.getDisplayName());
        UserLabel.setText(SessionSaving.getDisplayName());

        // Initialize topic ID map
        topicIdMap = new HashMap<>();

        // Load courses into ComboBox
        loadCoursesIntoComboBox();

        // Setup cascading dropdown (Course -> Topics)
        setupCascadingDropdown();

        // Setup button actions
        setupButtonActions();

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Window closing event triggered.");
                int choice = JOptionPane.showConfirmDialog(
                        DeleteTopic.this,
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

        System.out.println("=== Delete Topic Form Ready ===\n");
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

    // Setup cascading dropdown: When course is selected, load its topics
    private void setupCascadingDropdown() {
        System.out.println("--- Setting up Cascading Dropdown ---");

        CourseCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCourse = (String) CourseCombo.getSelectedItem();

                if (selectedCourse != null && !selectedCourse.equals("Select one...")) {
                    System.out.println("\n>>> Course Selected: " + selectedCourse);
                    loadTopicsForCourse(selectedCourse);
                } else {
                    System.out.println("\n>>> Course ComboBox reset to default");
                    // Clear topic combo when no course selected
                    TopicCombo.removeAllItems();
                    TopicCombo.addItem("Select course first...");
                    topicIdMap.clear();
                }
            }
        });

        // Initialize TopicCombo with placeholder
        TopicCombo.removeAllItems();
        TopicCombo.addItem("Select course first...");

        System.out.println("Cascading dropdown configured successfully.\n");
    }

    // Load topics for selected course into TopicCombo
    private void loadTopicsForCourse(String courseName) {
        System.out.println("--- Loading Topics for Course: " + courseName + " ---");
        TopicCombo.removeAllItems();
        TopicCombo.addItem("Select one...");
        topicIdMap.clear();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            System.out.println("Database connection established.");

            // Get topics for the selected course
            String sql = "SELECT id, topic_name, difficulty FROM courses WHERE course_name = ? ORDER BY topic_name";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courseName);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int topicId = rs.getInt("id");
                String topicName = rs.getString("topic_name");
                String difficulty = rs.getString("difficulty");

                // Create display string: "Topic Name (Difficulty)"
                String displayString = topicName + " (" + difficulty + ")";

                TopicCombo.addItem(displayString);
                topicIdMap.put(displayString, topicId);
                count++;
                System.out.println("  Loaded topic [ID: " + topicId + "]: " + displayString);
            }

            System.out.println("Total topics loaded for '" + courseName + "': " + count);

            if (count == 0) {
                System.out.println("WARNING: No topics found for course '" + courseName + "'");
                TopicCombo.removeAllItems();
                TopicCombo.addItem("No topics available");
                JOptionPane.showMessageDialog(this,
                        "No topics found for course '" + courseName + "'!\nPlease add topics first.",
                        "No Topics",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            System.err.println("ERROR: Failed to load topics from database.");
            System.err.println("SQL Exception: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading topics from database:\n" + ex.getMessage(),
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
        System.out.println("--- Topic Loading Complete ---\n");
    }

    // Setup button action listeners
    private void setupButtonActions() {
        System.out.println("--- Setting up Button Actions ---");

        // Delete button - Delete selected topic with confirmation
        Deletetn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("\n=== Delete Button Clicked ===");
                deleteTopic();
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
                        DeleteTopic.this,
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

    // Delete selected topic after confirmation
    private void deleteTopic() {
        // Get selected values
        String selectedCourse = (String) CourseCombo.getSelectedItem();
        String selectedTopic = (String) TopicCombo.getSelectedItem();

        System.out.println("--- Validating Selection ---");
        System.out.println("Selected Course: " + selectedCourse);
        System.out.println("Selected Topic: " + selectedTopic);

        // Validation
        if (selectedCourse == null || selectedCourse.equals("Select one...")) {
            System.out.println("VALIDATION FAILED: No course selected.");
            JOptionPane.showMessageDialog(this,
                    "Please select a course!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedTopic == null || selectedTopic.equals("Select one...") ||
            selectedTopic.equals("Select course first...") || selectedTopic.equals("No topics available")) {
            System.out.println("VALIDATION FAILED: No topic selected.");
            JOptionPane.showMessageDialog(this,
                    "Please select a topic to delete!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get topic ID from map
        Integer topicId = topicIdMap.get(selectedTopic);
        if (topicId == null) {
            System.err.println("ERROR: Topic ID not found in map for: " + selectedTopic);
            JOptionPane.showMessageDialog(this,
                    "Error: Unable to identify topic ID!",
                    "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Validation passed.");
        System.out.println("Topic ID to delete: " + topicId);

        // Confirmation dialog
        System.out.println("\n--- Showing Confirmation Dialog ---");
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this topic?\n\n" +
                "Course: " + selectedCourse + "\n" +
                "Topic: " + selectedTopic + "\n\n" +
                "This action cannot be undone!",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            System.out.println("Deletion cancelled by user.");
            return;
        }

        System.out.println("Deletion confirmed. Proceeding to delete...");

        // Delete from database
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DB.getConnection();
            System.out.println("Database connection established.");

            String sql = "DELETE FROM courses WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, topicId);

            System.out.println("Executing DELETE query for ID: " + topicId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("\nSUCCESS: Topic deleted successfully!");
                System.out.println("  Course: " + selectedCourse);
                System.out.println("  Topic: " + selectedTopic);
                System.out.println("  Topic ID: " + topicId);
                System.out.println("  Rows Deleted: " + rowsAffected);

                JOptionPane.showMessageDialog(this,
                        "Topic deleted successfully!\n\n" +
                        "Course: " + selectedCourse + "\n" +
                        "Topic: " + selectedTopic,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reload topics for the current course
                System.out.println("\nReloading topics for course: " + selectedCourse);
                loadTopicsForCourse(selectedCourse);

            } else {
                System.out.println("WARNING: No rows were deleted. Topic may not exist.");
                JOptionPane.showMessageDialog(this,
                        "Topic was not deleted!\nIt may have already been removed.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            System.err.println("\nERROR: Failed to delete topic from database.");
            System.err.println("SQL Exception: " + ex.getMessage());
            System.err.println("SQL State: " + ex.getSQLState());
            System.err.println("Error Code: " + ex.getErrorCode());
            ex.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Error deleting topic from database:\n" + ex.getMessage(),
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
        System.out.println("=== Delete Process Complete ===\n");
    }

    public static void showDeteletopic(){
        System.out.println("\n>>> Opening Delete Topic Form <<<");
        SwingUtilities.invokeLater(() -> {
            DeleteTopic deleteTopic=new DeleteTopic();
            deleteTopic.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showDeteletopic();
        });
    }
}
