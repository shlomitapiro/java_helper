
import java.nio.file.*;
import java.util.*;

public class Solution {

    public List<String> wordLadder(Path dictionary, String startWord, String endWord) throws Exception {
            
        List<String> words = validateInput(dictionary, startWord, endWord);

        List<String> result = new ArrayList<>();

        if (startWord.equals(endWord)) {
            result.add(startWord);
            return result;
        }
        
        if (differByOneLetter(startWord, endWord)) {
            result.add(startWord);
            result.add(endWord);
            return result;
        }
        
        // Build dictionary set with words of the same length as startWord and no duplicates
        Set<String> dictSet = new HashSet<>();
        for (String word : words) {
            if (word.length() == startWord.length()) {
                dictSet.add(word.toLowerCase());
            }
        }
        
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        // path memory
        Map<String, String> parent = new HashMap<>();
        
        queue.add(startWord); // words to explore
        visited.add(startWord);
        parent.put(startWord, null);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(endWord)) {
                return reconstructPath(parent, startWord, endWord);
            }
            
            if (differByOneLetter(current, endWord)) {
                parent.put(endWord, current);
                return reconstructPath(parent, startWord, endWord);
            }
            
            // Explore neighbors - words that differ by one letter to current
            for (String neighbor : dictSet) {
                if (differByOneLetter(current, neighbor)) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                    }
                }
            }
        }
        
        System.out.println("NO WORD LADDER AVAILABLE");
        return null;
    }


    //--- Helper methods ---

    private List<String> validateInput(Path dictionary, String startWord, String endWord) throws Exception {
        
        if (dictionary == null || !Files.exists(dictionary)) {
            System.out.println("NO DICTIONARY");
            throw new Exception("NO DICTIONARY");
        }
        
        List<String> words = Files.readAllLines(dictionary);
        if (words.isEmpty()) {
            System.out.println("NO DICTIONARY");
            throw new Exception("NO DICTIONARY");
        }
        
        if (startWord == null || startWord.isEmpty()) {
            System.out.println("NO START WORD");
            throw new Exception("NO START WORD");
        }
        
        if (endWord == null || endWord.isEmpty()) {
            System.out.println("NO END WORD");
            throw new Exception("NO END WORD");
        }
        
        if (startWord.length() != endWord.length()) {
            System.out.println("WORD LENGTH DOES NOT MATCH");
            throw new Exception("WORD LENGTH DOES NOT MATCH");
        }
        
        return words;
    }

    private boolean differByOneLetter(String word1, String word2) {
        if (word1.length() != word2.length()) {
            return false;
        }
        
        int differences = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                differences++;
            }
        }
        
        return differences == 1;
    }

    private List<String> reconstructPath(Map<String, String> parent, String startWord, String endWord) {
        List<String> path = new ArrayList<>();
        String current = endWord;
        
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        
        Collections.reverse(path);
        return path;
    }
    

    //--- main for testing ---  
    public static void main(String[] args) {
        Solution solver = new Solution();
        Path dict = Paths.get("resources/dictionary.txt");
        
        try {
            System.out.println("===  1: clap → plum ===");
            List<String> result = solver.wordLadder(dict, "clap", "plum");
            System.out.println("res: " + result);
            System.out.println();
            
            System.out.println("===  2: aloha → aloha ===");
            result = solver.wordLadder(dict, "aloha", "aloha");
            System.out.println("res: " + result);
            System.out.println();
            
            System.out.println("===  3: maze → waze ===");
            result = solver.wordLadder(dict, "maze", "waze");
            System.out.println("res: " + result);
            System.out.println();
            
            System.out.println("===  4: pill → clay ===");
            result = solver.wordLadder(null, "stat", "slot");
            System.out.println("res: " + result);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}


import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.*;
import java.util.*;
import javax.print.DocFlavor;


public class Solution {

    public int degreesOfSeparation(Path network, String personA, String personB) throws Exception{

        List<String []> connectionsList = validateInput(network, personA, personB);

        if (personA.equals(personB)) {
            return 0;
        }

        // mapping for each person all its connections
        Map <String, List<String>> connectionsMap = new HashMap<>();
        connectionsMap = generateMpaConnections(connectionsList, connectionsMap);

        Queue <String> queue = new ArrayDeque<>();
        Set <String> visited = new HashSet<>();

        // mapping for each person the degree of separation from personA
        Map <String, Integer> degreeMap = new HashMap<>();
        int degree = 0;
        degreeMap.put(personA, degree);

        queue.add(personA);
        visited.add(personA);

        while(!queue.isEmpty()){
            String current = queue.poll();
            List<String> connections = connectionsMap.get(current);
            int currDegree = degreeMap.get(current);
            for (String person : connections) {
                if (!visited.contains(person)) {
                    visited.add(person);
                    queue.add(person);
                    degreeMap.put(person, currDegree + 1);
                }
            }
        }
        
        if (!degreeMap.containsKey(personB)) {
            System.out.println("NO CONNECTION");
            return -1;
        }
        
        return degreeMap.get(personB);
    }

    // ---- helpers ----

    public List<String []> validateInput(Path network, String personA, String personB) throws Exception{
         if(network == null || !Files.exists(network)){
            throw new Exception("NO NETWORK");
         }

         List<String []> connections = new ArrayList<>();
         String line;
         BufferedReader reader = new BufferedReader(new FileReader(network.toFile()));
         
         while ((line = reader.readLine()) != null) {
            String [] couple = line.split(",");
            connections.add(couple);
         }

         if (connections.isEmpty()) {
            throw new Exception("NO NETWORK");
         }

         if (personA == null || personA.isEmpty()) {
            throw new Exception("NO PERSON A");
         }

         if (personB == null || personB.isEmpty()) {
            throw new Exception("NO PERSON B");
         }

         return connections;
    }

    public Map<String, List<String>> generateMpaConnections(List<String[]> connectionsList, Map<String, List<String>> connectionsMap){
        for (String [] conn : connectionsList) {
            List <String> couple;
            couple = connectionsMap.getOrDefault(conn[0], new ArrayList<String>());
            couple.add(conn[1]);
            connectionsMap.put(conn[0], couple);

            couple = connectionsMap.getOrDefault(conn[1], new ArrayList<>());
            couple.add(conn[0]);
            connectionsMap.put(conn[1], couple);
        }
        return connectionsMap;
    }

    public static void main(String[] args) throws Exception {
    
    // יצירת קובץ בדיקה זמני
    Path networkFile = Path.of("test_network.txt");
    Files.writeString(networkFile, 
        "alice,bob\n" +
        "bob,charlie\n" +
        "charlie,diana\n" +
        "alice,eve\n"
    );
    
    Solution sol = new Solution();

    System.out.println(sol.degreesOfSeparation(networkFile, "alice", "diana"));
    System.out.println(sol.degreesOfSeparation(networkFile, "alice", "bob"));
    System.out.println(sol.degreesOfSeparation(networkFile, "alice", "alice"));
    System.out.println(sol.degreesOfSeparation(networkFile, "alice", "frank"));
}
}