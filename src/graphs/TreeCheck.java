package graphs;

import mpi.*;

public class TreeCheck extends SimpleGraph {


    public TreeCheck(int graphSize) {
        super(graphSize);
    }

    public TreeCheck(int[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public void check(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Исходная матрица смежности (заполняется только на нулевом процессе)
        int n = adjacencyMatrix.length; // Количество вершин

        // Рассылка количества вершин всем процессам
        int[] buffer = new int[1];
        if (rank == 0) buffer[0] = n;
        MPI.COMM_WORLD.Bcast(buffer, 0, 1, MPI.INT, 0);
        n = buffer[0];

        // Рассылка матрицы смежности всем процессам
        int[] flatMatrix = new int[n * n];
        if (rank == 0) {
            for (int i = 0; i < n; i++) {
                System.arraycopy(adjacencyMatrix[i], 0, flatMatrix, i * n, n);
            }
        }
        MPI.COMM_WORLD.Bcast(flatMatrix, 0, n * n, MPI.INT, 0);

        // Восстановление локальной матрицы смежности
        int[][] localMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(flatMatrix, i * n, localMatrix[i], 0, n);
        }

        // Проверка числа рёбер (параллельно)
        int localEdgeCount = 0;
        for (int i = rank; i < n; i += size) {
            for (int j = i + 1; j < n; j++) { // Проверяем только верхний треугольник
                if (adjacencyMatrix[i][j] == 1) {
                    localEdgeCount++;
                }
            }
        }

        int[] edgeCountBuffer = new int[1];
        edgeCountBuffer[0] = localEdgeCount;
        int[] globalEdgeCountBuffer = new int[1];
        MPI.COMM_WORLD.Reduce(edgeCountBuffer, 0, globalEdgeCountBuffer, 0, 1, MPI.INT, MPI.SUM, 0);

        // Проверка связности (поиск в ширину, выполняется на нулевом процессе)
        boolean isConnected = false;
        if (rank == 0) {
            boolean[] visited = new boolean[n];
            dfs(0, adjacencyMatrix, visited);

            isConnected = true;
            for (boolean v : visited) {
                if (!v) {
                    isConnected = false;
                    break;
                }
            }

            int edgeCount = globalEdgeCountBuffer[0];
            if (isConnected && edgeCount == n - 1) {
                System.out.println("Graph is Tree.");
            } else {
                System.out.println("Graph is not Tree.");
            }
        }

        MPI.Finalize();
    }

    // Обход в глубину
    private static void dfs(int node, int[][] adjacencyMatrix, boolean[] visited) {
        visited[node] = true;
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            if (adjacencyMatrix[node][i] == 1 && !visited[i]) {
                dfs(i, adjacencyMatrix, visited);
            }
        }
    }
}
