// PROG2 VT2025, inlämningsuppgift del 2
// grupp 75
// Sama Matloub sama3201
// Yasin Akdeve
// Petter Rosén pero0033

package se.su.inlupp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.scene.shape.Line;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Optional;

public class Gui extends Application {
    private Stage stage;
    private final FileChooser fileChooser = new FileChooser();
    private Button findPathButton, showConnectionButton, newPlaceButton, newConnectionButton, changeConnectionButton;
    private boolean changed = false;
    private File imageFile;
    //Skapar en tom graf där varje nod är ett Place-objekt, för att sedan där kunna lägga till kopplingar/kanter
    private Graph<Place> graph = new ListGraph<>();
    //Lagra varje enskild place med namn och X- och Y-kordinator. Places innehåller noder utan kopplingar.
    private final List<Place> places = new ArrayList<>();
    private final ImageView imageView = new ImageView();
    private final Pane center = new Pane();
    private final List<Place> pickedPlaces = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PathFinder");
        BorderPane root = new BorderPane();
        stage = primaryStage;

        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        // ====== Menyn File i top-BorderPane ======
        VBox vbox = new VBox();
        MenuBar menuBar1 = new MenuBar();
        FlowPane controls = new FlowPane();

        vbox.getChildren().add(menuBar1);
        vbox.getChildren().add(controls);

        Menu fileMenu = new Menu("File");

        MenuItem newMap = new MenuItem("New Map");
        fileMenu.getItems().add(newMap); //Lägga till i fileMenu
        newMap.setOnAction(new NewMapHandler());
        MenuItem open = new MenuItem("Open");
        fileMenu.getItems().add(open);
        open.setOnAction(new OpenHandler());
        MenuItem save = new MenuItem("Save");
        fileMenu.getItems().add(save);
        save.setOnAction(new SaveHandler());
        MenuItem saveImage = new MenuItem("Save Image");
        fileMenu.getItems().add(saveImage);
        saveImage.setOnAction(new SaveImageHandler());
        MenuItem exit = new MenuItem("Exit");
        fileMenu.getItems().add(exit);
        exit.setOnAction(new ExitHandler());

        menuBar1.getMenus().addAll(fileMenu);
        menuBar1.setBackground(Background.fill(Color.LIGHTBLUE));

        root.setTop(vbox);

        // ====== Skapa knappar för HBox ======
        newPlaceButton = new Button("New Place");
        newPlaceButton.setOnAction(new NewPlaceHandler());
        newConnectionButton = new Button("New Connection");
        newConnectionButton.setOnAction(new NewConnection());
        showConnectionButton = new Button("Show Connection");
        changeConnectionButton = new Button("Change Connection");
        changeConnectionButton.setOnAction(new ChangeConnection());
        findPathButton = new Button("Find Path");
        findPathButton.setOnAction(new FindPathHandler());

        HBox menuBar2 = new HBox(10, findPathButton, showConnectionButton, newPlaceButton, newConnectionButton, changeConnectionButton);
        menuBar2.setAlignment(Pos.CENTER);
        menuBar2.setPadding(new Insets(30));

        // Inaktivera knapparna i HBox-menu vid start
        setButtonsDisabled(true);

        root.setBottom(menuBar2);

        //Visning av bild i mitten av Borderpane
        center.getChildren().add(imageView);
        root.setCenter(center);

        //Scene scene = new Scene(root, 640, 480);
        Scene scene = new Scene(root, 620, 840);
        primaryStage.setScene(scene);
        primaryStage.show();

