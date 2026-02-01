import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import DSA.AllCoursesLL;

public class AllCourses extends JFrame {
    private JPanel CoursesLpanel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel CoursesLogo;
    private JLabel UserLabel;
    private JLabel CoursesL;
    private JPanel MainPanel;
    private JButton MainMenubtn;
    private JButton LogoutBtn;
    private JPanel AlllCorsesPanel;
    private JScrollPane TableScrollPanel;
    private JTable courseTable;
    private JLabel CourseListLabel;
    private JButton PreviousBtn;
    private JButton NExtBtn;
    private JPanel LLPanel;

    // Doubly Linked List to manage courses
    private AllCoursesLL courseList;

    public AllCourses() {
        setTitle("COURSES LIST");
        setContentPane(CoursesLpanel);
        setSize(700, 550);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Display username from session
        UserLabel.setText(SessionSaving.getDisplayName());

        // Initialize Doubly Linked List
        courseList = new AllCoursesLL();

        // Setup JTable with columns
        setupTable();


        // Load courses from database
        loadCourses();

        // Setup button actions
        setupButtonActions();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        AllCourses.this,
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


    // Setup JTable with column names and styling
    private void setupTable() {
        String[] columnNames = {"Course Name", "Topic Name", "Difficulty"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        courseTable.setModel(model);

        // Set column widths
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(200);  // Course Name
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Topic Name
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Difficulty

        // Table styling - Dark theme
        Color darkGray = new Color(60, 70, 80); // #3C4650
        Color white = Color.WHITE;

        courseTable.setRowHeight(30);
        courseTable.setFont(new Font("Arial", Font.PLAIN, 14));
        courseTable.setBackground(darkGray);
        courseTable.setForeground(white);
        courseTable.setGridColor(Color.BLACK);
        courseTable.setSelectionBackground(new Color(75, 110, 175));
        courseTable.setSelectionForeground(white);

        // Table header styling
        courseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        courseTable.getTableHeader().setBackground(new Color(45, 48, 50));
        courseTable.getTableHeader().setForeground(white);

        // Make sure the table header is visible
        courseTable.getTableHeader().setVisible(true);
        courseTable.getTableHeader().setReorderingAllowed(false);

        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set JScrollPane background to match table
        TableScrollPanel.setBackground(darkGray);
        TableScrollPanel.getViewport().setBackground(darkGray);

        // Set white border for JScrollPane to match other sides
        TableScrollPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    // Load courses from database using Doubly Linked List
    private void loadCourses() {
        if (courseList.loadCoursesFromDatabase()) {
            updateDisplay();
            updateNavigationButtons();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No courses found in database!",
                    "Empty",
                    JOptionPane.INFORMATION_MESSAGE);
            PreviousBtn.setEnabled(false);
            NExtBtn.setEnabled(false);
        }
    }

    // Setup button action listeners
    private void setupButtonActions() {
        // Previous button - Navigate to previous course in DLL
        PreviousBtn.addActionListener(e -> {
            if (courseList.movePrevious()) {
                updateDisplay();
                updateNavigationButtons();
            }
        });

        // Next button - Navigate to next course in DLL
        NExtBtn.addActionListener(e -> {
            if (courseList.moveNext()) {
                updateDisplay();
                updateNavigationButtons();
            }
        });

        // Main Menu button
        MainMenubtn.addActionListener(e -> {
            dispose();
            AdminMainmenu.showAdminMenu();
        });

    }

    // Update JTable with current course data from DLL (all topics of one course)
    private void updateDisplay() {
        if (!courseList.isEmpty()) {
            Object[][] tableData = courseList.getCurrentCourseTableData();
            String[] columnNames = {"Course Name", "Topic Name", "Difficulty"};

            DefaultTableModel model = new DefaultTableModel(tableData, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            courseTable.setModel(model);

            // Reset column widths after model change
            courseTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            courseTable.getColumnModel().getColumn(1).setPreferredWidth(250);
            courseTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        } else {
            clearTable();
        }
    }

    // Clear the table
    private void clearTable() {
        DefaultTableModel model = (DefaultTableModel) courseTable.getModel();
        model.setRowCount(0);
    }

    // Update navigation button states
    private void updateNavigationButtons() {
        PreviousBtn.setEnabled(courseList.hasPrevious());
        NExtBtn.setEnabled(courseList.hasNext());
    }

    public static void showAllCourses() {
        SwingUtilities.invokeLater(() -> {
            AllCourses form = new AllCourses();
            form.setVisible(true);
        });
    }

    public static void main(String[] args) {
        showAllCourses();
    }
}