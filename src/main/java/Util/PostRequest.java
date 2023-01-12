package Util;

import ServerAmministratore.Drone;
import ServerAmministratore.Stats;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class PostRequest {

    public static ClientResponse postDrone(Client client, String url, Drone d) {
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(d);
        try {
            return webResource.type("application/json").accept("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            System.out.println("Server not available");
            return null;
        }
    }

    public static ClientResponse postStats(Client client, String url, Stats s) {
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(s);
        try {
            return webResource.type("application/json").accept("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            System.out.println("Server not available");
            return null;
        }
    }
}
