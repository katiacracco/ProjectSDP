package Network;

import Util.DeleteRequest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class ClosureThread extends Thread {
    Node node;

    public ClosureThread(Node n) {
        node = n;
    }

    @Override
    public void run() {
        System.out.println("Starting the closing procedure for drone " + node.getDrone().getId());

        // if the node is participating in an election, it waits to exit
        if (node.getFlagParticipant()) {
            synchronized (node.getLockElection()) {
                try {
                    node.getLockElection().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (node.getFlagMaster()) {
            masterExit(node);
        } else {
            droneExit(node);
        }
    }

    public static void droneExit(Node n) {
        // finishing a delivery
        waitDel(n);

        // exiting the network through SA DELETE
        exitSA(n);

        // closing communications with other drones
        System.exit(0);
    }

    public static void masterExit(Node n) {
        // finishing a delivery
        waitDel(n);

        // disconnecting the master from the broker MQTT
        synchronized (n.getLockMqtt()) {
            n.getLockMqtt().notify();
        }

        // waiting for the order queue to be empty
        if (!n.getOrders().isEmpty()) {
            synchronized (n.getLockEmpty()) {
                try {
                    n.getLockEmpty().wait(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // sending global statistics to the SA if they have not just been sent
        if (n.getDel() > 0) {
            n.sendGlobalStats();
        }

        // exiting the network through SA DELETE
        exitSA(n);

        // closing communications with other drones
        System.exit(0);
    }

    public static void waitDel(Node node) {
        // waiting to end the delivery - quit
        if(node.getDrone().getBattery() >= 15.0 && !node.getDrone().getFlagAvailable()) {
            synchronized (node.getLockEndDelivery()) {
                try {
                    node.getLockEndDelivery().wait();
                    // preventing the drone from taking other deliveries
                    node.modifyAvailabilty(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // preventing the drone from taking other deliveries
            node.modifyAvailabilty(false);
        }

    }

    public static void exitSA(Node node) {
        System.out.println("Exiting the drone " + node.getDrone().getId() + " from the network...");
        String serverAddress = "http://localhost:1234";
        String deletePath = "/drone/delete/" + node.getDrone().getId();
        Client clientDrone = Client.create();
        ClientResponse response = DeleteRequest.deleteDrone(clientDrone, serverAddress + deletePath, node.getDrone().getId());
        //System.out.println("Drone cancellation status: " + response.getStatus());
    }

}
