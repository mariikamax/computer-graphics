package com.cgvsu.ui;

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

    public PropertiesPane() {
        Label title = new Label("Свойства");
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

        wireframeCheckBox = new CheckBox("Каркас");
        wireframeCheckBox.setPadding(new Insets(0, 0, 5, 0));

        Label colorLabel = new Label("Цвет:");
        colorPicker = new ColorPicker(Color.LIGHTGRAY);
        colorPicker.setPrefWidth(130);

        HBox colorBox = new HBox(5, colorLabel, colorPicker);
        colorBox.setAlignment(Pos.CENTER_LEFT);

        // Компоновка
        this.getChildren().addAll(title, grid, visibleCheckBox,
                wireframeCheckBox, colorBox);
        this.setSpacing(6);
        this.setPadding(new Insets(8));
        this.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 4;");
    }

    // Метод обновления свойств
    public void updateProperties(String name, int vertices, int polygons) {
        nameField.setText(name);
        vertexCountLabel.setText(String.valueOf(vertices));
        polygonCountLabel.setText(String.valueOf(polygons));
    }
}