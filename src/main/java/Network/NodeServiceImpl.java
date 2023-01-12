package Network;

import Dronazon.SubMasterThread;
import ServerAmministratore.Drone;
import Util.Color;
import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceGrpc.NodeServiceImplBase;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;

// server side - service
public class NodeServiceImpl extends NodeServiceImplBase {
    Node mySelf;

    public NodeServiceImpl(Node n) {
        mySelf = n;
    }

    // each drone inserts the new one in the network
    @Override
    public void presentation(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

        Drone d = mySelf.getDrone();
        if (request.getId() != d.getId()) {
            System.out.println(Color.ANSI_BLUE + "Inserting drone" + request.getId() + Color.ANSI_RESET);

            mySelf.addDrones(new Drone(request.getId(), "localhost", request.getPort()));

            // finding the next drone
            int[] n = {100,0};
            List<Drone> list = mySelf.getDrones();
            if ((request.getId() < d.getId()) ||
                    (list.get(list.size() - 1).getId() == request.getId()
                            && list.get(0).getId() == d.getId())) {
                n[0] = d.getId();
                n[1] = d.getPort();
            }

            // if there is an election in progress, wait until it ends
            boolean flag = mySelf.getFlagParticipant();
            if (flag) {
                synchronized (mySelf.getLockAsk()) {
                    try {
                        mySelf.getLockAsk().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // sending the id master
            int[] master = mySelf.getMaster();

            HelloResponse response = HelloResponse.newBuilder().setMaster(master[0]).setNext(HelloResponse.Next.newBuilder().setId(n[0]).setPort(n[1])).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            Context.current().fork().run(new Runnable() {
                @Override
                public void run() {
                    // updating the network
                    int[] next = mySelf.getNext();
                    if (request.getId() > d.getId() && (request.getId() < next[0] ||
                            (next[0] <= d.getId() && (mySelf.getDrones().get(mySelf.getDrones().size() - 1).getId() == request.getId()
                            && mySelf.getDrones().get(0).getId() == next[0])))
                            || (mySelf.getDrones().get(mySelf.getDrones().size() - 1).getId() == d.getId()
                                    && mySelf.getDrones().get(0).getId() == request.getId())) {
                        next[0] = request.getId();
                        next[1] = request.getPort();
                        mySelf.setNext(next);
                        System.out.println("Next: " + next[0]);
                    }

                    if (mySelf.getFlagMaster()) {
                        // saving the position of the new drone in the master
                        int[] p = new int[2];
                        p[0] = request.getPos().getX();
                        p[1] = request.getPos().getY();
                        Grid g = new Grid(request.getId(), p);
                        mySelf.addDronesPos(g);

                        // notifying the master that there is a new drone
                        synchronized (mySelf.getLockDelivery()) {
                            mySelf.getLockDelivery().notify();
                        }
                    }
                }
            });
        }
    }

    // a drone making a delivery
    @Override
    public void delivering(DeliveryRequest request, StreamObserver<DeliveryResponse> responseObserver) {
        mySelf.modifyAvailabilty(false);
        float bat = mySelf.getDrone().getBattery();
        if (bat >= 15.0 && !mySelf.getWantRecharge()) {
            //System.out.println("Flag want recharge: " + mySelf.getWantRecharge());
            System.out.println("Delivering order " + request.getIdOrder());
            try {
                Thread.sleep(5000); // time for delivering
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            long timestamp = System.currentTimeMillis();
            float newBat = Math.round((bat - bat * 0.1f) * 100.0f) / 100.0f;
            mySelf.modifyBattery(newBat);
            //System.out.println("Battery: " + mySelf.getDrone().getBattery() + "\n");

            // checking the battery level
            if (newBat < 15.0) { // if the battery is lower than 15%, then close the drone
                if (mySelf.getFlagClosure()) {
                    System.out.println("Closing procedure already in progress");
                } else {
                    mySelf.setFlagClosure(true);
                    mySelf.modifyAvailabilty(false);
                    ClosureThread exit = new ClosureThread(mySelf);
                    exit.start();
                }
            } else { // otherwise make the drone available for another order
                if (!mySelf.getWantRecharge()) {
                    mySelf.modifyAvailabilty(true);
                }
            }

            DeliveryResponse response = DeliveryResponse.newBuilder().setBattery(newBat).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            Context.current().fork().run(new Runnable() {
                @Override
                public void run() {
                    // computing km traveled
                    int[] p = new int[2];
                    p[0] = request.getPickUp().getX();
                    p[1] = request.getPickUp().getY();
                    int[] d = new int[2];
                    d[0] = request.getDel().getX();
                    d[1] = request.getDel().getY();
                    int[] pos = mySelf.getPos();
                    float km = 0;

                    int x = (p[0] - pos[0]) * (p[0] - pos[0]);
                    int y = (p[1] - pos[1]) * (p[1] - pos[1]);
                    float distance = (float)Math.round(Math.sqrt(x + y) * 100.0f) / 100.0f;
                    km += distance;

                    x = (d[0] - p[0]) * (d[0] - p[0]);
                    y = (d[1] - p[1]) * (d[1] - p[1]);
                    distance = (float)Math.round(Math.sqrt(x + y) * 100.0f) / 100.0f;
                    km += distance;

                    // updating statistics
                    mySelf.setPos(d);
                    mySelf.setDelTimestamp(timestamp);
                    mySelf.addNumDel(1);
                    mySelf.addDelKm(km);

                    // sending statistics
                    StatsThread stats = new StatsThread(mySelf);
                    stats.start();

                    try {
                        stats.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (mySelf.getLockEndDelivery()) {
                        mySelf.getLockEndDelivery().notify();
                    }

                }
            });
        } else if (bat < 15.0) {
            System.out.println("The battery of drone" + mySelf.getDrone().getId() + " is less than 15%");
            mySelf.modifyAvailabilty(false);
            if (mySelf.getFlagClosure()) {
                System.out.println("Closing procedure already in progress");
            } else {
                mySelf.setFlagClosure(true);
                ClosureThread exit = new ClosureThread(mySelf);
                exit.start();
            }
            responseObserver.onError(new Exception());
        } else if (mySelf.getWantRecharge()) {
            System.out.println("\nThe drone" + mySelf.getDrone().getId() + " is charging, it cannot deliver the order\n");
            responseObserver.onError(new Exception());
        }
    }

    // when a drone receive a message of election
    @Override
    public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
        //System.out.println(Color.ANSI_CYAN + "Message: <" + request.getEl() + ", ID(" + request.getId() + ")>" + Color.ANSI_RESET);
        int master = 0;
        float myBat = mySelf.getDrone().getBattery();
        int myId = mySelf.getDrone().getId();
        String e = "";

        // election algorithm
        if (request.getEl().equalsIgnoreCase("election")) {
            if (request.getId() == myId) {
                e = "Elected";
                master = request.getId();
            } else if ((request.getBat() < myBat) || (request.getBat() == myBat && request.getId() < myId)) {
                e = "Election";
                if (mySelf.getFlagParticipant()) {
                    return; // does not forward the message
                } else {
                    master = myId;
                    mySelf.setFlagParticipant(true);
                }
            } else if ((request.getBat() > myBat) || (request.getBat() == myBat && request.getId() > myId)) {
                e = "Election";
                master = request.getId();
                if (!mySelf.getFlagParticipant())
                    mySelf.setFlagParticipant(true);
            }
        } else if (request.getEl().equalsIgnoreCase("elected")) {
            int[] newMaster = new int[2];
            newMaster[0] = request.getId();
            List<Drone> copy = mySelf.getDrones();
            for(Drone d: copy) {
                if(d.getId() == newMaster[0])
                    newMaster[1] = d.getPort();
            }
            mySelf.setMaster(newMaster);
            e = request.getEl();
            master = request.getId();
        }

        String el = e;
        int m = master;

        ElectionResponse response = ElectionResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        Context.current().fork().run(new Runnable() {
            @Override
            public void run() {
                // forwarding the election/elected message
                forwardingMsg(mySelf, request, el, m, myBat);

                // election algorithm
                if ((request.getEl().equalsIgnoreCase("election") && (request.getId() == myId)) || (request.getEl().equalsIgnoreCase("elected"))) {
                    mySelf.setFlagParticipant(false);
                    synchronized (mySelf.getLockElection()) {
                        mySelf.getLockElection().notify();
                    }
                    synchronized (mySelf.getLockPing()) {
                        mySelf.getLockPing().notify();
                    }
                    synchronized (mySelf.getLockAsk()) {
                        mySelf.getLockAsk().notify();
                    }
                }
            }
        });
    }

    // the master receives the statistics
    @Override
    public void sending(StatsRequest request, StreamObserver<StatsResponse> responseObserver) {
        System.out.println("Statistics received from drone" + request.getId() + "\n");

        StatsResponse response = StatsResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        Context.current().fork().run(new Runnable() {
            @Override
            public void run() {
                // updating the position of the drone, in the master list
                List<Grid> grid = mySelf.getDronesPos();
                Grid gg = null;
                for (Grid g: grid) {
                    if (g.getId() == request.getId()) {
                        gg = g;
                    }
                }
                grid.remove(gg);

                int[] pos = new int[2];
                pos[0] = request.getPos().getX();
                pos[1] = request.getPos().getY();
                Grid update = new Grid(request.getId(), pos);
                grid.add(update);
                mySelf.setDronesPos(grid);

                // updating the level battery
                List<Drone> copy = mySelf.getDrones();
                for (Drone drone: copy) {
                    if (drone.getId() == request.getId()) {
                        drone.setBattery(request.getPow());
                    }
                }
                mySelf.setDrones(copy);

                // computing global statistics
                mySelf.addDel(1);
                mySelf.addKm(request.getKm());
                mySelf.addPoll(request.getAvgList());
                mySelf.addPow(request.getPow());

                // notifying that the order queue is empty
                if (mySelf.getOrders().isEmpty()) {
                    synchronized (mySelf.getLockEmpty()) {
                        mySelf.getLockEmpty().notify();
                    }
                }
            }
        });
    }

    // check if the following node is still up
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        //System.out.println(Color.ANSI_GREEN + "\n\t*** " + request.getS() + " received ***\n" + Color.ANSI_RESET);
        PingResponse response = PingResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // drones tell if they are recharging
    @Override
    public void recharging(RechargeRequest request, StreamObserver<RechargeResponse> responseObserver) {
        try {
            if (mySelf.getFlagRecharge()) { // if I'm already charging, the other drone has to wait
                if (!(mySelf.getDrone().getId() == request.getId())) {
                    synchronized (mySelf.getLockRecharge()) {
                        mySelf.getLockRecharge().wait();
                    }
                }
            } else if (!mySelf.getFlagRecharge() && mySelf.getWantRecharge()) { // if I'm not recharging but I want to, I have to compare the timestamps
                if (request.getTs() > mySelf.getRechargeTs()) {
                    synchronized (mySelf.getLockRecharge()) {
                        mySelf.getLockRecharge().wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RechargeResponse response = RechargeResponse.newBuilder().setM("ok").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // the master receives the new position and the new battery level of a drone
    @Override
    public void updating(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
        System.out.println(Color.ANSI_PURPLE + "\nThe drone" + request.getId() + " is been recharged\n" + Color.ANSI_RESET);
        UpdateResponse response = UpdateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        Context.current().fork().run(new Runnable() {
            @Override
            public void run() {
                // updating the position of the drone, in the master list
                List<Grid> grid = mySelf.getDronesPos();
                Grid gg = null;
                for (Grid g: grid) {
                    if (g.getId() == request.getId()) {
                        gg = g;
                    }
                }
                grid.remove(gg);

                int[] pos = new int[2];
                pos[0] = request.getPos().getX();
                pos[1] = request.getPos().getY();
                Grid update = new Grid(request.getId(), pos);
                grid.add(update);
                mySelf.setDronesPos(grid);

                // updating the battery level of the drone
                List<Drone> drones = mySelf.getDrones();
                for (Drone d: drones) {
                    if (d.getId() == request.getId()) {
                        d.setBattery(request.getPow());
                        d.setFlagAvailable(true);
                    }
                }
                mySelf.setDrones(drones);

                // notifying the master that a drone is available
                synchronized (mySelf.getLockDelivery()) {
                    mySelf.getLockDelivery().notify(); // there is a notify when a new drone enter in the network and at the end of a delivery
                }
            }
        });
    }

    public static void forwardingMsg(Node n, ElectionRequest r, String e, int m, float bat) {
        if (r.getEl().equalsIgnoreCase("elected") && r.getId() == n.getDrone().getId()) { // last message of the election
            n.setFlagMaster(true);

            // storing the position of the drones in the network and the battery level
            List<Grid> list = new ArrayList<>();
            int id = 0;
            List<ElectionRequest.Pos> pp = r.getPList();
            List<Drone> copyList = n.getDrones();
            for (int p=0; p<pp.size(); p++) {
                Grid g = new Grid();
                int[] pos = new int[2];
                id = pp.get(p).getId();
                pos[0] = pp.get(p).getPos().getX();
                pos[1] = pp.get(p).getPos().getY();
                g.setId(id);
                g.setPos(pos);
                list.add(g);
                for (Drone d: copyList) {
                    if (d.getId() == id) {
                        d.setBattery(pp.get(p).getPow());
                    }
                }
            }
            n.setDronesPos(list);
            n.setDrones(copyList);

            System.out.println(Color.ANSI_YELLOW + "I am the new master [drone" + r.getId() + "]" + Color.ANSI_RESET);
            SubMasterThread sub = new SubMasterThread(n);
            sub.start();
        } else { // message to be forwarded for the election
            int[] next = n.getNext();
            final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + next[1]).usePlaintext().build();
            NodeServiceGrpc.NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

            ElectionRequest newRequest = ElectionRequest.newBuilder().setEl(e).setId(m).setBat(bat).build();
            // adding drone's position to the list for the new master
            if (e.equalsIgnoreCase("elected")) {
                int[] pos = n.getPos();
                ElectionRequest.Builder req = ElectionRequest.newBuilder();
                req.setEl(e).setId(m).setBat(bat).build();

                List<ElectionRequest.Pos> l = r.getPList();
                for (ElectionRequest.Pos p: l) {
                    req.addP(p);
                }
                req.addP(ElectionRequest.Pos.newBuilder().setId(n.getDrone().getId()).setPow(bat).setPos(ElectionRequest.Pos.Position.newBuilder().setX(pos[0]).setY(pos[1])));
                newRequest = req.build();

                if (m != n.getDrone().getId()) {
                    System.out.println(Color.ANSI_YELLOW + "The drone" + m + " is the new master" + Color.ANSI_RESET);
                }
            }

            stub.election(newRequest, new StreamObserver<ElectionResponse>() {
                @Override
                public void onNext(ElectionResponse value) {
                    // it is empty because it is the gRPC response that returns to the drone that sent the gRPC request
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

                    // after updating the ring, resend the message only if the crushed node wasn't the new master
                    if (e.equalsIgnoreCase("elected") && (m == next[0])) {
                        channel.shutdownNow();
                    } else {
                        forwardingMsg(n, r, e, m, bat);
                        channel.shutdownNow();
                    }
                }

                @Override
                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

}
