import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


public class cheapestFlight {
    public List<String> cheapestFlight(Path routes, String origin, String destination) throws Exception {
    
        if (routes == null || !Files.exists(routes)) throw new Exception("NO ROUTES");
        if (origin == null || origin.isEmpty()) throw new Exception("NO CITY");
        if (destination == null || destination.isEmpty()) throw new Exception("NO CITY");
        
        Map<String, List<String[]>> flightMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(routes.toFile()));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] arr = line.split(",");
            flightMap.computeIfAbsent(arr[0], k -> new ArrayList<>())
                    .add(new String[]{arr[1], arr[2]});
        }
        reader.close();
        
        if (origin.equals(destination)) return List.of(origin);
        
        // Dijkstra
        PriorityQueue<String[]> pq = new PriorityQueue<>((a, b) -> Integer.parseInt(a[1]) - Integer.parseInt(b[1]));
        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>();
        
        pq.add(new String[]{origin, "0"});
        parent.put(origin, null);
        
        while (!pq.isEmpty()) {
            String[] current = pq.poll();
            String city = current[0];
            int cost = Integer.parseInt(current[1]);
            
            if (visited.contains(city)) continue;
            visited.add(city);
            
            if (city.equals(destination)) {
                return reconstructPath(parent, origin, destination);
            }
            
            if (!flightMap.containsKey(city)) continue;
            
            for (String[] neighbor : flightMap.get(city)) {
                if (!visited.contains(neighbor[0])) {
                    parent.put(neighbor[0], city);
                    pq.add(new String[]{neighbor[0], String.valueOf(cost + Integer.parseInt(neighbor[1]))});
                }
            }
        }
        
        System.out.println("NO ROUTE AVAILABLE");
        return null;
    }

    private List<String> reconstructPath(Map<String, String> parent, String origin, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;
        while (current != null) {
            path.add(0, current);
            current = parent.get(current);
        }
        return path;
    }
}