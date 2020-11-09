package org.medok.extdns.crd;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.Set;

/**
 * Record type for the ExternalDNS "DNSEndpoint" CRD.
 *
 * Example:
 * ----------------
 * apiVersion: externaldns.k8s.io/v1alpha1
 * kind: DNSEndpoint
 * metadata:
 *   name: examplednsrecord
 * spec:
 *   endpoints:
 *   - dnsName: foo.bar.com
 *     recordTTL: 180
 *     recordType: A
 *     targets:
 *     - 192.168.99.216
 * ----------------
 */
@Data
@JsonDeserialize
@RegisterForReflection
public class DNSEndpointResourceSpec {

    /**
     * The targets the DNS record points to
     */
    Set<Endpoint> endpoints;

}