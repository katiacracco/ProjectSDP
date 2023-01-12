package ServerAmministratore;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.sql.Timestamp;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ClientAm {

    public static void main(String args[]){
        Client client = Client.create();
        String serverAddress = "http://localhost:1234";
        ClientResponse clientResp = null;

        Scanner scanner = new Scanner(System.in);
        String t = null;
        long t1 = 0;
        long t2 = 0;
        int input = 10;
        while(input != 0) {
            do {
                System.out.println("\nChoose between the following services:\n" +
                            "1- get the list of drones\n2- get the last n statistics\n" +
                            "3- get the average of deliveries\n4- get the average of kilometers\n" +
                            "0- exit");
                try {
                input = scanner.nextInt();
                } catch (InputMismatchException ex) {
                    System.out.println("Expected input is an integer. [" + ex + "]");
                    scanner.nextLine();
                    input = 10;
                }
            } while (input > 4);

            switch(input) {
                case 1:
                    // GET - get drones
                    String getPathD = "/drone/get/drones";
                    clientResp = getRequest(client, serverAddress + getPathD);
                    DronesList dr = clientResp.getEntity(DronesList.class);
                    List<Drone> drones = dr.getDrones();
                    if (drones.size() == 0) {
                        System.out.println("There are no drones");
                    } else if (drones.size() == 1) {
                        System.out.println("There is one drone");
                    } else {
                        System.out.println("There are " + drones.size() + " drones");
                    }
                    for (Drone d : drones) {
                        System.out.println("ID: " + d.getId() + " - IP: " + d.getIp() + " - PORT: " + d.getPort());
                    }
                    break;
                case 2:
                    int n = 20;
                    do {
                        System.out.println("How many statistics do you want?");
                        try {
                            n = scanner.nextInt();
                        } catch (InputMismatchException ex) {
                            System.out.println("Expected input is an integer between 0 and 10. [" + ex + "]\n");
                            scanner.nextLine();
                        }
                    } while(!(n>=0 && n<=10));

                    // GET - get n stats
                    String getPathS = "/drone/get/stats/" + n;
                    clientResp = getRequest(client, serverAddress + getPathS);
                    StatsHistory sh = clientResp.getEntity(StatsHistory.class);
                    List<Stats> lastStats = sh.getLastNstats(n);
                    for (Stats s : lastStats) {
                        System.out.println("Number of Deliveries: " + String.format("%.2f", s.getDeliveries()) + "\tNumber of Kilometers: " + String.format("%.2f", s.getKilometers()) +
                                "\tLevel of Pollution: " + String.format("%.2f", s.getPollutionLev()) + "\tLevel of Battery: " + String.format("%.2f", s.getBatteryLev()) + " ts " + s.getTimestamp());
                    }
                    break;
                case 3:
                    scanner.nextLine(); // emptying the scanner

                    do {
                        System.out.println("Write the first timestamp (yyyy-mm-dd hh:mm:ss)");
                        t = scanner.nextLine();
                        if (!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                            System.out.println("The format must be yyyy-mm-dd hh:mm:ss");
                        }
                    } while(!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
                    t1 = Timestamp.valueOf(t).getTime();

                    do {
                        System.out.println("Write the second timestamp (yyyy-mm-dd hh:mm:ss)");
                        t = scanner.nextLine();
                        if (!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                            System.out.println("The format must be yyyy-mm-dd hh:mm:ss");
                        }
                    } while(!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
                    t2 = Timestamp.valueOf(t).getTime();

                    // GET - get deliveries avg
                    String getPathDel = "/drone/get/delivery/" + t1 + "/" + t2;
                    clientResp = getRequest(client, serverAddress + getPathDel);
                    float avgDel = Float.parseFloat(clientResp.getEntity(String.class));
                    System.out.println("Average of Deliveries between the two timestamps: " + String.format("%.2f", avgDel));
                    break;
                case 4:
                    scanner.nextLine(); // emptying the scanner

                    do {
                        System.out.println("Write the first timestamp (yyyy-mm-dd hh:mm:ss)");
                        t = scanner.nextLine();
                        if (!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                            System.out.println("The format must be yyyy-mm-dd hh:mm:ss");
                        }
                    } while(!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
                    t1 = Timestamp.valueOf(t).getTime();

                    do {
                        System.out.println("Write the second timestamp (yyyy-mm-dd hh:mm:ss)");
                        t = scanner.nextLine();
                        if (!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                            System.out.println("The format must be yyyy-mm-dd hh:mm:ss");
                        }
                    } while(!t.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
                    t2 = Timestamp.valueOf(t).getTime();

                    // GET - get kilometers avg
                    String getPathKm = "/drone/get/kilometers/" + t1 + "/" + t2;
                    clientResp = getRequest(client, serverAddress + getPathKm);
                    float avgKm = Float.parseFloat(clientResp.getEntity(String.class));
                    System.out.println("Average of Kilometers between the two timestamps: " + String.format("%.2f", avgKm));
                    break;
                case 0:
                    break;
            }
        }
    }

    public static ClientResponse getRequest(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").accept("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server unavailable");
            return null;
        }
    }
}
