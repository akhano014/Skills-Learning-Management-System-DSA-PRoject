// WelcomeForm.java
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WelcomeForm extends JFrame {
    private JPanel WelcomePanel;
    private JPanel LoginPanel;
    private JLabel Logo;
    private JButton Loginbtn;
    private JButton Registerbtn;

    public WelcomeForm() {
        setTitle("Welcome");
        setContentPane(WelcomePanel);
        setSize(650, 450);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Loginbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelcomeForm.this.setVisible(false);

                LoginForm loginDialog = new LoginForm(WelcomeForm.this);

                if (loginDialog.USER != null) {
                    WelcomeForm.this.dispose();
                }
            }
        });

        Registerbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelcomeForm.this.setVisible(false);

                Registrationform regDialog = new Registrationform(WelcomeForm.this);

                if (regDialog.USER != null) {
                    WelcomeForm.this.dispose();
                } else {
                    WelcomeForm.this.setVisible(true);
                }
            }
        });
    }

    public static void showWelcomeForm() {
        SwingUtilities.invokeLater(() -> {
            WelcomeForm frame = new WelcomeForm();
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        showWelcomeForm();
    }
}