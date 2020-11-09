package org.medok;

import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.medok.extdns.crd.DNSEndpointResource;
import org.medok.extdns.crd.DNSEndpointResourceDoneable;
import org.medok.extdns.crd.DNSEndpointResourceList;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

public class ClientProvider {

    @Inject
    MedokConfiguration config;

    /**
     * Generic Kubernetes client for managing generic objects in the
     * Mailu namespace.
     */
    @Produces
    @Singleton
    KubernetesClient mailuKubernetesClient() {

        String namespace = config.mailuNamespace;

        return new DefaultKubernetesClient().inNamespace(namespace);
    }


    /**
     * Client for ExternalDNS CRD entries.
     *
     * The CRDs will be in the same namespace as Mailu
     */
    @Produces
    @Singleton
    @Named("dnsEndpointClient")
    NonNamespaceOperation<DNSEndpointResource, DNSEndpointResourceList, DNSEndpointResourceDoneable, Resource<DNSEndpointResource, DNSEndpointResourceDoneable>>
    dnsEndpointClient(KubernetesClient defaultClient) {

        String namespace = config.mailuNamespace;

        // This is shipped by ExternalDNS, not by us
        // String apiVersion = "externaldns.k8s.io/v1alpha1";
        // String kind = "DNSEndpoint";
        // KubernetesDeserializer.registerCustomKind(apiVersion, kind, DNSEndpointResource.class);

        CustomResourceDefinition crd = defaultClient
            .customResourceDefinitions()
            .list()
            .getItems()
            .stream()
            .filter(d -> config.externalDnsCrdFullName.equals(d.getMetadata().getName()))
            .findAny()
            .orElseThrow( () ->
                new RuntimeException(
                    "Deployment error: Custom resource definition " +
                        config.externalDnsCrdFullName +
                        "not found."
                )
            );

        return defaultClient
            .customResources(crd, DNSEndpointResource.class, DNSEndpointResourceList.class, DNSEndpointResourceDoneable.class)
            .inNamespace(namespace);
    }
}