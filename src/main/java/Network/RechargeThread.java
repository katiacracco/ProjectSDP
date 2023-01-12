package Network;

import ServerAmministratore.Drone;
import Util.Color;
import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class RechargeThread extends Thread {
    Node n;

    public RechargeThread(Node n) {
        this.n = n;
    }

    @Override
    public void run() {
        System.out.println(Color.ANSI_PURPLE + "Recharging drone" + n.getDrone().getId() + Color.ANSI_RESET);

        // check if there is another drone in charge
        ArrayList<Thread> threads = new ArrayList<>();
        List<Drone> drones = n.getDrones();
        for(Drone d: drones) {
            DistributedThread thread = new DistributedThread(n, d);
            threads.add(thread);
            thread.start();
        }

        // wait till every drone answer "OK"
        for(Thread t: threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        n.setFlagRecharge(true);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Color.ANSI_PURPLE + "RECHARGED" + Color.ANSI_RESET);
        int[] p = {0,0};
        n.setFlagRecharge(false);
        n.setWantRecharge(false);
        n.modifyBattery(100);
        n.setPos(p);
        n.modifyAvailabilty(true);

        // communicating the new position and the new battery level to the master
        updateMaster(n);
    }

    public static void updateMaster(Node m) {
        // connecting with master drone
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + m.getMaster()[1]).usePlaintext().build();
        NodeServiceGrpc.NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

        NodeServiceOuterClass.UpdateRequest req = NodeServiceOuterClass.UpdateRequest.newBuilder().setId(m.getDrone().getId()).setPos(NodeServiceOuterClass.UpdateRequest.Position.newBuilder().setX(0).setY(0)).setPow(100).build();

        stub.updating(req, new StreamObserver<NodeServiceOuterClass.UpdateResponse>() {
            @Override
            public void onNext(NodeServiceOuterClass.UpdateResponse response) {
            }

            @Override
            public void onError(Throwable throwable) {
                int[] master = m.getMaster();
                System.out.println("The master " + master[0] + " is crushed: update not sent");

                channel.shutdownNow();
            }

            @Override
            public void onCompleted() {
                channel.shutdownNow();
            }
        });
    }
}
