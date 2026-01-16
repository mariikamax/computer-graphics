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

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            Model mesh,
            final int width,
            final int height,
            final Color fillColor) {

        if (mesh == null) return;

        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();

        graphicsContext.clearRect(0, 0, width, height);

        if (!mesh.isTriangulated()) {
            ModelUtils.triangulate(mesh);
        }
        if (mesh.normals.isEmpty()) {
            ModelUtils.calculateNormals(mesh);
        }

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

        if (!settings.isFillPolygons() && !settings.isDrawWireframe() &&
                !settings.isUseTexture() && !settings.isUseLighting()) {
            renderWithLibrary(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, fillColor);
            return;
        }

        if (settings.isDrawWireframe() && !settings.isFillPolygons()) {
            drawWireframeOnly(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, fillColor);
            return;
        }

        if (settings.isFillPolygons()) {
            boolean needAdvancedRender = settings.isUseLighting() || settings.isUseTexture();

            if (needAdvancedRender) {
                Light light = null;
                Texture texture = null;

                if (settings.isUseLighting() && currentScene != null) {
                    light = currentScene.getLight();
                }

                if (settings.isUseTexture()) {
                    texture = settings.getCurrentTexture();
                }

                renderAdvanced(
                        graphicsContext, cameraToUse, mesh, modelViewProjectionMatrix,
                        width, height, light, texture, fillColor
                );
            } else {
                renderFillPolygons(graphicsContext, mesh, modelViewProjectionMatrix,
                        width, height, fillColor);
            }
        }

        if (settings.isDrawWireframe() && settings.isFillPolygons()) {
            drawWireframeOverlay(graphicsContext, mesh, modelViewProjectionMatrix,
                    width, height, Color.BLACK);
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
            final Model mesh,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height,
            final Color fillColor) {

        for (Polygon polygon : mesh.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() == 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                Point2f[] screenPoints = new Point2f[3];
                float[] zValues = new float[3];

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

                PixelWriterWrapper wrapper = new PixelWriterWrapper(
                        graphicsContext.getPixelWriter(),
                        zBuffer,
                        (int) screenPoints[0].x, (int) screenPoints[0].y, zValues[0],
                        (int) screenPoints[1].x, (int) screenPoints[1].y, zValues[1],
                        (int) screenPoints[2].x, (int) screenPoints[2].y, zValues[2]
                );

                Rasterization.drawTriangleByIterator(
                        wrapper,
                        (int) screenPoints[0].x, (int) screenPoints[0].y,
                        (int) screenPoints[1].x, (int) screenPoints[1].y,
                        (int) screenPoints[2].x, (int) screenPoints[2].y,
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

                javax.vecmath.Vector3f vec1 = new javax.vecmath.Vector3f(v1.x, v1.y, v1.z);
                javax.vecmath.Vector3f vec2 = new javax.vecmath.Vector3f(v2.x, v2.y, v2.z);

                Point2f p1 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1), width, height);
                Point2f p2 = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2), width, height);

                graphicsContext.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private static void renderAdvanced(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelViewProjectionMatrix,
            final int width,
            final int height,
            final Light light,
            final Texture texture,
            final Color fillColor) {


        com.cgvsu.math.Vector3f cameraPosOur = camera.getPosition();
        javax.vecmath.Vector3f cameraPosMath = new javax.vecmath.Vector3f(
                cameraPosOur.x, cameraPosOur.y, cameraPosOur.z);

        int triangleCount = 0;
        int renderedTriangles = 0;
        int skippedTriangles = 0;

        for (Polygon polygon : mesh.polygons) {
            triangleCount++;
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            if (vertexIndices.size() == 3) {
                Vector3f v0 = mesh.vertices.get(vertexIndices.get(0));
                Vector3f v1 = mesh.vertices.get(vertexIndices.get(1));
                Vector3f v2 = mesh.vertices.get(vertexIndices.get(2));

                Point2f[] screenPoints = new Point2f[3];
                float[] zValues = new float[3];
                Vector3f[] worldPositions = new Vector3f[3];
                Vector3f[] normals = new Vector3f[3];
                Vector2f[] texCoords = new Vector2f[3];

                javax.vecmath.Vector3f vec0 = new javax.vecmath.Vector3f(v0.x, v0.y, v0.z);
                javax.vecmath.Vector3f proj0 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec0);
                screenPoints[0] = vertexToPoint(proj0, width, height);
                zValues[0] = proj0.z;
                worldPositions[0] = v0;

                javax.vecmath.Vector3f vec1 = new javax.vecmath.Vector3f(v1.x, v1.y, v1.z);
                javax.vecmath.Vector3f proj1 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1);
                screenPoints[1] = vertexToPoint(proj1, width, height);
                zValues[1] = proj1.z;
                worldPositions[1] = v1;

                javax.vecmath.Vector3f vec2 = new javax.vecmath.Vector3f(v2.x, v2.y, v2.z);
                javax.vecmath.Vector3f proj2 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2);
                screenPoints[2] = vertexToPoint(proj2, width, height);
                zValues[2] = proj2.z;
                worldPositions[2] = v2;

                float minX = Math.min(screenPoints[0].x, Math.min(screenPoints[1].x, screenPoints[2].x));
                float maxX = Math.max(screenPoints[0].x, Math.max(screenPoints[1].x, screenPoints[2].x));
                float minY = Math.min(screenPoints[0].y, Math.min(screenPoints[1].y, screenPoints[2].y));
                float maxY = Math.max(screenPoints[0].y, Math.max(screenPoints[1].y, screenPoints[2].y));

                if (maxX < 0 || minX >= width || maxY < 0 || minY >= height) {
                    skippedTriangles++;
                    continue;
                }

                if (Math.abs(maxX - minX) < 0.5f && Math.abs(maxY - minY) < 0.5f) {
                    skippedTriangles++;
                    continue;
                }

                if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                    normals[0] = mesh.normals.get(normalIndices.get(0));
                    normals[1] = mesh.normals.get(normalIndices.get(1));
                    normals[2] = mesh.normals.get(normalIndices.get(2));
                } else {
                    Vector3f faceNormal = computeFaceNormal(v0, v1, v2);
                    normals[0] = faceNormal;
                    normals[1] = faceNormal;
                    normals[2] = faceNormal;
                }

                if (texture != null && !textureIndices.isEmpty() && textureIndices.size() >= 3) {
                    texCoords[0] = mesh.textureVertices.get(textureIndices.get(0));
                    texCoords[1] = mesh.textureVertices.get(textureIndices.get(1));
                    texCoords[2] = mesh.textureVertices.get(textureIndices.get(2));
                } else {
                    texCoords[0] = null;
                    texCoords[1] = null;
                    texCoords[2] = null;
                }

                PixelWriterAdvancedWrapper wrapper = new PixelWriterAdvancedWrapper(
                        graphicsContext.getPixelWriter(),
                        zBuffer,
                        (int) screenPoints[0].x, (int) screenPoints[0].y, zValues[0],
                        worldPositions[0], normals[0], texCoords[0],
                        (int) screenPoints[1].x, (int) screenPoints[1].y, zValues[1],
                        worldPositions[1], normals[1], texCoords[1],
                        (int) screenPoints[2].x, (int) screenPoints[2].y, zValues[2],
                        worldPositions[2], normals[2], texCoords[2],
                        light, texture, cameraPosMath, fillColor
                );

                Rasterization.drawTriangleByIterator(
                        wrapper,
                        (int) screenPoints[0].x, (int) screenPoints[0].y,
                        (int) screenPoints[1].x, (int) screenPoints[1].y,
                        (int) screenPoints[2].x, (int) screenPoints[2].y,
                        fillColor
                );
                renderedTriangles++;
            }
        }
    }

    private static Vector3f computeFaceNormal(
            Vector3f v0,
            Vector3f v1,
            Vector3f v2) {

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

        Vector3f normal = Vector3f.crossProduct(edge1, edge2);
        return normal.normalize();
    }

    private static float edgeFunction(float ax, float ay, float bx, float by, float px, float py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    private static class PixelWriterWrapper implements PixelWriter {
        private final PixelWriter delegate;
        private final ZBuffer zBuffer;
        private final float z0, z1, z2;
        private final int x0, y0, x1, y1, x2, y2;
        private final float edge0, edge1, edge2;

        public PixelWriterWrapper(PixelWriter delegate, ZBuffer zBuffer,
                                  int x0, int y0, float z0,
                                  int x1, int y1, float z1,
                                  int x2, int y2, float z2) {
            this.delegate = delegate;
            this.zBuffer = zBuffer;
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;

            this.edge0 = (x2 - x1) * (y0 - y1) - (y2 - y1) * (x0 - x1);
            this.edge1 = (x0 - x2) * (y1 - y2) - (y0 - y2) * (x1 - x2);
            this.edge2 = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0);
        }

        @Override
        public void setColor(int x, int y, Color c) {
            float w0 = ((x2 - x1) * (y - y1) - (y2 - y1) * (x - x1)) / edge0;
            float w1 = ((x0 - x2) * (y - y2) - (y0 - y2) * (x - x2)) / edge1;
            float w2 = ((x1 - x0) * (y - y0) - (y1 - y0) * (x - x0)) / edge2;

            if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                float z = w0 * z0 + w1 * z1 + w2 * z2;

                if (zBuffer.testAndSet(x, y, z)) {
                    delegate.setColor(x, y, c);
                }
            }
        }

        @Override
        public <T extends Buffer> void setPixels(int x, int y, int w, int h,
                                                 PixelFormat<T> pixelFormat, T buffer, int stride) {
            delegate.setPixels(x, y, w, h, pixelFormat, buffer, stride);
        }

        @Override
        public void setPixels(int x, int y, int w, int h,
                              PixelFormat<ByteBuffer> pixelFormat, byte[] buffer, int offset, int stride) {
            delegate.setPixels(x, y, w, h, pixelFormat, buffer, offset, stride);
        }

        @Override
        public void setPixels(int x, int y, int w, int h,
                              PixelFormat<IntBuffer> pixelFormat, int[] buffer, int offset, int stride) {
            delegate.setPixels(x, y, w, h, pixelFormat, buffer, offset, stride);
        }

        @Override
        public void setPixels(int x, int y, int w, int h,
                              PixelReader reader, int srcX, int srcY) {
            delegate.setPixels(x, y, w, h, reader, srcX, srcY);
        }

        @Override
        public PixelFormat getPixelFormat() {
            return delegate.getPixelFormat();
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

    private static class PixelWriterAdvancedWrapper implements PixelWriter {
        private final PixelWriter delegate;
        private final ZBuffer zBuffer;
        private final float z0, z1, z2;
        private final int x0, y0, x1, y1, x2, y2;
        private final Vector3f[] worldPositions;
        private final Vector3f[] normals;
        private final Vector2f[] texCoords;
        private final Light light;
        private final Texture texture;
        private final javax.vecmath.Vector3f cameraPos;
        private final Color baseColor;

        public PixelWriterAdvancedWrapper(PixelWriter delegate, ZBuffer zBuffer,
                                          int x0, int y0, float z0,
                                          Vector3f worldPos0, Vector3f normal0, Vector2f texCoord0,
                                          int x1, int y1, float z1,
                                          Vector3f worldPos1, Vector3f normal1, Vector2f texCoord1,
                                          int x2, int y2, float z2,
                                          Vector3f worldPos2, Vector3f normal2, Vector2f texCoord2,
                                          Light light, Texture texture,
                                          javax.vecmath.Vector3f cameraPos, Color baseColor) {
            this.delegate = delegate;
            this.zBuffer = zBuffer;
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;

            this.worldPositions = new Vector3f[]{worldPos0, worldPos1, worldPos2};
            this.normals = new Vector3f[]{normal0, normal1, normal2};
            this.texCoords = new Vector2f[]{texCoord0, texCoord1, texCoord2};
            this.light = light;
            this.texture = texture;
            this.cameraPos = cameraPos;
            this.baseColor = baseColor;
        }

        @Override
        public void setColor(int x, int y, Color c) {
            float area = edgeFunction(x0, y0, x1, y1, x2, y2);
            if (Math.abs(area) < 1e-6f) return;

            float w0 = edgeFunction(x1, y1, x2, y2, x, y) / area;
            float w1 = edgeFunction(x2, y2, x0, y0, x, y) / area;
            float w2 = edgeFunction(x0, y0, x1, y1, x, y) / area;

            if (w0 >= -0.001f && w1 >= -0.001f && w2 >= -0.001f) {
                float z = w0 * z0 + w1 * z1 + w2 * z2;

                if (!zBuffer.testAndSet(x, y, z)) {
                    return;
                }

                Color finalColor = c;

                if (texture != null && texCoords[0] != null && texture.isLoaded()) {
                    try {
                        float zInv0 = 1.0f / (z0 + 1e-6f);
                        float zInv1 = 1.0f / (z1 + 1e-6f);
                        float zInv2 = 1.0f / (z2 + 1e-6f);

                        float texU_divZ = w0 * texCoords[0].x * zInv0 +
                                w1 * texCoords[1].x * zInv1 +
                                w2 * texCoords[2].x * zInv2;
                        float texV_divZ = w0 * texCoords[0].y * zInv0 +
                                w1 * texCoords[1].y * zInv1 +
                                w2 * texCoords[2].y * zInv2;
                        float zInv = w0 * zInv0 + w1 * zInv1 + w2 * zInv2;

                        float texU = texU_divZ / zInv;
                        float texV = texV_divZ / zInv;

                        texV = 1.0f - texV;

                        texU = texU - (float)Math.floor(texU);
                        texV = texV - (float)Math.floor(texV);

                        finalColor = texture.getColor(texU, texV);

                    } catch (Exception e) {
                        System.err.println("Texture error: " + e.getMessage());
                        finalColor = Color.RED;
                    }
                }

                if (light != null && settings.isUseLighting()) {
                    Vector3f worldPos = new Vector3f(
                            w0 * worldPositions[0].x + w1 * worldPositions[1].x + w2 * worldPositions[2].x,
                            w0 * worldPositions[0].y + w1 * worldPositions[1].y + w2 * worldPositions[2].y,
                            w0 * worldPositions[0].z + w1 * worldPositions[1].z + w2 * worldPositions[2].z
                    );

                    Vector3f normal = new Vector3f(
                            w0 * normals[0].x + w1 * normals[1].x + w2 * normals[2].x,
                            w0 * normals[0].y + w1 * normals[1].y + w2 * normals[2].y,
                            w0 * normals[0].z + w1 * normals[1].z + w2 * normals[2].z
                    );
                    normal.normalize();

                    Vector3f lightDir = light.getDirectionTo(worldPos);
                    lightDir.normalize();

                    float diff = Math.max(Vector3f.dot(normal, lightDir), 0.0f);
                    float ambient = 0.2f;

                    float lightIntensity = ambient + diff * light.getIntensity();
                    lightIntensity = Math.min(1.0f, Math.max(0.0f, lightIntensity));

                    float r = (float)finalColor.getRed() * lightIntensity;
                    float g = (float)finalColor.getGreen() * lightIntensity;
                    float b = (float)finalColor.getBlue() * lightIntensity;

                    Vector3f lightColor = light.getColor();
                    r *= lightColor.x;
                    g *= lightColor.y;
                    b *= lightColor.z;

                    finalColor = Color.color(
                            Math.min(1.0f, Math.max(0.0f, r)),
                            Math.min(1.0f, Math.max(0.0f, g)),
                            Math.min(1.0f, Math.max(0.0f, b)),
                            finalColor.getOpacity()
                    );
                }

                delegate.setColor(x, y, finalColor);
            }
        }

        @Override
        public <T extends Buffer> void setPixels(int x, int y, int w, int h,
                                                 PixelFormat<T> pixelFormat, T buffer, int stride) {
            delegate.setPixels(x, y, w, h, pixelFormat, buffer, stride);
        }

        @Override
        public void setPixels(int x, int y, int w, int h,
                              PixelFormat<ByteBuffer> pixelFormat, byte[] buffer, int offset, int stride) {
            delegate.setPixels(x, y, w, h, pixelFormat, buffer, offset, stride);
        }

        @Override
        public void setPixels(int x, int y, int w, int h,
                              PixelFormat<IntBuffer> pixelFormat, int[] buffer, int offset, int stride) {
            delegate.setPixels(x, y, w, h, pixelFormat, buffer, offset, stride);
        }

        @Override
        public void setPixels(int x, int y, int w, int h,
                              PixelReader reader, int srcX, int srcY) {
            delegate.setPixels(x, y, w, h, reader, srcX, srcY);
        }

        @Override
        public PixelFormat getPixelFormat() {
            return delegate.getPixelFormat();
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
}