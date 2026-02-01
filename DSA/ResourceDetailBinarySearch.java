package DSA;

import java.util.ArrayList;
import java.util.List;

public class ResourceDetailBinarySearch {

    public static class TopicData {
        public String topicName;
        public String difficulty;
        public String content;

        public TopicData(String topicName, String difficulty, String content) {
            this.topicName = topicName;
            this.difficulty = difficulty;
            this.content = content;
        }

        @Override
        public String toString() {
            return topicName + " [" + difficulty + "]";
        }
    }

    public static List<String> searchTopics(TopicData[] topics, String searchQuery) {
        List<String> results = new ArrayList<>();

        if (topics == null || topics.length == 0) {
            return results;
        }

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return results;
        }

        sortTopicsByName(topics);

        String query = searchQuery.toLowerCase().trim();

        for (TopicData topic : topics) {
            if (topic.topicName.toLowerCase().contains(query)) {
                results.add(topic.toString());
            }
        }

        return results;
    }

    public static int binarySearchExact(TopicData[] topics, String targetName) {
        if (topics == null || topics.length == 0) {
            return -1;
        }

        sortTopicsByName(topics);

        int left = 0;
        int right = topics.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            int comparison = topics[mid].topicName.compareToIgnoreCase(targetName);

            if (comparison == 0) {
                return mid;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return -1;
    }

    public static List<String> searchByPrefix(TopicData[] topics, String startsWith) {
        List<String> results = new ArrayList<>();

        if (topics == null || topics.length == 0 || startsWith == null) {
            return results;
        }

        sortTopicsByName(topics);

        String prefix = startsWith.toLowerCase().trim();

        for (TopicData topic : topics) {
            if (topic.topicName.toLowerCase().startsWith(prefix)) {
                results.add(topic.toString());
            }
        }

        return results;
    }

    public static void sortTopicsByName(TopicData[] topics) {
        if (topics == null || topics.length <= 1) {
            return;
        }

        int n = topics.length;
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                if (topics[j].topicName.compareToIgnoreCase(topics[j + 1].topicName) > 0) {
                    TopicData temp = topics[j];
                    topics[j] = topics[j + 1];
                    topics[j + 1] = temp;
                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }

    public static void quickSort(TopicData[] topics, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(topics, low, high);

            quickSort(topics, low, pivotIndex - 1);
            quickSort(topics, pivotIndex + 1, high);
        }
    }

    private static int partition(TopicData[] topics, int low, int high) {
        TopicData pivot = topics[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (topics[j].topicName.compareToIgnoreCase(pivot.topicName) < 0) {
                i++;
                TopicData temp = topics[i];
                topics[i] = topics[j];
                topics[j] = temp;
            }
        }

        TopicData temp = topics[i + 1];
        topics[i + 1] = topics[high];
        topics[high] = temp;

        return i + 1;
    }

    public static List<String> searchByDifficulty(TopicData[] topics, String difficulty) {
        List<String> results = new ArrayList<>();

        if (topics == null || topics.length == 0 || difficulty == null) {
            return results;
        }

        for (TopicData topic : topics) {
            if (topic.difficulty.equalsIgnoreCase(difficulty)) {
                results.add(topic.toString());
            }
        }

        return results;
    }

    public static List<String> advancedSearch(TopicData[] topics, String query, String difficulty) {
        List<String> results = new ArrayList<>();

        if (topics == null || topics.length == 0) {
            return results;
        }

        sortTopicsByName(topics);

        for (TopicData topic : topics) {
            boolean matchesQuery = (query == null || query.trim().isEmpty()
                    || topic.topicName.toLowerCase().contains(query.toLowerCase().trim()));

            boolean matchesDifficulty = (difficulty == null || difficulty.trim().isEmpty()
                    || topic.difficulty.equalsIgnoreCase(difficulty));

            if (matchesQuery && matchesDifficulty) {
                results.add(topic.toString());
            }
        }

        return results;
    }

    public static void demonstrateBinarySearch() {
        System.out.println("=== Binary Search Demonstration ===\n");

        TopicData[] topics = {
                new TopicData("Arrays", "1. Easy", "Content about arrays..."),
                new TopicData("Binary Search", "2. Medium", "Content about binary search..."),
                new TopicData("CRUD Operations", "1. Easy", "Content about CRUD..."),
                new TopicData("Database Indexing", "3. Hard", "Content about indexing..."),
                new TopicData("Linked Lists", "2. Medium", "Content about linked lists...")
        };

        System.out.println("Original topics (unsorted):");
        for (int i = 0; i < topics.length; i++) {
            System.out.println(i + ". " + topics[i]);
        }

        sortTopicsByName(topics);

        System.out.println("\n\nAfter sorting:");
        for (int i = 0; i < topics.length; i++) {
            System.out.println(i + ". " + topics[i]);
        }

        String searchTarget = "CRUD Operations";
        System.out.println("\n\nSearching for: \"" + searchTarget + "\"");
        int index = binarySearchExact(topics, searchTarget);

        if (index != -1) {
            System.out.println("Found at index: " + index);
            System.out.println("Topic: " + topics[index]);
        } else {
            System.out.println("Not found!");
        }

        String searchQuery = "Data";
        System.out.println("\n\nSearching for topics containing: \"" + searchQuery + "\"");
        List<String> results = searchTopics(topics, searchQuery);

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            System.out.println("Found " + results.size() + " result(s):");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }
        }
    }

    public static void main(String[] args) {
        demonstrateBinarySearch();
    }
}