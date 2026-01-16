package com.cgvsu.model;

import java.util.ArrayList;

public class Polygon {
    private ArrayList<Integer> vertexIndices;
    private ArrayList<Integer> textureVertexIndices;
    private ArrayList<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

    // Конструктор копирования
    public Polygon(Polygon other) {
        this.vertexIndices = new ArrayList<>(other.vertexIndices);
        this.textureVertexIndices = new ArrayList<>(other.textureVertexIndices);
        this.normalIndices = new ArrayList<>(other.normalIndices);
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    public ArrayList<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public ArrayList<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public ArrayList<Integer> getNormalIndices() {
        return normalIndices;
    }

    // Методы для удобного добавления индексов
    public void addVertexIndex(int index) {
        vertexIndices.add(index);
    }

    public void addTextureVertexIndex(int index) {
        textureVertexIndices.add(index);
    }

    public void addNormalIndex(int index) {
        normalIndices.add(index);
    }

    // Получить количество вершин в полигоне
    public int getVertexCount() {
        return vertexIndices.size();
    }

    @Override
    public String toString() {
        return String.format(
                "Polygon(Vertices: %s, Textures: %s, Normals: %s)",
                vertexIndices, textureVertexIndices, normalIndices
        );
    }
}