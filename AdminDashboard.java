import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminDashboard extends JFrame {
    // Form components
    private JPanel DashboardPanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel CoursesLogo;
    private JLabel UserLabel;
    private JLabel CoursesheadingL;
    private JPanel StatisticsPanel;
    private JPanel TotalCoursesPanel;
    private JLabel CoursesHL;
    private JLabel CourseLogo;
    private JPanel TotalStudentsPanel;
    private JLabel TotalStudents;
    private JLabel StudentsLogo;
    private JPanel TotalFeedbackPanel;
    private JLabel TotalQuizezL;
    private JLabel QuizezLabel;
    private JPanel TotalQuizezPanel;
    private JLabel TotalFeedbackL;
    private JPanel CardHeadingPanel;
    private JLabel CardHeadingL;
    private JLabel DisplayTcoursesL;
    private JLabel DisplayTStudentsL;
    private JLabel DisplayTQuizezL;
    private JLabel TeedbackLogo;
    private JLabel DisplayTFeedbackL;
    private JPanel FeedbackAnalysisPanel;
    private JPanel CoursesAnalysisPanel;
    private JLabel CourseAnalysisH;
    private JLabel CourseRatedL;
    private JLabel RecentFeedbackH;
    private JScrollPane CoursesScrollPanel;
    private JTable topViewedTable;
    private JLabel FeedbackH;
    private JScrollPane FeedbackScrollPanel;
    private JTable Feedbacktable;
    private JLabel TotalStudentsH;
    private JLabel TotalQuizezH;
    private JPanel StudentsAnalysisPanel;
    private JPanel ProgressAnalysisPanel;
    private JLabel StudentsHL;
    private JScrollPane StudentsScrollPanel;
    private JTable Studentstabel;
    private JLabel TquizezH;
    private JScrollPane QzizezPanel;
    private JTable QuizezTable;
    private JButton logoutButton;
    private JButton QuickActionTbn;
    private JButton quickActionButton;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Resource Management System");
        setContentPane(DashboardPanel);
        setSize(980, 840);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Display admin username from session
        UserLabel.setText(SessionSaving.getDisplayName());

        // Initialize all data
        initializeDashboard();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeDashboard() {
        try {
            // Load statistics counts
            loadStatisticsCounts();

            // Load course analytics (top and least viewed)
            loadCourseAnalytics();

            // Load recent feedbacks
            loadRecentFeedbacks();

            // Load students list
            loadStudentsList();

            // Load quizzes list
            loadQuizzesList();

            System.out.println("Admin Dashboard loaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading dashboard data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== STATISTICS COUNTS ====================

    private void loadStatisticsCounts() {
        // Total Unique Courses
        int uniqueCoursesCount = getUniqueCoursesCount();
        DisplayTcoursesL.setText(String.valueOf(uniqueCoursesCount));

        // Total Students
        int totalStudents = getTotalStudentsCount();
        DisplayTStudentsL.setText(String.valueOf(totalStudents));

        // Total Quizzes
        int totalQuizzes = getTotalQuizzesCount();
        DisplayTQuizezL.setText(String.valueOf(totalQuizzes));

        // Total Feedbacks
        int totalFeedbacks = getTotalFeedbacksCount();
        DisplayTFeedbackL.setText(String.valueOf(totalFeedbacks));
    }

    private int getUniqueCoursesCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT COUNT(DISTINCT course_name) as total FROM courses";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting unique courses count: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return 0;
    }

    private int getTotalStudentsCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT COUNT(*) as total FROM students";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting students count: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return 0;
    }

    private int getTotalQuizzesCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT COUNT(*) as total FROM quizzes";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting quizzes count: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return 0;
    }

    private int getTotalFeedbacksCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT COUNT(*) as total FROM feedbacks";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting feedbacks count: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return 0;
    }

    // ==================== COURSE ANALYTICS ====================

    private void loadCourseAnalytics() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Course Name", "Total Views"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT c.course_name, COUNT(rv.id) as total_views " +
                    "FROM courses c " +
                    "LEFT JOIN resource_views rv ON c.id = rv.course_id " +
                    "GROUP BY c.course_name " +
                    "ORDER BY total_views DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                int totalViews = rs.getInt("total_views");
                model.addRow(new Object[]{courseName, totalViews});
            }

            topViewedTable.setModel(model);
            topViewedTable.setRowHeight(25);
            topViewedTable.getTableHeader().setReorderingAllowed(false);
            topViewedTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        } catch (SQLException e) {
            System.err.println("Error loading course analytics: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    // ==================== RECENT FEEDBACKS ====================

    private void loadRecentFeedbacks() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Student Name", "Subject", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT student_name, subject, status " +
                    "FROM feedbacks " +
                    "ORDER BY submitted_at DESC " +
                    "LIMIT 10";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String studentName = rs.getString("student_name");
                String subject = rs.getString("subject");
                String status = rs.getString("status");
                model.addRow(new Object[]{studentName, subject, status});
            }

            Feedbacktable.setModel(model);
            Feedbacktable.setRowHeight(25);
            Feedbacktable.getTableHeader().setReorderingAllowed(false);
            Feedbacktable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        } catch (SQLException e) {
            System.err.println("Error loading recent feedbacks: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    // ==================== STUDENTS LIST ====================

    private void loadStudentsList() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Student Name", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            String sql = "SELECT name, email FROM students ORDER BY name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                model.addRow(new Object[]{name, email});
            }

            Studentstabel.setModel(model);
            Studentstabel.setRowHeight(25);
            Studentstabel.getTableHeader().setReorderingAllowed(false);
            Studentstabel.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        } catch (SQLException e) {
            System.err.println("Error loading students list: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    // ==================== QUIZZES LIST ====================

    private void loadQuizzesList() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Course Name", "Topic Name", "Quiz Count"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            // Group by course and topic to show unique topics with quiz count
            String sql = "SELECT c.course_name, c.topic_name, COUNT(q.id) as quiz_count " +
                    "FROM courses c " +
                    "LEFT JOIN quizzes q ON c.id = q.course_id " +
                    "GROUP BY c.id, c.course_name, c.topic_name " +
                    "HAVING quiz_count > 0 " +
                    "ORDER BY c.course_name, c.topic_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String topicName = rs.getString("topic_name");
                int quizCount = rs.getInt("quiz_count");
                model.addRow(new Object[]{courseName, topicName, quizCount});
            }

            QuizezTable.setModel(model);
            QuizezTable.setRowHeight(25);
            QuizezTable.getTableHeader().setReorderingAllowed(false);
            QuizezTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        } catch (SQLException e) {
            System.err.println("Error loading quizzes list: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    // ==================== BUTTON LISTENERS ====================

    private void setupButtonListeners() {
        // Quick Action button - Opens Admin Main Menu
        Component[] components = DashboardPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText().equals("Quick Action")) {
                    quickActionButton = btn;
                    break;
                }
            }
        }

        if (quickActionButton != null) {
            quickActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                    AdminMainmenu.showAdminMenu();
                }
            });
        }

        // Logout button
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        AdminDashboard.this,
                        "Are you sure you want to logout?",
                        "Logout Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    SessionSaving.clearSession();
                    dispose();
                    LoginForm.showLoginForm(null);
                }
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    // ==================== STATIC METHODS ====================

    public static void showAdminDashboard() {
        SwingUtilities.invokeLater(() -> {
            try {
                AdminDashboard dashboard = new AdminDashboard();
                dashboard.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error opening Admin Dashboard: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SessionSaving.setUserSession("Admin", "Admin", 1);
            showAdminDashboard();
        });
    }
}

