import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class maxTopN {
    public List<Integer> topN(int[] arr, int n){

        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for(int i = 0; i < n; i++){
            minHeap.offer(arr[i]);
        }

        for(int i = n; i < arr.length; i++){
            int head = minHeap.peek();
            if (head < arr[i]) {
                minHeap.poll();
                minHeap.offer(arr[i]);
            }
        }

        List<Integer> topNList = new ArrayList<>();
        while(!minHeap.isEmpty()){
            topNList.add(0,minHeap.poll());
        }

        return topNList;
    }
}
