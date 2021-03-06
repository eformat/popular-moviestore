package org.acme.mvc.pages;

import org.acme.moviestore.DBHelper;
import org.acme.mvc.Thymeleaf;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Path("")
public class CartPage {

    private final Logger log = LoggerFactory.getLogger(CartPage.class);

    @Inject
    private Thymeleaf thymeleaf;

    @Inject
    private DBHelper DBHelper;

    @GET
    @Path("/cart/show")
    public CompletionStage<Response> path(@Context HttpRequest request, @Context HttpResponse response) {
        return thymeleaf.view("cart.html").render(
                DBHelper.getCartItems(request.getHttpHeaders().getCookies().get(DBHelper.MOVIE_STORE).getValue())
        );
    }

}
