package graphs;

import mpi.*;

import java.util.Random;

public class EdgeCount {

    private int[][] adjacencyMatrix;

    public EdgeCount(int graphSize) {
        this.adjacencyMatrix = new int[graphSize][graphSize];
        this.createRandomMatrix(graphSize);
    }

    public EdgeCount(int[][] adjacencyMatrix) {
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

    public void count(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Размер графа
        int n = adjacencyMatrix.length;

        // Определение количества строк, обрабатываемых каждым процессом
        int rowsPerProcess = n / size;
        int startRow = rank * rowsPerProcess;
        int endRow = (rank == size - 1) ? n : startRow + rowsPerProcess;

        // Подсчёт рёбер в части матрицы, назначенной текущему процессу
        int localEdgeCount = 0;
        for (int i = startRow; i < endRow; i++) {
            for (int j = i + 1; j < n; j++) {  // Считаем только верхнюю треугольную часть
                if (adjacencyMatrix[i][j] == 1) {
                    localEdgeCount++;
                }
            }
        }

        // Суммируем результаты всех процессов
        int[] globalEdgeCount = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{localEdgeCount}, 0, globalEdgeCount, 0, 1, MPI.INT, MPI.SUM, 0);


        if (rank == 0) {
            System.out.println("Edge counts in graphs: " + globalEdgeCount[0]);
        }

        MPI.Finalize();
    }
}
