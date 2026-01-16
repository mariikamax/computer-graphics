package com.cgvsu;

import com.cgvsu.model.Light;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.List;

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

        RenderSettings settings = RenderEngine.getRenderSettings();
        settings.setFillPolygons(true);
        settings.setDrawWireframe(false);
        settings.setUseTexture(false);
        settings.setUseLighting(false);
        settings.setFillColor(Color.LIGHTGRAY);

        fillPolygonsCheckBox.setSelected(true);
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(false);

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
        RenderEngine.getRenderSettings().setDrawWireframe(wireframeCheckBox.isSelected());
    }

    @FXML
    private void handleFillPolygons(ActionEvent event) {
        RenderEngine.getRenderSettings().setFillPolygons(fillPolygonsCheckBox.isSelected());
    }

    @FXML
    private void handleTexture(ActionEvent event) {
        RenderEngine.getRenderSettings().setUseTexture(textureCheckBox.isSelected());
    }

    @FXML
    private void handleLighting(ActionEvent event) {
        RenderEngine.getRenderSettings().setUseLighting(lightingCheckBox.isSelected());
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
        Camera newCamera = new Camera(
                new Vector3f(20, 20, 50), // позиция
                new Vector3f(0, 0, 0),    // target
                (float) Math.toRadians(60.0),
                1.0f,
                0.01F,
                100.0F
        );
        scene.addCamera(newCamera);
        updateCameraMenu();
    }

    @FXML
    private void removeCamera() {
        if (scene.getCameras().size() > 1) {
            scene.removeCamera(camera);
            camera = scene.getActiveCamera();
        } else {
            showInfo("Cannot remove", "Must have at least one camera");
        }
    }

    @FXML
    private void switchCamera() {
        List<Camera> cameras = scene.getCameras();
        if (cameras.size() > 1) {
            Camera current = scene.getActiveCamera();
            int currentIndex = cameras.indexOf(current);
            int nextIndex = (currentIndex + 1) % cameras.size();
            scene.setActiveCamera(cameras.get(nextIndex));

            showInfo("Camera Switched",
                    "Now using camera " + (nextIndex + 1) + " of " + cameras.size());
        }
    }

    @FXML
    private void attachLightToCamera() {
        Light light = scene.getLight();
        Camera activeCam = scene.getActiveCamera();

        if (activeCam != null) {
            // Для DIRECTIONAL света - направление как у камеры
            light.setType(Light.LightType.DIRECTIONAL);

            // Позиция света - позади камеры
            Vector3f camPos = activeCam.getPosition();
            Vector3f camTarget = activeCam.getTarget();
            Vector3f camDir = new Vector3f(
                    camTarget.x - camPos.x,
                    camTarget.y - camPos.y,
                    camTarget.z - camPos.z
            );

            // Нормализуем и отодвигаем назад
            float length = (float)Math.sqrt(camDir.x*camDir.x + camDir.y*camDir.y + camDir.z*camDir.z);
            if (length > 0) {
                camDir.x /= length;
                camDir.y /= length;
                camDir.z /= length;
            }

            // Свет позади и сверху от камеры
            Vector3f lightPos = new Vector3f(
                    camPos.x - camDir.x * 50,
                    camPos.y - camDir.y * 50 + 30,
                    camPos.z - camDir.z * 50
            );

            light.setPosition(lightPos);
            light.setColor(new Vector3f(1, 1, 1)); // Белый свет

            showInfo("Light Attached", "Light is now following the camera (directional)");
        }
    }

    private void updateCameraMenu() {
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
    private void handleChooseColor(ActionEvent event) {
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(RenderEngine.getRenderSettings().getFillColor());

        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Выберите цвет заливки");

        GridPane grid = new GridPane();
        grid.add(new Label("Цвет:"), 0, 0);
        grid.add(colorPicker, 1, 0);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return colorPicker.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(color -> {
            RenderEngine.getRenderSettings().setFillColor(color);
        });


        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                return colorPicker.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(color -> {
            RenderEngine.getRenderSettings().setFillColor(color);
            System.out.println("Color selected: " + color);
        });
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