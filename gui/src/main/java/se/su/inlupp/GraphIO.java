package se.su.inlupp;

import java.io.*;
import java.util.*;

public class GraphIO {

    public static void saveGraphFile(File file, String imageUrl, List<Place> places, Graph<Place> graph) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(imageUrl); // Rad 1: bakgrundsbild

            // Rad 2+: platser
            for (Place p : places) {
                writer.println("PLACE:" + p.getName() + "," + p.getX() + "," + p.getY());
            }

            // Förbindelser (undvik dubbletter)
            Set<String> savedEdges = new HashSet<>();
            for (Place from : graph.getNodes()) {
                for (Edge<Place> edge : graph.getEdgesFrom(from)) {
                    Place to = edge.getDestination();

                    String key1 = from.getName() + ":" + to.getName();
                    String key2 = to.getName() + ":" + from.getName();
                    if (savedEdges.contains(key1) || savedEdges.contains(key2)) continue;

                    writer.println("EDGE:" + from.getName() + "," + to.getName() + "," + edge.getName() + "," + edge.getWeight());
                    savedEdges.add(key1);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadGraphFile(File file, List<Place> places, Graph<Place> graph) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String imageUrl = reader.readLine();
            Map<String, Place> nameToPlace = new HashMap<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PLACE:")) {
                    String[] parts = line.substring(6).split(",");
                    String name = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    Place p = new Place(name, x, y);
                    places.add(p);
                    graph.add(p);
                    nameToPlace.put(name, p);

                } else if (line.startsWith("EDGE:")) {
                    String[] parts = line.substring(5).split(",");
                    String fromName = parts[0];
                    String toName = parts[1];
                    String edgeName = parts[2];
                    int weight = Integer.parseInt(parts[3]);

                    Place from = nameToPlace.get(fromName);
                    Place to = nameToPlace.get(toName);

                    if (from != null && to != null) {
                        graph.connect(from, to, edgeName, weight);
                    }
                }
            }

            return imageUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}