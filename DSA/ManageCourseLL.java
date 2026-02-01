package DSA;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageCourseLL {


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

        void addTopic(int id, String topicName, String difficulty, String description) {
            topics.add(new TopicData(id, topicName, difficulty, description));
        }
    }

    private static class TopicData {
        int id;
        String topicName;
        String difficulty;
        String description;

        TopicData(int id, String topicName, String difficulty, String description) {
            this.id = id;
            this.topicName = topicName;
            this.difficulty = difficulty;
            this.description = description;
        }
    }

    private CourseNode head;
    private CourseNode tail;
    private CourseNode current;
    private int courseCount;

    public ManageCourseLL() {
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

            String sql = "SELECT id, course_name, topic_name, difficulty, description FROM courses ORDER BY course_name, id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            String currentCourseName = null;
            CourseNode currentCourseNode = null;

            while (rs.next()) {
                int id = rs.getInt("id");
                String courseName = rs.getString("course_name");
                String topicName = rs.getString("topic_name");
                String difficulty = rs.getString("difficulty");
                String description = rs.getString("description");

                // If this is a new course, create a new node
                if (currentCourseName == null || !currentCourseName.equals(courseName)) {
                    currentCourseName = courseName;
                    currentCourseNode = new CourseNode(courseName);
                    insertatTail(currentCourseNode);
                }

                // Add topic to current course node
                if (currentCourseNode != null) {
                    currentCourseNode.addTopic(id, topicName, difficulty, description);
                }
            }

            // Set current to head if list is not empty
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

    // Insert course node at tail
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

    // Get current course data formatted for JTable display
    // Returns ALL topics for the current course
    public Object[][] getCurrentCourseTableData() {
        if (current == null || current.topics.isEmpty()) {
            return new Object[][]{{"No courses available.", "", ""}};
        }

        // Create table with one row per topic (3 columns: Course Name, Topic Name, Difficulty)
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

    // Move to next course (for Next button)
    public boolean moveNext() {
        if (current == null || current.next == null) {
            return false;
        }
        current = current.next;
        return true;
    }

    // Move to previous course (for Previous button)
    public boolean movePrevious() {
        if (current == null || current.prev == null) {
            return false;
        }
        current = current.prev;
        return true;
    }

    // Check if there's a next course
    public boolean hasNext() {
        return current != null && current.next != null;
    }

    // Check if there's a previous course
    public boolean hasPrevious() {
        return current != null && current.prev != null;
    }

    // Get current course name
    public String getCurrentCourseName() {
        return current != null ? current.courseName : null;
    }

    // Get list of all topic IDs for current course
    public List<Integer> getCurrentTopicIds() {
        List<Integer> ids = new ArrayList<>();
        if (current != null) {
            for (TopicData topic : current.topics) {
                ids.add(topic.id);
            }
        }
        return ids;
    }

    // Get current topic data by index
    public TopicData getCurrentTopic(int index) {
        if (current != null && index >= 0 && index < current.topics.size()) {
            return current.topics.get(index);
        }
        return null;
    }

    // Get current course position (1-based)
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

    // Get total number of unique courses
    public int getCourseCount() {
        return courseCount;
    }

    // Get total number of courses (alias for compatibility)
    public int getSize() {
        return courseCount;
    }

    // Get number of topics in current course
    public int getCurrentTopicCount() {
        return current != null ? current.topics.size() : 0;
    }

    // Check if list is empty
    public boolean isEmpty() {
        return courseCount == 0;
    }

    // Get all course names for combo box
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

    // Move to specific course by index (0-based)
    public boolean moveToIndex(int index) {
        if (index < 0 || index >= courseCount) {
            return false;
        }

        current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return true;
    }

    // Get current course information summary
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

    // Helper method to access DB class from default package
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

