package org.acme.data;

public class MovieCartItem {
    private Movie movie;
    private int quantity;
    private double total;

    public MovieCartItem(Movie movie) {
        this.movie = movie;
        this.setQuantity(0);
        this.setTotal(0);
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
