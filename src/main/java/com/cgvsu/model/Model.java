package com.cgvsu.model;

import java.util.ArrayList;

public class Model {
    public ArrayList<com.cgvsu.math.Vector3f> vertices = new ArrayList<>();
    public ArrayList<com.cgvsu.math.Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<com.cgvsu.math.Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();

    private String fileName;
    private boolean visible = true;

    // Геттеры и сеттеры для новых полей
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}