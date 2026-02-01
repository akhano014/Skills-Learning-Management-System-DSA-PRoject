package DSA;

import java.util.ArrayList;
import java.util.List;

public class FeedbackBinarySearch {

    /**
     * CourseData class - Stores information about a single course
     */
    public static class CourseData {
        public int courseId;
        public String courseName;

        public CourseData(int courseId, String courseName) {
            this.courseId = courseId;
            this.courseName = courseName;
        }

        @Override
        public String toString() {
            return courseName;
        }
    }

    public static List<String> searchCourses(CourseData[] courses, String searchQuery) {
        List<String> results = new ArrayList<>();

        // Step 1: Validate input
        if (courses == null || courses.length == 0) {
            return results; // Return empty list if no courses
        }

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return results; // Return empty list if search query is empty
        }

        // Step 2: Sort the courses array by name (required for binary search)
        sortCoursesByName(courses);

        // Step 3: Convert search query to lowercase for case-insensitive search
        String query = searchQuery.toLowerCase().trim();

        // Step 4: Perform modified binary search

        for (CourseData course : courses) {
            if (course.courseName.toLowerCase().contains(query)) {
                results.add(course.toString());
            }
        }

        return results;
    }

    public static int binarySearchExact(CourseData[] courses, String targetName) {
        if (courses == null || courses.length == 0) {
            return -1;
        }

        // Make sure array is sorted first
        sortCoursesByName(courses);

        int left = 0;                    // Start of search range
        int right = courses.length - 1;   // End of search range

        // Binary Search Algorithm
        while (left <= right) {
            int mid = left + (right - left) / 2;  // Find middle element (avoid overflow)

            // Compare middle element with target
            int comparison = courses[mid].courseName.compareToIgnoreCase(targetName);

            if (comparison == 0) {
                // Found exact match!
                return mid;
            } else if (comparison < 0) {
                // Target is in right half
                left = mid + 1;
            } else {
                // Target is in left half
                right = mid - 1;
            }
        }

        // Not found
        return -1;
    }

    public static List<String> searchByPrefix(CourseData[] courses, String startsWith) {
        List<String> results = new ArrayList<>();

        if (courses == null || courses.length == 0 || startsWith == null) {
            return results;
        }

        sortCoursesByName(courses);

        String prefix = startsWith.toLowerCase().trim();

        for (CourseData course : courses) {
            if (course.courseName.toLowerCase().startsWith(prefix)) {
                results.add(course.toString());
            }
        }

        return results;
    }

    public static void sortCoursesByName(CourseData[] courses) {
        if (courses == null || courses.length <= 1) {
            return; // Already sorted or empty
        }

        int n = courses.length;
        boolean swapped;

        // Bubble sort with optimization
        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                // Compare adjacent courses (case-insensitive)
                if (courses[j].courseName.compareToIgnoreCase(courses[j + 1].courseName) > 0) {
                    // Swap courses[j] and courses[j+1]
                    CourseData temp = courses[j];
                    courses[j] = courses[j + 1];
                    courses[j + 1] = temp;
                    swapped = true;
                }
            }

            // If no swaps were made, array is already sorted
            if (!swapped) {
                break;
            }
        }
    }

    public static void quickSort(CourseData[] courses, int low, int high) {
        if (low < high) {
            // Partition the array and get pivot index
            int pivotIndex = partition(courses, low, high);

            // Recursively sort left and right subarrays
            quickSort(courses, low, pivotIndex - 1);
            quickSort(courses, pivotIndex + 1, high);
        }
    }

    private static int partition(CourseData[] courses, int low, int high) {
        CourseData pivot = courses[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (courses[j].courseName.compareToIgnoreCase(pivot.courseName) < 0) {
                i++;
                // Swap courses[i] and courses[j]
                CourseData temp = courses[i];
                courses[i] = courses[j];
                courses[j] = temp;
            }
        }

        // Swap courses[i+1] and courses[high] (pivot)
        CourseData temp = courses[i + 1];
        courses[i + 1] = courses[high];
        courses[high] = temp;

        return i + 1;
    }

    public static CourseData getCourseByName(CourseData[] courses, String courseName) {
        if (courses == null || courseName == null) {
            return null;
        }

        for (CourseData course : courses) {
            if (course.courseName.equalsIgnoreCase(courseName)) {
                return course;
            }
        }

        return null;
    }

    public static void printSearchResults(List<String> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        System.out.println("\n=== SEARCH RESULTS ===");
        System.out.println("Found " + results.size() + " course(s):");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }
        System.out.println("======================\n");
    }


    public static void printAllCourses(CourseData[] courses) {
        if (courses == null || courses.length == 0) {
            System.out.println("No courses available.");
            return;
        }

        System.out.println("\n=== ALL COURSES ===");
        for (int i = 0; i < courses.length; i++) {
            System.out.println((i + 1) + ". ID: " + courses[i].courseId +
                             " | Name: " + courses[i].courseName);
        }
        System.out.println("Total: " + courses.length + " courses");
        System.out.println("===================\n");
    }
}

