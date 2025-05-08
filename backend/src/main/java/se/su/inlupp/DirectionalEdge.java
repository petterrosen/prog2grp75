package se.su.inlupp;

import java.util.Objects;

public class DirectionalEdge<T> implements Edge<T> {
    private final T destination;
    private int weight;
    private final String name;

    public DirectionalEdge(T destination, String name, int weight) {
        if (weight < 0) throw new IllegalArgumentException("Negative weight not allowed");
        this.destination = Objects.requireNonNull(destination);
        this.name = Objects.requireNonNull(name);
        this.weight = weight;
    }

    @Override
    public T getDestination() {
        return destination;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        if (weight < 0) throw new IllegalArgumentException("Negative weight not allowed");
        this.weight = weight;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectionalEdge<?> other)) return false;
        return weight == other.weight
                && Objects.equals(destination, other.destination)
                && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, name, weight);
    }

    @Override
    public String toString() {
        return String.format("till %s med %s tar %d",
                destination, name, weight);
    }
}