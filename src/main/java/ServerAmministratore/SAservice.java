package ServerAmministratore;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Random;

@Path("/drone")
public class SAservice {
    // INTERFACE FOR DRONES

    // insert a new drone in the smart-city
    @Path("add/drone")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addDrone(Drone d) {
        Random random = new Random();
        Drone drone = DronesList.getInstance().getByID(d.getId());
        int[] pos = {random.nextInt(10), random.nextInt(10)};
        if(drone == null) {
            DronesList.getInstance().add(d);
            final ComplexResult result = new ComplexResult(DronesList.getInstance().getDrones(), pos);
            return Response.ok(result).build(); // return the drones list
        } else
            return Response.status(Response.Status.fromStatusCode(409)).build(); // conflict
    }

    // remove a drone from the smart-city
    @Path("delete/{droneID}")
    @DELETE
    @Produces({"application/json", "application/xml"})
    public Response removeDrone(int ID) {
        Drone drone = DronesList.getInstance().getByID(ID);
        if(drone != null) {
            DronesList.getInstance().remove(drone);
            return Response.ok().build();
        } else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    // insert new statistics
    @Path("add/stats")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addStats(Stats s) {
        StatsHistory.getInstance().add(s);
        return Response.ok(StatsHistory.getInstance()).build();
    }

    // INTERFACE FOR ADMINISTRATORS

    // get information on drones of the smart-city
    @Path("get/drones")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getDroneList() {
        return Response.ok(DronesList.getInstance()).build();
    }

    // get information on statistics of the smart-city
    @Path("get/stats/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getStatsHistory(@PathParam("n") int n) {
        return Response.ok(StatsHistory.getInstance()).build();
    }

    // get the average number of deliveries between two timestamps
    @Path("get/delivery/{t1}/{t2}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getDeliveriesAvg(@PathParam("t1") long t1, @PathParam("t2") long t2) {
        float del = StatsHistory.getInstance().getDelBetweenTimestamp(t1, t2);
        return Response.ok(String.valueOf(del)).build();
    }

    // get the average of kilometers traveled between two timestamps
    @Path("get/kilometers/{t1}/{t2}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getKmAvg(@PathParam("t1") long t1, @PathParam("t2") long t2) {
        return Response.ok(String.valueOf(StatsHistory.getInstance().getKmBetweenTimestamp(t1,t2))).build();
    }
}
