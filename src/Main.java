import NonBlockReceive.FilteringAndProbe;
import NonBlockReceive.TestReceive;
import matrixoperations.MatrixMultiplication;
import mpi.*;
import com.google.common.base.Stopwatch;

import java.lang.*;
import java.time.Duration;

public class Main {

    public static void main(String[] args) throws Exception, MPIException {
//        testCorrectMultiply(args);
        timeMultiply(args);
    }

    public static void timeMultiply(String[] args) {
        int[] sizes = new int[]{100, 1000, 2500, 5000};
        for (int size : sizes) {
            MatrixMultiplication mult = new MatrixMultiplication(size, size, size);
            mult.fillMatrixByRandom();
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                mult.imultiply(args);
            } finally {
                int rank = MPI.COMM_WORLD.Rank();
                if (rank == 0) {
                    Duration duration = stopwatch.elapsed();
                    System.out.println("Time: " + duration);
                }
            }
        }
    }

    public static void testCorrectMultiply(String[] args) {
        // убедиться, что работает
        MatrixMultiplication mult = new MatrixMultiplication(3, 3, 3);
        mult.fillMatrix(
                new int[][]{new int[]{1, 1, 12}, new int[]{2, 2, 2}, new int[]{3, 3, 3}},
                new int[][]{new int[]{2, 1, 3}, new int[]{3, 2, 3}, new int[]{3, 3, 2}}
        );
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            mult.syncMultiply(args);
        } finally {
            int rank = MPI.COMM_WORLD.Rank();
            if (rank == 0) {
                mult.printResultMatrix();
                Duration duration = stopwatch.elapsed();
                System.out.println("Time: " + duration);
            }
        }
    }

    public static void taskEvenNotEven() {
        int myrank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int TAG = 0;

        if ((myrank % 2) == 0) {
            // Процессы с четным рангом отправляют сообщение
            if ((myrank + 1) != size) {
                int dest = myrank + 1;
                System.out.println("Thread " + myrank + " send message to " + dest + "!");
                MPI.COMM_WORLD.Send(new int[]{myrank}, 0, 1, MPI.INT, myrank + 1, TAG);
            }
        } else {
            // Процессы с нечетным рангом принимают сообщение
            if (myrank != 0) {
                int[] receivedMessage = new int[1];
                MPI.COMM_WORLD.Recv(receivedMessage, 0, 1, MPI.INT, myrank - 1, TAG);
                System.out.println(
                        "Thread " + myrank + " received message! " +
                                "Message: " + receivedMessage[0]
                );
            }
        }
    }

    /**
     * Отправка сообщений по кольцу, блокирующая
     */
    public static void taskBlockMessageCircle() {
        int myrank = MPI.COMM_WORLD.Rank(); // Получение ранга процесса
        int size = MPI.COMM_WORLD.Size();   // Получение общего количества процессов
        int tag = 0; // Тег сообщений

        int[] sum = new int[1]; // Буфер для хранения суммы
        sum[0] = myrank; // Каждый процесс добавляет свой ранг к начальной сумме

        int[] recvSum = new int[1]; // Буфер для приема суммы

        // Определяем соседей для каждого потока
        int rightNeighbor = (myrank + 1) % size;        // Сосед справа
        int leftNeighbor = (myrank - 1 + size) % size;  // Сосед слева

        if (myrank == 0) {
            // Начальная отправка
            printSend(myrank, rightNeighbor);
            MPI.COMM_WORLD.Send(sum, 0, 1, MPI.INT, rightNeighbor, tag);
        } else {
            // Принимаем от соседа
            MPI.COMM_WORLD.Recv(recvSum, 0, 1, MPI.INT, leftNeighbor, tag);
            printReceived(myrank, leftNeighbor);

            // Обновляем текущую сумму
            sum[0] = recvSum[0] + myrank;
            MPI.COMM_WORLD.Send(sum, 0, 1, MPI.INT, rightNeighbor, tag);
            printSend(myrank, rightNeighbor);
        }
        if (myrank == 0) {
            MPI.COMM_WORLD.Recv(recvSum, 0, 1, MPI.INT, leftNeighbor, tag);
            printReceived(myrank, leftNeighbor);
            sum[0] = recvSum[0] + myrank;
            System.out.println("Total sum of all ranks: " + sum[0]);
        }
    }

    /**
     * Вывести сообщение об отправке
     *
     * @param from От кого
     * @param to   Кому
     */
    public static void printSend(int from, int to)
    {
        System.out.println(from + " send to: " + to);
    }

    /**
     * Вывести сообщение о получении
     *
     * @param to Кто принял
     * @param from   От кого
     */
    public static void printReceived(int to, int from)
    {
        System.out.println(to + " received from: " + from);
    }
}