package org.medok;

import io.fabric8.kubernetes.api.model.Pod;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/medok")
public class MedokResource {

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "not-set")
    String version;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String version() {

        return version;
    }

}