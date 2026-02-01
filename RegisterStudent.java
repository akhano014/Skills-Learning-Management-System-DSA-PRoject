import javax.swing.*;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class RegisterStudent extends JFrame {
    private JPanel Registerpanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel CoursesLogo;
    private JLabel UserLabel;
    private JLabel CoursesL;
    private JPanel MainPanel;
    private JButton ApprovedtBtn;
    private JButton Menubtn;
    private JButton LogoutBtn;
    private JPanel RegisterStudentP;
    private JTextArea StudentsArea;
    private JLabel ArticleNL;
    private JLabel SStudentsL;
    private JComboBox StudentsCombo;

    // Store student data for approval
    private Map<String, StudentData> pendingStudents = new HashMap<>();

    private user loggedInAdmin;

    // Helper class to store student information
    private static class StudentData {
        int id;
        String name;
        String email;
        String phone;
        String password;
        String status;
        String requestedAt;

        public StudentData(int id, String name, String email, String phone, String password, String status, String requestedAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.status = status;
            this.requestedAt = requestedAt;
        }
    }

    public RegisterStudent(user admin) {
        this.loggedInAdmin = admin;

        setTitle("Students Registration");
        setContentPane(Registerpanel);
        setSize(800, 500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set monospace font for better column alignment
        StudentsArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        StudentsArea.setEditable(false);

        // Load pending students when form opens
        loadPendingStudents();

        // ✅ Add F5 key binding to refresh the list
        getRootPane().registerKeyboardAction(
            e -> {
                System.out.println("Refreshing student list (F5 pressed)...");
                loadPendingStudents();
            },
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // ✅ Add DELETE key binding to force delete a student from database
        getRootPane().registerKeyboardAction(
            e -> {
                System.out.println("DELETE key pressed - clearing Ahtisham khan...");
                clearSpecificStudent("ahtishamkhan@gmail.com");
            },
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, java.awt.event.InputEvent.CTRL_DOWN_MASK),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        RegisterStudent.this,
                        "Go back to Admin Menu?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    AdminMainmenu.showAdminMenu(loggedInAdmin);
                }
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // ✅ Auto-refresh when window gains focus
                System.out.println("Window activated - refreshing student list...");
                loadPendingStudents();
            }
        });

        ApprovedtBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Check if combo box has items
                    if (StudentsCombo.getItemCount() <= 1) {
                        JOptionPane.showMessageDialog(RegisterStudent.this,
                                "No pending students to approve!",
                                "No Students",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    Object selectedItem = StudentsCombo.getSelectedItem();
                    if (selectedItem == null) {
                        JOptionPane.showMessageDialog(RegisterStudent.this,
                                "Please select a student to approve!",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String selectedStudent = selectedItem.toString();

                    if (selectedStudent.equals("Select Student...")) {
                        JOptionPane.showMessageDialog(RegisterStudent.this,
                                "Please select a student to approve!",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Get student data
                    StudentData student = pendingStudents.get(selectedStudent);
                    if (student == null) {
                        JOptionPane.showMessageDialog(RegisterStudent.this,
                                "Student data not found!\nPlease refresh the list.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        loadPendingStudents(); // Refresh the list
                        return;
                    }

                    // Confirm approval
                    int confirm = JOptionPane.showConfirmDialog(RegisterStudent.this,
                            "Are you sure you want to approve:\n\n" +
                            "Name: " + student.name + "\n" +
                            "Email: " + student.email + "\n" +
                            "Phone: " + student.phone,
                            "Confirm Approval",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Show processing message
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        ApprovedtBtn.setEnabled(false);

                        if (approveStudent(student)) {
                            JOptionPane.showMessageDialog(RegisterStudent.this,
                                    "Student '" + student.name + "' approved successfully!\n\n" +
                                            "They can now login to the system with their email and password.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // Reload the pending students list
                            loadPendingStudents();
                        } else {
                            JOptionPane.showMessageDialog(RegisterStudent.this,
                                    "Failed to approve student.\nPlease check the console for details.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                        setCursor(Cursor.getDefaultCursor());
                        ApprovedtBtn.setEnabled(true);
                    }
                } catch (Exception ex) {
                    System.err.println("Error in approve button handler: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterStudent.this,
                            "Unexpected error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    setCursor(Cursor.getDefaultCursor());
                    ApprovedtBtn.setEnabled(true);
                }
            }
        });

        Menubtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                AdminMainmenu.showAdminMenu(loggedInAdmin);
            }
        });

        LogoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                WelcomeForm.showWelcomeForm();
            }
        });
    }

    // ✅ Method to force delete a specific student from database
    private void clearSpecificStudent(String email) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            String deleteQuery = "DELETE FROM requested_students WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setString(1, email);

            int deleted = stmt.executeUpdate();
            System.out.println("Force deleted " + deleted + " record(s) for email: " + email);

            stmt.close();
            conn.close();

            // Refresh the list
            loadPendingStudents();

            JOptionPane.showMessageDialog(this,
                    "Deleted student with email: " + email + "\nRecords deleted: " + deleted,
                    "Force Delete Complete",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            System.err.println("Error deleting student: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting student: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPendingStudents() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // ✅ Force new connection to avoid caching
            conn = DB.getConnection();

            System.out.println("=== Loading Pending Students ===");
            System.out.println("Database URL: " + DB.getUrl());
            System.out.println("Timestamp: " + new java.util.Date());

            // ✅ Enhanced query to ensure we only get truly pending students
            String query = "SELECT id, name, email, phone, password, status, requested_at " +
                          "FROM requested_students " +
                          "WHERE status = 'Pending' " +
                          "ORDER BY requested_at DESC";

            stmt = conn.prepareStatement(query,
                                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                                        ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();

            // ✅ Clear ALL previous data to prevent stale display
            pendingStudents.clear();
            StudentsCombo.removeAllItems();
            StudentsCombo.addItem("Select Student...");

            StringBuilder displayText = new StringBuilder();


            String leftMargin = " ";  // Add 1 space left margin to match right spacing

            // Header with better formatting
            displayText.append("\n");
            displayText.append(leftMargin).append("╔").append("═".repeat(98)).append("╗\n");
            displayText.append(leftMargin).append("║").append(String.format("%-98s", String.format(" %-22s %-32s %-14s %-24s ", "NAME", "EMAIL", "STATUS", "REQUESTED AT"))).append("║\n");
            displayText.append(leftMargin).append("╠").append("═".repeat(98)).append("╣\n");

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String password = rs.getString("password");
                String status = rs.getString("status");
                String requestedAt = rs.getString("requested_at");

                // ✅ Verify this is truly a pending student
                System.out.println("Loading Student #" + (count + 1) + ": ID=" + id + ", Name=" + name + ", Email=" + email + ", Status=" + status);

                // Store student data
                StudentData student = new StudentData(id, name, email, phone, password, status, requestedAt);
                pendingStudents.put(name, student);

                // Add to combo box
                StudentsCombo.addItem(name);

                // Add to display text with better formatting
                String displayName = name.length() > 21 ? name.substring(0, 18) + "..." : name;
                String displayEmail = email.length() > 31 ? email.substring(0, 28) + "..." : email;

                String rowContent = String.format(" %-22s %-32s %-14s %-24s ", displayName, displayEmail, status, requestedAt);
                displayText.append(leftMargin).append("║").append(String.format("%-98s", rowContent)).append("║\n");

                count++;
            }

            if (count == 0) {
                displayText.append(leftMargin).append("║").append(" ".repeat(98)).append("║\n");
                displayText.append(leftMargin).append("║").append(String.format("%-98s", "        No pending student registrations found.")).append("║\n");
                displayText.append(leftMargin).append("║").append(String.format("%-98s", "        All registration requests have been processed.")).append("║\n");
                displayText.append(leftMargin).append("║").append(" ".repeat(98)).append("║\n");
                displayText.append(leftMargin).append("╚").append("═".repeat(98)).append("╝");
            } else {
                // Add footer separator and close the table
                displayText.append(leftMargin).append("╠").append("═".repeat(98)).append("╣\n");
                displayText.append(leftMargin).append("║").append(String.format(" %-97s", "Total Pending Students: " + count)).append("║\n");
                displayText.append(leftMargin).append("╚").append("═".repeat(98)).append("╝");
            }

            StudentsArea.setText(displayText.toString());

            System.out.println("Total Pending Students Loaded: " + count);
            System.out.println("=== Load Complete ===\n");

            // ✅ Force UI update
            StudentsArea.revalidate();
            StudentsArea.repaint();
            StudentsCombo.revalidate();
            StudentsCombo.repaint();

        } catch (Exception ex) {
            System.err.println("Error loading pending students: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading pending students: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // ✅ Ensure all resources are properly closed
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception closeEx) {
                System.err.println("Error closing database resources: " + closeEx.getMessage());
            }
        }
    }

    private boolean approveStudent(StudentData student) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Debug: Check if loggedInAdmin is set
            System.out.println("=== DEBUG: Approving Student ===");
            System.out.println("Student Name: " + student.name);
            System.out.println("Student Email: " + student.email);
            System.out.println("Logged In Admin Object: " + (loggedInAdmin != null ? "EXISTS" : "NULL"));
            if (loggedInAdmin != null) {
                System.out.println("Admin ID: " + loggedInAdmin.id);
                System.out.println("Admin Name: " + loggedInAdmin.name);
                System.out.println("Admin Email: " + loggedInAdmin.email);
            } else {
                System.out.println("WARNING: loggedInAdmin is NULL! Cannot set approved_by!");
            }

            // Insert with approved_by column storing admin's NAME (not ID, since foreign key is dropped)
            String insertQuery = "INSERT INTO students (name, email, phone, password, role, approved_by) VALUES (?, ?, ?, ?, 'Student', ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            insertStmt.setString(1, student.name);
            insertStmt.setString(2, student.email);
            insertStmt.setString(3, student.phone);
            insertStmt.setString(4, student.password); // Password is already hashed

            // Set the admin's NAME in approved_by column
            if (loggedInAdmin != null && loggedInAdmin.name != null && !loggedInAdmin.name.trim().isEmpty()) {
                insertStmt.setString(5, loggedInAdmin.name);  // Store admin's NAME
                System.out.println("approved_by NAME to be inserted: " + loggedInAdmin.name);
            } else {
                insertStmt.setString(5, "Unknown Admin");  // Default value if admin not logged in
                System.out.println("approved_by NAME: Unknown Admin (loggedInAdmin is null or has no name)");
            }

            int inserted = insertStmt.executeUpdate();
            System.out.println("INSERT executed, rows affected: " + inserted);
            insertStmt.close();

            if (inserted > 0) {
                System.out.println("Student inserted successfully with approved_by = " +
                    (loggedInAdmin != null ? loggedInAdmin.name : "Unknown Admin"));

                // Delete from requested_students table
                String deleteQuery = "DELETE FROM requested_students WHERE id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, student.id);
                int deleted = deleteStmt.executeUpdate();
                System.out.println("Deleted from requested_students, rows affected: " + deleted);
                deleteStmt.close();

                conn.commit(); // Commit transaction
                System.out.println("Transaction committed successfully!");
                System.out.println("=== Approval Complete ===\n");
                return true;
            }

            conn.rollback();
            return false;

        } catch (Exception ex) {
            System.err.println("Error approving student: " + ex.getMessage());
            ex.printStackTrace();

            // Show detailed error to user
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Database Error:\n" + ex.getMessage() + "\n\nPlease check:\n" +
                        "1. Database connection is working\n" +
                        "2. 'students' table exists\n" +
                        "3. 'approved_by' column exists and is varchar type\n" +
                        "4. Table has required columns: name, email, phone, password, role, approved_by",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            });

            try {
                if (conn != null) {
                    conn.rollback();
                    conn.close();
                }
            } catch (Exception rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception closeEx) {
                System.err.println("Error closing connection: " + closeEx.getMessage());
            }
        }
    }

    public static void showRegisterStudent(user admin) {
        SwingUtilities.invokeLater(() -> {
            RegisterStudent form = new RegisterStudent(admin);
            form.setVisible(true);
        });
    }

    // Backward compatibility method
    public static void showRegisterStudent() {
        showRegisterStudent(null);
    }

    public static void main(String[] args) {
        showRegisterStudent();
    }
}