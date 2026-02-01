import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import DSA.AllStudentsLL;

public class
AllStudents extends JFrame {
    private JPanel StudentsPanel;
    private JPanel StudentsLpanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel StudentLogo;
    private JLabel UserLabel;
    private JLabel StudentsL;
    private JPanel MainPanel;
    private JLabel ArticleNL;
    private JScrollPane TableScrollPanel;
    private JTable courseTable;
    private JPanel LLPanel;
    private JButton PreviousBtn;
    private JButton NExtBtn;
    private JButton MainMenubtn;

    private AllStudentsLL studentList;

    public AllStudents() {
        setTitle("STUDENTS LIST");
        setContentPane(StudentsLpanel);
        setSize(700, 570);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        UserLabel.setText(SessionSaving.getDisplayName());

        studentList = new AllStudentsLL();

        setupTable();

        loadStudents();

        setupButtonActions();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        AllStudents.this,
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
    }

    private void setupTable() {
        String[] columnNames = {"Student Name", "Email", "Phone", "Approved By"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable.setModel(model);

        courseTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(150);

        Color darkGray = new Color(60, 70, 80);
        Color white = Color.WHITE;

        courseTable.setRowHeight(30);
        courseTable.setFont(new Font("Arial", Font.PLAIN, 14));
        courseTable.setBackground(darkGray);
        courseTable.setForeground(white);
        courseTable.setGridColor(Color.BLACK);
        courseTable.setSelectionBackground(new Color(75, 110, 175));
        courseTable.setSelectionForeground(white);

        courseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        courseTable.getTableHeader().setBackground(new Color(45, 48, 50));
        courseTable.getTableHeader().setForeground(white);

        courseTable.getTableHeader().setVisible(true);
        courseTable.getTableHeader().setReorderingAllowed(false);

        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableScrollPanel.setBackground(darkGray);
        TableScrollPanel.getViewport().setBackground(darkGray);

        TableScrollPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private void loadStudents() {
        if (studentList.loadStudentsFromDatabase()) {
            updateDisplay();
            updateNavigationButtons();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No students found in database!",
                    "Empty",
                    JOptionPane.INFORMATION_MESSAGE);
            PreviousBtn.setEnabled(false);
            NExtBtn.setEnabled(false);
        }
    }

    private void setupButtonActions() {
        PreviousBtn.addActionListener(e -> {
            if (studentList.movePrevious()) {
                updateDisplay();
                updateNavigationButtons();
            }
        });

        NExtBtn.addActionListener(e -> {
            if (studentList.moveNext()) {
                updateDisplay();
                updateNavigationButtons();
            }
        });

        MainMenubtn.addActionListener(e -> {
            dispose();
            AdminMainmenu.showAdminMenu();
        });
    }

    private void updateDisplay() {
        if (!studentList.isEmpty()) {
            Object[][] tableData = studentList.getCurrentStudentTableData();
            String[] columnNames = {"Student Name", "Email", "Phone", "Approved By"};

            DefaultTableModel model = new DefaultTableModel(tableData, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            courseTable.setModel(model);

            courseTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            courseTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            courseTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            courseTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        } else {
            clearTable();
        }
    }

    private void clearTable() {
        DefaultTableModel model = (DefaultTableModel) courseTable.getModel();
        model.setRowCount(0);
    }

    private void updateNavigationButtons() {
        PreviousBtn.setEnabled(studentList.hasPrevious());
        NExtBtn.setEnabled(studentList.hasNext());
    }

    public static void showAllStudents() {
        SwingUtilities.invokeLater(() -> {
            AllStudents form = new AllStudents();
            form.setVisible(true);
        });
    }

    public static void main(String[] args) {
        showAllStudents();
    }
}