package DSA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewFeedbackDLL {

    private static class FeedbackNode {
        int id;
        int studentId;
        String studentName;
        String studentEmail;
        String courseName;
        String feedbackType;
        String subject;
        String message;
        String priority;
        String status;
        String submittedAt;
        FeedbackNode next;
        FeedbackNode prev;

        FeedbackNode(int id, int studentId, String studentName, String studentEmail,
                     String courseName, String feedbackType, String subject, String message,
                     String priority, String status, String submittedAt) {
            this.id = id;
            this.studentId = studentId;
            this.studentName = studentName;
            this.studentEmail = studentEmail;
            this.courseName = courseName;
            this.feedbackType = feedbackType;
            this.subject = subject;
            this.message = message;
            this.priority = priority;
            this.status = status;
            this.submittedAt = submittedAt;
            this.next = null;
            this.prev = null;
        }
    }

    private FeedbackNode head;
    private FeedbackNode tail;
    private FeedbackNode current;
    private int feedbackCount;

    public ViewFeedbackDLL() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.feedbackCount = 0;
    }

    public boolean loadFeedbacksByStatus(String status) {
        clearList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String sql = "SELECT f.id, f.student_id, f.student_name, f.student_email, " +
                    "COALESCE(c.course_name, 'None') as course_name, " +
                    "f.feedback_type, f.subject, f.message, f.priority, f.status, " +
                    "DATE_FORMAT(f.submitted_at, '%Y-%m-%d %H:%i:%s') as submitted_at " +
                    "FROM feedbacks f " +
                    "LEFT JOIN courses c ON f.course_id = c.id " +
                    "WHERE f.status = ? " +
                    "ORDER BY f.submitted_at DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FeedbackNode node = new FeedbackNode(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_email"),
                        rs.getString("course_name"),
                        rs.getString("feedback_type"),
                        rs.getString("subject"),
                        rs.getString("message"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("submitted_at")
                );
                insertAtTail(node);
            }

            if (head != null) {
                current = head;
            }

            return feedbackCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    public boolean loadFeedbacksByPriority(String priority) {
        clearList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String sql = "SELECT f.id, f.student_id, f.student_name, f.student_email, " +
                    "COALESCE(c.course_name, 'None') as course_name, " +
                    "f.feedback_type, f.subject, f.message, f.priority, f.status, " +
                    "DATE_FORMAT(f.submitted_at, '%Y-%m-%d %H:%i:%s') as submitted_at " +
                    "FROM feedbacks f " +
                    "LEFT JOIN courses c ON f.course_id = c.id " +
                    "WHERE f.priority = ? " +
                    "ORDER BY f.submitted_at DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, priority);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FeedbackNode node = new FeedbackNode(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_email"),
                        rs.getString("course_name"),
                        rs.getString("feedback_type"),
                        rs.getString("subject"),
                        rs.getString("message"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("submitted_at")
                );
                insertAtTail(node);
            }

            if (head != null) {
                current = head;
            }

            return feedbackCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    public boolean loadAllFeedbacks() {
        clearList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String sql = "SELECT f.id, f.student_id, f.student_name, f.student_email, " +
                    "COALESCE(c.course_name, 'None') as course_name, " +
                    "f.feedback_type, f.subject, f.message, f.priority, f.status, " +
                    "DATE_FORMAT(f.submitted_at, '%Y-%m-%d %H:%i:%s') as submitted_at " +
                    "FROM feedbacks f " +
                    "LEFT JOIN courses c ON f.course_id = c.id " +
                    "ORDER BY f.submitted_at DESC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FeedbackNode node = new FeedbackNode(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_email"),
                        rs.getString("course_name"),
                        rs.getString("feedback_type"),
                        rs.getString("subject"),
                        rs.getString("message"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("submitted_at")
                );
                insertAtTail(node);
            }

            if (head != null) {
                current = head;
            }

            return feedbackCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void insertAtTail(FeedbackNode newNode) {
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        feedbackCount++;
    }

    private void clearList() {
        head = null;
        tail = null;
        current = null;
        feedbackCount = 0;
    }

    public Object[][] getCurrentFeedbackTableData(String filterType) {
        if (current == null) {
            return new Object[][]{{"No feedbacks available.", "", "", "", "", ""}};
        }

        Object[][] tableData = new Object[1][6];

        if ("status".equalsIgnoreCase(filterType)) {
            tableData[0][0] = current.studentName;
            tableData[0][1] = current.studentEmail;
            tableData[0][2] = current.feedbackType;
            tableData[0][3] = current.subject;
            tableData[0][4] = current.status;
            tableData[0][5] = "";
        } else if ("priority".equalsIgnoreCase(filterType)) {
            tableData[0][0] = current.studentName;
            tableData[0][1] = current.studentEmail;
            tableData[0][2] = current.feedbackType;
            tableData[0][3] = current.subject;
            tableData[0][4] = current.priority;
            tableData[0][5] = "";
        } else {
            tableData[0][0] = current.studentName;
            tableData[0][1] = current.studentEmail;
            tableData[0][2] = current.feedbackType;
            tableData[0][3] = current.subject;
            tableData[0][4] = current.status;
            tableData[0][5] = current.priority;
        }

        return tableData;
    }

    public boolean moveNext() {
        if (current == null || current.next == null) {
            return false;
        }
        current = current.next;
        return true;
    }

    public boolean movePrevious() {
        if (current == null || current.prev == null) {
            return false;
        }
        current = current.prev;
        return true;
    }

    public boolean hasNext() {
        return current != null && current.next != null;
    }

    public boolean hasPrevious() {
        return current != null && current.prev != null;
    }

    public int getCurrentFeedbackId() {
        return current != null ? current.id : -1;
    }

    public String getCurrentStudentName() {
        return current != null ? current.studentName : "";
    }

    public String getCurrentStudentEmail() {
        return current != null ? current.studentEmail : "";
    }

    public String getCurrentCourseName() {
        return current != null ? current.courseName : "";
    }

    public String getCurrentFeedbackType() {
        return current != null ? current.feedbackType : "";
    }

    public String getCurrentSubject() {
        return current != null ? current.subject : "";
    }

    public String getCurrentMessage() {
        return current != null ? current.message : "";
    }

    public String getCurrentPriority() {
        return current != null ? current.priority : "";
    }

    public String getCurrentStatus() {
        return current != null ? current.status : "";
    }

    public String getCurrentSubmittedAt() {
        return current != null ? current.submittedAt : "";
    }

    public int getFeedbackCount() {
        return feedbackCount;
    }

    public int getCurrentPosition() {
        if (current == null) return 0;

        int position = 1;
        FeedbackNode temp = head;
        while (temp != null && temp != current) {
            position++;
            temp = temp.next;
        }
        return position;
    }

    public boolean submitAdminResponse(String responseType, String response, int adminId, String adminName) {
        if (current == null) {
            System.out.println("Error: No feedback selected.");
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getDBConnection();
            String sql = "INSERT INTO feedback_responses " +
                    "(feedback_id, admin_id, admin_name, response, response_type) " +
                    "VALUES (?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, current.id);
            pstmt.setInt(2, adminId);
            pstmt.setString(3, adminName);
            pstmt.setString(4, response);
            pstmt.setString(5, responseType);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                System.out.println("Admin response submitted successfully!");

                updateFeedbackStatusToReviewed();

                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateFeedbackStatusToReviewed() {
        if (current == null) return;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getDBConnection();

            String checkSql = "SELECT COUNT(*) FROM feedback_responses WHERE feedback_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, current.id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 1) {
                rs.close();
                pstmt.close();

                String updateSql = "UPDATE feedbacks SET status = 'Reviewed' WHERE id = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, current.id);
                pstmt.executeUpdate();

                current.status = "Reviewed";

                System.out.println("Feedback status updated to 'Reviewed'.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void printAllFeedbacks() {
        FeedbackNode temp = head;
        int count = 1;
        System.out.println("\n=== ALL FEEDBACKS ===");
        while (temp != null) {
            System.out.println(count + ". " + temp.studentName + " | " +
                    temp.feedbackType + " | " + temp.subject);
            temp = temp.next;
            count++;
        }
        System.out.println("Total: " + feedbackCount + " feedbacks\n");
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