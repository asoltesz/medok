package org.medok;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "medok")
public class MedokConfiguration {

    /**
     * The namespace into which the Mailu instance is deployed
     * The Mailu frontend pod must be deployed into this namespace
     */
    public String mailuNamespace = "mailu";

    /**
     * The fully qualified name for the MX DNS entry that should
     * point to the host/node running the Mailu Front pod.
     */
    public String mailuServerFqn = "mailu.example.com";

    /**
     * The TTL for the DNS entry.
     */
    public Integer mailuServerDnsTtl = 180;

    /**
     * The label selector which finds a frontend pod of the Mailu instance
     */
    public String mailuFrontendPodSelector = "app=mailu,component=front";

    /**
     * The full name of the DNSEndpoint CRD type of External-DNS
     */
    public String externalDnsCrdFullName = "dnsendpoints.externaldns.k8s.io";

    /**
     * The Address Type to be used from the list of the addresses of the Node
     * to look up the IP address of the Node running the Frontend pod.
     *
     * The IP address from the record of this type will be written into the
     * DNS record created for Mailu.
     *
     * - On a normal Kubernetes node, this is "ExternalIP".
     * - A deprecated address type is "LegacyHostIP" if ExternalIP is not present.
     * - On Minikube, the "InternalIP" may be used for testing.
     */
    public String nodeAddressType = "ExternalIP";

    /**
     * The type of the DNS record to be created for Mailu.
     *
     * This is normally an "A" record which is referenced from your manually
     * configured MX record.
     */
    public String dnsRecordType = "A";

}
