package Network;

import ServerAmministratore.Drone;
import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceGrpc.NodeServiceStub;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class DistributedThread extends Thread {
    Node node;
    Drone drone;

    public DistributedThread(Node node, Drone drone) {
        this.node = node;
        this.drone = drone;
    }

    @Override
    public void run() {
        recharge(node, drone);
    }

    public static void recharge(Node n, Drone d) {
        // connecting with another drone
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + d.getPort()).usePlaintext().build();
        NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

        long ts = System.currentTimeMillis();
        n.setRechargeTs(ts);
        RechargeRequest request = RechargeRequest.newBuilder().setR("charger").setId(n.getDrone().getId()).setTs(ts).build();

        stub.recharging(request, new StreamObserver<RechargeResponse>() {
            @Override
            public void onNext(RechargeResponse response) {
                //System.out.println("Drone" + d.getId() + ": OK");
            }

            @Override
            public void onError(Throwable throwable) {
                // if the drone with which the node is communicating is crashed, we simply close the channel
                channel.shutdownNow();
            }

            @Override
            public void onCompleted() {
                channel.shutdownNow();
            }
        });

    }
}
