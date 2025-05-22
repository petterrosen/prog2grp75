package se.su.inlupp;

import java.io.*;

public class GraphIO {

    // Spara enbart bild-URL och dummy-noder som exempel
    public static void saveGraphFile(File file, String imageUrl) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(imageUrl); // Rad 1: bildens URL
            writer.println(); // Rad 2: exempel på platser
            writer.println(); // Exempel på förbindelse
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadGraphFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String imageUrl = reader.readLine(); // Första raden
            System.out.println("Bakgrundsbild: " + imageUrl);
            return imageUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}