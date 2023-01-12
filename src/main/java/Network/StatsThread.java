package Network;

import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.List;

public class StatsThread extends Thread {
    Node node;

    public StatsThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        sendingStats(node);
    }

    public static void sendingStats(Node n) {
        int[] master = n.getMaster();
        int id = n.getDrone().getId();

        if (id != master[0]) {
            // connecting to the master
            final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + master[1]).usePlaintext().build();
            NodeServiceGrpc.NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

            // inserting in the request: timestamp, id, position, km, power
            int[] pos = n.getPos();
            StatsRequest.Builder req = StatsRequest.newBuilder();
            req.setTs(n.getDelTimestamp()).setId(id).setPos(StatsRequest.Position.newBuilder().setX(pos[0]).setY(pos[1])).setKm(n.getDelKm()).setPow(n.getDrone().getBattery()).build();
            // adding to the request: average level of pollution
            List<Float> avg = n.getPollAvgs();
            for (float a : avg) {
                req.addAvg(a);
            }
            n.emptyAvgs();
            StatsRequest request = req.build();

            stub.sending(request, new StreamObserver<StatsResponse>() {
                @Override
                public void onNext(StatsResponse value) {
                }

                @Override
                public void onError(Throwable t) {
                    int[] master = n.getMaster();
                    System.out.println("The master " + master[0] + " is crushed: statistics not sent");

                    //if (!n.getFlagParticipant()) {
                    channel.shutdownNow();

                }

                @Override
                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        } else {
            // updating the position of the drone, in the master list
            List<Grid> grid = n.getDronesPos();
            Grid gg = null;
            for (Grid g: grid) {
                if (g.getId() == id) {
                    gg = g;
                }
            }
            grid.remove(gg);

            int[] pos = n.getPos();
            Grid update = new Grid(id, pos);
            grid.add(update);
            n.setDronesPos(grid);

            // computing global statistics
            n.addDel(1);
            n.addKm(n.getDelKm());
            n.addPoll(n.getPollAvgs());
            n.addPow(n.getDrone().getBattery());
        }
    }
}
