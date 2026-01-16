package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;

/**
 * Класс камеры с управлением
 * Работает с векторами-столбцами
 */
public class Camera {
    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    // Для управления
    private float yaw = 0.0f;   // Поворот вокруг Y
    private float pitch = 0.0f; // Поворот вокруг X
    private float roll = 0.0f;  // Поворот вокруг Z

    // Вектор "вверх" по умолчанию
    private Vector3f defaultUpVector = new Vector3f(0, 1, 0);

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;

        // Вычисляем начальные углы
        updateAnglesFromTarget();
    }

    // Геттеры и сеттеры
    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) {
        this.position = position;
        updateTargetFromAngles();
    }

    public Vector3f getTarget() { return target; }
    public void setTarget(Vector3f target) {
        this.target = target;
        updateAnglesFromTarget();
    }

    public float getFov() { return fov; }
    public void setFov(float fov) { this.fov = fov; }

    public float getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(float aspectRatio) { this.aspectRatio = aspectRatio; }

    public float getNearPlane() { return nearPlane; }
    public float getFarPlane() { return farPlane; }

    public Vector3f getDefaultUpVector() { return defaultUpVector; }
    public void setDefaultUpVector(Vector3f upVector) { this.defaultUpVector = upVector; }

    // Управление углами
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        updateTargetFromAngles();
    }

    public void setPitch(float pitch) {
        // Ограничиваем угол наклона (-89° до 89°)
        float maxPitch = (float) Math.toRadians(89.0f);
        this.pitch = Math.max(-maxPitch, Math.min(maxPitch, pitch));
        updateTargetFromAngles();
    }

    public void setRoll(float roll) {
        this.roll = roll;
        // Roll не влияет на target, только на ориентацию камеры
    }

    // Обновление target на основе углов
    private void updateTargetFromAngles() {
        // Вычисляем направление из углов Эйлера (Yaw, Pitch)
        float cosPitch = (float) Math.cos(pitch);
        float sinPitch = (float) Math.sin(pitch);
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);

        // Направление взгляда
        Vector3f direction = new Vector3f(
                cosPitch * cosYaw,
                sinPitch,
                cosPitch * sinYaw
        ).normalized();

        // Target = position + direction
        this.target = position.add(direction);
    }

    // Обновление углов на основе target
    private void updateAnglesFromTarget() {
        Vector3f direction = target.subtract(position).normalized();

        // Pitch: угол между направлением и горизонтальной плоскостью
        this.pitch = (float) Math.asin(direction.y);

        // Yaw: угол в горизонтальной плоскости
        this.yaw = (float) Math.atan2(direction.z, direction.x);
    }

    // Движение камеры относительно её ориентации
    public void move(Vector3f translation) {
        // Получаем базисные векторы камеры
        Vector3f forward = getForwardVector();
        Vector3f right = getRightVector();
        Vector3f up = getCameraUpVector(); // Используем переименованный метод

        // Движение в локальных координатах камеры
        Vector3f movement = right.multiply(translation.x)
                .add(up.multiply(translation.y))
                .add(forward.multiply(translation.z));

        // Применяем движение
        this.position = position.add(movement);
        this.target = target.add(movement);
    }

    // Вращение камеры
    public void rotate(float deltaYaw, float deltaPitch) {
        setYaw(yaw + deltaYaw);
        setPitch(pitch + deltaPitch);
    }

    // Получение векторов ориентации камеры
    public Vector3f getForwardVector() {
        return target.subtract(position).normalized();
    }

    public Vector3f getRightVector() {
        Vector3f forward = getForwardVector();
        return forward.cross(defaultUpVector).normalized();
    }

    public Vector3f getCameraUpVector() {
        Vector3f forward = getForwardVector();
        Vector3f right = getRightVector();
        return right.cross(forward).normalized();
    }

    // Получение матриц
    public Matrix4f getViewMatrix() {
        // Используем статический метод из Matrix4f
        // Передаем реальный вектор "вверх" камеры (не defaultUpVector)
        return Matrix4f.createLookAtMatrix(position, target, getCameraUpVector());
    }

    public Matrix4f getProjectionMatrix() {
        // Используем статический метод из Matrix4f
        return Matrix4f.createPerspectiveMatrix(fov, aspectRatio, nearPlane, farPlane);
    }

    @Override
    public String toString() {
        return String.format(
                "Camera(Pos: [%.2f, %.2f, %.2f], Target: [%.2f, %.2f, %.2f], FOV: %.1f°)",
                position.x, position.y, position.z,
                target.x, target.y, target.z,
                Math.toDegrees(fov)
        );
    }
}