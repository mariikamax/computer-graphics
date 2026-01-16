package com.cgvsu.ui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DeleteDialog extends Dialog<DeleteDialog.Result> {

    public static class Result {
        public final boolean isVertex;
        public final int index;

        public Result(boolean isVertex, int index) {
            this.isVertex = isVertex;
            this.index = index;
        }
    }

    private ToggleGroup toggleGroup;
    private RadioButton vertexRadio;
    private RadioButton polygonRadio;
    private Spinner<Integer> indexSpinner;

    public DeleteDialog(int maxVertexIndex, int maxPolygonIndex) {
        setTitle("Удаление элемента");
        setHeaderText("Выберите тип удаления и индекс элемента");

        toggleGroup = new ToggleGroup();
        vertexRadio = new RadioButton("Вершина (доступно: " + (maxVertexIndex + 1) + ")");
        vertexRadio.setToggleGroup(toggleGroup);
        vertexRadio.setSelected(true);

        polygonRadio = new RadioButton("Полигон (доступно: " + (maxPolygonIndex + 1) + ")");
        polygonRadio.setToggleGroup(toggleGroup);

        int initialMax = Math.max(maxVertexIndex, 0);
        indexSpinner = new Spinner<>(0, initialMax, 0, 1);

        vertexRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                indexSpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Math.max(maxVertexIndex, 0), 0, 1)
                );
            }
        });

        polygonRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                indexSpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Math.max(maxPolygonIndex, 0), 0, 1)
                );
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Тип элемента:"), 0, 0);
        VBox radioBox = new VBox(5, vertexRadio, polygonRadio);
        grid.add(radioBox, 1, 0);

        grid.add(new Label("Индекс (0-" + initialMax + "):"), 0, 1);
        grid.add(indexSpinner, 1, 1);

        getDialogPane().setContent(grid);

        ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(deleteButton, cancelButton);

        setResultConverter(buttonType -> {
            if (buttonType == deleteButton) {
                return new Result(vertexRadio.isSelected(), indexSpinner.getValue());
            }
            return null;
        });
    }
}