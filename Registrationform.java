// Registrationform.java
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

public class Registrationform extends JDialog {
    private JLabel LogoLabel;
    private JLabel RegisterLabel;
    private JLabel NameLabel;
    private JTextField Namefield;
    private JTextField EmailField;
    private JLabel EmailLabel;
    private JTextField PhoneField;
    private JLabel PhoneLabel;
    private JLabel PasswordLabel;
    private JPasswordField passwordField1;
    private JLabel ConfirmPassword;
    private JPasswordField passwordField2;
    private JButton Reigsterbtn;
    private JButton cancelButton;
    private JPanel Registerpanel;
    private JLabel SelectRole;
    private JComboBox RoleCombo;

    public user USER;

    public Registrationform(JFrame parent) {
        super(parent);
        setTitle("User Registration");
        setContentPane(Registerpanel);
        setMinimumSize(new Dimension(500, 500));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        Registrationform.this,
                        "Are you sure you want to exit?",
                        "Exit Registration",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    USER = null;
                    dispose();

                    Window owner = getOwner();
                    if (owner instanceof JFrame) {
                        ((JFrame) owner).dispose();
                    }

                    WelcomeForm.showWelcomeForm();
                }
            }
        });

        Reigsterbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registeruser();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                USER = null;
                dispose();
                WelcomeForm.showWelcomeForm();
            }
        });

        setVisible(true);
    }

    private void registeruser() {
        String name = Namefield.getText().trim();
        String email = EmailField.getText().trim();
        String phone = PhoneField.getText().trim();
        String password = String.valueOf(passwordField1.getPassword());
        String confirmpass = String.valueOf(passwordField2.getPassword());
        String role = (String) RoleCombo.getSelectedItem(); // ✅ Get selected role

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmpass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all fields", "Try again", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if role is selected
        if (role == null || role.equals("Select one...")) {
            JOptionPane.showMessageDialog(this, "Please select a role (Admin or Student)", "Role Required", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Please Enter a Valid Email address", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("\\d{7,15}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number (digits only, 7-15 digits)", "Invalid Phone", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmpass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Try again", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Add user to database with role
        USER = addUsertoDatabase(name, email, phone, password, role);

        if (USER != null) {
            // Show different messages based on role
            if (role.equals("Student")) {
                JOptionPane.showMessageDialog(this,
                        "Registration request submitted successfully!\n\n" +
                                "Your Status: PENDING\n" +
                                "Please wait for admin approval before you can login.",
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (role.equals("Admin")) {
                JOptionPane.showMessageDialog(this,
                        "Admin registration successful!\n\n" +
                                "You can now login to the system.",
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();
            Window owner = getOwner();
            if (owner instanceof JFrame) {
                ((JFrame) owner).dispose();
            }

            SwingUtilities.invokeLater(() -> {
                LoginForm.showLoginForm(null);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to register new user.\n\n" +
                    "Possible reasons:\n" +
                    "• Email is already registered\n" +
                    "• A registration request with this email is pending approval\n" +
                    "• Database connection error",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ Updated method with role parameter
    private user addUsertoDatabase(String name, String email, String phone, String password, String role) {
        user USER = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection cnn = DB.getConnection();

            // Hash password
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));

            // ✅ Check which role and insert into appropriate table
            if (role.equals("Student")) {
                // ✅ First check if student already exists in requested_students OR students table
                String checkQuery = "SELECT email FROM requested_students WHERE email = ? " +
                                   "UNION SELECT email FROM students WHERE email = ?";
                PreparedStatement checkStmt = cnn.prepareStatement(checkQuery);
                checkStmt.setString(1, email);
                checkStmt.setString(2, email);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Student already exists (either pending or approved)
                    System.out.println("Student already exists with email: " + email);
                    rs.close();
                    checkStmt.close();
                    cnn.close();
                    return null; // Return null to indicate duplicate
                }
                rs.close();
                checkStmt.close();

                // Insert into requested_students table (Pending approval)
                String insert_query = "INSERT INTO requested_students (name, email, phone, password, role, status) VALUES (?, ?, ?, ?, 'Student', 'Pending')";
                PreparedStatement preparedStatement = cnn.prepareStatement(insert_query);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, phone);
                preparedStatement.setString(4, hashed);

                int effect_rows = preparedStatement.executeUpdate();
                if (effect_rows > 0) {
                    USER = new user();
                    USER.name = name;
                    USER.email = email;
                    USER.phone = phone;
                    USER.role = "Student";
                    USER.password = null;
                }

                preparedStatement.close();

            } else if (role.equals("Admin")) {
                // Insert into admins table (Can login immediately)
                String insert_query = "INSERT INTO admins (name, email, phone, password, role) VALUES (?, ?, ?, ?, 'Admin')";
                PreparedStatement preparedStatement = cnn.prepareStatement(insert_query);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, phone);
                preparedStatement.setString(4, hashed);

                int effect_rows = preparedStatement.executeUpdate();
                if (effect_rows > 0) {
                    USER = new user();
                    USER.name = name;
                    USER.email = email;
                    USER.phone = phone;
                    USER.role = "Admin";
                    USER.password = null;
                }

                preparedStatement.close();
            }

            cnn.close();

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // Handle duplicate email error
            System.err.println("Email already registered: " + email);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return USER;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Registrationform form = new Registrationform(null);
            user USER = form.USER;

            if (USER != null) {
                System.out.println("Successful Registration of: " + USER.name + " as " + USER.role);
            } else {
                System.out.println("Registration Canceled");
            }
        });
    }
}