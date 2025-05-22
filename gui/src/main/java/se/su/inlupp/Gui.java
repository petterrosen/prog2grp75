package se.su.inlupp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Gui extends Application {
  private Stage stage;
  private final FileChooser fileChooser = new FileChooser();
  private final ImageView imageView = new ImageView();
  private final Pane center = new Pane();
  private File currentGraphFile;
  private boolean changed = false;

  @Override
  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    primaryStage.setTitle("PathFinder");

    // Layout
    BorderPane root = new BorderPane();
    VBox top = new VBox();
    MenuBar menuBar = new MenuBar();

    Menu fileMenu = new Menu("File");

    // ===== Menyval =====
    MenuItem newMap = new MenuItem("New Map");
    MenuItem open = new MenuItem("Open");
    MenuItem save = new MenuItem("Save");
    MenuItem saveImage = new MenuItem("Save Image");
    MenuItem exit = new MenuItem("Exit");

    // ===== Handlers =====
    newMap.setOnAction(this::handleNewMap);
    open.setOnAction(this::handleOpen);
    save.setOnAction(this::handleSave);
    saveImage.setOnAction(this::handleSaveImage);
    exit.setOnAction(this::handleExit);

    fileMenu.getItems().addAll(newMap, open, save, saveImage, new SeparatorMenuItem(), exit);
    menuBar.getMenus().add(fileMenu);

    // ===== Knapprad (valfri) =====
    HBox buttons = new HBox(10,
            new Button("New Place"),
            new Button("New Connection"),
            new Button("Show Connection"),
            new Button("Change Connection"),
            new Button("Find Path")
    );
    buttons.setAlignment(Pos.CENTER);
    buttons.setPadding(new Insets(10));

    top.getChildren().addAll(menuBar, buttons);
    root.setTop(top);

    // ===== Bildvisning i mitten =====
    imageView.setPreserveRatio(true);
    center.getChildren().add(imageView);
    root.setCenter(center);

    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  // === 4.1.1 New Map ===
  private void handleNewMap(ActionEvent e) {
    FileChooser fc = new FileChooser();
    fc.setTitle("Välj bakgrundsbild");
    fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    );
    File file = fc.showOpenDialog(stage);
    if (file != null) {
      Image img = new Image(file.toURI().toString());
      imageView.setImage(img);
      center.setPrefSize(img.getWidth(), img.getHeight());
      changed = true;
    }
  }

  // === 4.1.2 Open ===
  private void handleOpen(ActionEvent e) {
    if (changed && !confirmDiscard()) return;

    FileChooser fc = new FileChooser();
    fc.setTitle("Öppna .graph-fil");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
    File file = fc.showOpenDialog(stage);
    if (file != null) {
      String imageUrl = GraphIO.loadGraphFile(file);
      if (imageUrl != null) {
        imageView.setImage(new Image(imageUrl));
        changed = false;
        currentGraphFile = file;
      }
    }
  }

  // === 4.1.3 Save ===
  private void handleSave(ActionEvent e) {
    FileChooser fc = new FileChooser();
    fc.setTitle("Spara .graph-fil");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
    File file = fc.showSaveDialog(stage);
    if (file != null) {
      String imageUrl = imageView.getImage().getUrl();
      GraphIO.saveGraphFile(file, imageUrl);
      changed = false;
      currentGraphFile = file;
    }
  }

  // === 4.1.4 Save Image ===
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

  // === 4.1.5 Exit ===
  private void handleExit(ActionEvent e) {
    if (!changed || confirmDiscard()) {
      stage.close();
    }
  }

  private boolean confirmDiscard() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Osparde ändringar finns. Vill du förkasta dem?", ButtonType.OK, ButtonType.CANCEL);
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
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}