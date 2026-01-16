package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.render_engine.MouseController;
import javafx.fxml.FXML;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GuiController {
    @FXML
    AnchorPane anchorPane;
    @FXML
    private Canvas canvas;

    private Model mesh = null;
    private Camera camera = null;
    private MouseController mouseController = null;

    private final String MODEL_FILE_PATH = "models/Wolf.obj";

    // Список моделей на сцене
    private List<Model> sceneModels = new ArrayList<>();
    private int currentModelIndex = -1;

    // Таймер анимации
    private AnimationTimer timer;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // Инициализация камеры
        camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                (float) Math.toRadians(45.0),
                (float) canvas.getWidth() / (float) canvas.getHeight(),
                0.01f,
                100.0f
        );

        // Инициализация контроллера мыши
        mouseController = new MouseController(camera, anchorPane.getScene());

        // Настройка таймера рендеринга
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        timer.start();

        // Загрузка модели по умолчанию
        loadModel(MODEL_FILE_PATH);
    }

    private void render() {
        if (mesh == null) return;

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Обновляем соотношение сторон камеры
        camera.setAspectRatio((float) canvas.getWidth() / (float) canvas.getHeight());

        // Рендерим все модели на сцене
        for (Model model : sceneModels) {
            RenderEngine.render(
                    graphicsContext,
                    camera,
                    model,
                    model.getTransform().getTransformationMatrix(),
                    (int) canvas.getWidth(),
                    (int) canvas.getHeight()
            );
        }
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;

        loadModel(file.getAbsolutePath());
    }

    private void loadModel(String filePath) {
        try {
            String fileContent = Files.readString(new File(filePath).toPath());
            mesh = ObjReader.read(fileContent);

            // Добавляем модель на сцену
            sceneModels.add(mesh);
            currentModelIndex = sceneModels.size() - 1;

            // Сбрасываем трансформации для новой модели
            mesh.getTransform().reset();

        } catch (IOException exception) {
            System.out.println("Error loading model: " + exception.getMessage());
        }
    }

    // ===== Управление трансформациями модели =====

    @FXML
    private void onTranslateXPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().translate(0.1f, 0, 0);
        }
    }

    @FXML
    private void onTranslateXMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().translate(-0.1f, 0, 0);
        }
    }

    @FXML
    private void onTranslateYPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().translate(0, 0.1f, 0);
        }
    }

    @FXML
    private void onTranslateYMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().translate(0, -0.1f, 0);
        }
    }

    @FXML
    private void onTranslateZPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().translate(0, 0, 0.1f);
        }
    }

    @FXML
    private void onTranslateZMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().translate(0, 0, -0.1f);
        }
    }

    @FXML
    private void onRotateXPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f rot = model.getTransform().getRotation();
            model.getTransform().setRotation(new Vector3f(rot.x + 0.1f, rot.y, rot.z));
        }
    }

    @FXML
    private void onRotateXMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f rot = model.getTransform().getRotation();
            model.getTransform().setRotation(new Vector3f(rot.x - 0.1f, rot.y, rot.z));
        }
    }

    @FXML
    private void onRotateYPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f rot = model.getTransform().getRotation();
            model.getTransform().setRotation(new Vector3f(rot.x, rot.y + 0.1f, rot.z));
        }
    }

    @FXML
    private void onRotateYMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f rot = model.getTransform().getRotation();
            model.getTransform().setRotation(new Vector3f(rot.x, rot.y - 0.1f, rot.z));
        }
    }

    @FXML
    private void onRotateZPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f rot = model.getTransform().getRotation();
            model.getTransform().setRotation(new Vector3f(rot.x, rot.y, rot.z + 0.1f));
        }
    }

    @FXML
    private void onRotateZMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f rot = model.getTransform().getRotation();
            model.getTransform().setRotation(new Vector3f(rot.x, rot.y, rot.z - 0.1f));
        }
    }

    @FXML
    private void onScaleXPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f scale = model.getTransform().getScale();
            model.getTransform().setScale(new Vector3f(scale.x * 1.1f, scale.y, scale.z));
        }
    }

    @FXML
    private void onScaleXMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f scale = model.getTransform().getScale();
            model.getTransform().setScale(new Vector3f(scale.x * 0.9f, scale.y, scale.z));
        }
    }

    @FXML
    private void onScaleYPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f scale = model.getTransform().getScale();
            model.getTransform().setScale(new Vector3f(scale.x, scale.y * 1.1f, scale.z));
        }
    }

    @FXML
    private void onScaleYMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f scale = model.getTransform().getScale();
            model.getTransform().setScale(new Vector3f(scale.x, scale.y * 0.9f, scale.z));
        }
    }

    @FXML
    private void onScaleZPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f scale = model.getTransform().getScale();
            model.getTransform().setScale(new Vector3f(scale.x, scale.y, scale.z * 1.1f));
        }
    }

    @FXML
    private void onScaleZMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            Vector3f scale = model.getTransform().getScale();
            model.getTransform().setScale(new Vector3f(scale.x, scale.y, scale.z * 0.9f));
        }
    }

    @FXML
    private void onScaleUniformPlus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().scaleUniform(1.1f);
        }
    }

    @FXML
    private void onScaleUniformMinus() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().scaleUniform(0.9f);
        }
    }

    @FXML
    private void onResetTransformations() {
        if (currentModelIndex >= 0) {
            Model model = sceneModels.get(currentModelIndex);
            model.getTransform().reset();
        }
    }

    @FXML
    private void onSaveModelWithTransformations() {
        if (currentModelIndex < 0) return;

        Model model = sceneModels.get(currentModelIndex);
        Model transformedModel = model.getTransformedCopy();

        // Здесь должна быть реализация сохранения модели в файл
        // с учетом трансформаций (transformedModel)
        saveModelToFile(transformedModel, "transformed_model.obj");
    }

    @FXML
    private void onSaveModelWithoutTransformations() {
        if (currentModelIndex < 0) return;

        Model model = sceneModels.get(currentModelIndex);
        Model originalModel = model.getOriginalCopy();

        // Сохранение оригинальной модели
        saveModelToFile(originalModel, "original_model.obj");
    }

    private void saveModelToFile(Model model, String fileName) {
        // Реализация сохранения модели в OBJ файл
        // (должен быть реализован класс ObjWriter)
        System.out.println("Saving model to: " + fileName);
        // TODO: Реализовать сохранение
    }

    // ===== Управление камерой =====

    @FXML
    private void onCameraReset() {
        camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                (float) Math.toRadians(45.0),
                (float) canvas.getWidth() / (float) canvas.getHeight(),
                0.01f,
                100.0f
        );
        mouseController = new MouseController(camera, anchorPane.getScene());
    }

    @FXML
    private void onNextModel() {
        if (!sceneModels.isEmpty()) {
            currentModelIndex = (currentModelIndex + 1) % sceneModels.size();
        }
    }

    @FXML
    private void onPreviousModel() {
        if (!sceneModels.isEmpty()) {
            currentModelIndex = (currentModelIndex - 1 + sceneModels.size()) % sceneModels.size();
        }
    }

    @FXML
    private void onRemoveCurrentModel() {
        if (currentModelIndex >= 0 && currentModelIndex < sceneModels.size()) {
            sceneModels.remove(currentModelIndex);
            if (!sceneModels.isEmpty()) {
                currentModelIndex = Math.min(currentModelIndex, sceneModels.size() - 1);
            } else {
                currentModelIndex = -1;
            }
        }
    }
}