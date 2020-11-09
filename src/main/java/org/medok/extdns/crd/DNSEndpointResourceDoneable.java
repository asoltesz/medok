package org.medok.extdns.crd;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DNSEndpointResourceDoneable extends CustomResourceDoneable<DNSEndpointResource> {

    public DNSEndpointResourceDoneable(DNSEndpointResource resource, Function<DNSEndpointResource, DNSEndpointResource> function) {
        super(resource, function);
    }
}