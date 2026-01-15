package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.rasterization.Rasterization;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javax.vecmath.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {
    private static ZBuffer zBuffer;
    private static RenderSettings settings = new RenderSettings();

    public static void setRenderSettings(RenderSettings newSettings) {
        settings = newSettings;
    }

    public static RenderSettings getRenderSettings() {
        return settings;
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Color fillColor) {

        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();

        graphicsContext.clearRect(0, 0, width, height);

        if (mesh == null) return;

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        if (!settings.isFillPolygons() && !settings.isDrawWireframe()) {
            renderWithLibrary(graphicsContext, mesh, modelViewProjectionMatrix, width, height, fillColor);
            return;
        }

        if (settings.isDrawWireframe() && !settings.isFillPolygons()) {
            drawWireframeOnly(graphicsContext, mesh, modelViewProjectionMatrix, width, height, fillColor);
            return;
        }

        if (settings.isFillPolygons()) {
            renderFillPolygons(graphicsContext, camera, mesh, modelViewProjectionMatrix, width, height, fillColor);
        }

        if (settings.isDrawWireframe() && settings.isFillPolygons()) {
            drawWireframeOverlay(graphicsContext, mesh, modelViewProjectionMatrix, width, height, Color.BLACK);
        }
    }

    private static void renderWithLibrary(
            final GraphicsContext graphicsContext,
            final Model mesh,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height,
            final Color fillColor) {

        graphicsContext.setFill(fillColor);

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                double[] xPoints = new double[vertexIndices.size()];
                double[] yPoints = new double[vertexIndices.size()];

                for (int i = 0; i < vertexIndices.size(); i++) {
                    Vector3f vertex = mesh.vertices.get(vertexIndices.get(i));
                    // Конвертируем com.cgvsu.math.Vector3f в javax.vecmath.Vector3f
                    javax.vecmath.Vector3f vec = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);
                    Point2f point = vertexToPoint(
                            multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec),
                            width, height
                    );
                    xPoints[i] = point.x;
                    yPoints[i] = point.y;
                }

                graphicsContext.fillPolygon(xPoints, yPoints, vertexIndices.size());
            }
        }
    }

    private static void drawWireframeOnly(
            final GraphicsContext graphicsContext,
            final Model mesh,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height,
            final Color color) {

        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(1.0);

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Point2f> resultPoints = new ArrayList<>();

            for (Integer vertexIndex : vertexIndices) {
                Vector3f vertex = mesh.vertices.get(vertexIndex);
                // Конвертируем com.cgvsu.math.Vector3f в javax.vecmath.Vector3f
                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);
                Point2f resultPoint = vertexToPoint(
                        multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath),
                        width, height
                );
                resultPoints.add(resultPoint);
            }

            if (resultPoints.size() > 1) {
                for (int i = 1; i < resultPoints.size(); i++) {
                    graphicsContext.strokeLine(
                            resultPoints.get(i - 1).x, resultPoints.get(i - 1).y,
                            resultPoints.get(i).x, resultPoints.get(i).y
                    );
                }
                graphicsContext.strokeLine(
                        resultPoints.get(resultPoints.size() - 1).x,
                        resultPoints.get(resultPoints.size() - 1).y,
                        resultPoints.get(0).x, resultPoints.get(0).y
                );
            }
        }
    }

    private static void renderFillPolygons(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height,
            final Color fillColor) {

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() >= 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                Point2f[] screenPoints = new Point2f[3];
                float[] zValues = new float[3];

                // Конвертируем com.cgvsu.math.Vector3f в javax.vecmath.Vector3f
                javax.vecmath.Vector3f vec0 = new javax.vecmath.Vector3f(v0.x, v0.y, v0.z);
                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec0);
                screenPoints[0] = vertexToPoint(proj0, width, height);
                zValues[0] = proj0.z;

                javax.vecmath.Vector3f vec1 = new javax.vecmath.Vector3f(v1.x, v1.y, v1.z);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1);
                screenPoints[1] = vertexToPoint(proj1, width, height);
                zValues[1] = proj1.z;

                javax.vecmath.Vector3f vec2 = new javax.vecmath.Vector3f(v2.x, v2.y, v2.z);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2);
                screenPoints[2] = vertexToPoint(proj2, width, height);
                zValues[2] = proj2.z;

                // Используем Z-буфер для заливки
                PixelWriterWrapper wrapper = new PixelWriterWrapper(
                        graphicsContext.getPixelWriter(),
                        zBuffer,
                        (int)screenPoints[0].x, (int)screenPoints[0].y, zValues[0],
                        (int)screenPoints[1].x, (int)screenPoints[1].y, zValues[1],
                        (int)screenPoints[2].x, (int)screenPoints[2].y, zValues[2]
                );

                Rasterization.drawTriangleByIterator(
                        wrapper,
                        (int)screenPoints[0].x, (int)screenPoints[0].y,
                        (int)screenPoints[1].x, (int)screenPoints[1].y,
                        (int)screenPoints[2].x, (int)screenPoints[2].y,
                        fillColor
                );
            }
        }
    }

    private static void drawWireframeOverlay(
            final GraphicsContext graphicsContext,
            final Model mesh,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height,
            final Color color) {

        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(1.0);

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                int next = (i + 1) % vertexIndices.size();

                Vector3f v1 = mesh.vertices.get(vertexIndices.get(i));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(next));

                // Конвертируем com.cgvsu.math.Vector3f в javax.vecmath.Vector3f
                javax.vecmath.Vector3f vec1 = new javax.vecmath.Vector3f(v1.x, v1.y, v1.z);
                javax.vecmath.Vector3f vec2 = new javax.vecmath.Vector3f(v2.x, v2.y, v2.z);

                Point2f p1 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1), width, height);
                Point2f p2 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2), width, height);

                graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private static float edgeFunction(float ax, float ay, float bx, float by, float px, float py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }
}

