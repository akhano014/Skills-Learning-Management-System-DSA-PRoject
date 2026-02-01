package DSA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AttemptQuizBinarySearch {

    public static class QuizSearchResult {
        private final int quizId;
        private final int courseId;
        private final String courseName;
        private final String topicName;
        private final String question;
        private final String difficulty;

        public QuizSearchResult(int quizId, int courseId, String courseName, String topicName,
                                String question, String difficulty) {
            this.quizId = quizId;
            this.courseId = courseId;
            this.courseName = courseName;
            this.topicName = topicName;
            this.question = question;
            this.difficulty = difficulty;
        }

        public int getQuizId() { return quizId; }
        public int getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        public String getTopicName() { return topicName; }
        public String getQuestion() { return question; }
        public String getDifficulty() { return difficulty; }

        @Override
        public String toString() {
            return String.format("%s - %s (%s): %s", courseName, topicName, difficulty, question);
        }
    }

    public static List<QuizSearchResult> searchQuizzes(String searchText) {
        List<QuizSearchResult> allQuizzes = loadAllQuizzes();
        if (allQuizzes.isEmpty() || searchText == null || searchText.trim().isEmpty()) {
            return allQuizzes;
        }

        List<QuizSearchResult> results = new ArrayList<>();
        String searchLower = searchText.toLowerCase().trim();

        for (QuizSearchResult quiz : allQuizzes) {
            if (quiz.getQuestion().toLowerCase().contains(searchLower)) {
                results.add(quiz);
            }
        }
        return results;
    }

    public static List<QuizSearchResult> searchByCourse(String courseSearchText) {
        List<QuizSearchResult> allQuizzes = loadAllQuizzes();
        if (allQuizzes.isEmpty() || courseSearchText == null || courseSearchText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Collections.sort(allQuizzes, Comparator.comparing(q -> q.getCourseName().toLowerCase()));
        List<QuizSearchResult> results = new ArrayList<>();
        String searchLower = courseSearchText.toLowerCase().trim();

        int left = 0;
        int right = allQuizzes.size() - 1;
        int startIndex = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            String midCourse = allQuizzes.get(mid).getCourseName().toLowerCase();

            if (midCourse.contains(searchLower)) {
                startIndex = mid;
                right = mid - 1;
            } else if (midCourse.compareTo(searchLower) < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (startIndex != -1) {
            int i = startIndex;
            while (i >= 0 && allQuizzes.get(i).getCourseName().toLowerCase().contains(searchLower)) {
                results.add(0, allQuizzes.get(i));
                i--;
            }

            i = startIndex + 1;
            while (i < allQuizzes.size() && allQuizzes.get(i).getCourseName().toLowerCase().contains(searchLower)) {
                results.add(allQuizzes.get(i));
                i++;
            }
        }
        return results;
    }

    private static List<QuizSearchResult> loadAllQuizzes() {
        List<QuizSearchResult> quizzes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String sql = "SELECT q.id, q.course_id, c.course_name, q.topic_name, q.question, q.difficulty " +
                         "FROM quizzes q JOIN courses c ON q.course_id = c.id ORDER BY q.id";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                quizzes.add(new QuizSearchResult(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    rs.getString("topic_name"),
                    rs.getString("question"),
                    rs.getString("difficulty")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return quizzes;
    }

    private static Connection getDBConnection() throws SQLException {
        try {
            Class<?> dbClass = Class.forName("DB");
            java.lang.reflect.Method getConnectionMethod = dbClass.getMethod("getConnection");
            return (Connection) getConnectionMethod.invoke(null);
        } catch (Exception e) {
            throw new SQLException("Unable to access DB class: " + e.getMessage(), e);
        }
    }

    private static void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

