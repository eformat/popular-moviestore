package org.acme.data;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Movie {
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
    private LocalDate releaseDate;
    private boolean video;
    private int voteCount;

    public Movie() {
    }

    @ProtoFactory
    public Movie(String id, String posterPath, String logoPath, String overview, String title, float popularity, boolean adult, int[] genreIds, String originalLanguage, String originalTitle, boolean video, int voteCount) {
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

    @ProtoField(number = 1)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ProtoField(number = 2)
    public String getPosterPath() {
        return posterPath;
    }

    @JsonbProperty("poster_path")
    public void setPosterPath(String posterPath) {
        this.posterPath = "http://image.tmdb.org/t/p/w92" + posterPath;
    }

    @ProtoField(number = 3)
    public String getLogoPath() {
        return logoPath;
    }

    @JsonbProperty("backdrop_path")
    public void setLogoPath(String logoPath) {
        this.logoPath = "http://image.tmdb.org/t/p/w45" + logoPath;
    }

    @ProtoField(number = 4)
    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    @ProtoField(number = 5)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ProtoField(number = 6, defaultValue = "0.0f")
    public float getPopularity() {
        return popularity;
    }

    @JsonbProperty("vote_average")
    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @ProtoField(number = 7, defaultValue = "false")
    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    @ProtoField(number = 8)
    public int[] getGenreIds() {
        return genreIds;
    }

    @JsonbProperty("genre_ids")
    public void setGenreIds(int[] genreIds) {
        this.genreIds = genreIds;
    }

    @ProtoField(number = 9)
    public String getOriginalLanguage() {
        return originalLanguage;
    }

    @JsonbProperty("original_language")
    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    @ProtoField(number = 10)
    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonbProperty("original_title")
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @JsonbProperty("release_date")
    @JsonbDateFormat(value = "yyyy-MM-dd")
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    @ProtoField(number = 11, defaultValue = "false")
    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    @ProtoField(number = 12, defaultValue = "0")
    public int getVoteCount() {
        return voteCount;
    }

    @JsonbProperty("vote_count")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Float.compare(movie.popularity, popularity) == 0 &&
                Double.compare(movie.price, price) == 0 &&
                adult == movie.adult &&
                video == movie.video &&
                voteCount == movie.voteCount &&
                id.equals(movie.id) &&
                posterPath.equals(movie.posterPath) &&
                logoPath.equals(movie.logoPath) &&
                overview.equals(movie.overview) &&
                title.equals(movie.title) &&
                Arrays.equals(genreIds, movie.genreIds) &&
                originalLanguage.equals(movie.originalLanguage) &&
                originalTitle.equals(movie.originalTitle);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, posterPath, logoPath, overview, title, popularity, price, adult, originalLanguage, originalTitle, video, voteCount);
        result = 31 * result + Arrays.hashCode(genreIds);
        return result;
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
                "," + video +
                "," + voteCount +
                ")));";
    }
}
