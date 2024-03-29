# Popular Moviestore

![](/images/2020-11-04-18-26-59.png)

Moviestore application demonstrating:
- quarkus application using thymeleaf, vertx for web interface
- datagrid cluster with write-through file persistent store for movies and shopping basket cache (when all apps stopped/restarted)
- openshift 4+ deployment using helm
- istio configuration (optional)

## Prerequisites
- helm3
- gnu make
- oc logged in as cluster-admin
- ocp cluster 4+

## For the impatient 🤠
Deploy it ALL to OpenShift
```bash
make deploy-all
```
or with istio
```bash
make deploy-all-istio
```
And to remove
```bash
make undeploy-all
```
or for istio
```bash
make undeploy-all-istio
```

## Running the application in dev mode locally

(Optional) export your [moviedb](http://themoviedb.org/) apiKey, there are default test movies loaded if you dont have an account (its free!)
```bash
export API_KEY=<your api key>
```

Run infinispan cluster locally

cache file-store
```bash
mkdir /tmp/ispn1 && chmod -R 777 /tmp/ispn1
mkdir /tmp/ispn2 && chmod -R 777 /tmp/ispn2
```
run infinispan cluster
```bash
podman-compose up -d
```

You can run your application in dev mode that enables live coding using:
```bash
mvn quarkus:dev
```

## Packaging and running the application

The application can be packaged using
```bash
mvn package
```
It produces the `popular-moviestore-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.

This is a hollow jar with the dependencies copied into the `target/lib` directory.

The application is now runnable using `java -jar target/popular-moviestore-1.0-SNAPSHOT-runner.jar`.


## Embedded infinispan cache during development

You can try the embedded infinispan cache if you dont want to run your own external infinispan instance
```xml
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-infinispan-embedded</artifactId>
            <version>1.6.1.Final</version>
        </dependency>
```

```java
    @Inject
    EmbeddedCacheManager embeddedCacheManager;

    embeddedCacheManager.createCache("movieCache", new ConfigurationBuilder().build());
    movieCache = embeddedCacheManager.getCache("movieCache");
    embeddedCacheManager.createCache("cartCache", new ConfigurationBuilder().build());
    cartCache = embeddedCacheManager.getCache("cartCache");
```

## OpenShift

As cluster-admin user, requires:
- oc client logged in to cluster
- helm3

Deploy infinispan operator and cluster
```bash
make deploy-infinispan-operator
make deploy-infinispan
```

Deploy cert-utils operator (cluster scope)
```bash
make deploy-certutil-operator
```

Deploy application
```bash
make deploy-app
```

Get the route URL for movies (browse here!)
```bash
chrome $(oc get route -l app.kubernetes.io/name=popular-moviestore -o custom-columns=ROUTE:.spec.host --no-headers)
```

Delete everything
```bash
helm template my -f chart/values.yaml chart | oc delete -n popular-moviestore -f-
oc delete project popular-moviestore
make undeploy-certutil-operator
make undeploy-infinispan
make undeploy-infinispan-operator
```

### (Optional) Add Istio into the mix

Deploy Istio control plane
```bash
make deploy-istio-control-plane
```

Deploy Istio service mesh
```bash
make deploy-istio-mesh
```

Deploy infnispan, cert-uitl operatrs which are external to the mesh controlled project
```bash
make deploy-infinispan-operator
make deploy-infinispan
make deploy-certutil-operator
```

Deploy application
```bash
make deploy-app-istio
```

Get the istio ingress gateay route URL for movies (browse here!)
```bash
chrome $(oc get route -l maistra.io/gateway-name=movies -n istio-system -o custom-columns=ROUTE:.spec.host --no-headers)
```

Delete everything
```bash
helm template my -f chart/values.yaml chart --set istio.enabled=true | oc delete -n popular-moviestore -f-
oc delete project popular-moviestore
make undeploy-certutil-operator
make undeploy-infinispan
make undeploy-infinispan-operator
make undeploy-istio-mesh
make undeploy-istio-control-plane
```

## FIXME

### TLS Istio ingress route
- Istio in OpenShift does not yet seem to supprot SDS (Secret Discovery Service) - so you have to add this into the istio route manually for tls to work. The certificates are the same bound into the default ingress router.
```yaml
  tls:
    certificate: |-
      -----BEGIN CERTIFICATE-----
      -----END CERTIFICATE-----
    insecureEdgeTerminationPolicy: Redirect
    key: |-
      -----BEGIN RSA PRIVATE KEY-----
      -----END RSA PRIVATE KEY-----
    termination: edge
```

### excludeOutboundPorts
- Why does this not work by itself for infinispan in istio?
```yaml
        traffic.sidecar.istio.io/excludeOutboundPorts: "11222"
```
