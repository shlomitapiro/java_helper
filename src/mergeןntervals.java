
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mergeןntervals {
    public List<int[]> mergeIntervals(int[][]intervals){
        List<int[]> mergedIntervals = new ArrayList<>();
        if (intervals == null || intervals.length == 0) {
            return mergedIntervals;
        }

        // Sort intervals based on the start time
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

        int[] currentInterval = intervals[0];
        mergedIntervals.add(currentInterval);

        for (int[] interval : intervals) {
            int currentStart = currentInterval[0];
            int currentEnd = currentInterval[1];
            int nextStart = interval[0];
            int nextEnd = interval[1];

            if (currentEnd >= nextStart) { // Overlapping intervals
                currentInterval[1] = Math.max(currentEnd, nextEnd);
            } else {
                currentInterval = interval;
                mergedIntervals.add(currentInterval);
            }
        }

        return mergedIntervals;
    }
}
