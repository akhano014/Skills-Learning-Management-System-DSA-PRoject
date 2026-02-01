import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import DSA.ManageCourseLL;

public class ManageCourse extends JFrame {

    private JPanel ManageLabel;
    private JPanel HeadingPanel;
    private JLabel MainHeading;
    private JLabel userLogo;
    private JLabel CoursesLogo;
    private JLabel UserLabel;
    private JPanel SelectionPanel;
    private JLabel CoursesL;
    private JLabel SelectCourseL;
    private JComboBox<String> CourseCombo;
    private JLabel ChoiceL;
    private JComboBox<String> ChoiceCombo;
    private JPanel TablePanel;
    private JScrollPane TableScrollPanel;
    private JTable courseTable;
    private JLabel CourseListLabel;
    private JPanel ButtonPanel;
    private JButton PreviousBtn;
    private JButton NExtBtn;
    private JPanel BottomPanel;
    private JButton MainMenubtn;
    private JButton SubmitBtn;
    private JButton LogoutBtn;

    // Doubly Linked List to manage courses
    private ManageCourseLL courseList;

    public ManageCourse() {
        setTitle("Manage Course");
        setContentPane(ManageLabel);
        setSize(780, 680);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Display username from session
        UserLabel.setText(SessionSaving.getDisplayName());

        // Initialize Doubly Linked List
        courseList = new ManageCourseLL();

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
                        ManageCourse.this,
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
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(250);  // Course Name
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

        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set ScrollPane and viewport background to match table (fixes white area)
        TableScrollPanel.setBackground(darkGray);
        TableScrollPanel.getViewport().setBackground(darkGray);

        // Remove white border around the table
        TableScrollPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    // Load courses from database using Doubly Linked List
    private void loadCourses() {
        if (courseList.loadCoursesFromDatabase()) {
            updateDisplay();
            updateCourseComboBox();
            updateNavigationButtons();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No courses found in database!",
                    "Empty",
                    JOptionPane.INFORMATION_MESSAGE);

            PreviousBtn.setEnabled(false);
            NExtBtn.setEnabled(false);
            SubmitBtn.setEnabled(false);
        }
    }

    // Setup button action listeners
    private void setupButtonActions() {
        // Previous button - Navigate to previous course in DLL
        PreviousBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (courseList.movePrevious()) {
                    updateDisplay();
                    updateNavigationButtons();
                    updateCourseComboBoxSelection();
                }
            }
        });

        // Next button - Navigate to next course in DLL
        NExtBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (courseList.moveNext()) {
                    updateDisplay();
                    updateNavigationButtons();
                    updateCourseComboBoxSelection();
                }
            }
        });

        // Submit button - Execute selected action
        SubmitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (CourseCombo.getSelectedItem() == null || ChoiceCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(ManageCourse.this,
                            "Please select both course and action!",
                            "Incomplete Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String course = (String) CourseCombo.getSelectedItem();
                String action = (String) ChoiceCombo.getSelectedItem();

                // Check if default option is selected
                if (course.equals("Select one...") || action.equals("Select one...")) {
                    JOptionPane.showMessageDialog(ManageCourse.this,
                            "Please select both course and action!",
                            "Incomplete Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Open respective form based on the choice
                if (action.equals("1. Add Topic")) {
                    dispose();
                    AddTopic.showAddTopic();
                } else if (action.equals("2. Remove Topic")) {
                    dispose();
                    DeleteTopic.showDeteletopic();
                }
            }
        });

        // ComboBox selection - Jump to selected course
        CourseCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = CourseCombo.getSelectedIndex();
                if (selectedIndex > 0 && !courseList.isEmpty()) { // Skip index 0 ("Select one...")
                    courseList.moveToIndex(selectedIndex - 1);
                    updateDisplay();
                    updateNavigationButtons();
                }
            }
        });

        // ✅ FIXED: Changed from Menubtn to MainMenubtn
        // Main Menu button
        MainMenubtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                AdminMainmenu.showAdminMenu();
            }
        });

        // Logout button
        LogoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                WelcomeForm.showWelcomeForm();
            }
        });
    }

    // Update JTable with current course data from DLL
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
            courseTable.getColumnModel().getColumn(0).setPreferredWidth(250);  // Course Name
            courseTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Topic Name
            courseTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Difficulty
        } else {
            clearTable();
        }
    }

    // Clear the table
    private void clearTable() {
        DefaultTableModel model = (DefaultTableModel) courseTable.getModel();
        model.setRowCount(0);
    }

    // Update Course ComboBox with all course names
    private void updateCourseComboBox() {
        CourseCombo.removeAllItems();
        CourseCombo.addItem("Select one...");

        String[] courseNames = courseList.getAllCourseNames();
        for (String name : courseNames) {
            CourseCombo.addItem(name);
        }

        if (!courseList.isEmpty()) {
            CourseCombo.setSelectedIndex(courseList.getCurrentPosition());
        }
    }

    // Update ComboBox selection to match current course
    private void updateCourseComboBoxSelection() {
        if (!courseList.isEmpty()) {
            CourseCombo.setSelectedIndex(courseList.getCurrentPosition());
        }
    }

    // Update navigation button states
    private void updateNavigationButtons() {
        PreviousBtn.setEnabled(courseList.hasPrevious());
        NExtBtn.setEnabled(courseList.hasNext());
    }

    public static void showManageCourse() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ManageCourse form = new ManageCourse();
                form.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        showManageCourse();
    }
}