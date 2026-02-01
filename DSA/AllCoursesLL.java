package DSA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AllCoursesLL {

    private static class CourseNode {
        String courseName;
        List<TopicData> topics;
        CourseNode next;
        CourseNode prev;

        CourseNode(String courseName) {
            this.courseName = courseName;
            this.topics = new ArrayList<>();
            this.next = null;
            this.prev = null;
        }

        void addTopic(int id, String topicName, String difficulty) {
            topics.add(new TopicData(id, topicName, difficulty));
        }
    }

    private static class TopicData {
        int id;
        String topicName;
        String difficulty;

        TopicData(int id, String topicName, String difficulty) {
            this.id = id;
            this.topicName = topicName;
            this.difficulty = difficulty;
        }
    }

    private CourseNode head;
    private CourseNode tail;
    private CourseNode current;
    private int courseCount;

    public AllCoursesLL() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.courseCount = 0;
    }

    public boolean loadCoursesFromDatabase() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String sql = "SELECT id, course_name, topic_name, difficulty FROM courses ORDER BY course_name, id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            String currentCourseName = null;
            CourseNode currentCourseNode = null;

            while (rs.next()) {
                int id = rs.getInt("id");
                String courseName = rs.getString("course_name");
                String topicName = rs.getString("topic_name");
                String difficulty = rs.getString("difficulty");

                if (currentCourseName == null || !currentCourseName.equals(courseName)) {
                    currentCourseName = courseName;
                    currentCourseNode = new CourseNode(courseName);
                    insertatTail(currentCourseNode);
                }

                if (currentCourseNode != null) {
                    currentCourseNode.addTopic(id, topicName, difficulty);
                }
            }

            if (head != null) {
                current = head;
            }

            return courseCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertatTail(CourseNode newNode) {
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        courseCount++;
    }

    public Object[][] getCurrentCourseTableData() {
        if (current == null || current.topics.isEmpty()) {
            return new Object[][]{{"No courses available.", "", ""}};
        }

        int topicCount = current.topics.size();
        Object[][] tableData = new Object[topicCount][3];

        for (int i = 0; i < topicCount; i++) {
            TopicData topic = current.topics.get(i);
            tableData[i][0] = current.courseName;
            tableData[i][1] = topic.topicName;
            tableData[i][2] = topic.difficulty;
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

    public String getCurrentCourseName() {
        return current != null ? current.courseName : null;
    }

    public int getCurrentPosition() {
        if (current == null) return 0;

        int position = 1;
        CourseNode temp = head;
        while (temp != null && temp != current) {
            position++;
            temp = temp.next;
        }
        return position;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getCurrentTopicCount() {
        return current != null ? current.topics.size() : 0;
    }

    public boolean isEmpty() {
        return courseCount == 0;
    }

    public String[] getAllCourseNames() {
        if (isEmpty()) {
            return new String[]{"No courses available"};
        }

        String[] courseNames = new String[courseCount];
        CourseNode temp = head;
        int index = 0;

        while (temp != null) {
            courseNames[index] = temp.courseName + " (" + temp.topics.size() + " topics)";
            temp = temp.next;
            index++;
        }

        return courseNames;
    }

    public String getCurrentCourseInfo() {
        if (current == null) {
            return "No course selected";
        }
        return String.format("Course: %s | Topics: %d | Showing %d of %d courses",
                current.courseName,
                current.topics.size(),
                getCurrentPosition(),
                courseCount);
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