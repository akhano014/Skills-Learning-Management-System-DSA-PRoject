// Day 1: Welcome Form Only
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            WelcomeForm welcomeForm = new WelcomeForm();
            welcomeForm.setVisible(true);
        });
    }
}
