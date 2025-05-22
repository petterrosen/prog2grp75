package se.su.inlupp;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class GraphIO {

    public static void saveGraphFile(File file, String imageUrl, List<Place> places, Graph<Place> graph) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(imageUrl); // Rad 1: bakgrundsbild

            StringBuilder sb = new StringBuilder();
            for (Place p : places) {
                sb.append(p.getName()).append(",").append(p.getX()).append(",").append(p.getY()).append(";");
            }
            writer.println(sb); // Rad 2: alla platser

            // Förbindelser kan läggas till här om ni vill
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadGraphFile(File file, List<Place> places, Graph<Place> graph) {
        try (Scanner scan = new Scanner(file)) {
            String imageUrl = scan.nextLine();

            if (!scan.hasNextLine()) return imageUrl;
            String placeLine = scan.nextLine();
            String[] parts = placeLine.split(";");
            for (String part : parts) {
                if (part.isBlank()) continue;
                String[] data = part.split(",");
                String name = data[0];
                double x = Double.parseDouble(data[1]);
                double y = Double.parseDouble(data[2]);

                Place p = new Place(name, x, y);
                places.add(p);
                graph.add(p);
            }

            return imageUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}