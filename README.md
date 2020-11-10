# Popular Moviestore

![](/images/2020-11-04-18-26-59.png)

## Running the application in dev mode

(Optional) export your [moviedb](http://themoviedb.org/) apiKey, there are default test movies loaded if you dont have an account (its free!)
```bash
export API_KEY=<your api key>
```

Run infinispan cluster locally
```bash
# cache file-store
mkdir /tmp/ispn1 && chown -R 777 /tmp/ispn1
mkdir /tmp/ispn2 && chown -R 777 /tmp/ispn2

# run infinispan cluster
podman-compose up -d

# create a user
podman exec -it ispn1 /opt/infinispan/bin/cli.sh user create admin -p admin
podman exec -it ispn2 /opt/infinispan/bin/cli.sh user create admin -p admin
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

You can use the embedded infinispan cache if you dont want to run your own external infinispan instance:

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

Deploy infinispan cluster
```bash

```

Create secret
```bash
oc new-project popular-moviestore

cat <<EOF | oc apply -f -
apiVersion: "v1"
kind: "Secret"
metadata:
  name: "popular-moviestore"
data:
  API_KEY: "$(echo -n <moviestore apikey> | base64)"
  INFINISPAN_REALM: "$(echo -n default | base64)"
  INFINISPAN_USER: "$(echo -n developer | base64)"
  INFINISPAN_PASSWORD: "$(echo -n $(oc exec infinispan-0 -- cat ./server/conf/users.properties | grep developer | awk -F'[=&]' '{print $2}') | base64)"
EOF
```

Deploy application
```bash
helm template my -f chart/values.yaml chart | oc apply -n popular-moviestore -f-
```
