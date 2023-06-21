package org.acme.moviestore;

import com.acme.services.MoviestoreService;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.acme.data.Movie;
import org.acme.data.MovieCart;
import org.acme.data.MovieCartItem;
import org.acme.data.MovieResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class DBHelper {

    private final Logger log = LoggerFactory.getLogger(DBHelper.class);

    @Inject
    @RestClient
    MoviestoreService moviestoreService;

    @ConfigProperty(name = "API_KEY", defaultValue = "unset")
    public String apiKey;

    @ConfigProperty(name = "QUARKUS_PROFILE", defaultValue = "dev")
    public String quarkusProfile;

    @Inject
    RemoteCacheManager remoteCacheManager;

    RemoteCache<String, Movie> movieCache;
    RemoteCache<String, MovieCart> cartCache;

    final String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
    final String HOSTNAME = "hostname";
    final String CART_ITEMS = "cartItems";
    final String CART_COUNT = "cartCount";
    final String CART_TOTAL = "cartTotal";
    final String MOVIES = "movies";
    public final String MOVIE_STORE = "moviestore";

    private static final String FILESTORE = "<persistence passivation=\"false\">" +
            "<file-store preload=\"true\" read-only=\"false\" purge=\"false\" path=\"cacheStore\" />" +
            "</persistence>";

    private static final String DISTRIBUTED_CACHE_CONFIG = "<infinispan><cache-container>" +
            "<distributed-cache name=\"%s\" mode=\"ASYNC\">" +
            "<encoding media-type=\"application/x-protostream\"/>" +
            FILESTORE +
            "</distributed-cache>" +
            "</cache-container></infinispan>";

    //private Map<String, MovieCart> cartCache = new ConcurrentHashMap<>();
    //private Map<String, Movie> movieCache = new ConcurrentHashMap<>();

    void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
        log.info("On start - get caches - " + quarkusProfile);
        movieCache = remoteCacheManager.administration().getOrCreateCache("movieCache", new XMLStringConfiguration(String.format(DISTRIBUTED_CACHE_CONFIG, "movieCache")));
        cartCache = remoteCacheManager.administration().getOrCreateCache("cartCache", new XMLStringConfiguration(String.format(DISTRIBUTED_CACHE_CONFIG, "cartCache")));
    }

    public Map<String, Object> getMovies(String sessionId) {
        Map<String, Object> ret = new ConcurrentHashMap<String, Object>();
        List<MovieCartItem> movieList = new ArrayList<>();
        log.info("Request served by HOST {} ", hostname);
        if (movieCache.isEmpty()) {
            if (!apiKey.equals("unset")) {
                Uni<MovieResponse> uni = Uni.createFrom().item(moviestoreService.popularMovies(apiKey)).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
                Set<Movie> movieSet = uni.await().indefinitely().getMovieSet();
                movieSet.stream().iterator().forEachRemaining(movie -> {
                    movieList.add(new MovieCartItem(movie));
                    movieCache.put(movie.getId(), movie);
                });
                ret.put(MOVIES, movieList);
            } else {
                log.info("Test movies loaded on {} ", hostname);
                TestMovies testMovies = new TestMovies();
                ret.put(MOVIES, testMovies.movieList);
                testMovies.movieList.stream().forEach(movieCartItem -> {
                    movieCache.put(movieCartItem.getMovie().getId(), movieCartItem.getMovie());
                });
            }
        } else {
            log.info("Cached movies loaded on {} ", hostname);
            movieCache.forEach((id, movie) -> {
                movieList.add(new MovieCartItem(movie));
            });
            ret.put(MOVIES, movieList);
        }
        ret.put(CART_COUNT, getCartCount(sessionId));
        ret.put(HOSTNAME, hostname);
        return ret;
    }

    public int getCartCount(String sessionId) {
        AtomicReference<Integer> cartCount = new AtomicReference<Integer>(0);
        cartCache.forEach((sId, v) -> {
            if (sId.equals(sessionId)) {
                v.getMovieItems().forEach(movieItem -> {
                    cartCount.accumulateAndGet(movieItem.getCount(), Integer::sum);
                });
            }
        });
        return cartCount.get().intValue();
    }

    public Map<String, MovieCart> getCartCache() {
        return cartCache;
    }

    public Map<String, Object> getCartItems(String sessionId) {
        Map<String, Object> ret = new ConcurrentHashMap<String, Object>();
        List<MovieCartItem> movieList = new ArrayList<>();
        AtomicReference<Double> cartTotal = new AtomicReference<>(0.0);
        cartCache.forEach((sId, movieCart) -> {
            if (sId.equals(sessionId)) {
                movieCart.getMovieItems().forEach(movieItem -> {
                    Movie movie = movieCache.get(movieItem.getId());
                    MovieCartItem movieCartItem = new MovieCartItem(movie);
                    movieCartItem.setQuantity(movieItem.getCount());
                    double total = movieItem.getCount() * movie.getPrice();
                    movieCartItem.setTotal(total);
                    cartTotal.updateAndGet(aDouble -> aDouble + total);
                    movieList.add(movieCartItem);
                    log.info("Movie:{} total for {} items is {}", movie, movieItem.getCount(), total);
                });
            }
        });
        ret.put(HOSTNAME, hostname);
        ret.put(CART_ITEMS, movieList);
        ret.put(CART_COUNT, getCartCount(sessionId));
        ret.put(CART_TOTAL, DecimalFormat.getCurrencyInstance(Locale.US).format(cartTotal.get()));
        return ret;
    }
}
