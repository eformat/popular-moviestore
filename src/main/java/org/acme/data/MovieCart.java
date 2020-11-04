package org.acme.data;

import java.util.HashMap;
import java.util.Map;

public class MovieCart {
    public String userId;
    public String orderId;
    private Map<String, Integer> movieItems = new HashMap<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Map<String, Integer> getMovieItems() {
        return movieItems;
    }

    public void setMovieItems(Map<String, Integer> movieItems) {
        this.movieItems = movieItems;
    }

    @Override
    public String toString() {
        return "MovieCart{" +
                "userId='" + userId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", movieItems=" + movieItems +
                '}';
    }
}
