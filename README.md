# Mailu External-DNS Operator for Kubernetes (Medok)

This project makes it possible to automatically update your DNS record and direct incoming email traffic to a specific Kubernetes host on which the Mailu communication "frontend" is deployed.

Your DNS provider must be supported by [External-DNS](https://github.com/kubernetes-sigs/external-dns) and External-DNS must be deployed on your cluster.


# The problem

For an email server, incoming SMTP traffic is always expected to be done on port 25. For a certain domain (say: example.com) an "MX" typed DNS record identifies the server which is responsible for accepting emails for that domain. (The "MX" record cannot directly define an IP address but it references an "A" record.) 

Mailu has a so called "frontend" service which accepts incoming email and directs it to its backend internally.

Mailu always needs the Source IP of the sending SMTP service in order to be able to quickly ban spammers. (and ensure that only authorized IP ranges can send trough it).

When Mailu (or any other email service) is deployed on Kubernetes, several problems arise:
- A highly available Kubernetes cluster runs on multiple hosts/nodes and the Mailu Frontend pod can be scheduled/deployed on any of the hosts (so the exact host is hard/impossible to predict).
- The Mailu Frontend pod may be rescheduled onto another host (e.g.: the original host goes down). The MX record needs to be quickly updated to avoid email service outage. 
- Due to Kubernetes networking semantics, [preserving the Source IP](https://kubernetes.io/docs/tutorials/services/source-ip/) this need can only be served with directly reserving ports on the host. Standard, externally-exposable Kubernetes mechanisms (NodePort, LoadBalancer) either do NAT (which looses the Source IP), or has other problems (e.g.: is available only in managed clusters like GCE or Azure) 

External-DNS can be used to create DNS records, but as of this moment (2020-OCT-20), it cannot be configured to properly expose the ExternalIP of the node running the Mailu front pod with the hostPort entries (and InternalIPs are useless for this purpose).

# The solution

External-DNS can be used to create/maintain "A" records via its CRD mechanism, using a specific hostname within the domain. The DNS administrator can manually create an "MX" record beforehand (pointing to the hostname that the "A" record will define) so when the "A" record gets defined, the whole lookup mechanism starts working.

In order to create the necessary External-DNS CRD object, a custom operator needs to listen to Mailu pod scheduling events and maintain the necessary CRD. In turn, External-DNS will detect the changes in the CRD and maintain the DNS records.

# Deployment / Usage

See in the [deployment](docs/deployment.md)

# Developing / Building the project

See in the [developing/building page](docs/development.md)
