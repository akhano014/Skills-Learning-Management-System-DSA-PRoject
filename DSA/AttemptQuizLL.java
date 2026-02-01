package DSA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttemptQuizLL {

    private static class QuizNode {
        int quizId;
        int courseId;
        String courseName;
        String topicName;
        String question;
        String optionA;
        String optionB;
        String optionC;
        String optionD;
        String correctAnswer;
        String difficulty;
        QuizNode next;
        QuizNode prev;

        QuizNode(int quizId, int courseId, String courseName, String topicName, String question,
                 String optionA, String optionB, String optionC, String optionD,
                 String correctAnswer, String difficulty) {
            this.quizId = quizId;
            this.courseId = courseId;
            this.courseName = courseName;
            this.topicName = topicName;
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
            this.difficulty = difficulty;
            this.next = null;
            this.prev = null;
        }
    }

    private QuizNode head;
    private QuizNode tail;
    private QuizNode current;
    private int quizCount;
    private int currentPosition;

    public AttemptQuizLL() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.quizCount = 0;
        this.currentPosition = 0;
    }

    public boolean loadQuizzesFromDatabase(String courseName, String topicName, String difficulty) {
        clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDBConnection();
            String sql = "SELECT q.id, q.course_id, c.course_name, q.topic_name, q.question, " +
                         "q.option_a, q.option_b, q.option_c, q.option_d, q.correct_answer, q.difficulty " +
                         "FROM quizzes q JOIN courses c ON q.course_id = c.id " +
                         "WHERE c.course_name = ? AND q.topic_name = ? AND q.difficulty = ? ORDER BY q.id";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, courseName);
            pstmt.setString(2, topicName);
            pstmt.setString(3, difficulty);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                QuizNode newNode = new QuizNode(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    rs.getString("topic_name"),
                    rs.getString("question"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_answer"),
                    rs.getString("difficulty")
                );
                insertAtTail(newNode);
            }

            if (head != null) {
                current = head;
                currentPosition = 1;
            }

            return quizCount > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    private void insertAtTail(QuizNode newNode) {
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        quizCount++;
    }

    public boolean moveToNext() {
        if (current != null && current.next != null) {
            current = current.next;
            currentPosition++;
            return true;
        }
        return false;
    }

    public boolean moveToPrevious() {
        if (current != null && current.prev != null) {
            current = current.prev;
            currentPosition--;
            return true;
        }
        return false;
    }

    public QuizData getCurrentQuiz() {
        if (current == null) return null;
        return new QuizData(current.quizId, current.courseId, current.courseName, current.topicName,
                current.question, current.optionA, current.optionB, current.optionC, current.optionD,
                current.correctAnswer, current.difficulty, currentPosition, quizCount);
    }

    public boolean hasNext() {
        return current != null && current.next != null;
    }

    public boolean hasPrevious() {
        return current != null && current.prev != null;
    }

    public int getQuizCount() {
        return quizCount;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void clear() {
        head = null;
        tail = null;
        current = null;
        quizCount = 0;
        currentPosition = 0;
    }

    public boolean isEmpty() {
        return head == null;
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

    public static class QuizData {
        private final int quizId;
        private final int courseId;
        private final String courseName;
        private final String topicName;
        private final String question;
        private final String optionA;
        private final String optionB;
        private final String optionC;
        private final String optionD;
        private final String correctAnswer;
        private final String difficulty;
        private final int currentPosition;
        private final int totalQuizzes;

        public QuizData(int quizId, int courseId, String courseName, String topicName, String question,
                        String optionA, String optionB, String optionC, String optionD,
                        String correctAnswer, String difficulty, int currentPosition, int totalQuizzes) {
            this.quizId = quizId;
            this.courseId = courseId;
            this.courseName = courseName;
            this.topicName = topicName;
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
            this.difficulty = difficulty;
            this.currentPosition = currentPosition;
            this.totalQuizzes = totalQuizzes;
        }

        public int getQuizId() { return quizId; }
        public int getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        public String getTopicName() { return topicName; }
        public String getQuestion() { return question; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String getDifficulty() { return difficulty; }
        public int getCurrentPosition() { return currentPosition; }
        public int getTotalQuizzes() { return totalQuizzes; }

        public boolean isCorrect(String selectedAnswer) {
            return correctAnswer.equalsIgnoreCase(selectedAnswer);
        }
    }
}

