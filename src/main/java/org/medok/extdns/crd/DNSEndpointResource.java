package org.medok.extdns.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.client.CustomResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DNSEndpointResource extends CustomResource {

    private DNSEndpointResourceSpec spec;

    //------------------------


    public DNSEndpointResourceSpec getSpec() {
        return spec;
    }
    public void setSpec(DNSEndpointResourceSpec spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        String name = getMetadata() != null ? getMetadata().getName() : "unknown";
        String version = getMetadata() != null ? getMetadata().getResourceVersion() : "unknown";
        return "name=" + name + " version=" + version + " value=" + spec;
    }
}