package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.Light;
import com.cgvsu.model.Scene;
import com.cgvsu.model.ModelUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javax.vecmath.*;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {
    private static ZBuffer zBuffer;
    private static final RenderSettings settings = new RenderSettings();
    private static Scene currentScene = null;

    public static void setScene(Scene scene) {
        currentScene = scene;
    }

    public static void setRenderSettings(RenderSettings newSettings) {
        settings.setDrawWireframe(newSettings.isDrawWireframe());
        settings.setFillPolygons(newSettings.isFillPolygons());
        settings.setUseTexture(newSettings.isUseTexture());
        settings.setUseLighting(newSettings.isUseLighting());
        settings.setCurrentTexture(newSettings.getCurrentTexture());
        settings.setFillColor(newSettings.getFillColor());
    }

    public static RenderSettings getRenderSettings() {
        return settings;
    }

    private static javax.vecmath.Vector3f convertToJavax(Vector3f v) {
        return new javax.vecmath.Vector3f(v.x, v.y, v.z);
    }

    public static void renderCameras(
            final GraphicsContext graphicsContext,
            final Camera activeCamera,
            final List<Model> cameraModels,
            final int width,
            final int height) {

        if (cameraModels == null || cameraModels.isEmpty()) {
            return;
        }

        for (Model cameraModel : cameraModels) {
            renderCameraModel(graphicsContext, activeCamera, cameraModel, width, height);
        }
    }

    private static void renderCameraModel(
            GraphicsContext graphicsContext,
            Camera activeCamera,
            Model cameraModel,
            int width, int height) {

        graphicsContext.setStroke(Color.RED);
        graphicsContext.setLineWidth(2.0);

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = activeCamera.getViewMatrix();
        Matrix4f projectionMatrix = activeCamera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        for (Polygon polygon : cameraModel.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                int next = (i + 1) % vertexIndices.size();

                Vector3f v1 = cameraModel.vertices.get(vertexIndices.get(i));
                Vector3f v2 = cameraModel.vertices.get(vertexIndices.get(next));

                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                Point2f p1 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1), width, height);
                Point2f p2 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2), width, height);

                graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        graphicsContext.setStroke(Color.YELLOW);
        if (cameraModel.vertices.size() >= 5) {
            Vector3f camPos = cameraModel.vertices.get(0);
            Vector3f baseCenter = new Vector3f(
                    (cameraModel.vertices.get(1).x + cameraModel.vertices.get(3).x) / 2,
                    (cameraModel.vertices.get(1).y + cameraModel.vertices.get(3).y) / 2,
                    (cameraModel.vertices.get(1).z + cameraModel.vertices.get(3).z) / 2
            );

            javax.vecmath.Vector3f vec1 = convertToJavax(camPos);
            javax.vecmath.Vector3f vec2 = convertToJavax(baseCenter);

            Point2f p1 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1), width, height);
            Point2f p2 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2), width, height);

            graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            Model mesh,
            final int width,
            final int height,
            final Color fillColor) {

        if (mesh == null) return;

        graphicsContext.clearRect(0, 0, width, height);

        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();

        if (!mesh.triangulated) ModelUtils.triangulate(mesh);
        if (mesh.normals.isEmpty()) ModelUtils.calculateNormals(mesh);

        Camera cameraToUse = camera;
        if (currentScene != null && currentScene.getActiveCamera() != null) {
            cameraToUse = currentScene.getActiveCamera();
        }

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = cameraToUse.getViewMatrix();
        Matrix4f projectionMatrix = cameraToUse.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        boolean fill = settings.isFillPolygons();
        boolean wire = settings.isDrawWireframe();
        boolean tex = settings.isUseTexture();
        boolean light = settings.isUseLighting();

        Light sceneLight = currentScene != null ? currentScene.getLight() : null;
        Texture texture = tex ? settings.getCurrentTexture() : null;

        if (!fill && !wire && !tex && !light) {
            renderLibraryFill(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, fillColor);
            return;
        }

        if (wire && !fill) {
            renderWireframe(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, Color.BLACK);
            return;
        }

        if (fill) {
            if (tex && texture != null && texture.isLoaded()) {
                if (light && sceneLight != null) {
                    renderTexturedWithLight(graphicsContext, cameraToUse, mesh,
                            modelViewProjectionMatrix, width, height,
                            texture, sceneLight);
                } else {
                    renderTextured(graphicsContext, mesh, modelViewProjectionMatrix,
                            width, height, texture);
                }
            } else if (light && sceneLight != null) {
                renderWithLighting(graphicsContext, cameraToUse, mesh,
                        modelViewProjectionMatrix, width, height,
                        sceneLight, settings.getFillColor());
            } else {
                renderColorWithRasterization(graphicsContext, mesh, modelViewProjectionMatrix,
                        width, height, settings.getFillColor());
            }
        }

        if (wire && fill) {
            renderWireframe(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, Color.BLACK);
        }

        if (currentScene != null) {
            List<Model> cameraModels = currentScene.getCameraModels();
            renderCameras(graphicsContext, camera, cameraModels, width, height);
        }
    }

    private static void renderLibraryFill(
            GraphicsContext gc,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Color color) {

        gc.setFill(color);

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                double[] xPoints = new double[vertexIndices.size()];
                double[] yPoints = new double[vertexIndices.size()];

                for (int i = 0; i < vertexIndices.size(); i++) {
                    Vector3f vertex = mesh.vertices.get(vertexIndices.get(i));
                    javax.vecmath.Vector3f vec = convertToJavax(vertex);
                    javax.vecmath.Vector3f projected = multiplyMatrix4ByVector3(matrix, vec);
                    Point2f point = vertexToPoint(projected, width, height);
                    xPoints[i] = point.x;
                    yPoints[i] = point.y;
                }

                gc.fillPolygon(xPoints, yPoints, vertexIndices.size());
            }
        }
    }

    private static void renderColorWithRasterization(
            GraphicsContext gc,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Color color) {

        PixelWriter pw = gc.getPixelWriter();

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() == 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                javax.vecmath.Vector3f vec0 = convertToJavax(v0);
                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                float z0 = proj0.z;
                float z1 = proj1.z;
                float z2 = proj2.z;

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                drawTriangleWithZBuffer(pw, zBuffer,
                        p0.x, p0.y, z0,
                        p1.x, p1.y, z1,
                        p2.x, p2.y, z2,
                        color);
            }
        }
    }

    private static void drawTriangleWithZBuffer(
            PixelWriter pw, ZBuffer zBuffer,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            Color color) {

        int minX = (int) Math.max(0, Math.min(x0, Math.min(x1, x2)));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.max(x0, Math.max(x1, x2)));
        int minY = (int) Math.max(0, Math.min(y0, Math.min(y1, y2)));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.max(y0, Math.max(y1, y2)));

        if (minX > maxX || minY > maxY) return;

        float area = edgeFunction(new Point2f(x0, y0),
                new Point2f(x1, y1),
                new Point2f(x2, y2));

        if (Math.abs(area) < 0.00001f) return;

        float invArea = 1.0f / area;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x, y);

                float w0 = edgeFunction(new Point2f(x1, y1),
                        new Point2f(x2, y2), p) * invArea;
                float w1 = edgeFunction(new Point2f(x2, y2),
                        new Point2f(x0, y0), p) * invArea;
                float w2 = edgeFunction(new Point2f(x0, y0),
                        new Point2f(x1, y1), p) * invArea;

                if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                    float z = w0 * z0 + w1 * z1 + w2 * z2;

                    if (zBuffer.testAndSet(x, y, z)) {
                        pw.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static void renderWireframe(
            GraphicsContext gc,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Color color) {

        gc.setStroke(color);
        gc.setLineWidth(1.0);

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                int next = (i + 1) % vertexIndices.size();

                Vector3f v1 = mesh.vertices.get(vertexIndices.get(i));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(next));

                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                Point2f p1 = vertexToPoint(multiplyMatrix4ByVector3(matrix, vec1), width, height);
                Point2f p2 = vertexToPoint(multiplyMatrix4ByVector3(matrix, vec2), width, height);

                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private static void renderWithLighting(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Light light,
            Color baseColor) {

        PixelWriter pw = gc.getPixelWriter();

        Vector3f camPos = camera.getPosition();
        Vector3f camTarget = camera.getTarget();

        Vector3f lightDir = new Vector3f(
                camTarget.x - camPos.x,
                camTarget.y - camPos.y,
                camTarget.z - camPos.z
        );

        float len = (float) Math.sqrt(
                lightDir.x * lightDir.x + lightDir.y * lightDir.y + lightDir.z * lightDir.z
        );
        if (len > 0) {
            lightDir.x /= len;
            lightDir.y /= len;
            lightDir.z /= len;
        }

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            if (vertexIndices.size() == 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                Vector3f normal;
                if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                    normal = mesh.normals.get(normalIndices.get(0));
                } else {
                    Vector3f edge1 = new Vector3f(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
                    Vector3f edge2 = new Vector3f(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);

                    normal = new Vector3f(
                            edge1.y * edge2.z - edge1.z * edge2.y,
                            edge1.z * edge2.x - edge1.x * edge2.z,
                            edge1.x * edge2.y - edge1.y * edge2.x
                    );
                }

                float normalLen = (float) Math.sqrt(
                        normal.x * normal.x + normal.y * normal.y + normal.z * normal.z
                );
                if (normalLen > 0) {
                    normal.x /= normalLen;
                    normal.y /= normalLen;
                    normal.z /= normalLen;
                }

                float intensity = normal.x * lightDir.x +
                        normal.y * lightDir.y +
                        normal.z * lightDir.z;
                intensity = Math.max(intensity, 0.0f);
                float ambient = 0.3f;
                intensity = ambient + (1 - ambient) * intensity;

                Color shadedColor = Color.color(
                        Math.min(1.0, (double) baseColor.getRed() * intensity),
                        Math.min(1.0, (double) baseColor.getGreen() * intensity),
                        Math.min(1.0, (double) baseColor.getBlue() * intensity)
                );

                javax.vecmath.Vector3f vec0 = convertToJavax(v0);
                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                float z0 = proj0.z;
                float z1 = proj1.z;
                float z2 = proj2.z;

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                drawTriangleWithZBuffer(pw, zBuffer,
                        p0.x, p0.y, z0,
                        p1.x, p1.y, z1,
                        p2.x, p2.y, z2,
                        shadedColor);
            }
        }
    }

    private static void renderTextured(
            GraphicsContext gc,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Texture texture) {

        PixelWriter pw = gc.getPixelWriter();

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();

            if (vertexIndices.size() == 3 && textureIndices.size() >= 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                Vector2f uv0 = mesh.textureVertices.get(textureIndices.get(0));
                Vector2f uv1 = mesh.textureVertices.get(textureIndices.get(1));
                Vector2f uv2 = mesh.textureVertices.get(textureIndices.get(2));

                javax.vecmath.Vector3f vec0 = convertToJavax(v0);
                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                float z0 = proj0.z;
                float z1 = proj1.z;
                float z2 = proj2.z;

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                drawTexturedTriangle(pw, zBuffer, p0, p1, p2, uv0, uv1, uv2, texture, z0, z1, z2);
            }
        }
    }

    private static void drawTexturedTriangle(
            PixelWriter pw, ZBuffer zBuffer,
            Point2f p0, Point2f p1, Point2f p2,
            Vector2f uv0, Vector2f uv1, Vector2f uv2,
            Texture texture, float z0, float z1, float z2) {

        int minX = (int) Math.max(0, Math.min(p0.x, Math.min(p1.x, p2.x)));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.max(p0.x, Math.max(p1.x, p2.x)));
        int minY = (int) Math.max(0, Math.min(p0.y, Math.min(p1.y, p2.y)));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.max(p0.y, Math.max(p1.y, p2.y)));

        if (minX > maxX || minY > maxY) return;

        float area = edgeFunction(p0, p1, p2);
        if (Math.abs(area) < 0.00001f) return;

        float invArea = 1.0f / area;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x, y);

                float w0 = edgeFunction(p1, p2, p) * invArea;
                float w1 = edgeFunction(p2, p0, p) * invArea;
                float w2 = edgeFunction(p0, p1, p) * invArea;

                if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                    float z = w0 * z0 + w1 * z1 + w2 * z2;

                    if (zBuffer.testAndSet(x, y, z)) {
                        float u = w0 * uv0.x + w1 * uv1.x + w2 * uv2.x;
                        float v = w0 * uv0.y + w1 * uv1.y + w2 * uv2.y;

                        Color texColor = texture.getColor(u, v);
                        pw.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    private static float edgeFunction(Point2f a, Point2f b, Point2f c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    private static void renderTexturedWithLight(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Texture texture,
            Light light) {

        PixelWriter pw = gc.getPixelWriter();

        Vector3f camPos = camera.getPosition();
        Vector3f camTarget = camera.getTarget();

        Vector3f lightDir = new Vector3f(
                camTarget.x - camPos.x,
                camTarget.y - camPos.y,
                camTarget.z - camPos.z
        );

        float len = (float) Math.sqrt(
                lightDir.x * lightDir.x + lightDir.y * lightDir.y + lightDir.z * lightDir.z
        );
        if (len > 0) {
            lightDir.x /= len;
            lightDir.y /= len;
            lightDir.z /= len;
        }

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            if (vertexIndices.size() == 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                Vector3f normal;
                if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                    normal = mesh.normals.get(normalIndices.get(0));
                } else {
                    Vector3f edge1 = new Vector3f(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
                    Vector3f edge2 = new Vector3f(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);

                    normal = new Vector3f(
                            edge1.y * edge2.z - edge1.z * edge2.y,
                            edge1.z * edge2.x - edge1.x * edge2.z,
                            edge1.x * edge2.y - edge1.y * edge2.x
                    );
                }

                float normalLen = (float) Math.sqrt(
                        normal.x * normal.x + normal.y * normal.y + normal.z * normal.z
                );
                if (normalLen > 0) {
                    normal.x /= normalLen;
                    normal.y /= normalLen;
                    normal.z /= normalLen;
                }

                float intensity = normal.x * lightDir.x +
                        normal.y * lightDir.y +
                        normal.z * lightDir.z;
                intensity = Math.max(intensity, 0.0f);
                float ambient = 0.3f;
                intensity = ambient + (1 - ambient) * intensity;

                javax.vecmath.Vector3f vec0 = convertToJavax(v0);
                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                float z0 = proj0.z;
                float z1 = proj1.z;
                float z2 = proj2.z;

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                if (texture != null && texture.isLoaded() &&
                        textureIndices.size() >= 3) {

                    Vector2f uv0 = mesh.textureVertices.get(textureIndices.get(0));
                    Vector2f uv1 = mesh.textureVertices.get(textureIndices.get(1));
                    Vector2f uv2 = mesh.textureVertices.get(textureIndices.get(2));

                    drawTexturedTriangleWithLight(pw, zBuffer, p0, p1, p2,
                            uv0, uv1, uv2,
                            texture, intensity, z0, z1, z2);
                } else {
                    Color shadedColor = Color.color(intensity, intensity, intensity);
                    drawTriangleWithZBuffer(pw, zBuffer,
                            p0.x, p0.y, z0,
                            p1.x, p1.y, z1,
                            p2.x, p2.y, z2,
                            shadedColor);
                }
            }
        }
    }

    private static void drawTexturedTriangleWithLight(
            PixelWriter pw, ZBuffer zBuffer,
            Point2f p0, Point2f p1, Point2f p2,
            Vector2f uv0, Vector2f uv1, Vector2f uv2,
            Texture texture, float intensity,
            float z0, float z1, float z2) {

        int minX = (int) Math.max(0, Math.min(p0.x, Math.min(p1.x, p2.x)));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.max(p0.x, Math.max(p1.x, p2.x)));
        int minY = (int) Math.max(0, Math.min(p0.y, Math.min(p1.y, p2.y)));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.max(p0.y, Math.max(p1.y, p2.y)));

        if (minX > maxX || minY > maxY) return;

        float area = edgeFunction(p0, p1, p2);
        if (Math.abs(area) < 0.00001f) return;

        float invArea = 1.0f / area;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x, y);

                float w0 = edgeFunction(p1, p2, p) * invArea;
                float w1 = edgeFunction(p2, p0, p) * invArea;
                float w2 = edgeFunction(p0, p1, p) * invArea;

                if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                    float z = w0 * z0 + w1 * z1 + w2 * z2;

                    if (zBuffer.testAndSet(x, y, z)) {
                        float u = w0 * uv0.x + w1 * uv1.x + w2 * uv2.x;
                        float v = w0 * uv0.y + w1 * uv1.y + w2 * uv2.y;

                        Color texColor = texture.getColor(u, v);

                        Color finalColor = Color.color(
                                Math.min(1.0, (double) texColor.getRed() * intensity),
                                Math.min(1.0, (double) texColor.getGreen() * intensity),
                                Math.min(1.0, (double) texColor.getBlue() * intensity)
                        );

                        pw.setColor(x, y, finalColor);
                    }
                }
            }
        }
    }
}
