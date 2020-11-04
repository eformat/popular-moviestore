package org.acme.moviestore;

import org.acme.data.MovieCart;
import org.acme.data.MovieCartItem;
import org.acme.mvc.pages.HomePage;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@Path("")
public class ShoppingCart {

    private final Logger log = LoggerFactory.getLogger(HomePage.class);

    @Inject
    private DBHelper DBHelper;

    @GET
    @Path("/cart/add")
    public String addItem(@QueryParam("movieId") String movieId, @QueryParam("quantity") int qty, @Context HttpRequest request) {
        MovieCart movieCart;
        String sessionId;

        if (!request.getHttpHeaders().getCookies().containsKey(DBHelper.MOVIE_STORE)) {
            log.warn("no session cookie - use the homepage");
            return "0";
        } else {
            sessionId = request.getHttpHeaders().getCookies().get(DBHelper.MOVIE_STORE).getValue();
            if (DBHelper.getCartCache().containsKey(sessionId)) {
                movieCart = DBHelper.getCartCache().get(sessionId);
            } else {
                movieCart = new MovieCart();
                movieCart.setOrderId(UUID.randomUUID().toString());
            }

            Map<String, Integer> movieItems = movieCart.getMovieItems();

            if (movieItems.containsKey(movieId)) {
                movieItems.replace(movieId, qty);
            } else {
                movieItems.put(movieId, qty);
            }

            DBHelper.getCartCache().put(sessionId, movieCart);
            log.info("Movie Cart:{}", movieCart);
        }

        log.info("Adding/Updating {} with Quantity {} to cart ", movieId, qty);

        return String.valueOf(DBHelper.getCartCount(sessionId));
    }

    @POST
    @Path("/cart/pay")
    public Response pay(@Context HttpRequest request) {
        if (!request.getHttpHeaders().getCookies().containsKey(DBHelper.MOVIE_STORE)) {
            log.warn("no session cookie - use the homepage");
        } else {
            String sessionId = request.getHttpHeaders().getCookies().get(DBHelper.MOVIE_STORE).getValue();
            if (DBHelper.getCartCache().containsKey(sessionId)) {
                MovieCart movieCart = DBHelper.getCartCache().get(sessionId);
                log.info("Your request {} will be processed, thank your for shopping", movieCart);
                DBHelper.getCartCache().remove(sessionId);
            }
        }
        return Response.seeOther(URI.create("/")).build();
    }

    @GET
    @Path("/dump")
    public void dump(@Context HttpRequest request) {
        String sessionId = request.getHttpHeaders().getCookies().get(DBHelper.MOVIE_STORE).getValue();
        List<MovieCartItem> movieList = (List<MovieCartItem>)DBHelper.getCartItems(sessionId).get(DBHelper.CART_ITEMS);
        movieList.stream().forEach(movieCartItem -> {
            log.info(movieCartItem.getMovie().dump());
        });
    }
}
