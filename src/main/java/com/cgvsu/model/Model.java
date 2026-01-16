package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector4f;

import java.util.*;

public class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<>();
    public ArrayList<Vector3f> normals = new ArrayList<>();
    public ArrayList<Polygon> polygons = new ArrayList<>();

    // Трансформации модели
    private Transform transform = new Transform();

    // Оригинальные вершины (до трансформаций)
    private ArrayList<Vector3f> originalVertices = new ArrayList<>();
    private boolean isTransformed = false;

    public Model() {
        this.transform = new Transform();
    }

    /**
     * Применить трансформации к модели
     * @param applyToOriginal true - применить к оригинальным вершинам,
     *                       false - сбросить к оригинальным вершинам
     */
    public void applyTransformations(boolean applyToOriginal) {
        Matrix4f transformationMatrix = transform.getTransformationMatrix();

        if (applyToOriginal) {
            // Применяем трансформации и обновляем оригинальные вершины
            for (int i = 0; i < vertices.size(); i++) {
                Vector3f originalVertex = originalVertices.get(i);
                Vector4f transformed = transformationMatrix.multiply(originalVertex);
                vertices.set(i, transformed.toVector3f());
            }
            // Сбрасываем трансформации
            transform.reset();
            isTransformed = false;
        } else {
            // Восстанавливаем оригинальные вершины
            vertices.clear();
            vertices.addAll(originalVertices);
            transform.reset();
            isTransformed = false;
        }
    }

    /**
     * Обновить вершины с учетом текущих трансформаций
     */
    public void updateVerticesWithTransformations() {
        if (originalVertices.isEmpty()) {
            // Сохраняем оригинальные вершины при первом вызове
            originalVertices.addAll(vertices);
        }

        Matrix4f transformationMatrix = transform.getTransformationMatrix();
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f originalVertex = originalVertices.get(i);
            Vector4f transformed = transformationMatrix.multiply(originalVertex);
            vertices.set(i, transformed.toVector3f());
        }
        isTransformed = true;
    }

    /**
     * Применить текущие трансформации к вершинам и сбросить трансформации
     * (аналог applyTransformations(true))
     */
    public void applyAndResetTransformations() {
        applyTransformations(true);
    }

    /**
     * Сбросить все трансформации к исходному состоянию
     * (аналог applyTransformations(false))
     */
    public void resetAllTransformations() {
        applyTransformations(false);
    }

    // Геттеры и сеттеры для трансформаций
    public Transform getTransform() { return transform; }
    public void setTransform(Transform transform) { this.transform = transform; }

    public Vector3f getPosition() { return transform.getPosition(); }
    public void setPosition(Vector3f position) { transform.setPosition(position); }

    public Vector3f getRotation() { return transform.getRotation(); }
    public void setRotation(Vector3f rotation) { transform.setRotation(rotation); }

    public Vector3f getScale() { return transform.getScale(); }
    public void setScale(Vector3f scale) { transform.setScale(scale); }

    public boolean isTransformed() { return isTransformed; }

    /**
     * Получить копию модели с текущими трансформациями
     */
    public Model getTransformedCopy() {
        Model copy = new Model();

        // Копируем трансформированные вершины
        for (Vector3f vertex : vertices) {
            copy.vertices.add(new Vector3f(vertex));
        }

        // Копируем остальные данные
        for (Vector2f tv : textureVertices) {
            copy.textureVertices.add(new Vector2f(tv));
        }
        for (Vector3f normal : normals) {
            copy.normals.add(new Vector3f(normal));
        }
        for (Polygon polygon : polygons) {
            copy.polygons.add(new Polygon(polygon));
        }

        // Сохраняем оригинальные вершины
        for (Vector3f original : originalVertices) {
            copy.originalVertices.add(new Vector3f(original));
        }

        copy.transform = transform.copy();
        copy.isTransformed = isTransformed;

        return copy;
    }

    /**
     * Получить копию оригинальной модели (без трансформаций)
     */
    public Model getOriginalCopy() {
        Model copy = new Model();

        // Копируем оригинальные вершины
        for (Vector3f vertex : originalVertices) {
            copy.vertices.add(new Vector3f(vertex));
            copy.originalVertices.add(new Vector3f(vertex));
        }

        // Копируем остальные данные
        for (Vector2f tv : textureVertices) {
            copy.textureVertices.add(new Vector2f(tv));
        }
        for (Vector3f normal : normals) {
            copy.normals.add(new Vector3f(normal));
        }
        for (Polygon polygon : polygons) {
            copy.polygons.add(new Polygon(polygon));
        }

        copy.transform = new Transform();
        copy.isTransformed = false;

        return copy;
    }

    /**
     * Очистить все данные модели
     */
    public void clear() {
        vertices.clear();
        textureVertices.clear();
        normals.clear();
        polygons.clear();
        originalVertices.clear();
        transform.reset();
        isTransformed = false;
    }

    /**
     * Получить количество вершин в модели
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * Получить количество полигонов в модели
     */
    public int getPolygonCount() {
        return polygons.size();
    }

    @Override
    public String toString() {
        return String.format(
                "Model(Vertices: %d, Textures: %d, Normals: %d, Polygons: %d, Transformed: %b)",
                vertices.size(), textureVertices.size(), normals.size(), polygons.size(), isTransformed
        );
    }
}