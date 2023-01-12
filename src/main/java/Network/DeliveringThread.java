package Network;

import Dronazon.Order;
import ServerAmministratore.Drone;
import com.grpc.NodeServiceGrpc;
import com.grpc.NodeServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeliveringThread extends Thread {
    Drone d;
    Order o;
    Node m;

    public DeliveringThread(Drone d, Order o, Node m) {
        this.d = d;
        this.o = o;
        this.m = m;
    }

    @Override
    public void run() {
        delivery(d, o, m);
    }

    public static void delivery(Drone d, Order o, Node m) {
        // connecting to the drone that has to deliver
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + d.getPort()).usePlaintext().build();
        NodeServiceGrpc.NodeServiceStub stub = NodeServiceGrpc.newStub(channel);

        // building the request with order id, drone id, pickup point and delivery point
        int[] pickUp = o.getPickupPoint();
        int[] del = o.getDeliveryPoint();
        DeliveryRequest request = DeliveryRequest.newBuilder().setIdOrder(o.getId()).setIdDrone(d.getId()).setPickUp(DeliveryRequest.Position.newBuilder().setX(pickUp[0]).setY(pickUp[1])).setDel(DeliveryRequest.Position.newBuilder().setX(del[0]).setY(del[1])).build();

        stub.delivering(request, new StreamObserver<DeliveryResponse>() {
            @Override
            public void onNext(DeliveryResponse response) {
                System.out.println("\nOrder " + o.getId() + " delivered");

                // if the drone battery is not discharge, it becomes available for a new delivery - notify the master
                if (!(response.getBattery() < 15.0)) {
                    List<Drone> copy = m.getDrones();
                    for(Drone drone: copy) {
                        if (drone.getId() == d.getId()) {
                            drone.setFlagAvailable(true);
                        }
                    }
                    m.setDrones(copy);
                    synchronized (m.getLockDelivery()) {
                        m.getLockDelivery().notify();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                if (d.getBattery() < 15.0) {
                    System.out.println("The drone " + d.getId() + " is crushed");

                    // if the dropped drone is the next, it will be removed from the ping,
                    // otherwise it is removed now from the master's drone list
                    if (m.getNext()[0] != d.getId()) {
                        List<Drone> copyD = m.getDrones();
                        int index = copyD.indexOf(d);
                        copyD.remove(index);
                        m.setDrones(copyD);

                        List<Grid> copyG = m.getDronesPos();
                        copyG.remove(index);
                        m.setDronesPos(copyG);
                    }
                }

                // the node is not available so the order was not processed
                m.addOrder(o);

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
