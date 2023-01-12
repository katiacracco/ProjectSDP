package Network;

import Dronazon.SubMasterThread;
import ServerAmministratore.ComplexResult;
import ServerAmministratore.Drone;
import Util.Color;
import Util.PostRequest;
import com.sun.jersey.api.client.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Client clientDrone = Client.create();
        ClientResponse response = null;

        // initialization of the drone
        System.out.println("\nInitializing drone...");
        int id = (int) (Math.random() * 50) + 1;
        int port = (int) (Math.random() * 5000) + 1025;
        Drone drone = new Drone(id, "localhost", port);

        Node node = new Node(drone);

        try {
            Server server = ServerBuilder.forPort(port).addService(new NodeServiceImpl(node)).build();
            server.start();
            System.out.println("Drone started!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // procedure to end the drone
        InputThread inThread = new InputThread(node);
        inThread.start();

        // registration of the drone through SA POST
        System.out.println("Registering the drone" + id + "...");
        String serverAddress = "http://localhost:1234";
        String postPath = "/drone/add/drone";
        response = PostRequest.postDrone(clientDrone, serverAddress + postPath, drone);
        // if there is already a drone with the same id, change the id
        if (response.getStatus() != 200) {
            do {
                id = (int) (Math.random() * 50) + 1;
                drone.setId(id);
                node.setDrone(drone);
                System.out.println("Registering the drone" + id + "...");
                response = PostRequest.postDrone(clientDrone, serverAddress + postPath, drone);
            } while(response.getStatus() != 200);
        }

        // receiving the list of drones present in the network and the position of the drone just inserted
        ComplexResult output = response.getEntity(ComplexResult.class);
        node.setDrones(output.getDrones());
        node.setPos(output.getPos());

        // insertion of the drone into the network
        if (node.getDrones().size() == 1) { // if there is only one drone, it is the master
            node.setFlagMaster(true);
            int[] master = new int[2];
            master[0] = id;
            master[1] = port;
            node.setMaster(master);
            System.out.println(Color.ANSI_YELLOW + "The drone" + master[0] + " is the master" + Color.ANSI_RESET);
            int[] p = node.getPos();
            Grid g = new Grid(id, p);
            node.addDronesPos(g);
            SubMasterThread sub = new SubMasterThread(node);
            sub.start();
        } else { // if there are more drones, the new one must present itself to the others
            System.out.println("Presenting the drone...");
            ArrayList<Thread> threads = new ArrayList<>();
            List<Drone> drones = node.getDrones();
            for(Drone d: drones) {
                RingThread thread = new RingThread(node, d);
                threads.add(thread);
                thread.start();
            }
            for(Thread t: threads) {
                t.join();
            }
            System.out.println("Next: " + node.getNext()[0]);

            if (node.getMaster()[0] == 0) {
                System.out.println("There is not a master. Beginning an election!");
                ElectionThread election = new ElectionThread(node);
                election.start();
            }
        }

        // starting the sensor for detecting air pollution
        PollutionThread air = new PollutionThread(node);
        air.start();

        // printing on the screen: number of deliveries, kilometers traveled, remaining battery
        PrintThread print = new PrintThread(node);
        print.start();

        // ping
        PingThread ping = new PingThread(node);
        ping.start();
    }
}
