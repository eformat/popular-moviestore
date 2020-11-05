package org.acme.data;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.*;

public class MovieCart {
    public String userId;
    public String orderId;
    private List<MovieItem> movieItems = new ArrayList<>();

    public MovieCart() {
    }

    @ProtoFactory
    public MovieCart(String userId, String orderId, List<MovieItem> movieItems) {
        this.userId = userId;
        this.orderId = orderId;
        this.movieItems = movieItems;
    }

    @ProtoField(number = 1)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @ProtoField(number = 2)
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @ProtoField(number = 3, collectionImplementation = ArrayList.class)
    public List<MovieItem> getMovieItems() {
        return movieItems;
    }

    public void setMovieItems(List<MovieItem> movieItems) {
        this.movieItems = movieItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCart movieCart = (MovieCart) o;
        return Objects.equals(userId, movieCart.userId) &&
                Objects.equals(orderId, movieCart.orderId) &&
                Objects.equals(movieItems, movieCart.movieItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, orderId, movieItems);
    }
}
