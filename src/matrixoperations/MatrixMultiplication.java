package matrixoperations;

import mpi.MPI;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;

public class MatrixMultiplication {
    int[][] matrixA;
    int[][] matrixB;
    int[][] resultMatrix;

    int n1, n2, n3;

    /**
     * Конструктор для операции перемножения матриц
     * Матрица A имеет размер n1 x n2
     * Матрица B имеет размер n2 x n3
     * Результирующая матрица будет иметь размер n1 x n3
     *
     */
    public MatrixMultiplication(int n1, int n2, int n3) {
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        matrixA = new int[n1][n2];
        matrixB = new int[n2][n3];
        resultMatrix = new int[n1][n3];
    }

    public void fillMatrixByRandom() {
        Random rand = new Random();
        fillMatrixByRandom(matrixA, rand);
        fillMatrixByRandom(matrixB, rand);
    }

    public void fillMatrix(int[][] matrixA, int[][] matrixB) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
    }

    public void multiply(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Процесс 0 делит и отправляет части A и B другим процессам
        if (rank == 0) {
            // Логика разбиения и рассылки для A и B по процессам

            // Отправка частей матрицы A
            for (int row = 0, iteration = 1; row < n1; row++, iteration++) {
                int[] rowToSend = matrixA[row];
                int rankRecipient = iteration % size;
                if (rankRecipient == 0) {
                    iteration++;
                    rankRecipient++;
                }
                System.out.println(
                        "Send " + row + "(" + Arrays.toString(rowToSend) + ")" + " row to " + rankRecipient + " rank"
                );
                MPI.COMM_WORLD.Send(rowToSend, 0, n2, MPI.INT, rankRecipient, 0);
            }

            // Отправка частей матрицы B
            for (int col = 0, iteration = 1; col < n3; col++, iteration++) {
                int rankRecipient = iteration % size;
                if (rankRecipient == 0) {
                    iteration++;
                    rankRecipient++;
                }
                int[] column_to_send = new int[n2];
                for (int row_index = 0; row_index < n2; row_index++) {
                    column_to_send[row_index] = matrixB[row_index][col];
                }
                System.out.println(
                        "Send column " + col+ "(" + Arrays.toString(column_to_send) + ")"
                                + " to " + rankRecipient + " rank"
                );
                MPI.COMM_WORLD.Send(column_to_send, 0, n2, MPI.INT, rankRecipient, 1);
            }
        } else {
            // Получаем части матрицы A
            int[][] localRows = new int[n1][n2];
            for (int row = 0, iteration = 1; row < n1; row++, iteration++) {
                int rankRecipient = iteration % size;
                if (rankRecipient == 0) {
                    iteration++;
                    rankRecipient++;
                }
                if (rank == rankRecipient) {
                    MPI.COMM_WORLD.Recv(localRows[row], 0, n2, MPI.INT, 0, 0);
                    System.out.println(
                            "Rank " + rank + " get row " + row +
                            "(" + Arrays.toString(localRows[row]) + ")"
                            );
                }
            }

            // Получаем части матрицы B
            int[][] localColumns = new int[n3][n2];
            for (int col = 0, iteration = 1; col < n3; col++, iteration++) {
                int rankRecipient = iteration % size;
                if (rankRecipient == 0) {
                    iteration++;
                    rankRecipient++;
                }
                if (rank == rankRecipient) {
                    MPI.COMM_WORLD.Recv(localColumns[col], 0, n2, MPI.INT, 0, 1);
                    System.out.println(
                            "Rank " + rank + " get column " + col +
                                    "(" + Arrays.toString(localColumns[col]) + ")"
                    );
                }
            }

            for (int iteration = 0, rankSenderCounter = 0; iteration < n1; iteration++, rankSenderCounter++) {
                int rankSender = rankSenderCounter % size;
                if (rankSender == 0) {
                    rankSenderCounter++;
                    rankSender++;
                }
                if (rankSender != rank) {
                    continue;
                }
                int[] localRow = localRows[iteration];
                int[] localColumn = localColumns[iteration];
                int[] localRowForResult = new int[n1];

                for (int i = 0; i < n1; i++) {
                    localRowForResult[i] = 0;
                    for (int j = 0; j < n1; j++) {
                        localRowForResult[i] += localRow[j] * localColumn[j];
                    }
                }
                System.out.println("Send local Row Result" + Arrays.toString(localRowForResult) + " from " + rank);
                MPI.COMM_WORLD.Send(localRowForResult, 0, n1, MPI.INT, 0, 0);
            }
        }

        // Сбор локальных результатов на нулевом процессе
        if (rank == 0) {
            for (int iteration = 0, rankSenderCounter = 0; iteration < n1; iteration++, rankSenderCounter++) {
                int rankSender = rankSenderCounter % size;
                if (rankSender == 0) {
                    rankSenderCounter++;
                    rankSender++;
                }
                MPI.COMM_WORLD.Recv(resultMatrix[iteration], 0, n1, MPI.INT, rankSender, 0);
            }
        }

        MPI.Finalize();
    }

    public void printResultMatrix() {
        System.out.println("Result matrix C:");
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n3; j++) {
                System.out.print(resultMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    static private void fillMatrixByRandom(int[] @NotNull [] matrix, Random rand) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = rand.nextInt(100); // случайные числа от 0 до 99
            }
        }
    }
}
