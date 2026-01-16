package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;

/**
 * Класс для хранения и управления аффинными преобразованиями модели
 */
public class Transform {
    private Vector3f position;    // Позиция в мировых координатах
    private Vector3f rotation;    // Углы Эйлера в радианах (x, y, z)
    private Vector3f scale;       // Масштаб по осям

    public Transform() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Transform(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    // Конструктор копирования
    public Transform(Transform other) {
        this.position = new Vector3f(other.position);
        this.rotation = new Vector3f(other.rotation);
        this.scale = new Vector3f(other.scale);
    }

    /**
     * Создать копию объекта Transform
     */
    public Transform copy() {
        return new Transform(this);
    }

    /**
     * Получить матрицу аффинных преобразований модели
     * Порядок: M = T * R * S (для векторов-столбцов)
     */
    public Matrix4f getTransformationMatrix() {
        // 1. Матрица масштабирования
        Matrix4f scaleMatrix = Matrix4f.createScaleMatrix(scale.x, scale.y, scale.z);

        // 2. Матрицы вращения (углы Эйлера)
        Matrix4f rotationX = Matrix4f.createRotationXMatrix(rotation.x);
        Matrix4f rotationY = Matrix4f.createRotationYMatrix(rotation.y);
        Matrix4f rotationZ = Matrix4f.createRotationZMatrix(rotation.z);

        // 3. Комбинированная матрица вращения: R = Rz * Ry * Rx
        Matrix4f rotationMatrix = rotationZ.multiply(rotationY).multiply(rotationX);

        // 4. Матрица переноса
        Matrix4f translationMatrix = Matrix4f.createTranslationMatrix(position);

        // 5. Итоговая матрица модели: M = T * R * S
        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    /**
     * Альтернативный вариант: M = T * (R * S)
     * Часто используется в игровых движках
     */
    public Matrix4f getTransformationMatrixAlternative() {
        Matrix4f scaleMatrix = Matrix4f.createScaleMatrix(scale.x, scale.y, scale.z);

        Matrix4f rotationX = Matrix4f.createRotationXMatrix(rotation.x);
        Matrix4f rotationY = Matrix4f.createRotationYMatrix(rotation.y);
        Matrix4f rotationZ = Matrix4f.createRotationZMatrix(rotation.z);
        Matrix4f rotationMatrix = rotationZ.multiply(rotationY).multiply(rotationX);

        // Сначала масштаб, потом вращение
        Matrix4f scaleRotationMatrix = rotationMatrix.multiply(scaleMatrix);

        // Затем перенос
        return Matrix4f.createTranslationMatrix(position).multiply(scaleRotationMatrix);
    }

    // Геттеры и сеттеры
    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }

    public Vector3f getRotation() { return rotation; }
    public void setRotation(Vector3f rotation) { this.rotation = rotation; }

    public Vector3f getScale() { return scale; }
    public void setScale(Vector3f scale) { this.scale = scale; }

    // Методы для удобного изменения трансформаций
    public void translate(float dx, float dy, float dz) {
        this.position = position.add(new Vector3f(dx, dy, dz));
    }

    public void translate(Vector3f translation) {
        this.position = position.add(translation);
    }

    public void rotate(float dx, float dy, float dz) {
        this.rotation = rotation.add(new Vector3f(dx, dy, dz));
    }

    public void rotate(Vector3f rotation) {
        this.rotation = this.rotation.add(rotation);
    }

    public void scale(float sx, float sy, float sz) {
        this.scale = new Vector3f(scale.x * sx, scale.y * sy, scale.z * sz);
    }

    public void scale(Vector3f scaling) {
        this.scale = new Vector3f(scale.x * scaling.x, scale.y * scaling.y, scale.z * scaling.z);
    }

    public void scaleUniform(float factor) {
        this.scale = scale.multiply(factor);
    }

    // Сброс трансформаций
    public void reset() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    @Override
    public String toString() {
        return String.format(
                "Transform(Pos: [%.2f, %.2f, %.2f], Rot: [%.2f, %.2f, %.2f], Scale: [%.2f, %.2f, %.2f])",
                position.x, position.y, position.z,
                rotation.x, rotation.y, rotation.z,
                scale.x, scale.y, scale.z
        );
    }
}