package com.cgvsu.ui;

import com.cgvsu.model.Model;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class PropertiesPane extends VBox {
    private final TextField nameField;
    private final Label vertexCountLabel;
    private final Label polygonCountLabel;
    private final CheckBox visibleCheckBox;
    private final CheckBox wireframeCheckBox;
    private final ColorPicker colorPicker;
    private Model currentModel;

    public PropertiesPane() {
        Label title = new Label("Свойства модели");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setPadding(new Insets(5));

        grid.add(new Label("Имя:"), 0, 0);
        nameField = new TextField();
        nameField.setEditable(false);
        nameField.setPrefWidth(130);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Вершин:"), 0, 1);
        vertexCountLabel = new Label("0");
        grid.add(vertexCountLabel, 1, 1);

        grid.add(new Label("Полигонов:"), 0, 2);
        polygonCountLabel = new Label("0");
        grid.add(polygonCountLabel, 1, 2);

        visibleCheckBox = new CheckBox("Видимость");
        visibleCheckBox.setSelected(true);
        visibleCheckBox.setPadding(new Insets(5, 0, 0, 0));

        visibleCheckBox.setOnAction(e -> {
            if (currentModel != null) {
                currentModel.setVisible(visibleCheckBox.isSelected());
            }
        });

        wireframeCheckBox = new CheckBox("Режим каркаса");
        wireframeCheckBox.setPadding(new Insets(0, 0, 5, 0));

        wireframeCheckBox.setOnAction(e -> {
            if (currentModel != null) {
                currentModel.setWireframe(wireframeCheckBox.isSelected());
            }
        });

        Label colorLabel = new Label("Цвет модели:");
        colorPicker = new ColorPicker(Color.LIGHTBLUE);
        colorPicker.setPrefWidth(130);

        colorPicker.setOnAction(e -> {
            if (currentModel != null) {
                javafx.scene.paint.Color fxColor = colorPicker.getValue();
                currentModel.setColor(new com.cgvsu.math.Vector3f(
                        (float) fxColor.getRed(),
                        (float) fxColor.getGreen(),
                        (float) fxColor.getBlue()
                ));
            }
        });

        HBox colorBox = new HBox(5, colorLabel, colorPicker);
        colorBox.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(title, grid, visibleCheckBox,
                wireframeCheckBox, colorBox);
        this.setSpacing(6);
        this.setPadding(new Insets(8));
        this.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-color: #ffffff;");
    }

    public void updateProperties(String name, int vertices, int polygons) {
        nameField.setText(name);
        vertexCountLabel.setText(String.valueOf(vertices));
        polygonCountLabel.setText(String.valueOf(polygons));
    }

    public void updateFromModel(Model model) {
        this.currentModel = model;
        if (model != null) {
            visibleCheckBox.setSelected(model.isVisible());
            wireframeCheckBox.setSelected(model.isWireframe());
            if (model.getColor() != null) {
                colorPicker.setValue(javafx.scene.paint.Color.color(
                        model.getColor().x,
                        model.getColor().y,
                        model.getColor().z
                ));
            }
        }
    }
}