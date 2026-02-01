import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Addcourse extends JFrame {
    private JPanel CoursePanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel UserLabel;
    private JPanel MainPanel;
    private JLabel CoursesLogo;
    private JLabel CourseLabel;
    private JTextField CourseTF;
    private JTextArea LectureArea;
    private JComboBox DiffcultCombo;
    private JButton Submitbtn;
    private JLabel TopicNL;
    private JLabel LevelL;
    private JLabel ContentL;
    private JTextField NameTF;
    private JButton MianMenuBtn;
    private JLabel CoursesheadingL;

    public Addcourse() {
        setTitle("Add Course");
        setContentPane(CoursePanel);
        setSize(700, 550);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Display username from session
        UserLabel.setText(SessionSaving.getDisplayName());

        // Configure text area to prevent horizontal expansion
        LectureArea.setLineWrap(true);
        LectureArea.setWrapStyleWord(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        Addcourse.this,
                        "Go back to Admin Menu?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    AdminMainmenu.showAdminMenu();
                }
            }
        });

        Submitbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String courseName = CourseTF.getText().trim();      // CourseTF = Course Name
                String topicName = NameTF.getText().trim();         // NameTF = Topic Name
                String content = LectureArea.getText().trim();
                String difficulty = (String) DiffcultCombo.getSelectedItem();

                // Validation
                if (courseName.isEmpty() || topicName.isEmpty() || content.isEmpty()) {
                    JOptionPane.showMessageDialog(Addcourse.this,
                            "Please fill all fields!",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Check if difficulty is selected
                if (difficulty == null || difficulty.equals("Select one...")) {
                    JOptionPane.showMessageDialog(Addcourse.this,
                            "Please select difficulty level!",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Check if course already exists and insert into database
                Connection conn = null;
                ResultSet rs = null;
                PreparedStatement pstmt = null;

                try {
                    conn = DB.getConnection();

                    // First, check if course name already exists
                    String checkSql = "SELECT COUNT(*) as count FROM courses WHERE course_name = ?";
                    pstmt = conn.prepareStatement(checkSql);
                    pstmt.setString(1, courseName);
                    rs = pstmt.executeQuery();

                    if (rs.next() && rs.getInt("count") > 0) {
                        // Course already exists
                        int choice = JOptionPane.showConfirmDialog(
                                Addcourse.this,
                                "Course '" + courseName + "' already exists!\n\n" +
                                "To add a new topic to this course, please use 'Manage Course'.\n\n" +
                                "Would you like to go to Manage Course now?",
                                "Course Already Exists",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            // Close current form and open ManageCourse
                            dispose();
                            ManageCourse.showManageCourse();
                        }
                        return;
                    }

                    // Course doesn't exist, proceed with insertion
                    rs.close();
                    pstmt.close();

                    String sql = "INSERT INTO courses (course_name, topic_name, difficulty, description) VALUES (?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);

                    pstmt.setString(1, courseName);
                    pstmt.setString(2, topicName);
                    pstmt.setString(3, difficulty);
                    pstmt.setString(4, content);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(Addcourse.this,
                                "Course added successfully!\n\n" +
                                "Course: " + courseName + "\n" +
                                "Topic: " + topicName + "\n" +
                                "Difficulty: " + difficulty,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Clear fields after successful insertion
                        CourseTF.setText("");      // Clear Course Name
                        NameTF.setText("");        // Clear Topic Name
                        LectureArea.setText("");
                        DiffcultCombo.setSelectedIndex(0);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(Addcourse.this,
                            "Error adding course to database:\n" + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.err.println("Database error: " + ex.getMessage());
                } finally {
                    // Close resources
                    try {
                        if (rs != null) rs.close();
                        if (pstmt != null) pstmt.close();
                        if (conn != null) conn.close();
                    } catch (SQLException ex) {
                        System.err.println("Error closing resources: " + ex.getMessage());
                    }
                }
            }
        });

        MianMenuBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                AdminMainmenu.showAdminMenu();
            }
        });
    }

    public static void showAddCourse() {
        SwingUtilities.invokeLater(() -> {
            Addcourse form = new Addcourse();
            form.setVisible(true);
        });
    }

    public static void main(String[] args) {
        showAddCourse();
    }
}