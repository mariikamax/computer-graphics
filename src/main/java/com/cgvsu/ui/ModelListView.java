package com.cgvsu.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class ModelListView extends BorderPane {
    private final ListView<String> modelList;
    private final Button addButton;
    private final Button removeButton;
    private final Button selectAllButton;
    private final Button clearSelectionButton;
    private final Button deletePartButton;

    public ModelListView() {
        Label title = new Label("Модели на сцене");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Список моделей
        modelList = new ListView<>();
        modelList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        modelList.setPrefHeight(180);
        modelList.setMaxHeight(180);

        addButton = new Button("Добавить");
        removeButton = new Button("Удалить");
        selectAllButton = new Button("Выбрать все");
        clearSelectionButton = new Button("Снять выделение");
        deletePartButton = new Button("Удалить часть");

        String buttonStyle = "-fx-min-width: 120px; -fx-min-height: 30px; -fx-font-size: 12px; -fx-padding: 5px 10px;";
        addButton.setStyle(buttonStyle);
        removeButton.setStyle(buttonStyle);
        selectAllButton.setStyle(buttonStyle);
        clearSelectionButton.setStyle(buttonStyle);
        deletePartButton.setStyle(buttonStyle);

        VBox buttonPanel = new VBox(5);
        buttonPanel.getChildren().addAll(addButton, removeButton, selectAllButton,
                clearSelectionButton, deletePartButton);

        VBox mainPanel = new VBox(8, title, modelList, buttonPanel);
        mainPanel.setPadding(new Insets(8));
        this.setCenter(mainPanel);

        this.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-color: #ffffff;");
    }

    public ListView<String> getModelList() {
        return modelList;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public Button getSelectAllButton() {
        return selectAllButton;
    }

    public Button getClearSelectionButton() {
        return clearSelectionButton;
    }

    public Button getDeletePartButton() {
        return deletePartButton;
    }

    public void addModel(String modelName) {
        modelList.getItems().add(modelName);
    }

    public void removeSelectedModels() {
        ObservableList<Integer> selected = modelList.getSelectionModel().getSelectedIndices();
        for (int i = selected.size() - 1; i >= 0; i--) {
            modelList.getItems().remove(selected.get(i).intValue());
        }
    }

    public int[] getSelectedIndices() {
        return modelList.getSelectionModel().getSelectedIndices()
                .stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }
}