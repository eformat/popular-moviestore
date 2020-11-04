package com.acme.services;

import org.acme.data.MovieResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RegisterRestClient
public interface MoviestoreService {

    @GET
    @Path("/3/movie/popular")
    @Produces(MediaType.APPLICATION_JSON)
    MovieResponse popularMovies(@QueryParam("api_key") String apikey);
}
