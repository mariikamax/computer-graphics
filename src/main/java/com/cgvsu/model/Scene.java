package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private Camera activeCamera;
    private Light light;
    private List<Model> cameraModels = new ArrayList<>();

    public List<Model> getCameraModels() {
        return cameraModels;
    }

    public void updateCameraModels() {
        cameraModels.clear();
        for (Camera camera : cameras) {
            Model cameraModel = createSimpleCameraModel(camera);
            cameraModels.add(cameraModel);
        }
    }

    private Model createSimpleCameraModel(Camera camera) {
        Model model = new Model();

        Vector3f pos = camera.getPosition();
        Vector3f target = camera.getTarget();

        model.vertices.add(new Vector3f(pos.x, pos.y, pos.z)); // Вершина пирамиды

        float size = 2.0f;
        model.vertices.add(new Vector3f(pos.x - size, pos.y - size, pos.z + size * 2));
        model.vertices.add(new Vector3f(pos.x + size, pos.y - size, pos.z + size * 2));
        model.vertices.add(new Vector3f(pos.x + size, pos.y + size, pos.z + size * 2));
        model.vertices.add(new Vector3f(pos.x - size, pos.y + size, pos.z + size * 2));

        Polygon poly1 = new Polygon();
        poly1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.polygons.add(poly1);

        Polygon poly2 = new Polygon();
        poly2.setVertexIndices(new ArrayList<>(Arrays.asList(0, 2, 3)));
        model.polygons.add(poly2);

        Polygon poly3 = new Polygon();
        poly3.setVertexIndices(new ArrayList<>(Arrays.asList(0, 3, 4)));
        model.polygons.add(poly3);

        Polygon poly4 = new Polygon();
        poly4.setVertexIndices(new ArrayList<>(Arrays.asList(0, 4, 1)));
        model.polygons.add(poly4);

        Polygon base = new Polygon();
        base.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
        model.polygons.add(base);

        return model;
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
        if (activeCamera == null) {
            activeCamera = camera;
        }
        updateCameraModels();
    }

    public void removeCamera(Camera camera) {
        cameras.remove(camera);
        if (activeCamera == camera && !cameras.isEmpty()) {
            activeCamera = cameras.get(0);
        }
        updateCameraModels();
    }

    public Scene() {
        light = new Light();
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public void removeModel(Model model) {
        models.remove(model);
    }

    public List<Model> getModels() {
        return models;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }

    public void setActiveCamera(Camera camera) {
        this.activeCamera = camera;
    }

    public Light getLight() {
        return light;
    }
}