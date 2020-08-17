package org.acme.quarkus.sample;

import io.vertx.core.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps exceptions into corresponding response codes.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ErrorMapper implements ExceptionMapper<Exception> {

    /**
     * Return json containing code and error text.
     */
    @Override
    public Response toResponse(Exception exception) {
        int code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = exception.getMessage();

        if (exception instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        }

        JsonObject error = new JsonObject()
                .put("code", code)
                .put("reason", message);

        return Response.status(code).entity(error).build();
    }
}
