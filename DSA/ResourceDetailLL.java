package DSA;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDetailLL {

    private static class TopicNode {
        int id;
        String topicName;
        String difficulty;
        String content;
        String courseName;
        TopicNode next;
        TopicNode prev;

        TopicNode(int id, String courseName, String topicName, String difficulty, String content) {
            this.id = id;
            this.courseName = courseName;
            this.topicName = topicName;
            this.difficulty = difficulty;
            this.content = content;
            this.next = null;
            this.prev = null;
        }
    }

    private TopicNode head;
    private TopicNode tail;
    private TopicNode current;
    private int topicCount;

    public ResourceDetailLL() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.topicCount = 0;
    }

    public boolean loadTopicsForCourse(String courseName) {
        return loadTopicsForCourse(courseName, null, null);
    }

    public boolean loadTopicsForCourse(String courseName, String difficulty) {
        return loadTopicsForCourse(courseName, null, difficulty);
    }

    public boolean loadTopicsForCourse(String courseName, String topicName, String difficulty) {
        clear();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();

            String sql;

            if (topicName != null && !topicName.isEmpty() && difficulty != null && !difficulty.equals("All")) {
                sql = "SELECT id, course_name, topic_name, difficulty, description " +
                        "FROM courses WHERE course_name = ? AND topic_name = ? AND difficulty = ? ORDER BY id";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, courseName);
                pstmt.setString(2, topicName);
                pstmt.setString(3, difficulty);
            } else if (difficulty != null && !difficulty.equals("All") && !difficulty.isEmpty()) {
                sql = "SELECT id, course_name, topic_name, difficulty, description " +
                        "FROM courses WHERE course_name = ? AND difficulty = ? ORDER BY id";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, courseName);
                pstmt.setString(2, difficulty);
            } else {
                sql = "SELECT id, course_name, topic_name, difficulty, description " +
                        "FROM courses WHERE course_name = ? ORDER BY id";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, courseName);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String course = rs.getString("course_name");
                String topic = rs.getString("topic_name");
                String diff = rs.getString("difficulty");
                String content = rs.getString("description");

                TopicNode newNode = new TopicNode(id, course, topic, diff, content);
                insertAtTail(newNode);
            }

            if (head != null) {
                current = head;
            }

            return topicCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    private void insertAtTail(TopicNode newNode) {
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        topicCount++;
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

    public String getCurrentTopicName() {
        return current != null ? current.topicName : "No topic selected";
    }

    public String getCurrentTopicContent() {
        if (current == null || current.content == null) {
            return "No content available.";
        }
        return current.content;
    }

    public String getCurrentDifficulty() {
        return current != null ? current.difficulty : "";
    }

    public int getCurrentTopicId() {
        return current != null ? current.id : -1;
    }

    public int getCurrentPosition() {
        if (current == null) return 0;

        int position = 1;
        TopicNode temp = head;
        while (temp != null && temp != current) {
            position++;
            temp = temp.next;
        }
        return position;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public boolean isEmpty() {
        return topicCount == 0;
    }

    public String[] getAllTopicNames() {
        if (isEmpty()) {
            return new String[]{"No topics available"};
        }

        String[] topicNames = new String[topicCount];
        TopicNode temp = head;
        int index = 0;

        while (temp != null) {
            topicNames[index] = temp.topicName;
            temp = temp.next;
            index++;
        }

        return topicNames;
    }

    public String[] getUniqueTopicNames() {
        if (isEmpty()) {
            return new String[]{"No topics available"};
        }

        List<String> uniqueNames = new ArrayList<>();
        TopicNode temp = head;

        while (temp != null) {
            if (!uniqueNames.contains(temp.topicName)) {
                uniqueNames.add(temp.topicName);
            }
            temp = temp.next;
        }

        return uniqueNames.toArray(new String[0]);
    }

    public int getUniqueTopicCount() {
        return getUniqueTopicNames().length;
    }

    public List<String> getAvailableDifficulties() {
        List<String> difficulties = new ArrayList<>();
        TopicNode temp = head;

        while (temp != null) {
            if (!difficulties.contains(temp.difficulty)) {
                difficulties.add(temp.difficulty);
            }
            temp = temp.next;
        }

        return difficulties;
    }

    public boolean moveToIndex(int index) {
        if (index < 0 || index >= topicCount) {
            return false;
        }

        current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return true;
    }

    public List<String> binarySearch(String searchQuery) {
        if (isEmpty() || searchQuery == null || searchQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }

        ResourceDetailBinarySearch.TopicData[] topicArray =
                new ResourceDetailBinarySearch.TopicData[topicCount];

        TopicNode temp = head;
        int index = 0;

        while (temp != null) {
            topicArray[index] = new ResourceDetailBinarySearch.TopicData(
                    temp.topicName,
                    temp.difficulty,
                    temp.content
            );
            temp = temp.next;
            index++;
        }

        return ResourceDetailBinarySearch.searchTopics(topicArray, searchQuery);
    }

    public void clear() {
        head = null;
        tail = null;
        current = null;
        topicCount = 0;
    }

    public String getCurrentTopicInfo() {
        if (current == null) {
            return "No topic loaded";
        }

        return String.format("Topic %d of %d: %s [%s]",
                getCurrentPosition(),
                topicCount,
                current.topicName,
                current.difficulty);
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

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}