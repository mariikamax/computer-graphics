package com.cgvsu;

import com.cgvsu.model.ModelUtils;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.render_engine.RenderSettings;
import com.cgvsu.render_engine.Texture;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.math.Vector3f;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox controlPanel;

    @FXML
    private CheckBox wireframeCheckBox;

    @FXML
    private CheckBox fillPolygonsCheckBox;

    @FXML
    private CheckBox textureCheckBox;

    @FXML
    private CheckBox lightingCheckBox;

    @FXML
    private Label modelNameLabel;

    private Model mesh = null;

    private Camera camera = new Camera(
            new Vector3f(0, 10, 100),
            new Vector3f(0, 0, 0),
            (float) Math.toRadians(60.0),
            1.0f,
            0.01F,
            100.0F);

    private com.cgvsu.model.Scene scene;

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        scene = new com.cgvsu.model.Scene();
        scene.addCamera(camera);
        com.cgvsu.render_engine.RenderEngine.setScene(scene);

        com.cgvsu.model.Light light = scene.getLight();
        light.setPosition(new Vector3f(100, 200, 100));
        light.setColor(new Vector3f(1, 1, 1));
        light.setIntensity(1.5f);

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                if (!scene.getModels().contains(mesh)) {
                    scene.addModel(mesh);
                }

                com.cgvsu.render_engine.RenderEngine.render(
                        canvas.getGraphicsContext2D(),
                        camera,
                        mesh,
                        (int) width,
                        (int) height,
                        javafx.scene.paint.Color.LIGHTGRAY
                );
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        initRenderSettings();
    }



    private void initRenderSettings() {
        RenderSettings settings = RenderEngine.getRenderSettings();

        wireframeCheckBox.setSelected(settings.isDrawWireframe());
        fillPolygonsCheckBox.setSelected(settings.isFillPolygons());
        textureCheckBox.setSelected(settings.isUseTexture());
        lightingCheckBox.setSelected(settings.isUseLighting());
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);

            ModelUtils.triangulate(mesh);
            ModelUtils.calculateNormals(mesh);

            modelNameLabel.setText(file.getName());

        } catch (IOException exception) {
            showError("Failed to load model: " + exception.getMessage());
        }
    }

    @FXML
    private void handleWireframe(ActionEvent event) {
        RenderSettings settings = RenderEngine.getRenderSettings();
        settings.setDrawWireframe(wireframeCheckBox.isSelected());
    }

    @FXML
    private void handleFillPolygons(ActionEvent event) {
        RenderSettings settings = RenderEngine.getRenderSettings();
        settings.setFillPolygons(fillPolygonsCheckBox.isSelected());
    }

    @FXML
    private void handleTexture(ActionEvent event) {
        RenderSettings settings = RenderEngine.getRenderSettings();
        settings.setUseTexture(textureCheckBox.isSelected());
    }

    @FXML
    private void handleLighting(ActionEvent event) {
        RenderSettings settings = RenderEngine.getRenderSettings();
        settings.setUseLighting(lightingCheckBox.isSelected());
    }

    @FXML
    private void handleLoadTexture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                Texture texture = new Texture(file.getAbsolutePath());
                RenderEngine.getRenderSettings().setCurrentTexture(texture);

                textureCheckBox.setSelected(true);
                handleTexture(null);

            } catch (Exception e) {
                showError("Failed to load texture: " + e.getMessage());
            }
        }
    }

    @FXML
    private void addCamera() {
        showInfo("Add Camera", "Camera addition not implemented yet");
    }

    @FXML
    private void removeCamera() {
        showInfo("Remove Camera", "Camera removal not implemented yet");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}