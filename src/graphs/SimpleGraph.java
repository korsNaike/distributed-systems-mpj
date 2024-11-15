package graphs;

import java.util.Random;

public class SimpleGraph {

    public int[][] adjacencyMatrix;

    public SimpleGraph(int graphSize) {
        this.adjacencyMatrix = new int[graphSize][graphSize];
        this.createRandomMatrix(graphSize);
    }

    public SimpleGraph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }

    public void createRandomMatrix(int graphSize) {
        Random random = new Random();
        double edgeProbability = 0.5;

        for (int i = 0; i < graphSize; i++) {
            for (int j = i + 1; j < graphSize; j++) { // Для неориентированного графа заполняем только верхнюю часть
                if (random.nextDouble() < edgeProbability) {
                    adjacencyMatrix[i][j] = 1;
                    adjacencyMatrix[j][i] = 1; // Симметрично для неориентированного графа
                }
            }
        }
    }

}
