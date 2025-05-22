package se.su.inlupp;

import javafx.application.Application;
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
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Gui extends Application {
  private Stage stage;
  private final FileChooser fileChooser = new FileChooser();
  private final ImageView imageView = new ImageView();
  private final Pane center = new Pane();
  private File currentGraphFile;
  private boolean changed = false;

  private final List<Place> places = new ArrayList<>();
  private final List<Place> pickedPlaces = new ArrayList<>();
  private Graph<Place> graph = new ListGraph<>();

  private Button newPlaceButton;
  private Button newConnectionButton;
  private Button showConnectionButton;
  private Button changeConnectionButton;
  private Button findPathButton;

  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
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

    newMap.setOnAction(this::handleNewMap);
    open.setOnAction(this::handleOpen);
    save.setOnAction(this::handleSave);
    saveImage.setOnAction(this::handleSaveImage);
    exit.setOnAction(this::handleExit);

    fileMenu.getItems().addAll(newMap, open, save, saveImage, new SeparatorMenuItem(), exit);
    menuBar.getMenus().add(fileMenu);

    newPlaceButton = new Button("New Place");
    newPlaceButton.setOnAction(new NewPlaceHandler());
    newPlaceButton.setDisable(true);

    newConnectionButton = new Button("New Connection");
    newConnectionButton.setOnAction(new NewConnection());
    newConnectionButton.setDisable(true);

    showConnectionButton = new Button("Show Connection");
    showConnectionButton.setDisable(true);

    changeConnectionButton = new Button("Change Connection");
    changeConnectionButton.setDisable(true);

    findPathButton = new Button("Find Path");
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

    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.setOnCloseRequest(event -> {
      if (changed && !confirmDiscard()) event.consume();
    });
    primaryStage.show();
  }

  private void handleNewMap(ActionEvent e) {
    fileChooser.setTitle("Välj bakgrundsbild");
    fileChooser.getExtensionFilters().setAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
    );
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      Image img = new Image(file.toURI().toString());
      imageView.setImage(img);
      imageView.setUserData(file.toURI().toString());
      center.setPrefSize(img.getWidth(), img.getHeight());

      places.clear();
      graph = new ListGraph<>();
      center.getChildren().clear();
      center.getChildren().add(imageView);
      pickedPlaces.clear();

      changed = true;
      setButtonsDisabled(false);
    }
  }

  private void handleOpen(ActionEvent e) {
    if (changed && !confirmDiscard()) return;

    fileChooser.setTitle("Öppna .graph-fil");
    fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      places.clear();
      graph = new ListGraph<>();
      pickedPlaces.clear();

      String imageUrl = GraphIO.loadGraphFile(file, places, graph);
      if (imageUrl != null) {
        imageView.setImage(new Image(imageUrl));
        imageView.setUserData(imageUrl);
        center.setPrefSize(imageView.getImage().getWidth(), imageView.getImage().getHeight());

        center.getChildren().clear();
        center.getChildren().add(imageView);
        for (Place p : places) {
          center.getChildren().add(p);
          p.setOnMouseClicked(new PickedPlacesClickHandler());
        }

        changed = false;
        currentGraphFile = file;
        setButtonsDisabled(false);
      }
    }
  }

  private void handleSave(ActionEvent e) {
    fileChooser.setTitle("Spara .graph-fil");
    fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
    File file = fileChooser.showSaveDialog(stage);
    if (file != null) {
      String imageUrl = (String) imageView.getUserData();
      GraphIO.saveGraphFile(file, imageUrl, places, graph);
      changed = false;
      currentGraphFile = file;
    }
  }

  private void handleSaveImage(ActionEvent e) {
    WritableImage snapshot = center.snapshot(new SnapshotParameters(), null);
    File file = new File("capture.png");
    try {
      ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
      showInfo("Skärmbild sparad som capture.png.");
    } catch (IOException ex) {
      showError("Kunde inte spara bild: " + ex.getMessage());
    }
  }

  private void handleExit(ActionEvent e) {
    if (!changed || confirmDiscard()) {
      stage.close();
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
          showError("Vikt måste vara ett positivt heltal.");
          return;
        }

        graph.connect(p1, p2, name, weight);
        Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        center.getChildren().add(line);
        changed = true;
      }
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
            "Osparade ändringar finns. Vill du förkasta dem?", ButtonType.OK, ButtonType.CANCEL);
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