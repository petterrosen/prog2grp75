/*
package se.su.inlupp;

import java.util.*;


public class ListGraph implements Graph {
    private Map<T, Set<Edge>> cities = new HashMap<>();

    //add ska lägga till en nod i grafen
    @Override
    public void add(T node) {
        cities.putIfAbsent(T, new HashSet<>());
    }


    //connect ska koppla samman två noder som finns i grafen
    public void connect(City from, City to, String name, int weight) {
        if (!cities.containsKey(from) || !cities.containsKey(to)) {
            throw new NoSuchElementException("One of the given cities does not exist");
        }
        if(weight < 0) {
            throw new IllegalArgumentException("Negative weight not allowed");
        }

        Set<Edge> fromCities = cities.get(from);
        Set<Edge> toCities = cities.get(to);

        Edge fromEdge = new Edge(from, "a", weight);
        Edge toEdge = new Edge(to, "b", weight);
        if(fromCities.contains(toEdge) || toCities.contains(fromEdge)) {
            throw new IllegalStateException("Edge already exists");

        }
        fromCities.add(toEdge);
        toCities.add(fromEdge);

        cities.put(from, fromCities);
        cities.put(to, toCities);

    }

    public void disconnect(City from, City to){
        if(!cities.containsKey(from) || !cities.containsKey(to)){
            throw new NoSuchElementException("One of the given cities does not exist");
        }

        Edge fromEdge = getEdgeBetween(from, to);
        if (fromEdge == null){
            throw new IllegalStateException();
        }
        Edge toEdge = getEdgeBetween(to, from);
        //Hämtar uppsättningen (Set) av kanter som är kopplade till städerna from och to.
        Set<Edge> fromEdges = cities.get(from);
        fromEdges.remove(toEdge);
        Set<Edge> toEdges = cities.get(to);
        toEdges.remove(fromEdge);

        //ta bort kanten mellan from och to

        //Uppdatera mappen för cities med de nya kanterna
        cities.put(from, fromEdges);
        cities.put(to, toEdges);
    }

    //ska ta bort en nod och dens kanter i grafen
    public void remove(City city) {
        if (!cities.containsKey(city)) {
            throw new NoSuchElementException("Node does not exist");
        }
        //cities sparar kopplingar från stad och dess kopplingar
        //varje city har en dubbelriktad (två vägar)
        //hämta alla kopplingar som city har

        Set<Edge> edges = cities.get(city);

        //går igenom alla kopplingar som tillhör den valda staden

        for (Edge edge : edges) {

            //gå till destination och hämta dess kopplingar till andra städer
            Set<Edge> toEdges = cities.get(edge.getDestination());
            Edge result;

            //gå igenom alla kopplingar och hitta omvända kopplingar från destination till city
            for (Edge toEdge : toEdges) {
                if (city.equals(toEdge.getDestination())) {
                    result = toEdge;
                    break;
                }
            }
            //ta bort den omvända riktningen
            toEdges.remove(result);
            //uppdatera destinations kopplingar
            cities.put(edge.getDestination(), toEdges);
        }
        //ta bort staden
        cities.remove(city);
    }

    public void setConnectionWeight(City from, City to, int weight){
        if(!cities.containsKey(from) || !cities.containsKey(to)){
            throw new NoSuchElementException("One of the given cities does not exist");
        }

        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be less than zero!");
        }

        //Uppdatera vikten hos förbindelserna mellan dessa två noder om förbindelsen/kanten existerar.
        Edge fromEdge = getEdgeBetween(from, to);
        Edge toEdge = getEdgeBetween(to, from);
        if (fromEdge == null || toEdge == null){
            throw new NoSuchElementException("There is no connection between these cities!");
        }

        fromEdge.setWeight(weight);
        toEdge.setWeight(weight);
    }

    public Set<City> getNodes(){
        return new HashSet<>(cities.keySet());
    }

    public Set<Edge> getEdgesFrom(City city){
        if (!cities.containsKey(city)){
            throw new NoSuchElementException("The city doesn't exist!");
        }
        //hämta alla kopplingar som city har
        Set<Edge> edges = cities.get(city);
        return new HashSet<>(edges);
    }

    public Edge getEdgeBetween(City from, City to){
        if(!cities.containsKey(from) || !cities.containsKey(to)){
            throw new NoSuchElementException("One of the given cities does not exist");
        }

        for (Edge edge : cities.get(from)){
            if (edge.getDestination().equals(to)){
                return edge;
            }
        }
        return null;
    }


    public boolean pathExists(City a, City b) {
        //Om någon av noderna inte finns i grafen returneras också false.
        if (!cities.containsKey(a) || !cities.containsKey(b)) {
            return false;
        }

        Set<City> visited = new HashSet<>();
        return recursiveVisitAll(a, b, visited);
    }

    private boolean recursiveVisitAll(City from, City to, Set<City> visited) {
        visited.add(from);
        if (from.equals(to)) {
            return true;
        }
        for (Edge e : cities.get(from)) {
            if (!visited.contains(e.getDestination())){
                if (recursiveVisitAll(e.getDestination(), to, visited)) {
                    return true;
                }
            }
        }
        return false;
    }


    public List<Edge> getPath(City from, City to){
    }*/


