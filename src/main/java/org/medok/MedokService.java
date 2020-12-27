package org.medok;

import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.medok.extdns.crd.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The main business logic for Medok functionality.
 */
@ApplicationScoped
public class MedokService {

    private static final Logger log = Logger.getLogger(MedokService.class);

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "not-set")
    String version;

    @Inject
    KubernetesClient client;

    @Inject
    MedokConfiguration config;

    @Inject
    @Named("dnsEndpointClient")
    NonNamespaceOperation<DNSEndpointResource, DNSEndpointResourceList, DNSEndpointResourceDoneable, Resource<DNSEndpointResource, DNSEndpointResourceDoneable>> endpointClient;

    /**
     * If there is a sync necessary due to pod changes
     */
    private Boolean syncNeeded = false;

    /**
     * The timestamp when the Mailu pod watch started to filter out the
     * initial ADD event that is not an actual change.
     */
    private Instant watchStartedAt;


    /**
     * Record type to store data for a sync operation
     */
    private class SyncContext {

        /** Handler for the 'medok' DNSEndpoint POJO object of External-DNS */
        Resource<DNSEndpointResource, DNSEndpointResourceDoneable> dnsEndpointResource;

        /** The 'medok' DNSEndpoint POJO object of External-DNS */
        DNSEndpointResource dnsEndpoint;

        /** The Mailu frontend pods (normally, only one pod) */
        List<Pod> mailuFrontPods;
    }

    /**
     * When Medok starts, we do an initial sync on the Mailu Pods and the
     * DNS record.
     */
    void onStartup(@Observes StartupEvent startupEvent) {

        log.info("Medok starting (version: " + version + ")" );

        // Executing an initial sync
        syncPodsToEndpoints();

        // Starting watching for Mailu pod changes
        startWatchingMailuFrontPods();
    }

    /**
     * Finds the Mailu Frontend pods in the namespace with the configured
     * label selector.
     */
    Node getNode(String nodeName) {

        log.info("Getting node information about node:  (" + nodeName + ")");

        Node node = client.nodes().withName(nodeName).get();

        return node;
    }


    /**
     * Finds the Mailu Frontend pods in the namespace with the configured
     * label selector.
     */
    void findMailuFrontPods(SyncContext ctx) {

        String labelSel = config.mailuFrontendPodSelector;

        log.info("Listing Mailu frontend pods (" + labelSel + ")");

        ListOptions options = new ListOptions();
        options.setLabelSelector(labelSel);

        List<Pod> podList = client.pods().list(options).getItems();

        log.info("Found " + podList.size() + " Mailu frontend pods :");

        // Generating a DNS entry for all of them
        for (Pod pod : podList) {

            log.info(" - name: '" + pod.getMetadata().getName() + "' " +
                "node: " + pod.getSpec().getNodeName());
        }

        ctx.mailuFrontPods = podList;
    }

    /**
     * Starts watching changes in the Mailu Frontend pods in the namespace with
     * the configured label selector.
     */
    void startWatchingMailuFrontPods() {

        String labelSel = config.mailuFrontendPodSelector;

        log.info("Starting watching Mailu frontend pods (" + labelSel + ")");

        ListOptions options = new ListOptions();
        options.setLabelSelector(labelSel);

        client.pods().watch(options, new MailuPodWatcher());

        watchStartedAt = Instant.now();
    }


    /**
     * Finds the "medok" DNSEndpoint resource (if it exists) and its handler
     * into the context.
     */
    void getDnsEndpoint(SyncContext ctx) {

        log.info("Getting the External-DNS DNSEndpoint named 'medok' ");

        String name = "medok_" + config.mailuServerFqn;
        name = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "-");

        ctx.dnsEndpointResource = endpointClient.withName(name);

        ctx.dnsEndpoint = ctx.dnsEndpointResource.get();

        if (ctx.dnsEndpoint == null) {

            log.info("DNSEndpoint object '" + name + "' is NOT present ");

            DNSEndpointResource epr = new DNSEndpointResource();
            ctx.dnsEndpoint = epr;
            epr.setSpec(new DNSEndpointResourceSpec());
            epr.getSpec().setEndpoints(new HashSet<>());
            epr.getMetadata().setName(name);
            epr.setKind("DNSEndpoint");
        }

        Set<Endpoint> endpoints = ctx.dnsEndpoint.getSpec().getEndpoints();

        log.info("Found '" + name + "' DNSEndpoint. Embedded endpoints: " + endpoints.size() );

        for (Endpoint ep : endpoints) {

            log.info(" - Name: " + ep.getDnsName() + ", Targets: " + ep.getTargets());
        }

    }

    /**
     * Prepares the correct endpoint list that represents the list of
     * frontend pods.
     */
    void syncPodsToEndpoints() {

        log.info("Synchronizing Mailu Front pod nodes with Endpoints");

        SyncContext ctx = new SyncContext();

        findMailuFrontPods(ctx);

        getDnsEndpoint(ctx);

        Set endpoints = ctx.dnsEndpoint.getSpec().getEndpoints();
        endpoints.clear();

        Endpoint endpoint = new Endpoint();
        endpoints.add(endpoint);

        endpoint.setDnsName(config.mailuServerFqn);
        endpoint.setRecordTTL(config.mailuServerDnsTtl);

        endpoint.setRecordType(config.dnsRecordType);

        Set<String> targets = new HashSet<>();
        endpoint.setTargets(targets);

        for (Pod pod : ctx.mailuFrontPods) {

            String nodeName = pod.getSpec().getNodeName();
            Node node = getNode(nodeName);

            if (node == null) {

                log.error("Node not found: " + nodeName);
                return;
            }

            String exposedIP = findExposedIp(node);

            if (exposedIP == null) {

                log.error("Node's " + config.nodeAddressType + " not found: "
                          + nodeName);
                return;
            }
            targets.add(exposedIP);
        }

        if (targets.size() == 0) {

            log.info("No Mailu front pod found, removing the 'medok' DNSEndpoint record");
            ctx.dnsEndpointResource.delete();
        }
        else {
            log.info("Creating/updating the 'medok' DNSEndpoint record ");
            ctx.dnsEndpointResource.createOrReplace(ctx.dnsEndpoint);
        }

    }

    /**
     * Finding the IP address of a node that can be referenced by the Mailu
     * DNS record.
     */
    private String findExposedIp(Node node) {

        String exposedIP = null;

        for (NodeAddress address : node.getStatus().getAddresses()) {

            log.info("Address type: " + address.getType() +
                     ", address: " + address.getAddress());

            String addressType = address.getType();

            if (addressType.equals(config.nodeAddressType)) {

                exposedIP = address.getAddress();
            }
        }

        return exposedIP;
    }

    /**
     * Syncs the current Mailu Pod with the DNSEndpoint recod if there has
     * been a pod change detected in the Mailu namespace since the last
     * successful sync.
     */
    @Scheduled(every="30s")
    void syncPeriodically() throws InterruptedException {

        log.info("Periodic sync check starts");

        if (this.syncNeeded == Boolean.FALSE) {

            log.info("Sync is NOT needed, quitting");
            return;
        }

        log.info("Sync IS needed. Starting.");

        // Waiting a couple of seconds so that "Front" pod scheduling settles down
        TimeUnit.SECONDS.sleep(10);

        // Syncing the Mailu front pods to the DNSEndpoint record
        syncPodsToEndpoints();

        log.info("Sync finished successfully.");

        this.syncNeeded = false;
    }




    /**
     * Class for watching Pod events related to Mailu pods.
     *
     * Schedules a sync for the "medok" DNSEndpoint record.
     */
    class MailuPodWatcher implements Watcher<Pod> {

        @Override
        public void eventReceived(Action action, Pod pod) {

            Instant now = Instant.now();

            if (Duration.between(watchStartedAt, now).minusMillis(100).isNegative()) {

                log.info("Fake event immediately after watch start. NOT registering");
                return;
            }

            log.info("Mailu pods have changed, scheduling a sync");

            syncNeeded = true;
        }

        @Override
        public void onClose(KubernetesClientException e) {}
    }
}
