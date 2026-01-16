package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.Light;
import com.cgvsu.model.Scene;
import com.cgvsu.model.ModelUtils;
import com.cgvsu.rasterization.Rasterization;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javax.vecmath.*;
import java.util.ArrayList;

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

    // Вспомогательный метод для конвертации
    private static javax.vecmath.Vector3f convertToJavax(Vector3f v) {
        return new javax.vecmath.Vector3f(v.x, v.y, v.z);
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

        // ============ РЕЖИМЫ ОТРИСОВКИ ============
        boolean fill = settings.isFillPolygons();
        boolean wire = settings.isDrawWireframe();
        boolean tex = settings.isUseTexture();
        boolean light = settings.isUseLighting();

        Light sceneLight = currentScene != null ? currentScene.getLight() : null;
        Texture texture = tex ? settings.getCurrentTexture() : null;

        // 1. Если ничего не выбрано - библиотечная заливка цветом
        if (!fill && !wire && !tex && !light) {
            renderLibraryFill(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, fillColor);
            return;
        }

        // 2. Если только wireframe
        if (wire && !fill) {
            renderWireframe(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, Color.BLACK);
            return;
        }

        // 3. Заливка (разные варианты)
        if (fill) {
            if (tex && texture != null && texture.isLoaded()) {
                // С текстурами
                if (light && sceneLight != null) {
                    // Текстура + освещение
                    renderTexturedWithLight(graphicsContext, cameraToUse, mesh,
                            modelViewProjectionMatrix, width, height,
                            texture, sceneLight);
                } else {
                    // Только текстура
                    renderTextured(graphicsContext, mesh, modelViewProjectionMatrix,
                            width, height, texture);
                }
            } else if (light && sceneLight != null) {
                // Только освещение
                renderWithLighting(graphicsContext, cameraToUse, mesh,
                        modelViewProjectionMatrix, width, height,
                        sceneLight, settings.getFillColor());
            } else {
                // Только цвет (используем нашу растеризацию!)
                renderColorWithRasterization(graphicsContext, mesh, modelViewProjectionMatrix,
                        width, height, settings.getFillColor());
            }
        }

        // 4. Wireframe поверх (если включен)
        if (wire && fill) {
            renderWireframe(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, Color.BLACK);
        }
    }

    // ============ БИБЛИОТЕЧНАЯ ЗАЛИВКА ============
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
                    javax.vecmath.Vector3f vec = convertToJavax(vertex); // КОНВЕРТИРУЕМ
                    javax.vecmath.Vector3f projected = multiplyMatrix4ByVector3(matrix, vec);
                    Point2f point = vertexToPoint(projected, width, height);
                    xPoints[i] = point.x;
                    yPoints[i] = point.y;
                }

                gc.fillPolygon(xPoints, yPoints, vertexIndices.size());
            }
        }
    }


    // ============ ЗАЛИВКА ЦВЕТОМ С РАСТЕРИЗАЦИЕЙ ============
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

                // Преобразуем в экранные координаты
                javax.vecmath.Vector3f vec0 = convertToJavax(v0); // КОНВЕРТИРУЕМ
                javax.vecmath.Vector3f vec1 = convertToJavax(v1); // КОНВЕРТИРУЕМ
                javax.vecmath.Vector3f vec2 = convertToJavax(v2); // КОНВЕРТИРУЕМ

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                // Используем растеризацию из 2-го задания
                Rasterization.drawTriangleByIterator(pw,
                        (int)p0.x, (int)p0.y,
                        (int)p1.x, (int)p1.y,
                        (int)p2.x, (int)p2.y,
                        color);
            }
        }
    }

    // ============ WIREFRAME ============
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

                javax.vecmath.Vector3f vec1 = convertToJavax(v1); // КОНВЕРТИРУЕМ
                javax.vecmath.Vector3f vec2 = convertToJavax(v2); // КОНВЕРТИРУЕМ

                Point2f p1 = vertexToPoint(multiplyMatrix4ByVector3(matrix, vec1), width, height);
                Point2f p2 = vertexToPoint(multiplyMatrix4ByVector3(matrix, vec2), width, height);

                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    // ============ ОСВЕЩЕНИЕ ============
    private static void renderWithLighting(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Light light,
            Color baseColor) {

        PixelWriter pw = gc.getPixelWriter();

        // Свет привязан к камере
        Vector3f camPos = camera.getPosition();
        Vector3f camTarget = camera.getTarget();

        // Направление света
        Vector3f lightDir = new Vector3f(
                camTarget.x - camPos.x,
                camTarget.y - camPos.y,
                camTarget.z - camPos.z
        );

        // Нормализуем
        float len = (float)Math.sqrt(
                lightDir.x*lightDir.x + lightDir.y*lightDir.y + lightDir.z*lightDir.z
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

                // Нормаль грани (для освещения)
                Vector3f normal;
                if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                    normal = mesh.normals.get(normalIndices.get(0));
                } else {
                    // Вычисляем нормаль грани
                    Vector3f edge1 = new Vector3f(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
                    Vector3f edge2 = new Vector3f(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);

                    normal = new Vector3f(
                            edge1.y * edge2.z - edge1.z * edge2.y,
                            edge1.z * edge2.x - edge1.x * edge2.z,
                            edge1.x * edge2.y - edge1.y * edge2.x
                    );
                }

                // Нормализуем
                float normalLen = (float)Math.sqrt(
                        normal.x*normal.x + normal.y*normal.y + normal.z*normal.z
                );
                if (normalLen > 0) {
                    normal.x /= normalLen;
                    normal.y /= normalLen;
                    normal.z /= normalLen;
                }

                // Освещенность
                float intensity = normal.x * lightDir.x +
                        normal.y * lightDir.y +
                        normal.z * lightDir.z;
                intensity = Math.max(intensity, 0.0f);
                float ambient = 0.3f;
                intensity = ambient + (1 - ambient) * intensity;

                // Цвет с освещением
                Color shadedColor = Color.color(
                        (float)baseColor.getRed() * intensity,
                        (float)baseColor.getGreen() * intensity,
                        (float)baseColor.getBlue() * intensity
                );

                // Экранные координаты
                javax.vecmath.Vector3f vec0 = convertToJavax(v0); // КОНВЕРТИРУЕМ
                javax.vecmath.Vector3f vec1 = convertToJavax(v1); // КОНВЕРТИРУЕМ
                javax.vecmath.Vector3f vec2 = convertToJavax(v2); // КОНВЕРТИРУЕМ

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                // Используем растеризацию с освещением
                Rasterization.drawTriangleByIterator(pw,
                        (int)p0.x, (int)p0.y,
                        (int)p1.x, (int)p1.y,
                        (int)p2.x, (int)p2.y,
                        shadedColor);
            }
        }
    }

    // ============ ТЕКСТУРА ============
    // ============ ТЕКСТУРА С РЕАЛЬНЫМ НАЛОЖЕНИЕМ ============
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

                // Преобразуем в экранные координаты
                javax.vecmath.Vector3f vec0 = convertToJavax(v0);
                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                // Рисуем треугольник с текстурой
                drawTexturedTriangle(pw, p0, p1, p2, uv0, uv1, uv2, texture);
            }
        }
    }

    private static void drawTexturedTriangle(
            PixelWriter pw,
            Point2f p0, Point2f p1, Point2f p2,
            Vector2f uv0, Vector2f uv1, Vector2f uv2,
            Texture texture) {

        // Находим bounding box треугольника
        int minX = (int) Math.max(0, Math.min(p0.x, Math.min(p1.x, p2.x)));
        int maxX = (int) Math.min(Integer.MAX_VALUE, Math.max(p0.x, Math.max(p1.x, p2.x)));
        int minY = (int) Math.max(0, Math.min(p0.y, Math.min(p1.y, p2.y)));
        int maxY = (int) Math.min(Integer.MAX_VALUE, Math.max(p0.y, Math.max(p1.y, p2.y)));

        // Ограничиваем размером экрана (если знаем)
        maxX = Math.min(maxX, 2000); // временное ограничение
        maxY = Math.min(maxY, 2000);

        // Вычисляем площадь треугольника для барицентрических координат
        float area = edgeFunction(p0, p1, p2);
        if (Math.abs(area) < 0.0001f) return;

        float invArea = 1.0f / area;

        // Для каждого пикселя в bounding box
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x, y);

                // Барицентрические координаты
                float w0 = edgeFunction(p1, p2, p) * invArea;
                float w1 = edgeFunction(p2, p0, p) * invArea;
                float w2 = edgeFunction(p0, p1, p) * invArea;

                // Если точка внутри треугольника (с небольшим запасом)
                if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                    // Интерполируем текстурные координаты
                    float u = w0 * uv0.x + w1 * uv1.x + w2 * uv2.x;
                    float v = w0 * uv0.y + w1 * uv1.y + w2 * uv2.y;

                    // Получаем цвет из текстуры
                    Color texColor = texture.getColor(u, v);

                    // Рисуем пиксель
                    pw.setColor(x, y, texColor);
                }
            }
        }
    }

    private static float edgeFunction(Point2f a, Point2f b, Point2f c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    // ============ ТЕКСТУРА + ОСВЕЩЕНИЕ ============
    // ============ ТЕКСТУРА + ОСВЕЩЕНИЕ ============
    private static void renderTexturedWithLight(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            Matrix4f matrix,
            int width, int height,
            Texture texture,
            Light light) {

        PixelWriter pw = gc.getPixelWriter();

        // Свет привязан к камере
        Vector3f camPos = camera.getPosition();
        Vector3f camTarget = camera.getTarget();

        // Направление света
        Vector3f lightDir = new Vector3f(
                camTarget.x - camPos.x,
                camTarget.y - camPos.y,
                camTarget.z - camPos.z
        );

        // Нормализуем
        float len = (float)Math.sqrt(
                lightDir.x*lightDir.x + lightDir.y*lightDir.y + lightDir.z*lightDir.z
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

                // Нормаль грани
                Vector3f normal;
                if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                    normal = mesh.normals.get(normalIndices.get(0));
                } else {
                    // Вычисляем нормаль грани
                    Vector3f edge1 = new Vector3f(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
                    Vector3f edge2 = new Vector3f(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);

                    normal = new Vector3f(
                            edge1.y * edge2.z - edge1.z * edge2.y,
                            edge1.z * edge2.x - edge1.x * edge2.z,
                            edge1.x * edge2.y - edge1.y * edge2.x
                    );
                }

                // Нормализуем
                float normalLen = (float)Math.sqrt(
                        normal.x*normal.x + normal.y*normal.y + normal.z*normal.z
                );
                if (normalLen > 0) {
                    normal.x /= normalLen;
                    normal.y /= normalLen;
                    normal.z /= normalLen;
                }

                // Освещенность
                float intensity = normal.x * lightDir.x +
                        normal.y * lightDir.y +
                        normal.z * lightDir.z;
                intensity = Math.max(intensity, 0.0f);
                float ambient = 0.3f;
                intensity = ambient + (1 - ambient) * intensity;

                // Экранные координаты
                javax.vecmath.Vector3f vec0 = convertToJavax(v0);
                javax.vecmath.Vector3f vec1 = convertToJavax(v1);
                javax.vecmath.Vector3f vec2 = convertToJavax(v2);

                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(matrix, vec0);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(matrix, vec1);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(matrix, vec2);

                Point2f p0 = vertexToPoint(proj0, width, height);
                Point2f p1 = vertexToPoint(proj1, width, height);
                Point2f p2 = vertexToPoint(proj2, width, height);

                // Если есть текстура
                if (texture != null && texture.isLoaded() &&
                        textureIndices.size() >= 3) {

                    // Текстурные координаты
                    Vector2f uv0 = mesh.textureVertices.get(textureIndices.get(0));
                    Vector2f uv1 = mesh.textureVertices.get(textureIndices.get(1));
                    Vector2f uv2 = mesh.textureVertices.get(textureIndices.get(2));

                    // Рисуем с текстурой и освещением
                    drawTexturedTriangleWithLight(pw, p0, p1, p2,
                            uv0, uv1, uv2,
                            texture, intensity);
                } else {
                    // Просто цвет с освещением
                    Color shadedColor = Color.color(intensity, intensity, intensity);
                    Rasterization.drawTriangleByIterator(pw,
                            (int)p0.x, (int)p0.y,
                            (int)p1.x, (int)p1.y,
                            (int)p2.x, (int)p2.y,
                            shadedColor);
                }
            }
        }
    }

    private static void drawTexturedTriangleWithLight(
            PixelWriter pw,
            Point2f p0, Point2f p1, Point2f p2,
            Vector2f uv0, Vector2f uv1, Vector2f uv2,
            Texture texture, float intensity) {

        // Находим bounding box треугольника
        int minX = (int) Math.max(0, Math.min(p0.x, Math.min(p1.x, p2.x)));
        int maxX = (int) Math.min(2000, Math.max(p0.x, Math.max(p1.x, p2.x)));
        int minY = (int) Math.max(0, Math.min(p0.y, Math.min(p1.y, p2.y)));
        int maxY = (int) Math.min(2000, Math.max(p0.y, Math.max(p1.y, p2.y)));

        // Вычисляем площадь для барицентрических координат
        float area = edgeFunction(p0, p1, p2);
        if (Math.abs(area) < 0.0001f) return;
        float invArea = 1.0f / area;

        // Для каждого пикселя
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x, y);

                // Барицентрические координаты
                float w0 = edgeFunction(p1, p2, p) * invArea;
                float w1 = edgeFunction(p2, p0, p) * invArea;
                float w2 = edgeFunction(p0, p1, p) * invArea;

                if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                    // Текстурные координаты
                    float u = w0 * uv0.x + w1 * uv1.x + w2 * uv2.x;
                    float v = w0 * uv0.y + w1 * uv1.y + w2 * uv2.y;

                    // Цвет из текстуры
                    Color texColor = texture.getColor(u, v);

                    // Применяем освещение к текстуре
                    Color finalColor = Color.color(
                            Math.min(1.0f, (float)texColor.getRed() * intensity),
                            Math.min(1.0f, (float)texColor.getGreen() * intensity),
                            Math.min(1.0f, (float)texColor.getBlue() * intensity)
                    );

                    // Рисуем пиксель
                    pw.setColor(x, y, finalColor);
                }
            }
        }
    }
}
