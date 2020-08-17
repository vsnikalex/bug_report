package org.acme.quarkus.sample;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.cache.CacheResult;
import io.quarkus.runtime.StartupEvent;
import java.net.URI;
import java.net.URL;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import org.jboss.logging.Logger;
import static org.jboss.logging.Logger.getLogger;
import org.openapi4j.core.exception.EncodeException;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Schema;
import org.openapi4j.schema.validator.ValidationContext;
import org.openapi4j.schema.validator.v3.SchemaValidator;

/**
 * Contains an instance of {@linkplain OpenApi3} that will be used for validation.
 * The instance is created on startup and {@linkplain SchemaValidator} at each request depending on uri.
 */
@Singleton
public class SchemaValidatorConfig {

    private static final Logger LOGGER = getLogger(SchemaValidatorConfig.class);

    OpenApi3 api;

    /**
     * Reads OpenAPI schema on startup. In the case of Exception {@linkplain OpenApi3 api} variable remains null.
     * Exceptions are caught to prevent infinite restart attempts in CaaS platform.
     */
    @PostConstruct
    void readSchemaFile(@Observes StartupEvent startupEvent) {
        URL specPath = getClass().getClassLoader().getResource("META-INF/openapi.yaml");

        LOGGER.info("Reading OpenAPI schema");

        try {
            LOGGER.infof("Directory: %s", specPath.toString());

            api = new OpenApi3Parser().parse(specPath , false);
        } catch (ResolutionException | NullPointerException e) {
            LOGGER.error("OpenAPI schema not found");
        } catch (ValidationException e) {
            LOGGER.error("Schema is not valid");
        }
    }

    /**
     * Uses schema and URI info for {@linkplain SchemaValidator instance} creation.
     *
     * @param uri request URI, e.g. /troubleTicket.
     * @return {@linkplain SchemaValidator} instance, result is being cached with {@linkplain CacheResult}.
     * @throws EncodeException in case when {@linkplain Schema} instance cannot be converted to {@linkplain JsonNode}.
     */
    @CacheResult(cacheName = "validator-cache")
    public SchemaValidator schemaValidator(URI uri) throws EncodeException {
        LOGGER.debug("Creating SchemaValidator instance");

        Schema schema = api.getPath(uri.getPath())
                .getOperation("post")
                .getRequestBody()
                .getContentMediaType("application/json;charset=utf-8")
                .getSchema();

        JsonNode jsonNode = schema.toNode();

        ValidationContext<OAI3> validationContext = new ValidationContext<>(api.getContext());

        return new SchemaValidator(validationContext, "", jsonNode);
    }
}

