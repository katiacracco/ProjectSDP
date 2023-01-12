package Network;

import Dronazon.Order;
import ServerAmministratore.Drone;
import java.util.List;

// in execution on master
public class OrderThread extends Thread {
    Node master;

    public OrderThread(Node n) {
        master = n;
    }

    @Override
    public void run() {
        Drone d;
        Order o;

        try {
            while (true) {
                // waiting for an order
                if (master.getOrders().isEmpty()) {
                    synchronized (master.getLockWaitOrders()) {
                        master.getLockWaitOrders().wait();
                    }
                }

                // selecting an order and a drone
                o = master.getOrders().get(0);
                d = findDrone(master, o);

                // there is no drone available
                if (d == null) {
                    synchronized (master.getLockDelivery()) {
                        master.getLockDelivery().wait(); // there is a notify when a new drone enter in the network and at the end of a delivery
                    }
                } else {
                    // assigning the order to a drone
                    List<Drone> copy = master.getDrones();
                    for(Drone drone: copy) {
                        if (drone.getId() == d.getId()) {
                            drone.setFlagAvailable(false);
                        }
                    }
                    master.setDrones(copy);

                    // one thread for each delivery: the master can elaborate the orders while the deliveries are being made
                    DeliveringThread deliveries = new DeliveringThread(d, o, master);
                    deliveries.start();
                    master.removeOrder(o);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Drone findDrone(Node n, Order o) {
        Drone closer = n.getDrone(); // master
        int[] pos1 = o.getPickupPoint();
        int[] posMaster = n.getPos();
        int x = (posMaster[0] - pos1[0]) * (posMaster[0] - pos1[0]);
        int y = (posMaster[1] - pos1[1]) * (posMaster[1] - pos1[1]);
        double distance = Math.sqrt(x + y);

        List<Drone> copyD = n.getDrones();
        List<Grid> copyG = n.getDronesPos();
        for (Drone d: copyD) {
            for (Grid dd: copyG) {
                if (d.getId() == dd.getId() && d.getFlagAvailable()) {
                    int[] pos2 = dd.getPos();
                    x = (pos2[0] - pos1[0]) * (pos2[0] - pos1[0]);
                    y = (pos2[1] - pos1[1]) * (pos2[1] - pos1[1]);
                    if (Math.sqrt(x+y) < distance) { // d is closer to the delivery place
                        distance = Math.sqrt(x + y);
                        closer = d;
                    } else if (Math.sqrt(x+y) == distance) { // d and closer are at the same distance
                        if (d.getBattery() > closer.getBattery()) { // d has more battery than closer
                            closer = d;
                        } else if (d.getBattery() == closer.getBattery()) { // d and closer has the same battery
                            if (d.getId() >= closer.getId()) // d has an higher id or is the closer drone itself
                                closer = d;
                        }
                    }
                }
            }
        }

        // if the closer drone is the master but it is not available, then return null
        if (closer.getId() == n.getDrone().getId() && !n.getDrone().getFlagAvailable()) {
            closer = null;
        }

        return closer;
    }
}
