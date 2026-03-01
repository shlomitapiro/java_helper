import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class externalSortcsv {

    static class Entry {
        String value;
        BufferedReader reader;

        Entry(String value, BufferedReader reader) {
            this.value = value;
            this.reader = reader;
        }
    }

    public void externalSortCSV(Path inputFile, Path outputFile, int chunkSize, int columnIndex) throws Exception {

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

        String header = readHeader(inputFile);
        List<Path> tempFiles = splitAndSortCSV(inputFile, chunkSize, columnIndex);
        mergeCSV(tempFiles, outputFile, header, columnIndex);
        cleanup(tempFiles);
    }

    private String readHeader(Path inputFile) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            return reader.readLine();
        }
    }

    private List<Path> splitAndSortCSV(Path inputFile, int chunkSize, int columnIndex) throws Exception {
        List<Path> tempFiles = new ArrayList<>();
        List<String> chunk = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            reader.readLine(); // דילוג על header
            String line;
            while ((line = reader.readLine()) != null) {
                chunk.add(line);
                if (chunk.size() == chunkSize) {
                    List<String> currentChunk = new ArrayList<>(chunk);
                    Path tempFile = Files.createTempFile("chunk_", ".txt");
                    tempFiles.add(tempFile);
                    executor.submit(() -> sortAndSaveCSV(currentChunk, tempFile, columnIndex));
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) {
                Path tempFile = Files.createTempFile("chunk_", ".txt");
                tempFiles.add(tempFile);
                executor.submit(() -> sortAndSaveCSV(chunk, tempFile, columnIndex));
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return tempFiles;
    }

    private void sortAndSaveCSV(List<String> chunk, Path tempFile, int columnIndex) {
        chunk.sort((a, b) -> {
            String colA = a.split(",")[columnIndex];
            String colB = b.split(",")[columnIndex];
            return colA.compareTo(colB);
        });

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            for (String line : chunk) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mergeCSV(List<Path> tempFiles, Path outputFile, String header, int columnIndex) throws Exception {
        List<BufferedReader> readers = new ArrayList<>();
        for (Path tempFile : tempFiles) {
            readers.add(Files.newBufferedReader(tempFile));
        }

        PriorityQueue<Entry> pq = new PriorityQueue<>((a, b) -> {
            String colA = a.value.split(",")[columnIndex];
            String colB = b.value.split(",")[columnIndex];
            return colA.compareTo(colB);
        });

        for (BufferedReader reader : readers) {
            String line = reader.readLine();
            if (line != null) {
                pq.offer(new Entry(line, reader));
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write(header);
            writer.newLine();

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

    private void cleanup(List<Path> tempFiles) throws Exception {
        for (Path tempFile : tempFiles) {
            Files.deleteIfExists(tempFile);
        }
    }
}