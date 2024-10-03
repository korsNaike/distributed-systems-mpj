package NonBlockReceive;

import mpi.*;

import java.util.Arrays;

public class TestReceive {

    static public void startTest(String[] args) {
        MPI.Init(args);

        int[] data = new int[1];
        int[] buf = {1, 3, 5};
        int TAG = 0;
        Status st;

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            // Процесс 0 отправляет data процессу 2
            data[0] = 2016;
            MPI.COMM_WORLD.Send(data, 0, 1, MPI.INT, 2, TAG);
            System.out.println("Process 0 send to process 2: " + data[0]);
        } else if (rank == 1) {
            // Процесс 1 отправляет buf процессу 2
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, 2, TAG);
            System.out.println("Process 1 send to process 2: " + Arrays.toString(buf));
        } else if (rank == 2) {
            // Процесс 2 получает данные от процесса 0
            MPI.COMM_WORLD.Recv(data, 0, 1, MPI.INT, 0, TAG);
            System.out.println("Process 2 received from process 0: " + data[0]);

            // Процесс 2 получает данные от процесса 1
            int[] recvBuf = new int[buf.length];
            MPI.COMM_WORLD.Recv(recvBuf, 0, buf.length, MPI.INT, 1, TAG);
            System.out.print("Process 2 received from process 1: " + Arrays.toString(recvBuf));
            System.out.println();
        }

        MPI.Finalize();
    }
}
