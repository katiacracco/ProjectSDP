package Network;

import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceGrpc.NodeServiceStub;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;

public class PingThread extends Thread {
    Node n;

    public PingThread(Node n) {
        this.n = n;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(15000);
                ping(n);
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void ping(Node n) {
        if (n.getFlagParticipant()) {
            synchronized (n.getLockPing()) {
                try {
                    n.getLockPing().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        int[] next = n.getNext();
        if (next[0] != n.getDrone().getId() && next[0] != 100) {
            // connecting with the following drone
            final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + next[1]).usePlaintext().build();
            NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

            PingRequest request = PingRequest.newBuilder().setS("ping").build();

            stub.ping(request, new StreamObserver<PingResponse>() {
                @Override
                public void onNext(PingResponse response) {
                    // the next drone is still up, nothing has to happen
                }

                @Override
                public void onError(Throwable t) {
                    // searching the new next
                    NewNextThread nnt = new NewNextThread(n);
                    nnt.start();

                    // if the dropped drone is the master, an election must begin
                    int[] master = n.getMaster();
                    if (next[0] == master[0]) {
                        System.out.println("The master " + master[0] + " is down. Beginning an election!");
                        ElectionThread election = new ElectionThread(n);
                        election.start();
                    } else {
                        System.out.println("The drone " + next[0] + " is down");
                    }

                    try {
                        nnt.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    channel.shutdownNow();
                }

                @Override
                public void onCompleted() {
                    channel.shutdownNow();
                }
            });

            try {
                channel.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
