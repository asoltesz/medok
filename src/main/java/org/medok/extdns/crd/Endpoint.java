package org.medok.extdns.crd;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The "Endpoint" record type.
 *
 * Part of the ExternalDNS "DNSEndpoint" CRD type (the "endpoints" list).
 *
 * Example:
 * ----------------
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
public class Endpoint {

    /**
     * The hostname of the DNS record
     */
    String dnsName;

    /**
     * RecordType type of record, e.g. CNAME, A, SRV, TXT etc
     */
    String recordType;

    /**
     * TTL for the record
     */
    int recordTTL;

    /**
     * The targets the DNS record points to (IP addresses, typically, only one)
     */
    Set<String> targets;

    /**
     * Labels stores labels defined for the Endpoint
     */
    Map<String,String> labels = new HashMap<>();

//    /**
//     * ProviderSpecific stores provider specific config
//     */
//    ??? providerSpecific;


    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public int getRecordTTL() {
        return recordTTL;
    }

    public void setRecordTTL(int recordTTL) {
        this.recordTTL = recordTTL;
    }

    public Set<String> getTargets() {
        return targets;
    }

    public void setTargets(Set<String> targets) {
        this.targets = targets;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}