package org.acme.quarkus.sample;

import io.vertx.core.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1")
public class TroubleTicketResource {

    @POST
    @Path("/troubleTicket")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello(String troubleTicket) {
        JsonObject ok = new JsonObject()
                .put("code", 200)
                .put("reason", "Trouble Ticket is valid");

        return Response.status(200).entity(ok).build();
    }
}