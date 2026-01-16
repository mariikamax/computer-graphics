package com.cgvsu;

import com.cgvsu.ui.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GuiController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private ModelListView modelListView;
    private PropertiesPane propertiesPane;
    private SceneManager sceneManager;
    private MenuBarView menuBar;

    @FXML
    public void initialize() {
        sceneManager = new SceneManager();

        modelListView = new ModelListView();
        propertiesPane = new PropertiesPane();

        VBox leftPanel = new VBox(10, modelListView, propertiesPane);
        leftPanel.setPrefWidth(250);
        leftPanel.setMinWidth(250);
        leftPanel.setMaxWidth(250);
        leftPanel.setPadding(new Insets(5));
        leftPanel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        Pane rightPanel = new Pane();
        rightPanel.setPadding(new Insets(2));
        rightPanel.setStyle("-fx-background-color: WHITE; -fx-border-color: #cccccc; -fx-border-width: 1;");

        rightPanel.getChildren().add(canvas);

        BorderPane mainPane = new BorderPane();
        mainPane.setLeft(leftPanel);
        mainPane.setCenter(rightPanel);

        BorderPane.setMargin(leftPanel, new Insets(5));
        BorderPane.setMargin(rightPanel, new Insets(5));

        menuBar = new MenuBarView();

        VBox root = new VBox(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);

        anchorPane.getChildren().clear();
        anchorPane.getChildren().add(root);

        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);

        setupCanvasResizeListeners(rightPanel);

        setupListeners();
        setupMenuListeners();

        drawInitialCanvas();
    }

    private void setupCanvasResizeListeners(Pane rightPanel) {
        rightPanel.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                canvas.setWidth(newVal.doubleValue() - 4);
            }
        });

        rightPanel.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                canvas.setHeight(newVal.doubleValue() - 4);
            }
        });

        canvas.setWidth(1200);
        canvas.setHeight(800);
    }

    private void drawInitialCanvas() {
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText("Область отрисовки 3D моделей", 50, 50);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Загрузите модель через меню или кнопку 'Добавить'", 50, 100);
    }

    private void setupListeners() {
        modelListView.getAddButton().setOnAction(e -> loadModel());

        modelListView.getRemoveButton().setOnAction(e -> {
            int[] indices = modelListView.getSelectedIndices();
            if (indices.length > 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Подтверждение");
                alert.setHeaderText("Удалить выбранные модели?");

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        for (int i = indices.length - 1; i >= 0; i--) {
                            sceneManager.removeModel(indices[i]);
                            modelListView.getModelList().getItems().remove(indices[i]);
                        }
                    }
                });
            }
        });

        modelListView.getSelectAllButton().setOnAction(e ->
                modelListView.getModelList().getSelectionModel().selectAll()
        );

        modelListView.getClearSelectionButton().setOnAction(e ->
                modelListView.getModelList().getSelectionModel().clearSelection()
        );

        modelListView.getDeletePartButton().setOnAction(e -> showDeleteDialog());

        modelListView.getModelList().getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        int index = modelListView.getModelList().getSelectionModel().getSelectedIndex();
                        List<Integer> indices = new ArrayList<>();
                        indices.add(index);
                        sceneManager.setSelectedModelIndices(indices);

                        Model model = sceneManager.getActiveModel();
                        if (model != null) {
                            propertiesPane.updateProperties(
                                    model.getFileName(),
                                    model.vertices.size(),
                                    model.polygons.size()
                            );
                        }
                    }
                });
    }

    private void setupMenuListeners() {

        menuBar.getOpenMenuItem().setOnAction(e -> loadModel());

        menuBar.getSaveMenuItem().setOnAction(e -> saveModel());

        menuBar.getExitMenuItem().setOnAction(e -> {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.close();
        });

    }

    private void showDeleteDialog() {
        Model activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Нет выбранной модели");
            alert.setContentText("Выберите модель для удаления элементов.");
            alert.showAndWait();
            return;
        }

        int maxVertexIndex = Math.max(0, activeModel.vertices.size() - 1);
        int maxPolygonIndex = Math.max(0, activeModel.polygons.size() - 1);

        if (maxVertexIndex <= 0 && maxPolygonIndex <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Нет элементов для удаления");
            alert.setContentText("В выбранной модели нет вершин и полигонов.");
            alert.showAndWait();
            return;
        }

        DeleteDialog dialog = new DeleteDialog(maxVertexIndex, maxPolygonIndex);
        dialog.showAndWait().ifPresent(result -> {
            if (result.isVertex) {
                if (result.index < activeModel.vertices.size()) {
                    sceneManager.deleteVertexFromActiveModel(result.index);
                    propertiesPane.updateProperties(
                            activeModel.getFileName(),
                            activeModel.vertices.size(),
                            activeModel.polygons.size()
                    );
                    showAlert("Успех", "Вершина #" + result.index + " удалена");
                } else {
                    showAlert("Ошибка", "Неверный индекс вершины: " + result.index);
                }
            } else {
                if (result.index < activeModel.polygons.size()) {
                    sceneManager.deletePolygonFromActiveModel(result.index);
                    propertiesPane.updateProperties(
                            activeModel.getFileName(),
                            activeModel.vertices.size(),
                            activeModel.polygons.size()
                    );
                    showAlert("Успех", "Полигон #" + result.index + " удален");
                } else {
                    showAlert("Ошибка", "Неверный индекс полигона: " + result.index);
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadModel() {
        File file = FileLoader.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                String fileContent = Files.readString(file.toPath());
                Model model = ObjReader.read(fileContent);
                model.setFileName(file.getName());

                sceneManager.addModel(model, file.getName());
                modelListView.addModel(file.getName());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText("Модель успешно загружена!");
                alert.setContentText("Вершин: " + model.vertices.size() +
                        "\nПолигонов: " + model.polygons.size());
                alert.showAndWait();

            } catch (Exception e) {
                ErrorHandler.showErrorDialog("Ошибка загрузки",
                        "Не удалось загрузить модель: " + file.getName(), e);
            }
        }
    }

    private void saveModel() {
        Model activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Нет выбранной модели");
            alert.setContentText("Выберите модель для сохранения.");
            alert.showAndWait();
            return;
        }

        File file = FileLoader.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                com.cgvsu.objwriter.ObjWriter.write(activeModel, file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText("Модель успешно сохранена!");
                alert.setContentText("Файл: " + file.getName());
                alert.showAndWait();
            } catch (Exception e) {
                ErrorHandler.showErrorDialog("Ошибка сохранения",
                        "Не удалось сохранить модель: " + file.getName(), e);
            }
        }
    }
}