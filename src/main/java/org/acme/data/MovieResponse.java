package org.acme.data;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.Set;

public class MovieResponse implements Serializable {

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

    @JsonSetter("results")
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

    @JsonSetter("total_pages")
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    @JsonSetter("total_results")
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
