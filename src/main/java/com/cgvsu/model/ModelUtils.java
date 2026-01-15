package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.*;

public class ModelUtils {

    public static void triangulate(Model model) {
        if (model.isTriangulated()) return;

        ArrayList<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            int nVertices = vertexIndices.size();

            if (nVertices > 3) {
                for (int i = 1; i < nVertices - 1; i++) {
                    Polygon triangle = new Polygon();

                    ArrayList<Integer> triVertices = new ArrayList<>();
                    triVertices.add(vertexIndices.get(0));
                    triVertices.add(vertexIndices.get(i));
                    triVertices.add(vertexIndices.get(i + 1));
                    triangle.setVertexIndices(triVertices);

                    if (!textureIndices.isEmpty()) {
                        ArrayList<Integer> triTextures = new ArrayList<>();
                        triTextures.add(textureIndices.get(0));
                        triTextures.add(textureIndices.get(i));
                        triTextures.add(textureIndices.get(i + 1));
                        triangle.setTextureVertexIndices(triTextures);
                    }

                    if (!normalIndices.isEmpty()) {
                        ArrayList<Integer> triNormals = new ArrayList<>();
                        triNormals.add(normalIndices.get(0));
                        triNormals.add(normalIndices.get(i));
                        triNormals.add(normalIndices.get(i + 1));
                        triangle.setNormalIndices(triNormals);
                    }

                    newPolygons.add(triangle);
                }
            } else {
                newPolygons.add(polygon);
            }
        }

        model.polygons = newPolygons;
        model.setTriangulated(true);
    }

    public static void calculateNormals(Model model) {
        model.normals.clear();

        Vector3f[] vertexNormals = new Vector3f[model.vertices.size()];
        for (int i = 0; i < model.vertices.size(); i++) {
            vertexNormals[i] = new Vector3f(0, 0, 0);
        }

        int[] vertexFaceCount = new int[model.vertices.size()];

        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() < 3) {
                continue;
            }

            Vector3f v0 = model.vertices.get(vertexIndices.get(0));
            Vector3f v1 = model.vertices.get(vertexIndices.get(1));
            Vector3f v2 = model.vertices.get(vertexIndices.get(2));

            Vector3f edge1 = new Vector3f(
                    v1.x - v0.x,
                    v1.y - v0.y,
                    v1.z - v0.z
            );

            Vector3f edge2 = new Vector3f(
                    v2.x - v0.x,
                    v2.y - v0.y,
                    v2.z - v0.z
            );

            Vector3f faceNormal = Vector3f.crossProduct(edge1, edge2);
            faceNormal.normalize();

            for (int vertexIndex : vertexIndices) {
                vertexNormals[vertexIndex].add(faceNormal);
                vertexFaceCount[vertexIndex]++;
            }
        }

        for (int i = 0; i < model.vertices.size(); i++) {
            if (vertexFaceCount[i] > 0) {
                vertexNormals[i].multiply(1.0f / vertexFaceCount[i]);
                vertexNormals[i].normalize();
                model.normals.add(vertexNormals[i]);
            } else {
                model.normals.add(new Vector3f(0, 0, 0));
            }
        }
    }
}