package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector4f;

public class GraphicConveyor {

    /**
     * Создание матрицы модели из отдельных компонентов
     */
    public static Matrix4f createModelMatrix(
            Vector3f translation,
            Vector3f rotation, // углы Эйлера в радианах
            Vector3f scale) {

        // Матрица масштабирования
        Matrix4f scaleMatrix = Matrix4f.createScaleMatrix(scale.x, scale.y, scale.z);

        // Матрицы вращения
        Matrix4f rotX = Matrix4f.createRotationXMatrix(rotation.x);
        Matrix4f rotY = Matrix4f.createRotationYMatrix(rotation.y);
        Matrix4f rotZ = Matrix4f.createRotationZMatrix(rotation.z);

        // Комбинированное вращение: R = Rz * Ry * Rx
        Matrix4f rotationMatrix = rotZ.multiply(rotY).multiply(rotX);

        // Матрица переноса
        Matrix4f translationMatrix = Matrix4f.createTranslationMatrix(translation);

        // Итоговая матрица модели: M = T * R * S
        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    /**
     * Создание видовой матрицы (look at matrix)
     * Для векторов-столбцов: V = [R^T | -R^T * eye]
     */
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = target.subtract(eye).normalized();
        Vector3f xAxis = up.cross(zAxis).normalized();
        Vector3f yAxis = zAxis.cross(xAxis);

        return new Matrix4f(
                xAxis.x, yAxis.x, zAxis.x, 0,
                xAxis.y, yAxis.y, zAxis.y, 0,
                xAxis.z, yAxis.z, zAxis.z, 0,
                -xAxis.dot(eye), -yAxis.dot(eye), -zAxis.dot(eye), 1
        );
    }

    /**
     * Создание перспективной проекционной матрицы
     * Для векторов-столбцов, левосторонней системы координат
     */
    public static Matrix4f perspective(float fov, float aspect, float near, float far) {
        float tanHalfFov = (float) Math.tan(fov / 2.0f);
        float range = near - far;

        return new Matrix4f(
                1.0f / (aspect * tanHalfFov), 0, 0, 0,
                0, 1.0f / tanHalfFov, 0, 0,
                0, 0, (far + near) / range, 2 * far * near / range,
                0, 0, -1, 0
        );
    }

    /**
     * Конвертация вершины в экранные координаты
     */
    public static Vector2f vertexToPoint(Vector3f vertex, int width, int height) {
        return new Vector2f(
                (vertex.x + 1.0f) * 0.5f * width,
                (1.0f - (vertex.y + 1.0f) * 0.5f) * height
        );
    }

    /**
     * Альтернативный порядок: M = T * (R * S)
     */
    public static Matrix4f createModelMatrixTRS(
            Vector3f translation,
            Vector3f rotation,
            Vector3f scale) {

        Matrix4f scaleMatrix = Matrix4f.createScaleMatrix(scale.x, scale.y, scale.z);

        Matrix4f rotX = Matrix4f.createRotationXMatrix(rotation.x);
        Matrix4f rotY = Matrix4f.createRotationYMatrix(rotation.y);
        Matrix4f rotZ = Matrix4f.createRotationZMatrix(rotation.z);
        Matrix4f rotationMatrix = rotZ.multiply(rotY).multiply(rotX);

        // Сначала масштаб и вращение
        Matrix4f scaleRotationMatrix = rotationMatrix.multiply(scaleMatrix);

        // Затем перенос
        return Matrix4f.createTranslationMatrix(translation).multiply(scaleRotationMatrix);
    }

    /**
     * Полный графический конвейер
     * @return MVP матрица (Projection * View * Model)
     */
    public static Matrix4f getMVPMatrix(
            Matrix4f projectionMatrix,
            Matrix4f viewMatrix,
            Matrix4f modelMatrix) {

        return projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);
    }

    /**
     * Преобразование точки через полный конвейер
     */
    public static Vector3f transformPoint(
            Vector3f point,
            Matrix4f modelMatrix,
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix) {

        // 1. Локальные -> Мировые координаты (Model)
        Vector4f worldPoint = modelMatrix.multiply(point);

        // 2. Мировые -> Координаты камеры (View)
        Vector4f viewPoint = viewMatrix.multiply(worldPoint);

        // 3. Координаты камеры -> Однородные координаты (Projection)
        Vector4f clipPoint = projectionMatrix.multiply(viewPoint);

        // 4. Перспективное деление
        clipPoint.normalize();

        // 5. Возвращаем 3D координаты (однородные)
        return clipPoint.toVector3f();
    }
}