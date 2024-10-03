import mpi.*;
import java.lang.*;

public class CircleMessage {
    int[] alreadyPrintReceived;
    int[] alreadyPrintSended;

    /**
     * Неблокирующая отправка
     */
    public void unblockTaskCircleMessage() {
        int rank = MPI.COMM_WORLD.Rank(); // Ранг текущего процесса
        int size = MPI.COMM_WORLD.Size(); // Общее количество процессов
        int buf = rank; // Каждый процессор записывает свой ранг в buf для отправки в сообщении
        int[] recvBuf = new int[1]; // Буфер для получения данных
        int s = 0; // Переменная для суммы рангов

        alreadyPrintReceived = new int[size];
        alreadyPrintSended = new int[size];


        // Переменная для хранения сумм
        if (rank == 0) {
            s = buf;
        }

        // Цикл передачи сообщений по кольцу
        for (int i = 0; i < size; i++) {
            int to = (rank + 1) % size; // Сосед справа
            int from = (rank - 1 + size) % size; // Сосед слева

            Request sendReq = MPI.COMM_WORLD.Isend(new int[]{buf}, 0, 1, MPI.INT, to, 99);
            if (addToArraySendedIfNotExists(from)) {
                Main.printSend(from, to);
            }
            Request recvReq = MPI.COMM_WORLD.Irecv(recvBuf, 0, 1, MPI.INT, from, 99);

            // Ждем завершения операций отправки и приема (синхронизируем отправку)
            sendReq.Wait();
            recvReq.Wait();
            if (addToArrayReceivedIfNotExists(to)) {
                Main.printReceived(to, from);
            }

            buf = recvBuf[0]; // Обновляем buf принятым значением
            s += buf; // Суммируем

            if (rank == 0 && i == size - 1) {
                break; // Нулевой процесс завершает цикл, когда просуммировал все ранги
            }
        }

        if (rank == 0) {
            System.out.println("Total sum of all ranks: " + s);
        }
    }

    public boolean addToArrayReceivedIfNotExists(int number) {
        for (int i = 0; i < alreadyPrintReceived.length; i++) {
            if (alreadyPrintReceived[i] == number) {
                return false;
            }
        }

        // Если число не найдено в массиве, добавляем его
        int[] newArr = new int[alreadyPrintReceived.length + 1];
        for (int i = 0; i < alreadyPrintReceived.length; i++) {
            newArr[i] = alreadyPrintReceived[i];
        }
        newArr[alreadyPrintReceived.length] = number;
        alreadyPrintReceived = newArr;
        return true;
    }

    public boolean addToArraySendedIfNotExists(int number) {
        for (int i = 0; i < alreadyPrintSended.length; i++) {
            if (alreadyPrintSended[i] == number) {
                return false;
            }
        }

        // Если число не найдено в массиве, добавляем его
        int[] newArr = new int[alreadyPrintSended.length + 1];
        for (int i = 0; i < alreadyPrintSended.length; i++) {
            newArr[i] = alreadyPrintSended[i];
        }
        newArr[alreadyPrintSended.length] = number;
        alreadyPrintSended = newArr;
        return true;
    }
}
