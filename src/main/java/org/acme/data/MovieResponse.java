package org.acme.data;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.Set;

public class MovieResponse {

    public MovieResponse(Set<Movie> movieSet, int page, int totalPages, int totalResults) {
        this.movieSet = movieSet;
        this.page = page;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
    }

    public MovieResponse() {
    }

    private Set<Movie> movieSet;
    private int page;
    private int totalPages;
    private int totalResults;

    public Set<Movie> getMovieSet() {
        return movieSet;
    }

    @JsonbProperty("results")
    public void setMovieSet(Set<Movie> movieSet) {
        this.movieSet = movieSet;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    @JsonbProperty("total_pages")
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    @JsonbProperty("total_results")
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
