package DSA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AllStudentsLL {

    private static class StudentNode {
        int studentId;
        String name;
        String email;
        String phone;
        String approvedBy;

        StudentNode prev;
        StudentNode next;

        public StudentNode(int studentId, String name, String email, String phone, String approvedBy) {
            this.studentId = studentId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.approvedBy = approvedBy;
            this.prev = null;
            this.next = null;
        }
    }

    private StudentNode head;
    private StudentNode tail;
    private StudentNode current;
    private int size;

    public AllStudentsLL() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.size = 0;
    }

    // Load all students from database into Doubly Linked List
    public boolean loadStudentsFromDatabase() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String query = "SELECT id, name, email, phone, approved_by FROM students ORDER BY id";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String approvedBy = rs.getString("approved_by");

                // Add student to doubly linked list
                addStudent(id, name, email, phone, approvedBy);
            }

            // Set current to head (first student)
            current = head;

            return size > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Add student to the doubly linked list
    private void addStudent(int studentId, String name, String email, String phone, String approvedBy) {
        StudentNode newNode = new StudentNode(studentId, name, email, phone, approvedBy);

        if (head == null) {
            // First student
            head = newNode;
            tail = newNode;
        } else {
            // Add to end
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    // Move to next student
    public boolean moveNext() {
        if (current != null && current.next != null) {
            current = current.next;
            return true;
        }
        return false;
    }

    // Move to previous student
    public boolean movePrevious() {
        if (current != null && current.prev != null) {
            current = current.prev;
            return true;
        }
        return false;
    }

    // Check if there is a next student
    public boolean hasNext() {
        return current != null && current.next != null;
    }

    // Check if there is a previous student
    public boolean hasPrevious() {
        return current != null && current.prev != null;
    }

    // Check if list is empty
    public boolean isEmpty() {
        return size == 0 || current == null;
    }

    // Get current student data for table display (single row)
    public Object[][] getCurrentStudentTableData() {
        if (current == null) {
            return new Object[0][4];
        }

        // Return single student as one row
        Object[][] data = new Object[1][4];
        data[0][0] = current.name;
        data[0][1] = current.email;
        data[0][2] = current.phone;
        data[0][3] = current.approvedBy != null ? current.approvedBy : "N/A";

        return data;
    }


    public int getSize() {
        return size;
    }


    public int getCurrentPosition() {
        if (current == null) return 0;

        int position = 1;
        StudentNode temp = head;
        while (temp != null && temp != current) {
            position++;
            temp = temp.next;
        }
        return position;
    }


    private Connection getDBConnection() throws SQLException {
        try {
            Class<?> dbClass = Class.forName("DB");
            java.lang.reflect.Method getConnectionMethod = dbClass.getMethod("getConnection");
            return (Connection) getConnectionMethod.invoke(null);
        } catch (Exception e) {
            throw new SQLException("Unable to access DB class: " + e.getMessage(), e);
        }
    }
}

