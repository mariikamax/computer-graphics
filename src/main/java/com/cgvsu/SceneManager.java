package com.cgvsu;

import com.cgvsu.model.Model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;
import com.cgvsu.model.Polygon;

public class SceneManager {
    private ObservableList<Model> models = FXCollections.observableArrayList();
    private List<Integer> selectedModelIndices = new ArrayList<>();
    private Model activeModel = null;

    public SceneManager() {}

    public void addModel(Model model, String fileName) {
        model.setFileName(fileName);
        models.add(model);

        selectedModelIndices.clear();
        selectedModelIndices.add(models.size() - 1);
        activeModel = model;
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            Model removed = models.remove(index);

            List<Integer> newIndices = new ArrayList<>();
            for (int i : selectedModelIndices) {
                if (i < index) {
                    newIndices.add(i);
                } else if (i > index) {
                    newIndices.add(i - 1);
                }
            }
            selectedModelIndices = newIndices;

            if (removed == activeModel) {
                activeModel = models.isEmpty() ? null : models.get(0);
            }
        }
    }

    public ObservableList<Model> getModels() {
        return models;
    }

    public List<Integer> getSelectedModelIndices() {
        return selectedModelIndices;
    }

    public Model getActiveModel() {
        return activeModel;
    }

    public void setSelectedModelIndices(List<Integer> indices) {
        this.selectedModelIndices = indices;
        if (!indices.isEmpty() && !models.isEmpty()) {
            activeModel = models.get(indices.get(0));
        }
    }

    public void deleteVertexFromActiveModel(int vertexIndex) {
        if (activeModel != null && vertexIndex >= 0 && vertexIndex < activeModel.vertices.size()) {
            activeModel.vertices.remove(vertexIndex);

            List<Polygon> polygonsToRemove = new ArrayList<>();
            for (Polygon polygon : activeModel.polygons) {
                if (polygon.getVertexIndices().contains(vertexIndex)) {
                    polygonsToRemove.add(polygon);
                }
            }
            activeModel.polygons.removeAll(polygonsToRemove);

            for (Polygon polygon : activeModel.polygons) {
                List<Integer> newIndices = new ArrayList<>();
                for (int idx : polygon.getVertexIndices()) {
                    if (idx > vertexIndex) {
                        newIndices.add(idx - 1);
                    } else {
                        newIndices.add(idx);
                    }
                }
                polygon.setVertexIndices(new ArrayList<>(newIndices));
            }
        }
    }

    public void deletePolygonFromActiveModel(int polygonIndex) {
        if (activeModel != null && polygonIndex >= 0 && polygonIndex < activeModel.polygons.size()) {
            activeModel.polygons.remove(polygonIndex);
        }
    }}

