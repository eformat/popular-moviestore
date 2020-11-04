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
public class HomePage {

    private final Logger log = LoggerFactory.getLogger(HomePage.class);

    @Inject
    private Thymeleaf thymeleaf;

    @Inject
    private DBHelper DBHelper;

    @GET
    @Path("/")
    public CompletionStage<Response> path(@Context HttpRequest request, @Context HttpResponse response) {
        String sessionId;
        if (!request.getHttpHeaders().getCookies().containsKey(DBHelper.MOVIE_STORE)) {
            sessionId = DBHelper.MOVIE_STORE + "=" + UUID.randomUUID();
            response.getOutputHeaders().add(HttpHeaders.SET_COOKIE, sessionId);
        } else {
             sessionId = request.getHttpHeaders().getCookies().get(DBHelper.MOVIE_STORE).getValue();
        }
        return thymeleaf.view("home.html").render(
                DBHelper.getMovies(sessionId)
        );
    }

}
