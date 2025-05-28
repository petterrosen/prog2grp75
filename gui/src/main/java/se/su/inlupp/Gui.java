// PROG2 VT2025, inlämningsuppgift
// grupp 75
// Sama Matloub
// Yasin Akdeve
// Petter Rosén pero0033
package se.su.inlupp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class Gui extends Application {
  private Stage stage;
  private final FileChooser fileChooser = new FileChooser();
  private final ImageView imageView = new ImageView();
  private final Pane center = new Pane();
  private File currentGraphFile;
  private boolean changed = false;
  private File imageFile;

  private final List<Place> places = new ArrayList<>();
  private final List<Place> pickedPlaces = new ArrayList<>();
  private Graph<Place> graph = new ListGraph<>();

  private Button newPlaceButton, newConnectionButton, showConnectionButton, changeConnectionButton, findPathButton;

  @Override
  public void start(Stage primaryStage) {
    //this.stage = primaryStage;
    stage = primaryStage;
    primaryStage.setTitle("PathFinder");

    BorderPane root = new BorderPane();
    VBox top = new VBox();
    MenuBar menuBar = new MenuBar();

    Menu fileMenu = new Menu("File");
    MenuItem newMap = new MenuItem("New Map");
    MenuItem open = new MenuItem("Open");
    MenuItem save = new MenuItem("Save");
    MenuItem saveImage = new MenuItem("Save Image");
    MenuItem exit = new MenuItem("Exit");

    newMap.setOnAction(new handleNewMap());
    open.setOnAction(new OpenHandler());
    save.setOnAction(new handleSave());
    saveImage.setOnAction(new handleSaveImage());
    exit.setOnAction(new handleExit());

    fileMenu.getItems().addAll(newMap, open, save, saveImage , exit);
    menuBar.getMenus().add(fileMenu);

    newPlaceButton = new Button("New Place");
    newPlaceButton.setOnAction(new NewPlaceHandler());
    newPlaceButton.setDisable(true);

    newConnectionButton = new Button("New Connection");
    newConnectionButton.setOnAction(new NewConnection());
    newConnectionButton.setDisable(true);

    showConnectionButton = new Button("Show Connection");
    showConnectionButton.setOnAction(new ShowConnection());
    showConnectionButton.setDisable(true);

    changeConnectionButton = new Button("Change Connection");
    changeConnectionButton.setOnAction(new ChangeConnection());
    changeConnectionButton.setDisable(true);

    findPathButton = new Button("Find Path");
    findPathButton.setOnAction(new FindPath());
    findPathButton.setDisable(true);

    HBox buttons = new HBox(10,
            newPlaceButton,
            newConnectionButton,
            showConnectionButton,
            changeConnectionButton,
            findPathButton
    );
    buttons.setAlignment(Pos.CENTER);
    buttons.setPadding(new Insets(10));

    top.getChildren().addAll(menuBar, buttons);
    root.setTop(top);

    imageView.setPreserveRatio(true);
    center.getChildren().add(imageView);
    root.setCenter(center);

    //ändra fönstret efter bildens storlek
    imageView.imageProperty().addListener((obs, oldImage, newImage) -> {
      if (newImage != null) {
        Platform.runLater(() -> {
          center.setPrefSize(newImage.getWidth(), newImage.getHeight());
          center.setMinSize(newImage.getWidth(), newImage.getHeight());
          stage.sizeToScene();
        });
      }
    });

    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setOnCloseRequest(event -> {
      if (changed && !confirmDiscard()) event.consume();
    });
    primaryStage.show();
  }


  class handleNewMap implements EventHandler<ActionEvent> {
    public void handle(ActionEvent event) {

      fileChooser.setTitle("Välj bakgrundsbild");
      fileChooser.setInitialDirectory(new File("src/main/java/resources"));
      fileChooser.getExtensionFilters().setAll(
              new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
      );
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        Image img = new Image(file.toURI().toString());
        imageView.setImage(img);
        imageView.setUserData(file.toURI().toString());


        places.clear();
        graph = new ListGraph<>();
        center.getChildren().clear();
        center.getChildren().add(imageView);
        pickedPlaces.clear();

        changed = true;
        setButtonsDisabled(false);
      }
    }
  }


  /*private void handleOpen(ActionEvent e) {
    if (changed && !confirmDiscard()) return;

    fileChooser.setTitle("Öppna .graph-fil");
    fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      places.clear();
      graph = new ListGraph<>();
      pickedPlaces.clear();

      try (Scanner scanner = new Scanner(file)) {
        if (!scanner.hasNextLine()) {
          showError("Filen är tom.");
          return;
        }

        String imageUrl = scanner.nextLine();
        imageView.setImage(new Image(imageUrl));
        imageView.setUserData(imageUrl);
        center.setPrefSize(imageView.getImage().getWidth(), imageView.getImage().getHeight());

        center.getChildren().clear();
        center.getChildren().add(imageView);

        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          String[] parts = line.split(";");
          if (parts.length == 3) {
            // Place
            String name = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            Place p = new Place(name, x, y);
            places.add(p);
            graph.add(p);
            center.getChildren().add(p);
            p.setOnMouseClicked(new PickedPlacesClickHandler());
          } else if (parts.length == 4) {
            // Edge
            String fromName = parts[0];
            String toName = parts[1];
            String edgeName = parts[2];
            int weight = Integer.parseInt(parts[3]);

            Place from = findPlaceByName(fromName);
            Place to = findPlaceByName(toName);
            if (from != null && to != null) {
              graph.connect(from, to, edgeName, weight);
              center.getChildren().add(new Line(from.getX(), from.getY(), to.getX(), to.getY()));
            }
          }
        }

        changed = false;
        currentGraphFile = file;
        setButtonsDisabled(false);
      } catch (Exception ex) {
        showError("Fel vid inläsning: " + ex.getMessage());
      }
    }
  } */

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

  private void open(String fileName) {
    try {

      BufferedReader reader = new BufferedReader(new FileReader(fileName));

      //Första raden i filen för att ladda kartbilden
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

        //Circle dot = new Circle(x, y, 8, Color.BLUE);
        //Text label = new Text(x + 10, y, name);
        center.getChildren().add(place);

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
//=============================================

  class handleSave implements EventHandler<ActionEvent> {
    public void handle(ActionEvent event) {


      fileChooser.setTitle("Spara .graph-fil");
      fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
      File file = fileChooser.showSaveDialog(stage);
      if (file != null) {
        try (PrintWriter writer = new PrintWriter(file)) {
          String imageUrl = (String) imageView.getUserData();
          writer.println(imageUrl);

          for (Place p : places) {
            writer.printf("%s;%.1f;%.1f%n", p.getName(), p.getX(), p.getY());
          }

          Set<String> savedEdges = new HashSet<>();
          for (Place p1 : places) {
            for (Edge<Place> edge : graph.getEdgesFrom(p1)) {
              Place p2 = edge.getDestination();
              String id = p1.getName() + "-" + p2.getName();
              String reverseId = p2.getName() + "-" + p1.getName();
              if (!savedEdges.contains(id) && !savedEdges.contains(reverseId)) {
                writer.printf("%s;%s;%s;%d%n", p1.getName(), p2.getName(), edge.getName(), edge.getWeight());
                savedEdges.add(id);
              }
            }
          }

          changed = false;
          currentGraphFile = file;
        } catch (IOException ex) {
          showError("Kunde inte spara fil: " + ex.getMessage());
        }
      }
    }
  }

  private Place findPlaceByName(String name) {
    for (Place p : places) {
      if (p.getName().equals(name)) return p;
    }
    return null;
  }

  class handleSaveImage implements EventHandler<ActionEvent> {
    public void handle (ActionEvent event){

      WritableImage snapshot = center.snapshot(new SnapshotParameters(), null);
      File file = new File("src/main/java/se/su/inlupp/capture.png");
      try {
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        showInfo("Skärmbild sparad som capture.png.");
      } catch (IOException ex) {
        showError("Kunde inte spara bild: " + ex.getMessage());
      }
    }
  }

  class handleExit implements EventHandler<ActionEvent> {
    public void handle(ActionEvent event) {
      if (!changed || confirmDiscard()) {
        stage.close();
      }
    }
  }

  private void addNewPlace(MouseEvent event) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Name");
    dialog.setHeaderText("Name of Place:");

    Optional<String> result = dialog.showAndWait();

    if (result.isPresent()) {
      String name = result.get();
      double x = event.getX();
      double y = event.getY();

      Place place = new Place(name, x, y);
      places.add(place);
      graph.add(place);
      center.getChildren().add(place);

      place.setOnMouseClicked(new PickedPlacesClickHandler());

      center.setOnMouseClicked(null);
      center.setCursor(Cursor.DEFAULT);
      setButtonsDisabled(false);
      changed = true;
    }
  }

  class NewPlaceHandler implements EventHandler<ActionEvent> {
    public void handle(ActionEvent event) {
      center.setCursor(Cursor.CROSSHAIR);
      newPlaceButton.setDisable(true);
      center.setOnMouseClicked(new EventHandler<MouseEvent>() {
        public void handle(MouseEvent e) {
          addNewPlace(e);
        }
      });
    }
  }

  class PickedPlacesClickHandler implements EventHandler<MouseEvent> {
    public void handle(MouseEvent event) {
      Place place = (Place) event.getSource();
      if (place.isSelected()) {
        place.setSelected(false);
        pickedPlaces.remove(place);
      } else if (pickedPlaces.size() < 2) {
        place.setSelected(true);
        pickedPlaces.add(place);
      }
    }
  }

  class NewConnection implements EventHandler<ActionEvent> {
    public void handle(ActionEvent event) {
      if (pickedPlaces.size() < 2) {
        showError("Du måste markera två platser först.");
        return;
      }

      Place p1 = pickedPlaces.get(0);
      Place p2 = pickedPlaces.get(1);

      if (graph.getEdgeBetween(p1, p2) != null) {
        showError("Det finns redan en förbindelse mellan dessa platser.");
        return;
      }

      Dialog<Pair<String, String>> dialog = new Dialog<>();
      dialog.setTitle("Ny förbindelse");
      dialog.setHeaderText("Mellan " + p1.getName() + " och " + p2.getName());

      ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);

      TextField nameField = new TextField();
      TextField weightField = new TextField();

      grid.add(new Label("Namn:"), 0, 0);
      grid.add(nameField, 1, 0);
      grid.add(new Label("Vikt:"), 0, 1);
      grid.add(weightField, 1, 1);

      dialog.getDialogPane().setContent(grid);

      dialog.setResultConverter(new Callback<ButtonType, Pair<String, String>>() {
        public Pair<String, String> call(ButtonType button) {
          if (button == okButton) {
            return new Pair<>(nameField.getText(), weightField.getText());
          }
          return null;
        }
      });

      Optional<Pair<String, String>> result = dialog.showAndWait();

      if (result.isPresent()) {
        String name = result.get().getKey();
        String weightText = result.get().getValue();

        if (name.trim().isEmpty()) {
          showError("Namn får inte vara tomt.");
          return;
        }

        int weight;
        try {
          weight = Integer.parseInt(weightText.trim());
          if (weight < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
          showError("Tid måste vara ett positivt heltal.");
          return;
        }

        graph.connect(p1, p2, name, weight);
        Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        center.getChildren().add(line);
        changed = true;
      }
    }
  }

  class ShowConnection implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      if (pickedPlaces.size() < 2) {
        showError("Du måste markera två platser först.");
        return;
      }

      Place p1 = pickedPlaces.get(0);
      Place p2 = pickedPlaces.get(1);

      // Försök hämta förbindelsen mellan p1 och p2
      Edge<Place> edge = graph.getEdgeBetween(p1, p2);

      if (edge == null) {
        showError("Det finns ingen förbindelse mellan de valda platserna.");
        return;
      }

      // Visa dialogfönster med information om förbindelsen
      Dialog<Void> dialog = new Dialog<>();
      dialog.setTitle("Förbindelseinfo");
      dialog.setHeaderText("Information om förbindelsen");

      Label nameLabel = new Label("Namn:");
      TextField nameField = new TextField(edge.getName());
      nameField.setEditable(false);

      Label weightLabel = new Label("Vikt:");
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
        showError("Du måste markera två platser.");
        return;
      }

      Place p1 = pickedPlaces.get(0);
      Place p2 = pickedPlaces.get(1);

      Edge<Place> edge = graph.getEdgeBetween(p1, p2);
      if (edge == null) {
        showError("Det finns ingen förbindelse mellan de valda platserna.");
        return;
      }

      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle("Ändra förbindelse");
      dialog.setHeaderText("Ändra tiden för förbindelsen mellan " + p1.getName() + " och " + p2.getName());

      Label nameLabel = new Label("Namn:");
      TextField nameField = new TextField(edge.getName());
      nameField.setEditable(false);

      Label weightLabel = new Label("Ny vikt:");
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
          showError("Vikten måste vara ett positivt heltal.");
        }
      }
    }
  }

  class FindPath implements EventHandler<ActionEvent> {
    private final List<Line> pathLines = new ArrayList<>();

    @Override
    public void handle(ActionEvent event) {
      if (pickedPlaces.size() < 2) {
        showError("Du måste markera två platser först.");
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
        showError("Det finns ingen väg mellan de valda platserna.");
        return;
      }

      Place current = p1;
      int total = 0;
      String text = "Väg från " + p1.getName() + " till " + p2.getName() + ":\n\n";

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
      dialog.setTitle("Kortaste väg");
      dialog.setHeaderText(null);
      dialog.setContentText(text);
      dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
      dialog.showAndWait();
    }
  }



  private void setButtonsDisabled(boolean disabled) {
    newPlaceButton.setDisable(disabled);
    newConnectionButton.setDisable(disabled);
    showConnectionButton.setDisable(disabled);
    changeConnectionButton.setDisable(disabled);
    findPathButton.setDisable(disabled);
  }

  private boolean confirmDiscard() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Osparade ändringar finns. Vill du avsluta ändå?", ButtonType.OK, ButtonType.CANCEL);
    alert.setHeaderText(null);
    return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
  }

  private void showInfo(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR, message);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}