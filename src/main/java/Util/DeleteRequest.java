package Util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class DeleteRequest {

    public static ClientResponse deleteDrone(Client client, String url, int id) {
        WebResource webRes = client.resource(url);
        String input = String.valueOf(id);
        try {
            return webRes.type("application/json").accept("application/json").delete(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            System.out.println("Server not available");
            return null;
        }
    }
}
