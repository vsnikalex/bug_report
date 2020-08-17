package org.acme.quarkus.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.quarkus.sample.RequestReader;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import static org.jboss.logging.Logger.getLogger;
import org.openapi4j.core.exception.EncodeException;
import org.openapi4j.schema.validator.ValidationData;
import org.openapi4j.schema.validator.v3.SchemaValidator;

@Provider
@Priority(Priorities.USER)
public class ValidationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = getLogger(ValidationFilter.class);

    @Context
    UriInfo info;

    @Inject
    SchemaValidatorConfig schemaValidatorConfig;

    /**
     * {@inheritDoc}
     * Validate incoming JSON against OpenAPI definition /META-INF/openapi.yaml.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            String body = RequestReader.readBody(requestContext);

            SchemaValidator schemaValidator = schemaValidatorConfig.schemaValidator(info.getRequestUri());

            ObjectMapper mapper = new ObjectMapper();
            ValidationData<String> validationData = new ValidationData<>();
            schemaValidator.validate(mapper.readTree(body), validationData);

            if (!validationData.isValid()) {
                String responseMessage = validationData.results().items()
                        .stream()
                        .map(item -> String.format("%s %s",
                                item.dataCrumbs(), item.message()))
                        .collect(Collectors.joining(" - "));

                throw new WebApplicationException(responseMessage, Response.Status.BAD_REQUEST);
            }
        } catch (EncodeException e) {
            LOGGER.errorf("Exception occurred while creating SchemaValidator instance: %s", e.getMessage());
            throw new WebApplicationException("Error during validation", Response.Status.INTERNAL_SERVER_ERROR);
        } catch (JsonProcessingException e) {
            LOGGER.errorf("Exception occurred while parsing JSON: %s", e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (IOException e) {
            LOGGER.errorf("Exception occurred while reading request body: %s", e.getMessage());
            throw new WebApplicationException("Exception while reading request body", Response.Status.BAD_REQUEST);
        } catch (NullPointerException e) {
            LOGGER.error("Exception occurred while validating JSON");
            throw new WebApplicationException(String.format("Endpoint %s is not defined in schema or schema is invalid",
                    info.getRequestUri().getPath()), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
