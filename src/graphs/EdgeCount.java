package graphs;

import mpi.*;

import java.util.Random;

public class EdgeCount extends SimpleGraph {

    public EdgeCount(int graphSize) {
        super(graphSize);
    }

    public EdgeCount(int[][] adjacencyMatrix) {
        super(adjacencyMatrix);
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
