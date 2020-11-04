package org.acme.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@JsonIgnoreProperties("popularity")
public class Movie implements Serializable {
    private String id;
    private String posterPath;
    private String logoPath;
    private String overview;
    private String title;
    private float popularity;
    private double price = ThreadLocalRandom.current().nextDouble(1.0, 10.0);

    private boolean adult;
    private int[] genreIds;
    private String originalLanguage;
    private String originalTitle;
    private Date releaseDate;
    private boolean video;
    private int voteCount;

    public Movie() {
    }

    public Movie(String id, String posterPath, String logoPath, String overview, String title, float popularity, boolean adult, int[] genreIds, String originalLanguage, String originalTitle, Date releaseDate, boolean video, int voteCount) {
        this.id = id;
        this.posterPath = posterPath;
        this.logoPath = logoPath;
        this.overview = overview;
        this.title = title;
        this.popularity = popularity;
        this.adult = adult;
        this.genreIds = genreIds;
        this.originalLanguage = originalLanguage;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.video = video;
        this.voteCount = voteCount;
    }

    public Movie(String id, String posterPath, String logoPath, String overview, String title, float popularity) {
        this.id = id;
        this.posterPath = posterPath;
        this.logoPath = logoPath;
        this.overview = overview;
        this.title = title;
        this.popularity = popularity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    @JsonSetter("poster_path")
    public void setPosterPath(String posterPath) {
        this.posterPath = "http://image.tmdb.org/t/p/w92" + posterPath;
    }

    public String getLogoPath() {
        return logoPath;
    }

    @JsonSetter("backdrop_path")
    public void setLogoPath(String logoPath) {
        this.logoPath = "http://image.tmdb.org/t/p/w45" + logoPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getPopularity() {
        return popularity;
    }

    @JsonSetter("vote_average")
    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public int[] getGenreIds() {
        return genreIds;
    }

    @JsonSetter("genre_ids")
    public void setGenreIds(int[] genreIds) {
        this.genreIds = genreIds;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    @JsonSetter("original_language")
    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonSetter("original_title")
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    @JsonSetter("release_date")
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public int getVoteCount() {
        return voteCount;
    }

    @JsonSetter("vote_count")
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    // @see com.acme.moviestore.TestMovies
    public String dump() {
        return "movieList.add(new MovieCartItem(new Movie(\"" +
                id + '\"' +
                ",\"" + posterPath + '\"' +
                ",\"" + logoPath + '\"' +
                ",\"" + overview + '\"' +
                ",\"" + title + '\"' +
                "," + popularity + "f" +
                "," + adult +
                ",null" +
                ",\"" + originalLanguage + '\"' +
                ",\"" + originalTitle + '\"' +
                ",null" +
                "," + video +
                "," + voteCount +
                ")));";
    }
}
