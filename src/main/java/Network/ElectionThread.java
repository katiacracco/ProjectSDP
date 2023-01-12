package Network;

import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceGrpc.NodeServiceStub;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;

public class ElectionThread extends Thread {
    Node node;

    public ElectionThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        election(node);
    }

    // sending the first message of an election
    public static void election(Node n) {
        // connecting to the next node
        int[] next = n.getNext();
        n.setFlagParticipant(true);
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + next[1]).usePlaintext().build();
        NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

        ElectionRequest request = ElectionRequest.newBuilder().setEl("Election").setId(n.getDrone().getId()).setBat(n.getDrone().getBattery()).build();

        stub.election(request, new StreamObserver<ElectionResponse>() {
            @Override
            public void onNext(ElectionResponse response) {
            }

            @Override
            public void onError(Throwable t) {
                // if the next drone is down, throw an error
                System.out.println("The next drone [ID" + next[0] +"] is crushed");

                NewNextThread nnt = new NewNextThread(n);
                nnt.start();

                try {
                    nnt.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // after updating the ring, restart the election only if the crushed node wasn't the new master
                election(n);

                channel.shutdownNow();
            }

            @Override
            public void onCompleted() {
                channel.shutdownNow();
            }
        });

        // waiting for the answer coming from server
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
