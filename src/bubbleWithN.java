public class bubbleWithN implements Runnable{

    private int[] arr;
    private int idx;

    public bubbleWithN(int [] arr, int idx){
        this.arr = arr;
        this.idx = idx;
    }

    @Override
    public void run(){
        int tmp;
        if (arr[idx] > arr[idx + 1]) {
            tmp = arr[idx];
            arr[idx] = arr[idx + 1];
            arr[idx + 1] = tmp;
        }
    }

    public int[] bubbleSort(int[] arr, int steps) throws Exception{
        int n = arr.length;
        for(int step = 1; step <= steps; step++){
            int startIdx;
            if(step % 2 == 0){
                startIdx = 0;
            }else{
                startIdx = 1;
            }

            Thread [] threads = new Thread[n / 2];
            int threadsCount = 0;

            for(int i = startIdx; i < n-1; i+=2){
                bubbleWithN task = new bubbleWithN(arr, i);
                threads[threadsCount] = new Thread(task);
                threads[threadsCount].start();
                threadsCount++;
            }

            for(int i = 0; i < threadsCount; i++){
                threads[i].join();
            }
        }
        return arr;
    }
}
