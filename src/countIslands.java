import java.io.*;
import java.nio.file.*;
import java.util.*;

public class countIslands {
    public int countIslands(Path mapFile) throws Exception{
        List<List<String>> grid = validateInput(mapFile);

        if (grid.isEmpty()) {
            return 0;
        }

        int count = 0;
        int row = grid.size();
        int col = grid.get(0).size();
        int [][] directions = {{0,1}, {1,0}, {-1,0}, {0,-1}};
        boolean [][] visited = new boolean[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                visited[i][j] = false;
            }
        }

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (grid.get(i).get(j).equals("L") && visited[i][j] == false) {
                    count++;
                    dfs(grid, visited, directions, i, j);
                }
            }
        }

        return count;
    }

    //------ helpers -----
    private List<List<String>> validateInput(Path file){
        List<List<String>> grid = new ArrayList<>();
        if (file == null ||!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist");
        }

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file.toFile()));
            String line;
            int currLineLength;
            String firstLine = reader.readLine();
            int permitedLength = firstLine.length();
            grid.add(new ArrayList<>(Arrays.asList(firstLine.split(""))));
            while ((line = reader.readLine()) != null) {
                List<String> innList = new ArrayList<>(Arrays.asList(line.split("")));
                currLineLength = innList.size();
                if (currLineLength != permitedLength) {
                    throw new IllegalArgumentException("Inconsistent line lengths in the file");
                }
                grid.add(innList);
            }

            reader.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        return grid;
    }

    private void dfs(List<List<String>> grid, boolean [][] visited, int [][] directions, int row, int col){

        visited[row][col] = true;

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (newRow >= 0 && newRow < grid.size() &&
                newCol >= 0 && newCol < grid.get(0).size() &&
                visited[newRow][newCol] == false &&
                grid.get(newRow).get(newCol).equals("L")
                ) 
                {
                    dfs(grid, visited, directions, newRow, newCol);
                }    
            
        }
    }

    public static void main(String[] args) throws Exception {
    Path mapFile = Path.of("test_map.txt");
    Files.writeString(mapFile,
        "LWWLLL\n" +
        "LWWLLW\n" +
        "WWWWWW\n" +
        "WLLWWW\n"
    );

    countIslands sol = new countIslands();
    System.out.println(sol.countIslands(mapFile));
}
}
