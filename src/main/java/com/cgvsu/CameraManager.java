package com.cgvsu;

import com.cgvsu.model.Scene;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.math.Vector3f;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.List;

public class CameraManager {
    private Scene scene;
    private ListView<String> cameraListView;
    private VBox cameraPanel;
    private Label cameraInfoLabel;

    public CameraManager(Scene scene) {
        this.scene = scene;
        createCameraPanel();
    }

    private void createCameraPanel() {
        cameraPanel = new VBox(10);
        cameraPanel.setPadding(new Insets(10));
        cameraPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1;");

        Label titleLabel = new Label("Камеры сцены");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        cameraListView = new ListView<>();
        cameraListView.setPrefHeight(150);
        updateCameraList();

        cameraInfoLabel = new Label("Выберите камеру");
        cameraInfoLabel.setWrapText(true);

        HBox buttonBox = new HBox(5);
        Button addButton = new Button("+");
        Button removeButton = new Button("-");
        Button switchButton = new Button("Переключить");
        Button editButton = new Button("Редактировать");

        addButton.setOnAction(e -> addCamera());
        removeButton.setOnAction(e -> removeSelectedCamera());
        switchButton.setOnAction(e -> switchToSelectedCamera());
        editButton.setOnAction(e -> editSelectedCamera());

        buttonBox.getChildren().addAll(addButton, removeButton, switchButton, editButton);

        cameraListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateCameraInfo());

        cameraPanel.getChildren().addAll(
                titleLabel, cameraListView, cameraInfoLabel, buttonBox
        );
    }

    public VBox getCameraPanel() {
        return cameraPanel;
    }

    public void updateCameraList() {
        cameraListView.getItems().clear();
        List<Camera> cameras = scene.getCameras();

        for (int i = 0; i < cameras.size(); i++) {
            Camera cam = cameras.get(i);
            String status = (cam == scene.getActiveCamera()) ? " (активная)" : "";
            cameraListView.getItems().add("Камера " + (i + 1) + status);
        }

        if (!cameras.isEmpty()) {
            cameraListView.getSelectionModel().select(0);
        }
    }

    private void updateCameraInfo() {
        int selectedIndex = cameraListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < scene.getCameras().size()) {
            Camera cam = scene.getCameras().get(selectedIndex);
            Vector3f pos = cam.getPosition();
            Vector3f target = cam.getTarget();

            String info = String.format(
                    "Позиция: (%.1f, %.1f, %.1f)\n" +
                            "Цель: (%.1f, %.1f, %.1f)\n" +
                            "Состояние: %s",
                    pos.x, pos.y, pos.z,
                    target.x, target.y, target.z,
                    (cam == scene.getActiveCamera()) ? "активная" : "неактивная"
            );

            cameraInfoLabel.setText(info);
        }
    }

    public void addCamera() {
        Camera activeCam = scene.getActiveCamera();
        Vector3f newPos = new Vector3f(
                activeCam.getPosition().x + 20,
                activeCam.getPosition().y,
                activeCam.getPosition().z
        );

        Camera newCamera = new Camera(
                newPos,
                new Vector3f(0, 0, 0),
                (float) Math.toRadians(60.0),
                1.0f,
                0.01F,
                100.0F
        );

        scene.addCamera(newCamera);
        updateCameraList();
        cameraListView.getSelectionModel().select(scene.getCameras().size() - 1);
    }

    public void removeSelectedCamera() {
        int selectedIndex = cameraListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < scene.getCameras().size()) {
            if (scene.getCameras().size() > 1) {
                Camera cam = scene.getCameras().get(selectedIndex);
                scene.removeCamera(cam);
                updateCameraList();
            } else {
                showAlert("Ошибка", "Нельзя удалить последнюю камеру!");
            }
        }
    }

    public void switchToSelectedCamera() {
        int selectedIndex = cameraListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < scene.getCameras().size()) {
            Camera cam = scene.getCameras().get(selectedIndex);
            scene.setActiveCamera(cam);
            updateCameraList();
            updateCameraInfo();
        }
    }

    public void editSelectedCamera() {
        int selectedIndex = cameraListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < scene.getCameras().size()) {
            Camera cam = scene.getCameras().get(selectedIndex);
            showCameraEditorDialog(cam);
        }
    }

    private void showCameraEditorDialog(Camera camera) {
        Dialog<Camera> dialog = new Dialog<>();
        dialog.setTitle("Редактирование камеры");
        dialog.setHeaderText("Настройки камеры");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Vector3f pos = camera.getPosition();
        Vector3f target = camera.getTarget();

        TextField posXField = new TextField(String.valueOf(pos.x));
        TextField posYField = new TextField(String.valueOf(pos.y));
        TextField posZField = new TextField(String.valueOf(pos.z));

        TextField targetXField = new TextField(String.valueOf(target.x));
        TextField targetYField = new TextField(String.valueOf(target.y));
        TextField targetZField = new TextField(String.valueOf(target.z));

        float fovDeg = (float)Math.toDegrees(60.0); // предполагаем стандартное значение
        TextField fovField = new TextField(String.valueOf(fovDeg));

        grid.add(new Label("Позиция X:"), 0, 0);
        grid.add(posXField, 1, 0);
        grid.add(new Label("Y:"), 2, 0);
        grid.add(posYField, 3, 0);
        grid.add(new Label("Z:"), 4, 0);
        grid.add(posZField, 5, 0);

        grid.add(new Label("Цель X:"), 0, 1);
        grid.add(targetXField, 1, 1);
        grid.add(new Label("Y:"), 2, 1);
        grid.add(targetYField, 3, 1);
        grid.add(new Label("Z:"), 4, 1);
        grid.add(targetZField, 5, 1);

        grid.add(new Label("FOV (град):"), 0, 2);
        grid.add(fovField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Обновляем позицию
                    float newPosX = Float.parseFloat(posXField.getText());
                    float newPosY = Float.parseFloat(posYField.getText());
                    float newPosZ = Float.parseFloat(posZField.getText());
                    camera.setPosition(new Vector3f(newPosX, newPosY, newPosZ));

                    // Обновляем цель
                    float newTargetX = Float.parseFloat(targetXField.getText());
                    float newTargetY = Float.parseFloat(targetYField.getText());
                    float newTargetZ = Float.parseFloat(targetZField.getText());
                    camera.setTarget(new Vector3f(newTargetX, newTargetY, newTargetZ));

                    return camera;
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Некорректные числовые значения!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
        updateCameraList();
        updateCameraInfo();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}