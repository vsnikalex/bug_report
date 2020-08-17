package org.acme.quarkus.sample;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.container.ContainerRequestContext;
import org.jboss.logging.Logger;
import static org.jboss.logging.Logger.getLogger;

public class RequestReader {

    private static final Logger LOGGER = getLogger(RequestReader.class);

    // Added due to a Sonar rule: utility classes should not have public constructors
    private RequestReader() {

    }

    public static void logPathParameters(ContainerRequestContext requestContext) {
        requestContext.getUriInfo().getPathParameters()
                .forEach((k, v) -> LOGGER.debugf("Path Parameter Name: %s, Value: %s", k, v));
    }

    public static void logQueryParameters(ContainerRequestContext requestContext) {
        requestContext.getUriInfo().getQueryParameters()
                .forEach((k, v) -> LOGGER.debugf("Query Parameter Name: %s, " + "Value: %s", k, v));
    }

    public static void logRequestHeader(ContainerRequestContext requestContext) {
        LOGGER.debugf("----Start Header Section of request ----");
        requestContext.getHeaders().forEach((k, v) -> LOGGER.debugf("Header Name: %s, Header Value: %s ", k, v));
        LOGGER.debugf("----End Header Section of request ----");
    }

    public static void logRequestBody(ContainerRequestContext requestContext) {
        String body = null;
        try {
            body = readBody(requestContext);
        } catch (IOException e) {
            LOGGER.warnf("Exception occurred while reading request body: %s", e.getMessage());
        }
        LOGGER.infof("Request body: %s", body);
    }

    public static String readBody(ContainerRequestContext requestContext) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final InputStream inputStream = requestContext.getEntityStream();
        final StringBuilder builder = new StringBuilder();

        ByteStreams.copy(inputStream, outStream);
        byte[] requestEntity = outStream.toByteArray();
        if (requestEntity.length > 0) {
            builder.append(new String(requestEntity));
        }
        requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));

        return builder.toString();
    }
}
