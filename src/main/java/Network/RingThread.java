package Network;

import ServerAmministratore.Drone;
import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceGrpc.NodeServiceStub;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RingThread extends Thread {
    Node node;
    Drone drone;

    public RingThread(Node n, Drone d) {
        node = n;
        drone = d;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        droneJoinRing(node, drone);
    }

    public static void droneJoinRing(Node n, Drone d) {
        // plaintext channel on the address (ip/port) which offers the Presentation service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + d.getPort()).usePlaintext().build();

        // creating an asynchronous stub on the channel
        NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

        // creating the HelloRequest object which will be provided as input to the RPC method
        int[] p = n.getPos();
        HelloRequest request = HelloRequest.newBuilder().setId(n.getDrone().getId()).setPort(n.getDrone().getPort()).setPos(HelloRequest.Position.newBuilder().setX(p[0]).setY(p[1])).build();  // get position cell array

        // calling the presentation method
        stub.presentation(request, new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse response) {
                // saving the master drone
                int[] m = new int[2];
                if (response.getMaster() > 0) {
                    m[0] = response.getMaster();
                    List<Drone> copy = n.getDrones();
                    for (Drone d : copy) {
                        if (d.getId() == m[0])
                            m[1] = d.getPort();
                    }
                    n.setMaster(m);
                }

                // computing the next drone
                int[] next = n.getNext();
                int ne = response.getNext().getId();
                if (ne < next[0]) {
                    next[0] = ne;
                    next[1] = response.getNext().getPort();
                    n.setNext(next);
                }
            }

            @Override
            public void onError(Throwable t) {
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
