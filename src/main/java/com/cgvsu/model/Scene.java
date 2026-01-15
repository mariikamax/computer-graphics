package com.cgvsu.model;

import com.cgvsu.render_engine.Camera;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private Camera activeCamera;
    private Light light;

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

    public void addCamera(Camera camera) {
        cameras.add(camera);
        if (activeCamera == null) {
            activeCamera = camera;
        }
    }

    public void removeCamera(Camera camera) {
        cameras.remove(camera);
        if (activeCamera == camera && !cameras.isEmpty()) {
            activeCamera = cameras.get(0);
        }
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