class PixelWriterWrapper implements PixelWriter {
    private final PixelWriter delegate;
    private final ZBuffer zBuffer;
    private final float z0, z1, z2;
    private final int x0, y0, x1, y1, x2, y2;

    public PixelWriterWrapper(PixelWriter delegate, ZBuffer zBuffer,
                              int x0, int y0, float z0,
                              int x1, int y1, float z1,
                              int x2, int y2, float z2) {
        this.delegate = delegate;
        this.zBuffer = zBuffer;
        this.x0 = x0; this.y0 = y0; this.z0 = z0;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
    }

    @Override
    public void setColor(int x, int y, Color c) {
        float area = edgeFunction(x0, y0, x1, y1, x2, y2);
        if (Math.abs(area) < 1e-6f) return;

        float w0 = edgeFunction(x1, y1, x2, y2, x, y) / area;
        float w1 = edgeFunction(x2, y2, x0, y0, x, y) / area;
        float w2 = edgeFunction(x0, y0, x1, y1, x, y) / area;

        if (w0 >= -1e-6f && w1 >= -1e-6f && w2 >= -1e-6f) {
            float z = w0 * z0 + w1 * z1 + w2 * z2;
            if (zBuffer.testAndSet(x, y, z)) {
                delegate.setColor(x, y, c);
            }
        }
    }

    private float edgeFunction(float ax, float ay, float bx, float by, float px, float py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    @Override
    public <T extends Buffer> void setPixels(int x, int y, int w, int h,
                                             PixelFormat<T> pixelFormat, T buffer, int stride) {
    }

    @Override
    public void setPixels(int x, int y, int w, int h,
                          PixelFormat<ByteBuffer> pixelFormat, byte[] buffer, int offset, int stride) {
    }

    @Override
    public void setPixels(int x, int y, int w, int h,
                          PixelFormat<IntBuffer> pixelFormat, int[] buffer, int offset, int stride) {
    }

    @Override
    public void setPixels(int x, int y, int w, int h,
                          PixelReader reader, int srcX, int srcY) {
    }

    @Override
    public PixelFormat getPixelFormat() {
        return null;
    }

    @Override
    public void setArgb(int x, int y, int argb) {
        Color c = Color.rgb(
                (argb >> 16) & 0xFF,
                (argb >> 8) & 0xFF,
                argb & 0xFF,
                ((argb >> 24) & 0xFF) / 255.0
        );
        setColor(x, y, c);
    }
}