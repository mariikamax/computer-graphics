package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.model.Model;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

/**
 * Движок рендеринга с полным графическим конвейером
 * Локальные -> Мировые -> Камера -> Проекция -> Экранные координаты
 * Работа с векторами-столбцами
 */
public class RenderEngine {

    /**
     * Основной метод рендеринга с полным графическим конвейером
     * Для векторов-столбцов: MVP = Projection * View * Model
     */
    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final int width,
            final int height) {

        // 1. Получаем матрицы вида и проекции из камеры
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // 2. Вычисляем полную MVP матрицу: Projection * View * Model
        Matrix4f viewProjectionMatrix = projectionMatrix.multiply(viewMatrix);
        Matrix4f mvpMatrix = viewProjectionMatrix.multiply(modelMatrix);

        // 3. Рендерим каждый полигон модели
        for (com.cgvsu.model.Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue;

            // Преобразуем вершины полигона через полный конвейер
            ArrayList<Vector2f> screenPoints = new ArrayList<>();
            for (int vertexIndex : vertexIndices) {
                if (vertexIndex < 0 || vertexIndex >= mesh.vertices.size()) {
                    continue; // Пропускаем некорректные индексы
                }

                Vector3f vertex = mesh.vertices.get(vertexIndex);

                // Применяем MVP преобразование
                Vector4f transformed = mvpMatrix.multiply(vertex);

                // Перспективное деление (w != 1 после проекции)
                transformed.normalize();

                // Конвертируем в экранные координаты
                Vector2f screenPoint = vertexToScreenPoint(transformed.toVector3f(), width, height);
                screenPoints.add(screenPoint);
            }

            // Рисуем полигон (каркасная модель)
            drawPolygonWireframe(graphicsContext, screenPoints);
        }
    }

    /**
     * Рендеринг с автоматическим получением матрицы модели из Transform
     */
    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {

        // Обновляем вершины с учетом текущих трансформаций
        mesh.updateVerticesWithTransformations();

        // Используем матрицу трансформаций из модели
        Matrix4f modelMatrix = mesh.getTransform().getTransformationMatrix();
        render(graphicsContext, camera, mesh, modelMatrix, width, height);
    }

    /**
     * Рендеринг с использованием отдельных компонентов трансформации
     */
    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Vector3f position,
            final Vector3f rotation,
            final Vector3f scale,
            final int width,
            final int height) {

        // Создаем матрицу модели из компонентов
        Matrix4f modelMatrix = GraphicConveyor.createModelMatrix(position, rotation, scale);
        render(graphicsContext, camera, mesh, modelMatrix, width, height);
    }

    /**
     * Конвертация из пространства отсечения (-1..1) в экранные координаты
     */
    private static Vector2f vertexToScreenPoint(Vector3f clipSpaceVertex, int width, int height) {
        // Преобразуем из диапазона [-1, 1] в [0, width] и [0, height]
        float screenX = (clipSpaceVertex.x + 1.0f) * 0.5f * width;

        // Обратная ось Y (экранные координаты обычно начинаются сверху)
        float screenY = (1.0f - (clipSpaceVertex.y + 1.0f) * 0.5f) * height;

        return new Vector2f(screenX, screenY);
    }

    /**
     * Рисование полигона в виде каркаса
     */
    private static void drawPolygonWireframe(
            GraphicsContext gc,
            ArrayList<Vector2f> points) {

        if (points.size() < 2) return;

        // Настройка стиля линий
        gc.setLineWidth(1.0);
        gc.setStroke(javafx.scene.paint.Color.BLACK);

        // Рисуем линии между всеми вершинами
        for (int i = 0; i < points.size(); i++) {
            Vector2f current = points.get(i);
            Vector2f next = points.get((i + 1) % points.size());

            gc.strokeLine(current.x, current.y, next.x, next.y);
        }
    }

    /**
     * Отладочный рендеринг - показывает преобразования поэтапно
     */
    public static void debugRender(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final int width,
            final int height) {

        System.out.println("=== DEBUG RENDER ===");
        System.out.println("Model Matrix:\n" + modelMatrix);
        System.out.println("View Matrix:\n" + camera.getViewMatrix());
        System.out.println("Projection Matrix:\n" + camera.getProjectionMatrix());

        // Рендерим первую вершину для демонстрации
        if (!mesh.vertices.isEmpty()) {
            Vector3f vertex = mesh.vertices.get(0);

            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projectionMatrix = camera.getProjectionMatrix();

            Vector4f worldSpace = modelMatrix.multiply(vertex);
            System.out.println("\nVertex 0 transformations:");
            System.out.println("Local: " + vertex + " -> World: " + worldSpace.toVector3f());

            Vector4f viewSpace = viewMatrix.multiply(worldSpace);
            System.out.println("World -> View: " + viewSpace.toVector3f());

            Vector4f clipSpace = projectionMatrix.multiply(viewSpace);
            clipSpace.normalize();
            System.out.println("View -> Clip: " + clipSpace.toVector3f());

            Vector2f screenPoint = vertexToScreenPoint(clipSpace.toVector3f(), width, height);
            System.out.println("Clip -> Screen: " + screenPoint);
        }

        // Затем обычный рендеринг
        render(graphicsContext, camera, mesh, modelMatrix, width, height);
    }

    /**
     * Рендеринг с заливкой полигонов (базовая реализация)
     */
    public static void renderSolid(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final int width,
            final int height,
            final javafx.scene.paint.Color color) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f viewProjectionMatrix = projectionMatrix.multiply(viewMatrix);
        Matrix4f mvpMatrix = viewProjectionMatrix.multiply(modelMatrix);

        // Настройка заливки
        graphicsContext.setFill(color);
        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(1.0);

        for (com.cgvsu.model.Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue;

            // Преобразуем вершины
            double[] xPoints = new double[vertexIndices.size()];
            double[] yPoints = new double[vertexIndices.size()];

            for (int i = 0; i < vertexIndices.size(); i++) {
                int vertexIndex = vertexIndices.get(i);
                if (vertexIndex < 0 || vertexIndex >= mesh.vertices.size()) {
                    continue;
                }

                Vector3f vertex = mesh.vertices.get(vertexIndex);
                Vector4f transformed = mvpMatrix.multiply(vertex);
                transformed.normalize();

                Vector2f screenPoint = vertexToScreenPoint(transformed.toVector3f(), width, height);
                xPoints[i] = screenPoint.x;
                yPoints[i] = screenPoint.y;
            }

            // Рисуем заполненный полигон
            graphicsContext.fillPolygon(xPoints, yPoints, vertexIndices.size());
            graphicsContext.strokePolygon(xPoints, yPoints, vertexIndices.size());
        }
    }
}