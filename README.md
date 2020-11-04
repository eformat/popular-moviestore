# Popular Moviestore

![](/images/2020-11-04-18-26-59.png)

## Running the application in dev mode

(Optional) export your [moviedb](http://themoviedb.org/) apiKey, there are default test movies loaded if you dont have an account (its free!)
```bash
export API_KEY=<your api key>
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
