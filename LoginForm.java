import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;

public class LoginForm extends JDialog {
    private JPanel loginpanel;
    private JPanel leftpanel;
    private JPanel Rightpanel;
    private JLabel Heading1;
    private JLabel Heading2;
    private JLabel logo;
    private JLabel EmailLb;
    private JTextField tfEmail;
    private JLabel PasswordLb;
    private JPasswordField passwordF1;
    private JButton btnOK;
    private JButton btnCancel;
    private JLabel SelectRole;
    private JComboBox RoleCombo;

    public user USER;

    public LoginForm(JFrame parent) {
        super(parent);
        setTitle("User Login");
        setContentPane(loginpanel);
        setMinimumSize(new Dimension(530, 530));
        setModal(true);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        LoginForm.this,
                        "Are you sure you want to exit?",
                        "Exit Login",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    USER = null;
                    SessionSaving.clearSession();
                    dispose();

                    // Safely handle owner window
                    Window owner = getOwner();
                    if (owner instanceof JFrame) {
                        ((JFrame) owner).dispose();
                    }

                    SwingUtilities.invokeLater(() -> {
                        WelcomeForm.showWelcomeForm();
                    });
                }
            }
        });

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = tfEmail.getText().trim();
                String password = String.valueOf(passwordF1.getPassword());
                String role = (String) RoleCombo.getSelectedItem();

                // Validation
                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Please enter both email and password",
                            "Missing Information",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Check if role is selected
                if (role == null || role.equals("Select one...")) {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Please select your role (Admin or Student)",
                            "Role Required",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                USER = getAuthenticatedUser(email, password, role);

                if (USER != null) {
                    // Save session data with actual user ID
                    SessionSaving.setUserSession(USER.name, USER.role, USER.id);

                    // ✅ Open different menu based on role
                    final user loggedInUser = USER;
                    final String userRole = USER.role;

                    // Show success message
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Welcome " + loggedInUser.name + "!",
                            "Login Successful",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Get owner reference before disposal
                    Window owner = LoginForm.this.getOwner();

                    // Set modal to false to release the modal block
                    setModal(false);

                    // Hide immediately
                    setVisible(false);

                    // Use Timer to dispose after EDT processes the hide event
                    Timer disposeTimer = new Timer(50, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            // Dispose the login form
                            dispose();

                            // Dispose owner if it exists
                            if (owner instanceof JFrame) {
                                owner.dispose();
                            }

                            // Open the appropriate menu
                            if (userRole.equals("Admin")) {
                                AdminDashboard.showAdminDashboard();
                            } else if (userRole.equals("Student")) {
                                UserProfile.showProfileForm();
                            }
                        }
                    });
                    disposeTimer.setRepeats(false);
                    disposeTimer.start();
                } else {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Invalid Email, Password, or Role.\n\n" +
                            "Please check:\n" +
                            "1. Email and password are correct\n" +
                            "2. Role matches your registration\n" +
                            "3. Your account is approved (for students)",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                USER = null;
                SessionSaving.clearSession();

                // Hide and dispose the login form first
                LoginForm.this.setVisible(false);

                // Safely handle owner window
                Window owner = LoginForm.this.getOwner();
                if (owner instanceof JFrame) {
                    ((JFrame) owner).dispose();
                }

                // Dispose login form
                LoginForm.this.dispose();

                SwingUtilities.invokeLater(() -> {
                    WelcomeForm.showWelcomeForm();
                });
            }
        });

        setVisible(true);
    }

    // ✅ Updated authentication method with role support
    private user getAuthenticatedUser(String email, String password, String role) {
        user USER = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DB.getConnection();

            String query = "";

            // ✅ Check the appropriate table based on selected role
            if (role.equals("Admin")) {
                query = "SELECT * FROM admins WHERE email = ?";
                System.out.println("DEBUG: Searching in admins table for: " + email);
            } else if (role.equals("Student")) {
                query = "SELECT * FROM students WHERE email = ?";
                System.out.println("DEBUG: Searching in students table for: " + email);
            }

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("DEBUG: User found in database");

                // For students, check status if column exists
                if (role.equals("Student")) {
                    try {
                        String status = resultSet.getString("status");
                        System.out.println("DEBUG: Student status: " + status);

                        // If status exists and is not "Approved" or NULL
                        if (status != null && !"Approved".equals(status)) {
                            System.out.println("DEBUG: Student not approved yet");
                            JOptionPane.showMessageDialog(null,
                                    "Your account is not approved yet.\n" +
                                    "Status: " + status + "\n\n" +
                                    "Please wait for admin approval before logging in.",
                                    "Account Not Approved",
                                    JOptionPane.WARNING_MESSAGE);
                            resultSet.close();
                            preparedStatement.close();
                            conn.close();
                            return null;
                        }

                        // If status is NULL, treat as approved (legacy data)
                        if (status == null) {
                            System.out.println("DEBUG: Status is NULL - treating as approved (legacy user)");
                        }
                    } catch (Exception statusEx) {
                        // Status column doesn't exist - treat as approved (old database)
                        System.out.println("DEBUG: Status column not found - treating as approved (old database schema)");
                    }
                }

                String storedHash = resultSet.getString("password");
                System.out.println("DEBUG: Password hash retrieved, attempting verification");

                // ✅ Verify password with BCrypt
                if (BCrypt.checkpw(password, storedHash)) {

                    System.out.println("DEBUG: Password verified successfully!");
                    USER = new user();
                    USER.id = resultSet.getInt("id"); // ✅ Get user ID from database
                    USER.name = resultSet.getString("name");
                    USER.email = resultSet.getString("email");
                    USER.phone = resultSet.getString("phone");
                    USER.role = resultSet.getString("role");
                    USER.password = null; // Never store plain password

                    // ✅ For students, also get their status if column exists
                    if (role.equals("Student")) {
                        try {
                            USER.status = resultSet.getString("status");
                        } catch (Exception statusEx) {
                            USER.status = "Approved"; // Default for old database
                        }
                    }
                    System.out.println("DEBUG: User object created for: " + USER.name + " (ID: " + USER.id + ")");
                } else {
                    System.out.println("DEBUG: Password verification FAILED");
                }
            } else {
                System.out.println("DEBUG: No user found with email: " + email);

                // ✅ Check if student is pending approval
                if (role.equals("Student")) {
                    String pendingQuery = "SELECT status, name FROM requested_students WHERE email = ?";
                    PreparedStatement pendingStmt = conn.prepareStatement(pendingQuery);
                    pendingStmt.setString(1, email);
                    ResultSet pendingResult = pendingStmt.executeQuery();

                    if (pendingResult.next()) {
                        String status = pendingResult.getString("status");
                        String name = pendingResult.getString("name");
                        System.out.println("DEBUG: Found in requested_students - Status: " + status);

                        if (status.equals("Pending")) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null,
                                        "Hi " + name + ",\n\n" +
                                        "Your registration is still pending approval.\n" +
                                        "Status: Pending\n\n" +
                                        "Please wait for admin approval before logging in.",
                                        "Account Pending Approval",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });
                        }
                    } else {
                        System.out.println("DEBUG: Not found in requested_students either");
                    }

                    pendingResult.close();
                    pendingStmt.close();
                }
            }

            resultSet.close();
            preparedStatement.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("ERROR in getAuthenticatedUser: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database connection error:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return USER;
    }

    public static void showLoginForm(JFrame parent) {
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm(parent);
            loginForm.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm(null);
            user USER = loginForm.USER;

            if (USER != null) {
                System.out.println("Login successful: " + USER.name);
                System.out.println("           Email: " + USER.email);
                System.out.println("           Phone: " + USER.phone);
                System.out.println("            Role: " + USER.role);
            } else {
                System.out.println("Login canceled or failed");
            }
        });
    }
}