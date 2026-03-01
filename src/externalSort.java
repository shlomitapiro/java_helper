import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class externalSort {

    // מייצג שורה אחת מקובץ מסוים
    static class Entry {
        String value;
        BufferedReader reader;

        Entry(String value, BufferedReader reader) {
            this.value = value;
            this.reader = reader;
        }
    }

    public void externalSort(Path inputFile, Path outputFile, int chunkSize) throws Exception {

        // validation
        if (inputFile == null || !Files.exists(inputFile)) {
            System.out.println("NO INPUT FILE");
            throw new Exception("NO INPUT FILE");
        }
        if (outputFile == null) {
            System.out.println("NO OUTPUT PATH");
            throw new Exception("NO OUTPUT PATH");
        }
        if (chunkSize < 1) {
            System.out.println("INVALID CHUNK SIZE");
            throw new Exception("INVALID CHUNK SIZE");
        }

        // Phase 1 — פיצול ומיון במקביל
        List<Path> tempFiles = splitAndSort(inputFile, chunkSize);

        // Phase 2 — מיזוג
        merge(tempFiles, outputFile);

        // ניקוי
        cleanup(tempFiles);
    }

    // Phase 1 — קריאת הקובץ הגדול, פיצול ל chunks, מיון כל chunk במקביל
    private List<Path> splitAndSort(Path inputFile, int chunkSize) throws Exception {
        List<Path> tempFiles = new ArrayList<>();
        List<String> chunk = new ArrayList<>();

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                chunk.add(line);
                if (chunk.size() == chunkSize) {
                    List<String> currentChunk = new ArrayList<>(chunk);
                    Path tempFile = Files.createTempFile("chunk_", ".txt");
                    tempFiles.add(tempFile);
                    executor.submit(() -> sortAndSave(currentChunk, tempFile));
                    chunk.clear();
                }
            }
            // שארית
            if (!chunk.isEmpty()) {
                Path tempFile = Files.createTempFile("chunk_", ".txt");
                tempFiles.add(tempFile);
                executor.submit(() -> sortAndSave(chunk, tempFile));
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return tempFiles;
    }

    // מיין chunk ושמור לקובץ זמני
    private void sortAndSave(List<String> chunk, Path tempFile) {
        Collections.sort(chunk);
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            for (String line : chunk) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phase 2 — מיזוג כל הקבצים הזמניים לקובץ פלט אחד ממוין
    private void merge(List<Path> tempFiles, Path outputFile) throws Exception {
        List<BufferedReader> readers = new ArrayList<>();
        for (Path tempFile : tempFiles) {
            readers.add(Files.newBufferedReader(tempFile));
        }

        PriorityQueue<Entry> pq = new PriorityQueue<>(
            (a, b) -> a.value.compareTo(b.value)
        );

        // טוענים שורה ראשונה מכל קובץ
        for (BufferedReader reader : readers) {
            String line = reader.readLine();
            if (line != null) {
                pq.offer(new Entry(line, reader));
            }
        }

        // מיזוג לקובץ פלט
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            while (!pq.isEmpty()) {
                Entry smallest = pq.poll();
                writer.write(smallest.value);
                writer.newLine();

                String nextLine = smallest.reader.readLine();
                if (nextLine != null) {
                    pq.offer(new Entry(nextLine, smallest.reader));
                }
            }
        }

        for (BufferedReader reader : readers) {
            reader.close();
        }
    }

    // מחיקת הקבצים הזמניים
    private void cleanup(List<Path> tempFiles) throws Exception {
        for (Path tempFile : tempFiles) {
            Files.deleteIfExists(tempFile);
        }
    }
}