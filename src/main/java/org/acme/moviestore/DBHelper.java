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
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
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

    @Inject
    EmbeddedCacheManager cacheManager;

    Cache<String, Movie> movieCache;
    Cache<String, MovieCart> cartCache;

    final String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
    final String HOSTNAME = "hostname";
    final String CART_ITEMS = "cartItems";
    final String CART_COUNT = "cartCount";
    final String CART_TOTAL = "cartTotal";
    final String MOVIES = "movies";
    public final String MOVIE_STORE = "moviestore";

    private static final String DISTRIBUTED_CACHE_CONFIG = "<infinispan><cache-container>" +
            "<distributed-cache name=\"%s\" mode=\"ASYNC\"/>" +
            "</cache-container></infinispan>";

    //private Map<String, MovieCart> cartCache = new ConcurrentHashMap<>();
    //private Map<String, Movie> movieCache = new ConcurrentHashMap<>();

    void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
        log.info("On start - get caches configs " + cacheManager.getCacheConfigurationNames());
        cacheManager.createCache("movieCache",  new ConfigurationBuilder().build());
        movieCache = cacheManager.getCache("movieCache");
        cacheManager.createCache("cartCache",  new ConfigurationBuilder().build());
        cartCache = cacheManager.getCache("cartCache");
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
                v.getMovieItems().forEach((l, m) -> {
                    cartCount.accumulateAndGet(m, Integer::sum);
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
                movieCart.getMovieItems().forEach((id, quantity) -> {
                    Movie movie = movieCache.get(id);
                    MovieCartItem movieCartItem = new MovieCartItem(movie);
                    movieCartItem.setQuantity(quantity);
                    double total = quantity * movie.getPrice();
                    movieCartItem.setTotal(total);
                    cartTotal.updateAndGet(aDouble -> aDouble + total);
                    movieList.add(movieCartItem);
                    log.info("Movie:{} total for {} items is {}", movie, quantity, total);
                });
            }
        });
        ret.put(CART_ITEMS, movieList);
        ret.put(CART_COUNT, getCartCount(sessionId));
        ret.put(CART_TOTAL, DecimalFormat.getCurrencyInstance(Locale.US).format(cartTotal.get()));
        return ret;
    }
}