        //setOnCloseRequest() är en metod som används vid stängning av huvudfönstret med "X"
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!confirmDiscard()) {
                    event.consume(); // Avbryt stängning
                }
            }
        });
    }

    private void setButtonsDisabled(boolean disabled) {
        newPlaceButton.setDisable(disabled);
        newConnectionButton.setDisable(disabled);
        showConnectionButton.setDisable(disabled);
        changeConnectionButton.setDisable(disabled);
        findPathButton.setDisable(disabled);
    }

    //  ============== 4.1 Menyn File ==============
    private void manageMapImage(File selectedFile) {
        try {
            // Spara filen så att Save vet vilken bild som används
            imageFile = selectedFile;

            //Rensar Place-listan över tidigare platser
            places.clear();
            //Skapa en ny tom graf varje gång man öppnar/laddar en ny fil
            graph = new ListGraph<>();
            //Lägg tillbaka kartbilden igen
            center.getChildren().setAll(imageView);

            //Hämtar bild från vald fil och skapar ett Image-objekt
            Image image = new Image(selectedFile.toURI().toString());

            // Sätt bild i ImageView
            imageView.setImage(image);

            // Testar
            //imageView.setMouseTransparent(true);

            imageView.setPreserveRatio(true);

            center.setPrefSize(image.getWidth(), image.getHeight());
            stage.setWidth(image.getWidth() + 16);
            stage.setHeight(image.getHeight() + 150);
            stage.sizeToScene();

            // Aktivera knappar
            setButtonsDisabled(false);

            changed = true;

        } catch (Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
        alert.showAndWait();
            }
    }

    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            if (changed && !confirmDiscard())
                return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose a map picture");
            // Startkatalogsmapp där bild ska laddas ifrån
            fileChooser.setInitialDirectory(new File("src/main/resources"));

            //System.out.println("Current " + System.getProperty("user.dir")); <== Har använts vid felsökning.
            // Filtyper att kunna välja emellan
            fileChooser.getExtensionFilters().setAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.gif", "*.jpg", "*.jpeg", "*.png")
            );

            // Visa fildialogfönstret
            File selectedFile = fileChooser.showOpenDialog(stage);

            // Om användaren väljer en bild
            if (selectedFile != null) {
                manageMapImage(selectedFile);
            }
        }
    }

    private void open(String fileName) {
        try {
           BufferedReader reader = new BufferedReader(new FileReader(fileName));

            //Första raden i filen för att ladda kartbilden (krav enligt uppgiften)
            String imagePath = reader.readLine();
            System.out.println("imagepath " + imagePath);

            if (imagePath.startsWith("file:")) {
                imagePath = imagePath.substring(5);
            }

            // Sökväg till .graph-filen
            File graphFile = new File(fileName);
            File imageDir = graphFile.getParentFile();
            imageFile = new File(imageDir, imagePath);

            if (imagePath == null || imagePath.isEmpty()) {
                throw new IOException("File not found!");
            }

            //imageFile = new File(imagePath);
            Image image = new Image(imageFile.toURI().toString());

            imageView.setImage(image);
            center.setPrefSize(image.getWidth(), image.getHeight());
            stage.setWidth(image.getWidth() + 16);
            stage.setHeight(image.getHeight() + 150);
            stage.sizeToScene();

            //Rensar Place-listan över tidigare platser
            places.clear();
            //Skapa en ny tom graf varje gång man öppnar/laddar en ny fil
            graph = new ListGraph<>();
            //Lägg tillbaka kartbilden igen
            center.getChildren().setAll(imageView);

            // Läs in platser från graph-filen
            String placeLine = reader.readLine();
            String[] tokens = placeLine.split(";");
            for (int i = 0; i < tokens.length; i = i+3) {
                String name = tokens[i];
                double x = Double.parseDouble(tokens[i + 1]);
                double y = Double.parseDouble(tokens[i + 2]);

                // Skapa och lägg till place med X- och Y-kordinator
                Place place = new Place(name, x, y);
                places.add(place);
                graph.add(place);

                //Lägger till Clickhantering för platserna som laddas från graph-filen.
                place.setOnMouseClicked(new PickedPlacesClickHandler());

                center.getChildren().add(place);
            }

            // 2. Läs återstående rader i filen, förbindelserna.

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 4) continue;

                String fromName = parts[0];
                String toName = parts[1];
                String connectionName = parts[2];
                int weight = Integer.parseInt(parts[3]);

                Place from = findPlaceByName(fromName);
                Place to = findPlaceByName(toName);

                if (from != null && to != null &&
                        graph.getEdgeBetween(from, to) == null &&
                        graph.getEdgeBetween(to, from) == null) {

                    graph.connect(from, to, connectionName, weight);

                    // Rita linje
                    Line connLine = new Line(from.getX(), from.getY(), to.getX(), to.getY());
                    center.getChildren().add(connLine);
                }
            }

            setButtonsDisabled(false);

            changed = true;

        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Can't open file " + fileName + "!");
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Hjälpmetod för att hitta en plats via namn
    private Place findPlaceByName(String name) {
        return places.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    class OpenHandler implements EventHandler<ActionEvent> {
        public void handle(ActionEvent event) {
            if (changed && !confirmDiscard()) return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open a graf-file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph File", "*.graph"));
            // Startkatalogsmapp där bild ska laddas ifrån
            fileChooser.setInitialDirectory(new File("src/main/resources"));
            // Visa fildialogfönstret
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                System.out.println("Path is : " + selectedFile.getAbsolutePath());
                open(selectedFile.getAbsolutePath()); //returnerar en sträng med sökvärden
                //open("europa.gif"); //returnerar en sträng med sökvärden
                changed = false;
            }
        }
    }

    // ===== Sparar info om kartan och grafen med noder och förbindelser =====
    private void save(String fileName) {
        try {
            // Kontroll att det finns något att spara
            if (imageFile == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No image selected!");
                alert.setHeaderText("Save Error");
                alert.showAndWait();
                return;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            writer.write("file:" + imageFile.getName());
            writer.newLine();

            /*List<String> nodes = new ArrayList<>();
            for (Place place : places) {
                nodes.add(place.getName() + ";" + place.getX() + ";" + place.getY());
            }
            for (String node : nodes) {
                writer.write(node);
                writer.newLine();
            }*/

            // ====== Skriv alla noder =====
            List<String> nodes = new ArrayList<>();
            for (Place place : places) {
                writer.write(place.getName() + ";" + place.getX() + ";" + place.getY());
            }
            writer.write(String.join(";", nodes)); // Sammanfogar alla platser i en rad
            writer.newLine();

            // Skriv alla noder
            /*List<String> nodes = new ArrayList<>();
            for (Place place : places) {
                nodes.add(place.getName() + ";" + place.getX() + ";" + place.getY());
            }
            writer.write(String.join(";", nodes));
            writer.newLine();*/

           // Skriv varje förbindelse
           for (Place from : graph.getNodes()) {
                for (Edge<Place> edge : graph.getEdgesFrom(from)) {
                    writer.write(from + ";" + edge.getDestination() + ";" + edge.getName() + ";" + edge.getWeight());
                    writer.newLine();
                }
            }

            //writer.write(from.getName() + ";" + to.getName() + ";" + edge.getName() + ";" + edge.getWeight());

            /*for (Place from : graph.getNodes()) {
                for (Edge<Place> edge : graph.getEdgesFrom(from)) {
                    Place to = edge.getDestination();
                    if (from.getName().compareTo(to.getName()) < 0) {
                        writer.write(from.getName() + ";" + to.getName() + ";" + edge.getName() + ";" + edge.getWeight());
                        writer.newLine();
                    }
                }
            }*/

            writer.close();

            changed = false;

            } catch(FileNotFoundException e){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Can't open file!");
                alert.showAndWait();
            } catch(IOException e){
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
                alert.showAndWait();
            }
        }

    class SaveHandler implements EventHandler<ActionEvent> {
        public void handle(ActionEvent event){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save your graf-file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph File", "*.graph"));
            // Startkatalogsmapp där bild ska laddas ifrån
            fileChooser.setInitialDirectory(new File("src/main/resources"));
            // Visa fildialogfönstret
            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile != null) {
                save(selectedFile.getAbsolutePath());
                changed = false;
            }
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent> {
        public void handle(ActionEvent event) {
        WritableImage snapshot = center.snapshot(new SnapshotParameters(), null);
        File file = new File("src/main/java/se/su/inlupp/capture.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            showInfo("Skärmbild sparad som capture.png.");
        } catch (IOException ex) {
            errorMessage("Kunde inte spara bild: " + ex.getMessage());
        }
    }
    }

    class ExitHandler implements EventHandler<ActionEvent> {
        public void handle(ActionEvent event) {
            if (confirmDiscard()){
                Platform.exit(); // Avslutar programmet trots osparade ändringar
            }
        }
    }

    //============== Del 2: Start på HBox. 4.2.1 Knappen new place ====================
    //Punkt 2 enligt deluppgift 4.2.1 forts.
    private void addNewPlace(MouseEvent event) {
        //Forts. punkt 2 enligt deluppgift 4.2.1 placera ny plats.
        //TextInputDialog är en JavaFX-klass.
        TextInputDialog nameOfPlace = new TextInputDialog();
        nameOfPlace.setTitle("Name");
        nameOfPlace.setHeaderText("Name of Place: ");

        Optional<String> chosenPlaceName = nameOfPlace.showAndWait();

        //Skapa Place. isPresent() kollar om det finns ett faktiskt värde i Optional-objektet.
        if (chosenPlaceName.isPresent()) {
            String name = chosenPlaceName.get();
            double x = event.getX();
            double y = event.getY();

            Place place = new Place(name, x, y);
            places.add(place);
            graph.add(place);
            center.getChildren().add(place);

            // Clickhantering för markering "pickedPlaces"
            place.setOnMouseClicked(new PickedPlacesClickHandler());

            //3.b
            center.setOnMouseClicked(null);
            center.setCursor(Cursor.DEFAULT);
            //3.c
            setButtonsDisabled(false);

            changed = true;
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent> {
        public void handle(ActionEvent event) {
            //Punkt 1(a+b) enligt deluppgift 4.2.1
            center.setCursor(Cursor.CROSSHAIR);
            newPlaceButton.setDisable(true);
            //Punkt 2 enligt deluppgift 4.2.1 placera ny plats
            center.setOnMouseClicked(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    addNewPlace(event);
                }
            });
        }
    }

    //==============Del 2: 4.2.2 Platser==================
    class PickedPlacesClickHandler implements EventHandler<MouseEvent>{
        public void handle(MouseEvent event) {
            //För att ta reda på vilken Place-objekt som man klickade på samt skicka till metoden pickedPlaces().
            //event.getSource() frågar vilken Place som man klickade på
            Place place = (Place) event.getSource();

            if (place.isSelected()){
                place.setSelected(false);
                pickedPlaces.remove(place);
            } else if (pickedPlaces.size() < 2){
                place.setSelected(true);
                pickedPlaces.add(place);
            }
        }
    }

    //==============Del 2: New connection====================
    class NewConnection implements EventHandler<ActionEvent>{
        //Två platser måste vara valda
        public void handle(ActionEvent event) {
            if (pickedPlaces.size() < 2) {
                errorMessage("Two locations must be selected!");
                return;
            }

            //Hämta de två markerade platserna i listan pickedPlaces
            Place placeFrom = pickedPlaces.get(0);
            Place placeTo = pickedPlaces.get(1);

            //Kontrollera att det inte redan finns en förbindelse
            if (graph.getEdgeBetween(placeFrom, placeTo) != null) {
                errorMessage("Thers is allready a connection between these two locations!");
                return;
            }

            // ============== Fönstret för Connection ==============
            Dialog<Pair<String, String>> connection = new Dialog<>();
            connection.setTitle("Connection");

            connection.setHeaderText("Connection from " + placeFrom.getName() + " to " + placeTo.getName());

            ButtonType OK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType Cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            connection.getDialogPane().getButtonTypes().addAll(OK, Cancel);

            //Skapa ett formulär för namn och tid
            GridPane grid = new GridPane(10, 10);
            TextField nameInput = new TextField();
            grid.add(new Label("Name: "), 0, 0);
            grid.add(nameInput, 1, 0);

            TextField timeInput = new TextField();
            grid.add(new Label("Time: "), 0, 1);
            grid.add(timeInput, 1, 1);

            //Lägger in formuläret med TextFields i dialogrutan.
            connection.getDialogPane().setContent(grid);

            //Om man klickar "OK" tas användarens input från textfälten. Returnera Pair av namn och tid.
            connection.setResultConverter(new Callback<ButtonType, Pair<String, String>>() {
                public Pair<String, String> call(ButtonType clickButton) {
                    if (clickButton == OK) {
                        return new Pair<>(nameInput.getText(), timeInput.getText());
                    }
                    return null;
                }
            });
            //Visning av fönstret
            Optional<Pair<String, String>> output = connection.showAndWait();

            //Om klickar "OK" hämtas användarens input och sparas i variabler
            if (output.isPresent()) {
                Pair<String, String> pair = output.get();
                String nameOutput = pair.getKey();
                String timeOutput = pair.getValue();

                //Namn får inte vara tomt
                if (nameOutput.trim().isEmpty()) {
                    errorMessage("Name cannot be empty! Try again.");
                    return;
                }

                //Time får endast innehålla siffror
                try {
                    int weight = Integer.parseInt(timeOutput.trim());
                    //Om konverteringen till heltal inte funkar visas ett felmeddelande
                    if (weight < 0) throw new NumberFormatException();

                    //graph.connect(placeFrom, placeTo);
                    graph.connect(placeFrom, placeTo, nameOutput, weight);
                    Line line = new Line(placeFrom.getX(), placeFrom.getY(), placeTo.getX(), placeTo.getY());
                    center.getChildren().add(line);
                    changed = true;

                } catch (NumberFormatException e) {
                    errorMessage("Invalid time format! Please enter a number.");
                    return;
                }
            }
        }
    }

    class ShowConnection implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pickedPlaces.size() < 2) {
                errorMessage("You need to first select two locations!");
                return;
            }

            Place p1 = pickedPlaces.get(0);
            Place p2 = pickedPlaces.get(1);

            // Försök hämta förbindelsen mellan p1 och p2
            Edge<Place> edge = graph.getEdgeBetween(p1, p2);

            if (edge == null) {
                errorMessage("There is no connection between the chosen locations!");
                return;
            }

            // Visa dialogfönster med information om förbindelsen
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Förbindelseinfo");
            dialog.setHeaderText("Information om förbindelsen");

            Label nameLabel = new Label("Namn:");
            TextField nameField = new TextField(edge.getName());
            nameField.setEditable(false);

            Label weightLabel = new Label("Time:");
            TextField weightField = new TextField(String.valueOf(edge.getWeight()));
            weightField.setEditable(false);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(nameLabel, 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(weightLabel, 0, 1);
            grid.add(weightField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();
        }
    }

    class ChangeConnection implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pickedPlaces.size() < 2) {
                errorMessage("You need to select two locations!");
                return;
            }

            Place p1 = pickedPlaces.get(0);
            Place p2 = pickedPlaces.get(1);

            Edge<Place> edge = graph.getEdgeBetween(p1, p2);
            if (edge == null) {
                errorMessage("There is no connection between the chosen locations!");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Change connection");
            dialog.setHeaderText("Change the time för the connection between " + p1.getName() + " and " + p2.getName());

            Label nameLabel = new Label("Name:");
            TextField nameField = new TextField(edge.getName());
            nameField.setEditable(false);

            Label weightLabel = new Label("New time:");
            TextField weightField = new TextField();

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(nameLabel, 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(weightLabel, 0, 1);
            grid.add(weightField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    int newWeight = Integer.parseInt(weightField.getText().trim());
                    if (newWeight < 0) throw new NumberFormatException();

                    // Uppdatera båda riktningarna
                    Edge<Place> edge1 = graph.getEdgeBetween(p1, p2);
                    Edge<Place> edge2 = graph.getEdgeBetween(p2, p1);

                    if (edge1 != null && edge2 != null) {
                        edge1.setWeight(newWeight);
                        edge2.setWeight(newWeight);
                        changed = true;
                    }

                } catch (NumberFormatException ex) {
                    errorMessage("Time must be a number.");
                }
            }
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent> {
        private final List<Line> pathLines = new ArrayList<>();

        @Override
        public void handle(ActionEvent event) {
            if (pickedPlaces.size() < 2) {
                errorMessage("Two locations must be selected!");
                return;
            }

            Place p1 = pickedPlaces.get(0);
            Place p2 = pickedPlaces.get(1);

            List<Edge<Place>> path = graph.getPath(p1, p2);

            for (Line line : pathLines) {
                center.getChildren().remove(line);
            }
            pathLines.clear();

            if (path == null || path.isEmpty()) {
                errorMessage("There is no road between the selected locations.");
                return;
            }

            Place current = p1;
            int total = 0;
            String text = "Road from " + p1.getName() + " to " + p2.getName() + ":\n\n";

            for (Edge<Place> edge : path) {
                Place next = edge.getDestination();

                Line line = new Line(current.getX(), current.getY(), next.getX(), next.getY());
                line.setStyle("-fx-stroke: green; -fx-stroke-width: 2;");
                center.getChildren().add(line);
                pathLines.add(line);

                text +=" to " + current.getName() + " by " + edge.getName() + " takes " + edge.getWeight() + "\n";

                total += edge.getWeight();
                current = next;
            }

            text += "\nTotalt: " + total;

            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Shortest way");
            dialog.setHeaderText(null);
            dialog.setContentText(text);
            dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            dialog.showAndWait();
        }
    }

    // ============== Felmeddelanden ================
    private boolean confirmDiscard() {
        if (!changed) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Unsaved changes, exit anyway?", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    private void errorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Error");
        alert.showAndWait();
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
