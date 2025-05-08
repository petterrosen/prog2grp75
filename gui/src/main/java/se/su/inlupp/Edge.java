/*package se.su.inlupp;

public class Edge {

    private City destination;
    private int weight;
    private final String name;

    public Edge(City destination, String name, int weight) {
        this.destination = destination;
        this.weight = weight;
        this.name = name;

    }
    public City getDestination() {
        return destination;
    }

    public double getDistance() {
        return weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Negative weight not allowed");
        }
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Edge[destination=" + destination + ", name=" + name + " weight=" + weight + "]";
    }
}
*/